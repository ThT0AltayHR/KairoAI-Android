package com.altayhr.kairo.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.os.Build
import android.os.IBinder
import android.util.Base64
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.altayhr.kairo.MainActivity
import com.altayhr.kairo.automation.AutomationEvents
import org.json.JSONObject
import com.altayhr.kairo.R
import com.altayhr.kairo.data.SecureApiKeyStore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONArray
import java.net.URLEncoder
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/** Full-duplex Gemini Live session. It is started only after the user taps Start and grants microphone access. */
class GeminiLiveVoiceService : Service() {
    companion object {
        const val ACTION_START = "com.altayhr.kairo.voice.START"
        const val ACTION_STOP = "com.altayhr.kairo.voice.STOP"
        private const val CHANNEL = "kairo_live_voice"
        private const val NOTIFICATION_ID = 4102
        private const val PREFS = "kairo_preferences"
        private const val WS_ENDPOINT = "wss://generativelanguage.googleapis.com/ws/google.ai.generativelanguage.v1beta.GenerativeService.BidiGenerateContent"

        fun isRunning(context: Context) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean("voice_running", false)
        fun start(context: Context) {
            val intent = Intent(context, GeminiLiveVoiceService::class.java).setAction(ACTION_START)
            if (Build.VERSION.SDK_INT >= 26) context.startForegroundService(intent) else context.startService(intent)
        }
        fun stop(context: Context) { context.startService(Intent(context, GeminiLiveVoiceService::class.java).setAction(ACTION_STOP)) }
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val client by lazy { OkHttpClient.Builder().connectTimeout(8, TimeUnit.SECONDS).readTimeout(0, TimeUnit.MILLISECONDS).pingInterval(30, TimeUnit.SECONDS).build() }
    private val ready = AtomicBoolean(false)
    private var socket: WebSocket? = null
    private var recorder: AudioRecord? = null
    private var track: AudioTrack? = null
    private var captureJob: Job? = null
    private val trackLock = Any()
    private var currentOutputRate = 24000
    private val prefs by lazy { getSharedPreferences(PREFS, Context.MODE_PRIVATE) }

    override fun onCreate() {
        super.onCreate()
        createChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> { shutdown("Sesli sohbet durduruldu", false); stopSelf(); return START_NOT_STICKY }
            else -> {
                if (isRunning(this) && socket != null) return START_STICKY
                val key = SecureApiKeyStore(this).getApiKey().orEmpty()
                if (key.isBlank()) { setState("API anahtarı eksik", "Ayarlar bölümüne Gemini API anahtarını ekle.", false); stopSelf(); return START_NOT_STICKY }
                prefs.edit().putBoolean("voice_running", true).apply()
                AutomationEvents.emit(this, "voice.started", "Gemini Live sesli oturumu başlatıldı", JSONObject().put("model", SecureApiKeyStore(this).liveModel()).toString())
                promoteToForeground()
                setState("Gemini Live bağlanıyor", "Mikrofon oturumu güvenli bağlantı kurulurken hazırlanıyor.", true)
                connect(key, SecureApiKeyStore(this).liveModel(), SecureApiKeyStore(this).liveVoice())
            }
        }
        return START_STICKY
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= 26) getSystemService(NotificationManager::class.java).createNotificationChannel(
            NotificationChannel(CHANNEL, "Canlı sesli sohbet", NotificationManager.IMPORTANCE_LOW).apply {
                description = "Etkin Gemini Live oturumu"; setShowBadge(false)
            }
        )
    }

    private fun promoteToForeground() {
        val open = PendingIntent.getActivity(this, 11, Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val stop = PendingIntent.getService(this, 12, Intent(this, GeminiLiveVoiceService::class.java).setAction(ACTION_STOP), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val notification = NotificationCompat.Builder(this, CHANNEL).setSmallIcon(R.drawable.ic_status)
            .setContentTitle("Kairo sesli sohbet etkin")
            .setContentText("Mikrofon, sen durdurana kadar canlı görüşme için açık.")
            .setContentIntent(open).setOngoing(true).setOnlyAlertOnce(true)
            .addAction(0, "Uygulamayı aç", open).addAction(0, "Sohbeti bitir", stop).build()
        if (Build.VERSION.SDK_INT >= 29) ServiceCompat.startForeground(this, NOTIFICATION_ID, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE)
        else startForeground(NOTIFICATION_ID, notification)
    }

    private fun connect(apiKey: String, selectedModel: String, voiceName: String) {
        val model = selectedModel.removePrefix("models/").ifBlank { "gemini-3.8-live" }
        val encodedKey = URLEncoder.encode(apiKey, Charsets.UTF_8.name())
        val request = Request.Builder().url("$WS_ENDPOINT?key=$encodedKey").build()
        socket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                val setup = JSONObject().put("setup", JSONObject()
                    .put("model", "models/$model")
                    .put("generationConfig", JSONObject()
                        .put("responseModalities", JSONArray().put("AUDIO"))
                        .put("speechConfig", JSONObject().put("voiceConfig", JSONObject().put("prebuiltVoiceConfig", JSONObject().put("voiceName", voiceName))))
                    )
                    .put("inputAudioTranscription", JSONObject())
                    .put("outputAudioTranscription", JSONObject())
                    .put("systemInstruction", JSONObject().put("parts", JSONArray().put(JSONObject().put("text", LIVE_SYSTEM_PROMPT))))
                )
                webSocket.send(setup.toString())
            }

            override fun onMessage(webSocket: WebSocket, text: String) { handleServerMessage(text) }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                ready.set(false)
                val safeMessage = when (response?.code) {
                    400, 401, 403 -> "Gemini API anahtarı veya Live model erişimi doğrulanamadı."
                    429 -> "Gemini kullanım sınırına ulaşıldı. Bir süre sonra yeniden dene."
                    else -> "Canlı bağlantı kesildi: ${t.message?.take(110) ?: "ağ hatası"}"
                }
                setState("Bağlantı hatası", safeMessage, false)
                shutdown(safeMessage, true)
                stopSelf()
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                ready.set(false)
                if (prefs.getBoolean("voice_running", false)) {
                    shutdown("Canlı bağlantı kapandı", true)
                    stopSelf()
                }
            }
        })
    }

    private fun handleServerMessage(raw: String) {
        runCatching {
            val json = JSONObject(raw)
            if (json.has("setupComplete")) {
                ready.set(true)
                setState("Dinliyorum", "Konuşabilirsin. Görüşmeyi bitirmek için durdur düğmesine dokun.", true)
                startCapture()
                return
            }
            val error = json.optJSONObject("error")
            if (error != null) {
                val message = error.optString("message", "Gemini Live isteği reddedildi.").take(180)
                setState("Sesli sohbet hatası", message, false)
                shutdown(message, true)
                stopSelf()
                return
            }
            val server = json.optJSONObject("serverContent") ?: return
            if (server.optBoolean("interrupted", false)) synchronized(trackLock) { runCatching { track?.pause(); track?.flush(); track?.play() } }
            val inputText = server.optJSONObject("inputTranscription")?.optString("text").orEmpty().trim()
            if (inputText.isNotBlank()) setTranscript("voice_last_user", inputText)
            val outputText = server.optJSONObject("outputTranscription")?.optString("text").orEmpty().trim()
            if (outputText.isNotBlank()) setTranscript("voice_last_assistant", outputText)
            val parts = server.optJSONObject("modelTurn")?.optJSONArray("parts") ?: return
            for (i in 0 until parts.length()) {
                val part = parts.optJSONObject(i) ?: continue
                val data = part.optJSONObject("inlineData") ?: part.optJSONObject("inline_data") ?: continue
                val encoded = data.optString("data")
                if (encoded.isNotBlank()) {
                    val mime = data.optString("mimeType", data.optString("mime_type", "audio/pcm;rate=24000"))
                    val rate = Regex("rate=(\\d+)").find(mime)?.groupValues?.get(1)?.toIntOrNull() ?: 24000
                    playAudio(Base64.decode(encoded, Base64.DEFAULT), rate)
                }
            }
            if (server.optBoolean("turnComplete", false)) setState("Dinliyorum", "Yanıt tamamlandı. Devam edebilirsin.", true)
        }.onFailure { setState("Sesli yanıt işlenemedi", it.message ?: "Bilinmeyen yanıt hatası", false) }
    }

    private fun startCapture() {
        if (captureJob?.isActive == true || !ready.get()) return
        captureJob = scope.launch {
            try {
                val minBuffer = AudioRecord.getMinBufferSize(16000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT).coerceAtLeast(4096)
                val audio = AudioRecord(MediaRecorder.AudioSource.VOICE_RECOGNITION, 16000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, minBuffer * 2)
                if (audio.state != AudioRecord.STATE_INITIALIZED) { audio.release(); throw IllegalStateException("Mikrofon başlatılamadı.") }
                recorder = audio
                audio.startRecording()
                val buffer = ByteArray(2048)
                while (isActive && ready.get() && prefs.getBoolean("voice_running", false)) {
                    val count = audio.read(buffer, 0, buffer.size)
                    if (count > 0) {
                        val payload = Base64.encodeToString(buffer.copyOf(count), Base64.NO_WRAP)
                        socket?.send(JSONObject().put("realtimeInput", JSONObject().put("audio", JSONObject().put("mimeType", "audio/pcm;rate=16000").put("data", payload))).toString())
                    } else delay(20)
                }
            } catch (cancelled: CancellationException) { throw cancelled }
            catch (e: Exception) { setState("Mikrofon hatası", e.message ?: "Ses kaydı başlatılamadı", false) }
            finally { runCatching { recorder?.stop() }; recorder?.release(); recorder = null }
        }
    }

    private fun playAudio(data: ByteArray, sampleRate: Int) {
        if (data.isEmpty()) return
        synchronized(trackLock) {
            if (track == null || currentOutputRate != sampleRate) {
                runCatching { track?.stop(); track?.release() }
                currentOutputRate = sampleRate
                val min = AudioTrack.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT).coerceAtLeast(8192)
                track = AudioTrack.Builder()
                    .setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ASSISTANT).setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build())
                    .setAudioFormat(AudioFormat.Builder().setEncoding(AudioFormat.ENCODING_PCM_16BIT).setSampleRate(sampleRate).setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build())
                    .setBufferSizeInBytes(min * 2).setTransferMode(AudioTrack.MODE_STREAM).build().also { it.play() }
            }
            track?.write(data, 0, data.size, AudioTrack.WRITE_BLOCKING)
        }
    }

    private fun setTranscript(key: String, text: String) {
        prefs.edit().putString(key, text.take(900)).putLong("voice_last_text_at", System.currentTimeMillis()).apply()
    }

    private fun setState(title: String, detail: String, running: Boolean) {
        prefs.edit().putString("voice_status", title).putString("voice_detail", detail.take(240)).putBoolean("voice_running", running).apply()
        IslandOverlayService.publish(this, title, detail, when {
            !running && title.contains("hata", true) -> "error"
            running && title.contains("Dinliyorum", true) -> "listening"
            running -> "thinking"
            else -> "idle"
        })
    }

    private fun shutdown(detail: String, error: Boolean) {
        val hadSession = prefs.getBoolean("voice_running", false)
        ready.set(false)
        prefs.edit().putBoolean("voice_running", false).putString("voice_status", if (error) "Sesli sohbet durdu" else "Sesli sohbet kapalı").putString("voice_detail", detail.take(200)).apply()
        captureJob?.cancel(); captureJob = null
        runCatching { recorder?.stop() }; runCatching { recorder?.release() }; recorder = null
        runCatching { socket?.close(1000, "user stopped") }; socket = null
        synchronized(trackLock) { runCatching { track?.stop() }; runCatching { track?.release() }; track = null }
        if (hadSession) AutomationEvents.emit(this, if (error) "voice.failed" else "voice.stopped", if (error) "Sesli oturum hata ile kapandı" else "Sesli oturum kapatıldı", JSONObject().put("detail", detail.take(120)).toString())
        IslandOverlayService.publish(this, if (error) "Sesli sohbet hatası" else "Sesli sohbet tamamlandı", detail, if (error) "error" else "success")
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    override fun onDestroy() { shutdown("Sesli sohbet kapatıldı", false); scope.cancel(); client.dispatcher.executorService.shutdown(); super.onDestroy() }
    override fun onBind(intent: Intent?): IBinder? = null

    private val LIVE_SYSTEM_PROMPT = """
Sen Kairo AI'nin canlı Türkçe sesli asistanısın. Doğal, arkadaşça ve profesyonel konuş; kullanıcıya Türkçe yanıt ver. Kodlama ve araştırma sorularında uygulanabilir yardım sun. Kullanıcının özel verileri ve dosyaları hakkında varsayım yapma. Bir işlem gerçekten yapılmadıysa tamamlandığını söyleme. Gizli iç düşünce zincirini açıklama; yalnızca kısa sonuç ve gerekirse yüksek seviyeli özet ver.
""".trimIndent()
}
