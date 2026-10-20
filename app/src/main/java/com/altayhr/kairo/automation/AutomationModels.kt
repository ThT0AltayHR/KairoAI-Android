package com.altayhr.kairo.automation

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/** Locally stored automation. Secrets are referenced by name and fetched from Android Keystore-backed storage at run time. */
data class AutomationWorkflow(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val enabled: Boolean = true,
    val trigger: String = "manual", // manual | schedule | event
    val triggerValue: String = "", // event name
    val intervalMinutes: Int = 15,
    val method: String = "POST",
    val url: String,
    val headersJson: String = "{}",
    val bodyTemplate: String = "{\"event\": \"{{event}}\", \"timestamp\": \"{{timestamp}}\"}",
    val secretName: String = "",
    val notifyOnResult: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis(),
    val lastRunAt: Long = 0L,
    val lastStatus: String = "Henüz çalıştırılmadı",
    // Provider-specific direct API configuration; credentials are referenced by name in the encrypted local vault.
    val actionType: String = "http", // http | n8n | github_dispatch | resend_email | notion_page
    val owner: String = "",
    val repo: String = "",
    val workflow: String = "",
    val ref: String = "main",
    val to: String = "",
    val from: String = "",
    val subject: String = "",
    val content: String = "",
    val databaseId: String = "",
    val titleProperty: String = "Name",
    val title: String = ""
) {
    fun toJson() = JSONObject().apply {
        put("id", id); put("name", name); put("enabled", enabled); put("trigger", trigger); put("triggerValue", triggerValue)
        put("intervalMinutes", intervalMinutes.coerceIn(15, 10080)); put("actionType", actionType); put("method", method); put("url", url)
        put("headersJson", headersJson); put("bodyTemplate", bodyTemplate); put("secretName", secretName)
        put("owner", owner); put("repo", repo); put("workflow", workflow); put("ref", ref); put("to", to); put("from", from)
        put("subject", subject); put("content", content); put("databaseId", databaseId); put("titleProperty", titleProperty); put("title", title)
        put("notifyOnResult", notifyOnResult)
        put("updatedAt", updatedAt); put("lastRunAt", lastRunAt); put("lastStatus", lastStatus)
    }
    companion object {
        fun fromJson(o: JSONObject) = AutomationWorkflow(
            id = o.optString("id", UUID.randomUUID().toString()), name = o.optString("name", "İsimsiz akış"),
            enabled = o.optBoolean("enabled", true), trigger = o.optString("trigger", "manual").let { if (it == "webhook") "manual" else it }, triggerValue = o.optString("triggerValue", ""),
            intervalMinutes = o.optInt("intervalMinutes", 15).coerceIn(15, 10080), actionType = o.optString("actionType", "http"), method = o.optString("method", "POST"),
            url = o.optString("url", ""), headersJson = o.optString("headersJson", "{}"), bodyTemplate = o.optString("bodyTemplate", "{}"),
            secretName = o.optString("secretName", ""), owner = o.optString("owner", ""), repo = o.optString("repo", ""), workflow = o.optString("workflow", ""), ref = o.optString("ref", "main"),
            to = o.optString("to", ""), from = o.optString("from", ""), subject = o.optString("subject", ""), content = o.optString("content", ""), databaseId = o.optString("databaseId", ""), titleProperty = o.optString("titleProperty", "Name"), title = o.optString("title", ""),
            notifyOnResult = o.optBoolean("notifyOnResult", false), updatedAt = o.optLong("updatedAt", System.currentTimeMillis()),
            lastRunAt = o.optLong("lastRunAt", 0L), lastStatus = o.optString("lastStatus", "Henüz çalıştırılmadı")
        )
    }
}

data class AutomationEvent(val id: String, val type: String, val detail: String, val timestamp: Long = System.currentTimeMillis()) {
    fun toJson() = JSONObject().put("id", id).put("type", type).put("detail", detail).put("timestamp", timestamp)
    companion object { fun fromJson(o: JSONObject) = AutomationEvent(o.optString("id"), o.optString("type"), o.optString("detail"), o.optLong("timestamp")) }
}

class AutomationStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("kairo_automation", Context.MODE_PRIVATE)
    @Synchronized fun workflows(): List<AutomationWorkflow> = runCatching {
        val array = JSONArray(prefs.getString("workflows", "[]"))
        (0 until array.length()).mapNotNull { i -> array.optJSONObject(i)?.let(AutomationWorkflow::fromJson) }
    }.getOrDefault(emptyList())
    @Synchronized fun save(workflow: AutomationWorkflow) {
        val list = workflows().filterNot { it.id == workflow.id } + workflow.copy(updatedAt = System.currentTimeMillis())
        prefs.edit().putString("workflows", JSONArray().apply { list.forEach { put(it.toJson()) } }.toString()).apply()
    }
    @Synchronized fun delete(id: String) {
        val list = workflows().filterNot { it.id == id }
        prefs.edit().putString("workflows", JSONArray().apply { list.forEach { put(it.toJson()) } }.toString()).apply()
    }
    @Synchronized fun updateRun(id: String, status: String) {
        workflows().firstOrNull { it.id == id }?.let { save(it.copy(lastRunAt = System.currentTimeMillis(), lastStatus = status)) }
    }
    @Synchronized fun events(): List<AutomationEvent> = runCatching {
        val arr = JSONArray(prefs.getString("events", "[]")); (0 until arr.length()).mapNotNull { arr.optJSONObject(it)?.let(AutomationEvent::fromJson) }
    }.getOrDefault(emptyList())
    @Synchronized fun appendEvent(event: AutomationEvent) {
        val merged = (listOf(event) + events()).take(100)
        prefs.edit().putString("events", JSONArray().apply { merged.forEach { put(it.toJson()) } }.toString()).apply()
    }
}
