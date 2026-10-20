package com.altayhr.kairo.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class KairoNote(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val body: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toJson() = JSONObject().put("id", id).put("title", title).put("body", body).put("createdAt", createdAt).put("updatedAt", updatedAt)
    companion object {
        fun fromJson(j: JSONObject) = KairoNote(j.optString("id", UUID.randomUUID().toString()), j.optString("title", "Not"), j.optString("body"), j.optLong("createdAt"), j.optLong("updatedAt"))
    }
}

class NotesRepository(context: Context) {
    private val prefs = context.getSharedPreferences("kairo_notes", Context.MODE_PRIVATE)
    @Synchronized fun getAll(): List<KairoNote> = runCatching {
        val a = JSONArray(prefs.getString("notes", "[]"))
        (0 until a.length()).mapNotNull { a.optJSONObject(it)?.let(KairoNote::fromJson) }.sortedByDescending { it.updatedAt }
    }.getOrDefault(emptyList())
    @Synchronized fun saveAll(notes: List<KairoNote>) {
        prefs.edit().putString("notes", JSONArray().also { a -> notes.forEach { a.put(it.toJson()) } }.toString()).apply()
    }
}
