package com.altayhr.kairo.data

import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

private fun JSONArray.strings(): List<String> = (0 until length()).mapNotNull { optString(it).takeIf(String::isNotBlank) }

 data class WebSource(val title: String, val url: String)

data class WorkspaceFile(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val mimeType: String = "application/octet-stream",
    val size: Long = 0L,
    val relativePath: String,
    val origin: String = "uploaded",
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toJson() = JSONObject().apply {
        put("id", id); put("name", name); put("mimeType", mimeType); put("size", size)
        put("relativePath", relativePath); put("origin", origin); put("createdAt", createdAt)
    }
    companion object {
        fun fromJson(j: JSONObject) = WorkspaceFile(
            id = j.optString("id", UUID.randomUUID().toString()),
            name = j.optString("name", "Dosya"), mimeType = j.optString("mimeType", "application/octet-stream"),
            size = j.optLong("size"), relativePath = j.optString("relativePath"),
            origin = j.optString("origin", "uploaded"), createdAt = j.optLong("createdAt", System.currentTimeMillis())
        )
    }
}

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val role: String,
    val content: String,
    val createdAt: Long = System.currentTimeMillis(),
    val attachments: List<WorkspaceFile> = emptyList(),
    val sources: List<WebSource> = emptyList(),
    val isError: Boolean = false
) {
    fun toJson() = JSONObject().apply {
        put("id", id); put("role", role); put("content", content); put("createdAt", createdAt)
        put("attachments", JSONArray().also { a -> attachments.forEach { a.put(it.toJson()) } })
        put("sources", JSONArray().also { a -> sources.forEach { a.put(JSONObject().put("title", it.title).put("url", it.url)) } })
        put("isError", isError)
    }
    companion object {
        fun fromJson(j: JSONObject) = ChatMessage(
            id = j.optString("id", UUID.randomUUID().toString()), role = j.optString("role", "assistant"),
            content = j.optString("content"), createdAt = j.optLong("createdAt", System.currentTimeMillis()),
            attachments = (j.optJSONArray("attachments") ?: JSONArray()).let { a -> (0 until a.length()).mapNotNull { a.optJSONObject(it)?.let(WorkspaceFile::fromJson) } },
            sources = (j.optJSONArray("sources") ?: JSONArray()).let { a -> (0 until a.length()).mapNotNull { i -> a.optJSONObject(i)?.let { WebSource(it.optString("title", "Kaynak"), it.optString("url")) } } },
            isError = j.optBoolean("isError")
        )
    }
}

data class ChatSession(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "Yeni sohbet",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val pinned: Boolean = false,
    val projectId: String? = null,
    val messages: List<ChatMessage> = emptyList(),
    val files: List<WorkspaceFile> = emptyList()
) {
    fun toJson() = JSONObject().apply {
        put("id", id); put("title", title); put("createdAt", createdAt); put("updatedAt", updatedAt)
        put("pinned", pinned); put("projectId", projectId ?: JSONObject.NULL)
        put("messages", JSONArray().also { a -> messages.forEach { a.put(it.toJson()) } })
        put("files", JSONArray().also { a -> files.forEach { a.put(it.toJson()) } })
    }
    companion object {
        fun fromJson(j: JSONObject) = ChatSession(
            id = j.optString("id", UUID.randomUUID().toString()), title = j.optString("title", "Yeni sohbet"),
            createdAt = j.optLong("createdAt", System.currentTimeMillis()), updatedAt = j.optLong("updatedAt", System.currentTimeMillis()),
            pinned = j.optBoolean("pinned"), projectId = j.optString("projectId").takeIf { it.isNotBlank() && it != "null" },
            messages = (j.optJSONArray("messages") ?: JSONArray()).let { a -> (0 until a.length()).mapNotNull { a.optJSONObject(it)?.let(ChatMessage::fromJson) } },
            files = (j.optJSONArray("files") ?: JSONArray()).let { a -> (0 until a.length()).mapNotNull { a.optJSONObject(it)?.let(WorkspaceFile::fromJson) } }
        )
    }
}

data class WorkspaceProject(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val colorIndex: Int = 0,
    val files: List<WorkspaceFile> = emptyList()
) {
    fun toJson() = JSONObject().apply {
        put("id", id); put("name", name); put("description", description); put("createdAt", createdAt); put("colorIndex", colorIndex)
        put("files", JSONArray().also { a -> files.forEach { a.put(it.toJson()) } })
    }
    companion object {
        fun fromJson(j: JSONObject) = WorkspaceProject(
            id = j.optString("id", UUID.randomUUID().toString()), name = j.optString("name", "Yeni proje"),
            description = j.optString("description"), createdAt = j.optLong("createdAt", System.currentTimeMillis()), colorIndex = j.optInt("colorIndex"),
            files = (j.optJSONArray("files") ?: JSONArray()).let { a -> (0 until a.length()).mapNotNull { a.optJSONObject(it)?.let(WorkspaceFile::fromJson) } }
        )
    }
}
