package com.altayhr.kairo.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/** Local task/approval-style activity log. Stores task metadata only, not API keys or hidden reasoning. */
data class AgentRunRecord(
    val taskId: String,
    val chatId: String,
    val chatTitle: String,
    val stage: String,
    val status: String,
    val outcome: String,
    val startedAt: Long,
    val updatedAt: Long,
    val webSearch: Boolean
) {
    fun toJson() = JSONObject().apply {
        put("taskId", taskId); put("chatId", chatId); put("chatTitle", chatTitle)
        put("stage", stage); put("status", status); put("outcome", outcome)
        put("startedAt", startedAt); put("updatedAt", updatedAt); put("webSearch", webSearch)
    }
    companion object {
        fun fromJson(j: JSONObject) = AgentRunRecord(
            taskId = j.optString("taskId"), chatId = j.optString("chatId"), chatTitle = j.optString("chatTitle", "Görev"),
            stage = j.optString("stage", "Hazırlık"), status = j.optString("status", "Kuyrukta"),
            outcome = j.optString("outcome", "running"), startedAt = j.optLong("startedAt"),
            updatedAt = j.optLong("updatedAt"), webSearch = j.optBoolean("webSearch")
        )
    }
}

class AgentRunStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("kairo_agent_runs", Context.MODE_PRIVATE)

    @Synchronized fun recent(limit: Int = 30): List<AgentRunRecord> = runCatching {
        val array = JSONArray(prefs.getString("runs", "[]"))
        (0 until array.length()).mapNotNull { array.optJSONObject(it)?.let(AgentRunRecord::fromJson) }
            .sortedByDescending { it.updatedAt }.take(limit.coerceIn(1, 100))
    }.getOrDefault(emptyList())

    @Synchronized fun start(taskId: String, chatId: String, title: String, webSearch: Boolean) {
        val now = System.currentTimeMillis()
        val record = AgentRunRecord(taskId, chatId, title.take(120), "Kuyruk", "Görev sıraya alındı", "running", now, now, webSearch)
        save((listOf(record) + recent(99).filterNot { it.taskId == taskId }).take(30))
    }

    @Synchronized fun update(taskId: String, status: String, success: Boolean? = null) {
        if (taskId.isBlank()) return
        val current = recent(100).toMutableList()
        val index = current.indexOfFirst { it.taskId == taskId }
        if (index < 0) return
        val previous = current[index]
        val cleanStatus = status.trim().take(240).ifBlank { "Durum güncellendi" }
        val outcome = when (success) { true -> "success"; false -> "error"; null -> if (previous.outcome == "running") "running" else previous.outcome }
        val stage = when {
            success == true -> "Tamamlandı"
            success == false -> "Hata / durduruldu"
            cleanStatus.contains("kuyruk", true) -> "Kuyruk"
            cleanStatus.contains("bağlam", true) || cleanStatus.contains("analiz", true) -> "Bağlam analizi"
            cleanStatus.contains("model", true) || cleanStatus.contains("yanıt", true) -> "Model üretimi"
            cleanStatus.contains("dosya", true) -> "Dosya çıktıları"
            cleanStatus.contains("kaynak", true) -> "Kaynaklar"
            else -> "Çalışıyor"
        }
        current[index] = previous.copy(stage = stage, status = cleanStatus, outcome = outcome, updatedAt = System.currentTimeMillis())
        save(current.sortedByDescending { it.updatedAt }.take(30))
    }

    /** Marks a task that has returned a command proposal and now waits for explicit user approval. */
    @Synchronized fun awaitApproval(taskId: String, status: String) {
        if (taskId.isBlank()) return
        val current = recent(100).toMutableList()
        val index = current.indexOfFirst { it.taskId == taskId }
        if (index < 0) return
        val previous = current[index]
        current[index] = previous.copy(
            stage = "Onay bekliyor",
            status = status.trim().take(240).ifBlank { "Terminal onayı bekleniyor" },
            outcome = "awaiting_approval",
            updatedAt = System.currentTimeMillis()
        )
        save(current.sortedByDescending { it.updatedAt }.take(30))
    }

    @Synchronized fun clearHistory() { prefs.edit().remove("runs").apply() }

    private fun save(records: List<AgentRunRecord>) {
        prefs.edit().putString("runs", JSONArray().also { a -> records.forEach { a.put(it.toJson()) } }.toString()).apply()
    }
}
