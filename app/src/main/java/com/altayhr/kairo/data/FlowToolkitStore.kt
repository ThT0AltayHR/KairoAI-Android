package com.altayhr.kairo.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/** Local equivalents for Flow's dictionary, replacements, snippets and writing preferences. */
data class WordReplacement(val id: String = UUID.randomUUID().toString(), val from: String, val to: String)
data class TextSnippet(val id: String = UUID.randomUUID().toString(), val trigger: String, val text: String)

class FlowToolkitStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("kairo_flow_tools", Context.MODE_PRIVATE)

    fun words(): List<String> = runCatching {
        val a = JSONArray(prefs.getString("words", "[]"))
        (0 until a.length()).mapNotNull { a.optString(it).trim().takeIf(String::isNotBlank) }.distinctBy { it.lowercase() }
    }.getOrDefault(emptyList())

    fun addWord(word: String): List<String> {
        val w = word.trim().take(100)
        val next = (words() + w).filter(String::isNotBlank).distinctBy { it.lowercase() }.take(300)
        prefs.edit().putString("words", JSONArray(next).toString()).apply()
        return words()
    }

    fun removeWord(word: String): List<String> {
        val next = words().filterNot { it.equals(word, true) }
        prefs.edit().putString("words", JSONArray(next).toString()).apply()
        return words()
    }

    fun replacements(): List<WordReplacement> = runCatching {
        val a = JSONArray(prefs.getString("replacements", "[]"))
        (0 until a.length()).mapNotNull { i ->
            a.optJSONObject(i)?.let { j ->
                val from = j.optString("from").trim()
                val to = j.optString("to").trim()
                if (from.isBlank() || to.isBlank()) null else WordReplacement(j.optString("id", UUID.randomUUID().toString()), from, to)
            }
        }
    }.getOrDefault(emptyList())

    fun saveReplacement(from: String, to: String, id: String? = null): List<WordReplacement> {
        val f = from.trim().take(160); val t = to.trim().take(160)
        if (f.isBlank() || t.isBlank()) return replacements()
        val existing = replacements().toMutableList()
        val index = existing.indexOfFirst { it.id == id || it.from.equals(f, true) }
        val value = WordReplacement(id ?: UUID.randomUUID().toString(), f, t)
        if (index >= 0) existing[index] = value else existing.add(value)
        saveReplacements(existing.take(300))
        return replacements()
    }

    fun removeReplacement(id: String): List<WordReplacement> {
        saveReplacements(replacements().filterNot { it.id == id })
        return replacements()
    }

    private fun saveReplacements(values: List<WordReplacement>) {
        prefs.edit().putString("replacements", JSONArray().also { a -> values.forEach { a.put(JSONObject().put("id", it.id).put("from", it.from).put("to", it.to)) } }.toString()).apply()
    }

    fun snippets(): List<TextSnippet> = runCatching {
        val a = JSONArray(prefs.getString("snippets", "[]"))
        (0 until a.length()).mapNotNull { i ->
            a.optJSONObject(i)?.let { j ->
                val trigger = j.optString("trigger").trim()
                val text = j.optString("text").trim()
                if (trigger.isBlank() || text.isBlank()) null else TextSnippet(j.optString("id", UUID.randomUUID().toString()), trigger, text)
            }
        }
    }.getOrDefault(emptyList())

    fun saveSnippet(trigger: String, text: String, id: String? = null): List<TextSnippet> {
        val t = trigger.trim().take(120); val body = text.trim().take(20_000)
        if (t.isBlank() || body.isBlank()) return snippets()
        val existing = snippets().toMutableList()
        val index = existing.indexOfFirst { it.id == id }
        val value = TextSnippet(id ?: UUID.randomUUID().toString(), t, body)
        if (index >= 0) existing[index] = value else existing.add(value)
        prefs.edit().putString("snippets", JSONArray().also { a -> existing.take(200).forEach { a.put(JSONObject().put("id", it.id).put("trigger", it.trigger).put("text", it.text)) } }.toString()).apply()
        return snippets()
    }

    fun removeSnippet(id: String): List<TextSnippet> {
        val next = snippets().filterNot { it.id == id }
        prefs.edit().putString("snippets", JSONArray().also { a -> next.forEach { a.put(JSONObject().put("id", it.id).put("trigger", it.trigger).put("text", it.text)) } }.toString()).apply()
        return snippets()
    }

    fun responseStyle(): String = prefs.getString("response_style", "natural") ?: "natural"
    fun saveResponseStyle(style: String) { prefs.edit().putString("response_style", style.takeIf { it in STYLE_IDS } ?: "natural").apply() }
    fun cleanupEnabled(): Boolean = prefs.getBoolean("cleanup_enabled", true)
    fun saveCleanupEnabled(enabled: Boolean) { prefs.edit().putBoolean("cleanup_enabled", enabled).apply() }

    /** User-facing preferences only; never returns model hidden reasoning. */
    fun modelGuidance(): String = buildString {
        val style = when (responseStyle()) {
            "concise" -> "Yanıt stili: kısa ve doğrudan; gereksiz tekrarları atla, önemli ayrıntıları sakla."
            "deep" -> "Yanıt stili: kapsamlı ve düzenli; varsayımlar, seçenekler, riskler ve doğrulama adımlarını görünür biçimde açıkla. Gizli düşünce zinciri verme."
            "professional" -> "Yanıt stili: profesyonel, net, tarafsız ve teslimata odaklı; başlıkları ve uygulanabilir maddeleri kullan."
            "friendly" -> "Yanıt stili: samimi ama dikkatli; açıklamaları kolay anlaşılır örneklerle destekle."
            "technical" -> "Yanıt stili: teknik; API, dosya yolu, hata koşulu ve kod ayrıntılarını belirt, kanıtsız iddia kullanma."
            else -> "Yanıt stili: doğal, dengeli ve gereksiz uzatmadan kaçınan Türkçe."
        }
        append(style)
        if (cleanupEnabled()) append("\nGirdi temizliği: Kullanıcının yazım niyetini koru; sözlük ve dönüşüm kurallarını yazım/dikte belirsizliğinde yardımcı sinyal olarak kullan. Kod, URL, dosya yolu, komut ve tanımlayıcıları otomatik değiştirme.")
        val ws = words().take(100)
        if (ws.isNotEmpty()) append("\nÖzel yazımlar (aynen koru): ").append(ws.joinToString(", "))
        val rs = replacements().take(100)
        if (rs.isNotEmpty()) append("\nDikte/düzeltme tercihleri: ").append(rs.joinToString("; ") { "${it.from} → ${it.to}" })
    }.take(12_000)

    companion object {
        val STYLE_IDS = setOf("natural", "concise", "deep", "professional", "friendly", "technical")
    }
}
