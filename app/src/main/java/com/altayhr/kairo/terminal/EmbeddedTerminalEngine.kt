package com.altayhr.kairo.terminal

import android.content.Context
import android.content.Intent
import org.json.JSONArray
import com.altayhr.kairo.data.SecureApiKeyStore
import com.altayhr.kairo.service.IslandOverlayService
import com.altayhr.kairo.data.WorkspaceStore
import org.json.JSONObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.File
import java.io.InputStream
import java.security.MessageDigest
import java.util.UUID
import java.util.concurrent.TimeUnit

/** A command that was proposed, approved, executed, or rejected inside Kairo. */
data class EmbeddedCommandRecord(
    val id: String,
    val sessionId: String,
    val command: String,
    val reason: String,
    val state: String,
    val stdout: String = "",
    val stderr: String = "",
    val exitCode: Int? = null,
    val message: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val finishedAt: Long? = null,
    val continuationPending: Boolean = false
) {
    fun toJson() = JSONObject().apply {
        put("id", id); put("sessionId", sessionId); put("command", command); put("reason", reason)
        put("state", state); put("stdout", stdout); put("stderr", stderr)
        put("exitCode", exitCode ?: JSONObject.NULL); put("message", message)
        put("createdAt", createdAt); put("finishedAt", finishedAt ?: JSONObject.NULL)
        put("continuationPending", continuationPending)
    }

    companion object {
        fun fromJson(json: JSONObject) = EmbeddedCommandRecord(
            id = json.optString("id"), sessionId = json.optString("sessionId"),
            command = json.optString("command"), reason = json.optString("reason"),
            state = json.optString("state", "error"), stdout = json.optString("stdout"),
            stderr = json.optString("stderr"),
            exitCode = if (json.isNull("exitCode")) null else json.optInt("exitCode"),
            message = json.optString("message"), createdAt = json.optLong("createdAt", System.currentTimeMillis()),
            finishedAt = if (json.isNull("finishedAt")) null else json.optLong("finishedAt"),
            continuationPending = json.optBoolean("continuationPending")
        )
    }
}

/**
 * Executes approved commands under Kairo's own Android application UID. It does not invoke another
 * app, use root, or write to a shared global shell. Each chat has a private working directory.
 *
 * This first embedded runtime uses Android's /system/bin/sh and system utilities. It deliberately
 * reports missing tools (for example git/node/npm/gradle) rather than pretending they are bundled.
 */
object EmbeddedTerminalEngine {
    const val ACTION_UPDATED = "com.altayhr.kairo.terminal.EMBEDDED_COMMANDS_UPDATED"
    private const val HISTORY_SECRET = "embedded_terminal_history_v1"
    private const val MAX_RECORDS = 20
    private const val MAX_COMMAND_CHARS = 5000
    private const val MAX_CAPTURE_CHARS = 6_000
    private const val TIMEOUT_SECONDS = 900L

    /** Uses the selected chat's real file area or its linked project folder as the shell cwd. */
    fun workspace(context: Context, sessionId: String): File {
        val id = sessionId.ifBlank { "personal" }
        val store = WorkspaceStore(context)
        val chat = store.getChats().firstOrNull { it.id == id }
        val workspace = if (chat?.projectId != null) store.projectFolder(chat.projectId) else store.workspaceFolder(id)
        workspace.mkdirs()
        homeDirectory(context, id).mkdirs()
        tempDirectory(context, id).mkdirs()
        toolDirectory(context).mkdirs()
        return workspace
    }

    fun homeDirectory(context: Context, sessionId: String): File = File(context.filesDir, "kairo-terminal-state/${stableKey(sessionId.ifBlank { "personal" })}/home")
    fun tempDirectory(context: Context, sessionId: String): File = File(context.cacheDir, "kairo-terminal-tmp/${stableKey(sessionId.ifBlank { "personal" })}")
    fun toolDirectory(context: Context): File = File(context.filesDir, "kairo-tools/bin")

    @Synchronized
    fun history(context: Context, sessionId: String, limit: Int = 20): List<EmbeddedCommandRecord> = readAll(context)
        .filter { it.sessionId == sessionId }
        .sortedByDescending { it.createdAt }
        .take(limit.coerceIn(1, 60))

    @Synchronized
    fun propose(context: Context, sessionId: String, commandText: String, reasonText: String): EmbeddedCommandRecord {
        val command = commandText.trim()
        require(command.isNotEmpty()) { "Terminal komutu boş olamaz." }
        require(command.length <= MAX_COMMAND_CHARS) { "Komut en fazla $MAX_COMMAND_CHARS karakter olabilir." }
        require(command.none { it == '\u0000' }) { "Komut geçersiz bir karakter içeriyor." }
        try {
            TerminalCommandPolicy.validate(command)
        } catch (error: IllegalArgumentException) {
            val blocked = EmbeddedCommandRecord(
                id = UUID.randomUUID().toString(), sessionId = sessionId.ifBlank { "personal" },
                command = command, reason = reasonText.trim().take(500).ifBlank { "Kullanıcı tarafından istenen terminal işlemi" },
                state = "blocked", message = error.message ?: "Komut güvenlik politikası tarafından engellendi.",
                finishedAt = System.currentTimeMillis()
            )
            persist(context, (listOf(blocked) + readAll(context)).take(MAX_RECORDS))
            publish(context)
            IslandOverlayService.publish(context, "Terminal komutu engellendi", command.take(140), "error")
            throw error
        }
        val record = EmbeddedCommandRecord(
            id = UUID.randomUUID().toString(), sessionId = sessionId.ifBlank { "personal" },
            command = command, reason = reasonText.trim().take(500).ifBlank { "Kullanıcı tarafından istenen terminal işlemi" },
            state = "approval_required", message = "Komut yalnızca açık onaydan sonra çalıştırılır."
        )
        persist(context, (listOf(record) + readAll(context)).take(MAX_RECORDS))
        publish(context)
        return record
    }

    suspend fun runApproved(context: Context, recordId: String): EmbeddedCommandRecord = withContext(Dispatchers.IO) {
        // Claim the approval atomically. Two callers can never execute the same approved record.
        val running = synchronized(this@EmbeddedTerminalEngine) {
            val current = readAll(context).firstOrNull { it.id == recordId }
                ?: throw IllegalStateException("Terminal komut kaydı bulunamadı.")
            require(current.state == "approval_required") { "Bu komut zaten onaylandı veya tamamlandı." }
            TerminalCommandPolicy.validate(current.command)
            val claimed = current.copy(state = "running", message = "Komut Kairo içinde çalışıyor.", continuationPending = false)
            persist(context, readAll(context).map { if (it.id == recordId) claimed else it })
            claimed
        }
        publish(context)
        IslandOverlayService.publish(context, "Terminal komutu çalışıyor", running.command.take(140), "working")

        val dir = workspace(context, running.sessionId).canonicalFile
        val proc = try {
            ProcessBuilder("/system/bin/sh", "-c", running.command)
                .directory(dir)
                .apply {
                    environment().clear()
                    environment()["HOME"] = homeDirectory(context, running.sessionId).absolutePath
                    environment()["TMPDIR"] = tempDirectory(context, running.sessionId).absolutePath
                    environment()["PATH"] = "${toolDirectory(context).absolutePath}:/system/bin:/system/xbin"
                    environment()["TERM"] = "xterm-256color"
                    environment()["LANG"] = "C.UTF-8"
                    environment()["PWD"] = dir.absolutePath
                }.start()
        } catch (error: Exception) {
            val failed = running.copy(
                state = "error", message = "Komut başlatılamadı: ${error.message ?: error.javaClass.simpleName}",
                finishedAt = System.currentTimeMillis(), continuationPending = true
            )
            update(context, failed)
            IslandOverlayService.publish(context, "Terminal komutu başlatılamadı", failed.message.take(180), "error")
            return@withContext failed
        }

        val result = try {
            coroutineScope {
                val stdoutJob = async(Dispatchers.IO) { readLimited(context, running.sessionId, running.command, "STDOUT", proc.inputStream, MAX_CAPTURE_CHARS) }
                val stderrJob = async(Dispatchers.IO) { readLimited(context, running.sessionId, running.command, "STDERR", proc.errorStream, MAX_CAPTURE_CHARS) }
                val finished = proc.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                if (!finished) {
                    proc.destroy()
                    if (proc.isAlive) proc.destroyForcibly()
                    runCatching { proc.inputStream.close() }
                    runCatching { proc.errorStream.close() }
                }
                // A child process can accidentally keep an inherited pipe open after its parent exits.
                // Bound the drain wait so an orphaned writer cannot wedge the AI task forever.
                val rawStdout = withTimeoutOrNull(5_000) { stdoutJob.await() } ?: run {
                    runCatching { proc.inputStream.close() }
                    stdoutJob.cancel()
                    "[STDOUT yakalama kanalı kapatıldı: akış zaman aşımı]"
                }
                val rawStderr = withTimeoutOrNull(5_000) { stderrJob.await() } ?: run {
                    runCatching { proc.errorStream.close() }
                    stderrJob.cancel()
                    "[STDERR yakalama kanalı kapatıldı: akış zaman aşımı]"
                }
                val stdout = redactSensitiveOutput(rawStdout)
                val stderr = redactSensitiveOutput(rawStderr)
                val code = if (finished) proc.exitValue() else null
                val state = when { !finished -> "timeout"; code == 0 -> "success"; else -> "error" }
                val message = when {
                    !finished -> "Komut ${TIMEOUT_SECONDS} saniyelik güvenlik sınırını aştı ve durduruldu."
                    code == 0 -> "Komut tamamlandı · çıkış kodu 0."
                    else -> "Komut başarısız oldu · çıkış kodu $code."
                }
                running.copy(
                    state = state, stdout = stdout.takeLast(MAX_CAPTURE_CHARS), stderr = stderr.takeLast(MAX_CAPTURE_CHARS),
                    exitCode = code, message = message, finishedAt = System.currentTimeMillis(), continuationPending = true
                )
            }
        } catch (error: Exception) {
            if (proc.isAlive) proc.destroyForcibly()
            running.copy(state = "error", message = "Komut çıktısı okunamadı: ${error.message ?: error.javaClass.simpleName}", finishedAt = System.currentTimeMillis(), continuationPending = true)
        }
        update(context, result)
        runCatching { WorkspaceStore(context).refreshTerminalWorkspace(result.sessionId) }
        IslandOverlayService.publish(
            context,
            when (result.state) { "success" -> "Terminal komutu tamamlandı"; "timeout" -> "Terminal zaman aşımı"; else -> "Terminal komutu başarısız" },
            "${result.command.take(95)} · ${result.exitCode?.let { "çıkış $it" } ?: result.state}",
            if (result.state == "success") "success" else "error"
        )
        result
    }

    /** Returns one completed command result for the AI continuation, then marks it as consumed. */
    @Synchronized
    fun consumeLatestResultForAi(context: Context, sessionId: String): String? {
        val rows = readAll(context)
        val index = rows.indexOfFirst { it.sessionId == sessionId && it.continuationPending && it.state in setOf("success", "error", "timeout") }
        if (index < 0) return null
        val row = rows[index]
        rows[index] = row.copy(continuationPending = false)
        persist(context, rows)
        return buildString {
            appendLine("[KAIRO GÖMÜLÜ TERMİNAL SONUCU — bu komut kullanıcı tarafından onaylandı]")
            appendLine("Oturum: ${row.sessionId}")
            appendLine("Komut: ${row.command}")
            appendLine("Durum: ${row.state} · çıkış kodu: ${row.exitCode?.toString() ?: "zaman aşımı / yok"}")
            if (row.stdout.isNotBlank()) { appendLine("STDOUT:"); appendLine(row.stdout.takeLast(14_000)) }
            if (row.stderr.isNotBlank()) { appendLine("STDERR:"); appendLine(row.stderr.takeLast(8_000)) }
            appendLine("Not: Yalnızca bu komut çalıştırıldı. Henüz çalıştırılmayan sonraki adımlar için yeni onay iste.")
        }.takeLast(24_000)
    }

    @Synchronized
    fun hasUnconsumedResult(context: Context, sessionId: String): Boolean = readAll(context).any {
        it.sessionId == sessionId && it.continuationPending && it.state in setOf("success", "error", "timeout")
    }

    @Synchronized
    fun reject(context: Context, recordId: String) {
        val rows = readAll(context)
        val index = rows.indexOfFirst { it.id == recordId && it.state == "approval_required" }
        if (index < 0) return
        rows[index] = rows[index].copy(state = "rejected", message = "Kullanıcı komutu reddetti.", finishedAt = System.currentTimeMillis())
        persist(context, rows)
        publish(context)
    }

    @Synchronized
    fun clearFinishedHistory(context: Context, sessionId: String) {
        persist(context, readAll(context).filterNot { it.sessionId == sessionId && it.state in setOf("success", "error", "timeout", "blocked") })
        publish(context)
    }


    private fun readLimited(context: Context, sessionId: String, command: String, streamLabel: String, stream: InputStream, maxChars: Int): String {
        val output = StringBuilder()
        val liveTail = StringBuilder()
        var lastCapsuleUpdate = 0L
        stream.bufferedReader(Charsets.UTF_8).use { reader ->
            val buffer = CharArray(2048)
            while (true) {
                val n = reader.read(buffer)
                if (n < 0) break
                if (output.length < maxChars) output.append(buffer, 0, minOf(n, maxChars - output.length))
                liveTail.append(buffer, 0, n)
                if (liveTail.length > 1_400) liveTail.delete(0, liveTail.length - 1_000)
                val now = System.currentTimeMillis()
                if (now - lastCapsuleUpdate >= 850) {
                    val lastLine = liveTail.toString().substringAfterLast('\n').trim().ifBlank { liveTail.toString().trim() }
                    if (lastLine.isNotBlank()) {
                        IslandOverlayService.publish(
                            context,
                            "Terminal · $streamLabel",
                            "${sessionId.take(10)} · ${command.take(36)} · ${redactSensitiveOutput(lastLine).take(155)}",
                            "working"
                        )
                    }
                    lastCapsuleUpdate = now
                }
                // Continue draining after the capture cap to avoid blocking the child on a full pipe.
            }
        }
        return output.toString()
    }

    private fun redactSensitiveOutput(raw: String): String {
        var value = raw
        value = Regex("(?i)(authorization\\s*[:=]\\s*bearer\\s+)[^\\s]+").replace(value) { it.groupValues[1] + "[REDACTED]" }
        value = Regex("(?i)((?:[A-Z0-9_]*(?:API[_-]?KEY|ACCESS[_-]?TOKEN|REFRESH[_-]?TOKEN|CLIENT[_-]?SECRET|PASSWORD|GITHUB_TOKEN|GH_TOKEN|NPM_TOKEN|SECRET_ACCESS_KEY)[A-Z0-9_]*|authorization)\\s*[:=]\\s*(?:bearer\\s+)?)[^\\s,;]+").replace(value) { it.groupValues[1] + "[REDACTED]" }
        value = value.replace(Regex("ghp_[A-Za-z0-9]{20,}"), "[REDACTED_GITHUB_TOKEN]")
        value = value.replace(Regex("github_pat_[A-Za-z0-9_]{20,}"), "[REDACTED_GITHUB_TOKEN]")
        value = value.replace(Regex("AIza[A-Za-z0-9_-]{30,}"), "[REDACTED_GOOGLE_KEY]")
        value = value.replace(Regex("sk-[A-Za-z0-9_-]{24,}"), "[REDACTED_SECRET]")
        return value
    }

    @Synchronized
    private fun update(context: Context, row: EmbeddedCommandRecord) {
        val rows = readAll(context)
        val index = rows.indexOfFirst { it.id == row.id }
        if (index >= 0) rows[index] = row else rows.add(0, row)
        persist(context, rows.sortedByDescending { it.createdAt }.take(MAX_RECORDS))
        publish(context)
    }

    @Synchronized
    private fun readAll(context: Context): MutableList<EmbeddedCommandRecord> = runCatching {
        val serialized = SecureApiKeyStore(context).getSecret(HISTORY_SECRET) ?: "[]"
        val array = JSONArray(serialized)
        (0 until array.length()).mapNotNull { array.optJSONObject(it)?.let(EmbeddedCommandRecord::fromJson) }.toMutableList()
    }.getOrDefault(mutableListOf())

    private fun persist(context: Context, records: List<EmbeddedCommandRecord>) {
        val json = JSONArray().also { array -> records.forEach { array.put(it.toJson()) } }
        SecureApiKeyStore(context).saveSecret(HISTORY_SECRET, json.toString())
    }

    private fun publish(context: Context) {
        context.sendBroadcast(Intent(ACTION_UPDATED).setPackage(context.packageName))
    }

    private fun stableKey(value: String): String = MessageDigest.getInstance("SHA-256")
        .digest(value.toByteArray(Charsets.UTF_8)).take(10).joinToString("") { "%02x".format(it) }
}
