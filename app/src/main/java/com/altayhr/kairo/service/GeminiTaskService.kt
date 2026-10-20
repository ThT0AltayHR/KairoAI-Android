package com.altayhr.kairo.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.altayhr.kairo.MainActivity
import com.altayhr.kairo.automation.AutomationEvents
import org.json.JSONObject
import com.altayhr.kairo.R
import com.altayhr.kairo.data.AgentRunStore
import com.altayhr.kairo.data.ChatMessage
import com.altayhr.kairo.data.SecureApiKeyStore
import com.altayhr.kairo.data.WorkspaceStore
import com.altayhr.kairo.features.SoundEffects
import com.altayhr.kairo.network.GeminiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import java.util.concurrent.ConcurrentLinkedQueue

/** User-initiated AI generation queue. Results are committed to local chat storage, not held only in an Activity coroutine. */
class GeminiTaskService : Service() {
    data class Task(val chatId: String, val webSearch: Boolean, val taskId: String)
    companion object {
        const val ACTION_ENQUEUE = "com.altayhr.kairo.task.ENQUEUE"
        const val ACTION_TASK_UPDATE = "com.altayhr.kairo.task.UPDATE"
        private const val CHANNEL = "kairo_ai_tasks"
        private const val NOTIFICATION_ID = 4103
        fun enqueue(context: Context, chatId: String, webSearch: Boolean) {
            val taskId = UUID.randomUUID().toString()
            val title = WorkspaceStore(context).getChats().firstOrNull { it.id == chatId }?.title ?: "Yeni görev"
            AgentRunStore(context).start(taskId, chatId, title, webSearch)
            val i = Intent(context, GeminiTaskService::class.java).setAction(ACTION_ENQUEUE)
                .putExtra("chat_id", chatId).putExtra("web_search", webSearch).putExtra("task_id", taskId)
            if (Build.VERSION.SDK_INT >= 26) context.startForegroundService(i) else context.startService(i)
        }
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val queue = ConcurrentLinkedQueue<Task>()
    private val stateLock = Any()
    private var processing = false
    private var lastStartId = 0
    @Volatile private var notificationChatId: String? = null
    @Volatile private var activeTaskId: String? = null

    override fun onCreate() { super.onCreate(); createChannel() }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action != ACTION_ENQUEUE) return START_NOT_STICKY
        val id = intent.getStringExtra("chat_id") ?: return START_NOT_STICKY
        var startWorker = false
        synchronized(stateLock) {
            lastStartId = startId
            notificationChatId = id
            val taskId = intent.getStringExtra("task_id") ?: UUID.randomUUID().toString()
            if (AgentRunStore(this).recent(100).none { it.taskId == taskId }) {
                val title = WorkspaceStore(this).getChats().firstOrNull { it.id == id }?.title ?: "Yeni görev"
                AgentRunStore(this).start(taskId, id, title, intent.getBooleanExtra("web_search", false))
            }
            queue.offer(Task(id, intent.getBooleanExtra("web_search", false), taskId))
            if (!processing) { processing = true; startWorker = true }
        }
        if (startWorker) {
            promote("Görev kuyruğu hazırlanıyor", id)
            scope.launch { processQueue() }
        } else updateNotification("Yeni görev sıraya eklendi")
        return START_NOT_STICKY
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= 26) getSystemService(NotificationManager::class.java).createNotificationChannel(
            NotificationChannel(CHANNEL, "AI görevleri", NotificationManager.IMPORTANCE_LOW).apply {
                description = "Kullanıcının başlattığı Kairo AI yanıt üretimi"; setShowBadge(false)
            }
        )
    }

    private fun openChatPendingIntent(chatId: String?): PendingIntent {
        val target = Intent(this, MainActivity::class.java).putExtra("kairo_route", "chat")
        chatId?.let { target.putExtra("chat_id", it) }
        target.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        return PendingIntent.getActivity(this, 21, target, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    private fun promote(status: String, chatId: String) {
        val open = openChatPendingIntent(chatId)
        val notification = NotificationCompat.Builder(this, CHANNEL).setSmallIcon(R.drawable.ic_status)
            .setContentTitle("Kairo AI çalışıyor").setContentText(status).setContentIntent(open)
            .setOngoing(true).setOnlyAlertOnce(true).build()
        if (Build.VERSION.SDK_INT >= 29) ServiceCompat.startForeground(this, NOTIFICATION_ID, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        else startForeground(NOTIFICATION_ID, notification)
    }

    private fun updateNotification(status: String) {
        val n = NotificationCompat.Builder(this, CHANNEL).setSmallIcon(R.drawable.ic_status)
            .setContentTitle("Kairo AI çalışıyor").setContentText(status).setContentIntent(openChatPendingIntent(notificationChatId))
            .setOngoing(true).setOnlyAlertOnce(true).build()
        getSystemService(NotificationManager::class.java).notify(NOTIFICATION_ID, n)
    }

    private suspend fun processQueue() {
        try {
            while (true) {
                val task = queue.poll()
                if (task == null) {
                    activeTaskId = null
                    val canStop = synchronized(stateLock) {
                        if (queue.isEmpty()) {
                            processing = false
                            stopForeground(STOP_FOREGROUND_REMOVE)
                            stopSelfResult(lastStartId)
                            true
                        } else false
                    }
                    if (canStop) return
                    else continue
                }
                activeTaskId = task.taskId
                val store = WorkspaceStore(this)
                val chat = store.getChats().firstOrNull { it.id == task.chatId }
                if (chat == null) { publish(task.chatId, "Sohbet bulunamadı", false); continue }
                updateNotification("${chat.title.take(36)} · bağlam hazırlanıyor")
                IslandOverlayService.publish(this, "İstek analiz ediliyor", "${chat.title}: mesajlar ve ek dosyalar hazırlanıyor.", "thinking")
                publish(task.chatId, "Bağlam hazırlanıyor", null)
                try {
                    val secrets = SecureApiKeyStore(this)
                    val key = secrets.getApiKey().orEmpty().ifBlank { secrets.getSecret("gemini_agent_key_1").orEmpty() }
                    if (key.isBlank()) throw IllegalStateException("Gemini API anahtarını Ayarlar bölümüne veya Ajan filosundaki Sohbet ajanı yuvasına ekle.")
                    val client = GeminiClient(this, store)
                    val terminalResultContext = com.altayhr.kairo.terminal.EmbeddedTerminalEngine.consumeLatestResultForAi(this, task.chatId)
                    val agentContext = if (terminalResultContext != null) "" else {
                        updateNotification("${chat.title.take(36)} · ajanlar görev planlıyor")
                        IslandOverlayService.publish(this, "Ajanlar planlıyor", "Görev dağıtıcı, planlama, kodlama ve entegrasyon rolleri hazırlanıyor.", "thinking")
                        publish(task.chatId, "Uzman ajanlar görev bağlamını hazırlıyor", null)
                        AgentOrchestrator.prepare(this, chat, client, secrets, secrets.model(), task.webSearch, secrets.thinkingLevel()) { stage ->
                            updateNotification("${chat.title.take(36)} · $stage")
                            IslandOverlayService.publish(this, stage, "Uzman rol kısa görev çıktısı üretiyor.", "thinking")
                            publish(task.chatId, stage, null)
                        }
                    }
                    val baseMessages = chat.messages.toMutableList()
                    val lastUserIndex = baseMessages.indexOfLast { it.role == "user" }
                    if (lastUserIndex >= 0 && agentContext.isNotBlank()) {
                        baseMessages[lastUserIndex] = baseMessages[lastUserIndex].copy(content = baseMessages[lastUserIndex].content + "\n\n[UZMAN AJAN ÖZETLERİ — dahili görev bağlamı, kullanıcıya ayrı mesaj olarak gösterme]\n" + agentContext)
                    }
                    if (terminalResultContext != null) baseMessages += ChatMessage(role = "user", content = terminalResultContext)
                    val effectiveChat = chat.copy(messages = baseMessages)
                    updateNotification("${chat.title.take(36)} · ana ajan yanıt üretiyor")
                    IslandOverlayService.publish(this, "Ana ajan çalışıyor", "Uzman özetleri birleştiriliyor ve kullanıcı yanıtı hazırlanıyor.", "thinking")
                    publish(task.chatId, "Ana ajan yanıt hazırlıyor", null)
                    val lastUserRequest = chat.messages.lastOrNull { it.role == "user" }?.content.orEmpty()
                    val allowTerminalTools = terminalResultContext != null || Regex("(?i)(proje|kod|uygula|derle|build|npm|node|git|gradle|expo|terminal|dosya|test|hata|entegrasyon|repository|depo|indir|kur|çalıştır|oluştur|geliştir|komut)").containsMatchIn(lastUserRequest)
                    val reply = client.reply(effectiveChat, key, secrets.model(), task.webSearch, secrets.thinkingLevel(), allowTerminalTools)
                    val terminalProposal = reply.terminalRequest?.let { request ->
                        runCatching {
                            com.altayhr.kairo.terminal.EmbeddedTerminalEngine.propose(this, task.chatId, request.command, request.reason)
                        }.getOrNull()
                    }
                    val terminalToolRejected = reply.terminalRequest != null && terminalProposal == null
                    val reviewNotes = if (reply.terminalRequest != null) "" else AgentOrchestrator.review(chat, reply.text, client, secrets, secrets.model(), secrets.thinkingLevel()) { stage ->
                        updateNotification("${chat.title.take(36)} · $stage")
                        IslandOverlayService.publish(this, stage, "Yanıt güvenlik ve doğruluk açısından gözden geçiriliyor.", "thinking")
                    }
                    val finalText = when {
                        terminalProposal != null -> buildString {
                            appendLine("### Terminal komutu onay bekliyor")
                            appendLine(terminalProposal.reason)
                            appendLine()
                            appendLine("```sh")
                            appendLine(terminalProposal.command)
                            appendLine("```")
                            appendLine("Komut Kairo'nun kendi terminalinde henüz çalıştırılmadı. Terminal bölümünde inceleyip onayla; reddedersen çalıştırılmaz.")
                        }
                        terminalToolRejected -> "Terminal komutu güvenlik kontrolünden geçmedi veya hazırlanamadı. Komut engellendi; terminal ekranında daha güvenli bir adım önerip yeniden deneyebilirsin."
                        reviewNotes.isBlank() -> reply.text
                        else -> reply.text + "\n\n### İnceleme notu\n" + reviewNotes
                    }
                    updateNotification("${chat.title.take(36)} · dosya çıktıları işleniyor")
                    IslandOverlayService.publish(this, "Dosya çıktıları hazırlanıyor", "Kod blokları dosya kartlarına dönüştürülüyor.", "thinking")
                    val files = withContext(Dispatchers.IO) { com.altayhr.kairo.saveCodeArtifacts(this@GeminiTaskService, store, task.chatId, finalText) }
                    val latestChats = store.getChats().toMutableList()
                    val latest = latestChats.firstOrNull { it.id == task.chatId } ?: chat
                    val answer = ChatMessage(role = "assistant", content = finalText, sources = reply.sources, attachments = files)
                    val replacement = latest.copy(messages = latest.messages + answer, files = (latest.files + files).distinctBy { it.relativePath }, updatedAt = System.currentTimeMillis())
                    val index = latestChats.indexOfFirst { it.id == task.chatId }
                    if (index >= 0) latestChats[index] = replacement else latestChats.add(replacement)
                    store.saveChats(latestChats)
                    AutomationEvents.emit(this, "chat.response.completed", "Yapay zekâ yanıtı tamamlandı", JSONObject()
                        .put("chatId", task.chatId).put("taskId", task.taskId).put("sourceCount", reply.sources.size).put("fileCount", files.size).toString())
                    if (terminalProposal != null) {
                        IslandOverlayService.publish(this, "Terminal onay bekliyor", terminalProposal.command.take(150), "thinking")
                        publishTerminalApprovalWaiting(task.chatId, "Terminal komutu onay bekliyor")
                    } else {
                        IslandOverlayService.publish(this, if (terminalToolRejected) "Terminal komutu engellendi" else "Görev tamamlandı", if (terminalToolRejected) finalText.take(180) else "${chat.title}: yanıt hazır${if (files.isNotEmpty()) " · ${files.size} dosya üretildi" else ""}${if (reply.sources.isNotEmpty()) " · ${reply.sources.size} kaynak" else ""}.", if (terminalToolRejected) "error" else "success")
                        if (!terminalToolRejected) SoundEffects.play(this, "finish") else SoundEffects.play(this, "error")
                        publish(task.chatId, if (terminalToolRejected) "Terminal komutu engellendi" else if (reply.sources.isNotEmpty()) "Yanıt tamamlandı · ${reply.sources.size} kaynak" else "Yanıt tamamlandı", !terminalToolRejected)
                    }
                } catch (e: Exception) {
                    val latestChats = store.getChats().toMutableList()
                    val latest = latestChats.firstOrNull { it.id == task.chatId }
                    if (latest != null) {
                        val errorText = e.message ?: "İstek başarısız oldu."
                        val replacement = latest.copy(messages = latest.messages + ChatMessage(role = "assistant", content = errorText, isError = true), updatedAt = System.currentTimeMillis())
                        val index = latestChats.indexOfFirst { it.id == task.chatId }
                        if (index >= 0) latestChats[index] = replacement
                        store.saveChats(latestChats)
                    }
                    AutomationEvents.emit(this, "chat.response.failed", "Yapay zekâ yanıtı tamamlanamadı", JSONObject()
                        .put("chatId", task.chatId).put("taskId", task.taskId).put("error", (e.message ?: "İstek başarısız oldu.").take(180)).toString())
                    IslandOverlayService.publish(this, "Görev tamamlanamadı", e.message ?: "Bir hata oluştu.", "error")
                    SoundEffects.play(this, "error")
                    publish(task.chatId, "İstek tamamlanamadı", false)
                }
            }
        } catch (fatal: Throwable) {
            publish("", fatal.message ?: "Görev servisi beklenmeyen bir hata aldı", false)
        } finally {
            synchronized(stateLock) {
                if (processing && queue.isEmpty()) {
                    processing = false
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelfResult(lastStartId)
                } else if (queue.isNotEmpty() && !processing) {
                    processing = true
                    scope.launch { processQueue() }
                }
            }
        }
    }

    private fun publishTerminalApprovalWaiting(chatId: String, status: String) {
        activeTaskId?.let { AgentRunStore(this).awaitApproval(it, status) }
        val intent = Intent(ACTION_TASK_UPDATE).setPackage(packageName)
            .putExtra("chat_id", chatId).putExtra("status", status).putExtra("task_id", activeTaskId.orEmpty())
            .putExtra("terminal_waiting", true)
        sendBroadcast(intent)
    }

    private fun publish(chatId: String, status: String, success: Boolean?) {
        activeTaskId?.let { AgentRunStore(this).update(it, status, success) }
        val intent = Intent(ACTION_TASK_UPDATE).setPackage(packageName)
            .putExtra("chat_id", chatId).putExtra("status", status).putExtra("task_id", activeTaskId.orEmpty())
        if (success != null) intent.putExtra("success", success)
        sendBroadcast(intent)
    }

    override fun onTimeout(startId: Int, fgsType: Int) {
        publish(notificationChatId.orEmpty(), "Android arka plan görev süresi sınırına ulaşıldı; görev durduruluyor.", false)
        activeTaskId = null
        queue.clear()
        scope.cancel()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf(startId)
    }

    override fun onDestroy() { scope.cancel(); super.onDestroy() }
    override fun onBind(intent: Intent?): IBinder? = null
}
