package com.altayhr.kairo.automation

import android.content.Context
import com.altayhr.kairo.data.SecureApiKeyStore
import com.altayhr.kairo.oauth.OAuthFlowManager
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.net.URI
import java.util.concurrent.TimeUnit

/** Device-local outgoing automation. Provider credentials are fetched at execution time and never inserted into AI prompts. */
object AutomationExecutor {
    data class Result(val success: Boolean, val message: String, val httpCode: Int? = null)
    private val client = OkHttpClient.Builder().connectTimeout(12, TimeUnit.SECONDS).readTimeout(20, TimeUnit.SECONDS).callTimeout(25, TimeUnit.SECONDS).build()

    fun run(context: Context, workflow: AutomationWorkflow, eventName: String = "manual", payloadJson: String = "{}"): Result {
        if (!workflow.enabled) return Result(false, "İş akışı devre dışı.")
        val secrets = SecureApiKeyStore(context)
        val secret = if (workflow.secretName.startsWith("oauth:")) {
            OAuthFlowManager.accessTokenForUse(context, workflow.secretName.removePrefix("oauth:"))
        } else workflow.secretName.takeIf { it.isNotBlank() }?.let(secrets::getSecret)
        if (workflow.secretName.isNotBlank() && secret.isNullOrBlank()) {
            return Result(false, "Şifreli kasadaki kimlik bilgisi bulunamadı: ${workflow.secretName}")
        }
        val payload = runCatching { JSONObject(payloadJson) }.getOrElse { JSONObject().put("value", payloadJson.take(10000)) }
        val event = eventName.take(100).replace("\"", "\\\"")
        val timestamp = System.currentTimeMillis().toString()
        val built = buildRequest(workflow, secret, event, timestamp, payload)
            ?: return Result(false, "Akış eylemi veya yapılandırma geçersiz.")
        val uri = runCatching { URI(built.url) }.getOrNull() ?: return Result(false, "URL geçerli değil.")
        val host = uri.host.orEmpty().lowercase()
        if (uri.scheme != "https" || host.isBlank() || uri.userInfo != null || host == "localhost" || host.endsWith(".localhost") ||
            host.endsWith(".local") || host.endsWith(".internal") || host.endsWith(".test") || host.endsWith(".invalid") ||
            host.endsWith(".example") || host.contains(":") || host.matches(Regex("\\d{1,3}(\\.\\d{1,3}){3}"))) {
            return Result(false, "Güvenlik için genel erişimli HTTPS URL'si kullanılmalı.")
        }
        val requestBuilder = Request.Builder().url(uri.toASCIIString()).header("User-Agent", "KairoAI-Android/1.0.0")
        built.headers.forEach { (key, value) -> if (!key.equals("host", true) && !key.equals("content-length", true)) requestBuilder.header(key, value) }
        if (!secret.isNullOrBlank() && built.headers.keys.none { it.equals("authorization", true) }) {
            requestBuilder.header("Authorization", "Bearer $secret")
        }
        val method = built.method.uppercase().takeIf { it in setOf("GET", "POST", "PUT", "PATCH", "DELETE") } ?: return Result(false, "HTTP yöntemi desteklenmiyor.")
        val body = if (method in setOf("GET", "DELETE")) null else built.body.toRequestBody("application/json; charset=utf-8".toMediaType())
        return try {
            client.newCall(requestBuilder.method(method, body).build()).execute().use { response ->
                // Never store or return authorization values or full remote response bodies.
                response.body?.close()
                Result(response.isSuccessful, "HTTP ${response.code}", response.code)
            }
        } catch (e: Exception) {
            Result(false, "İstek başarısız: ${e.message?.take(140) ?: "bağlantı hatası"}")
        }
    }

    private data class BuiltRequest(val url: String, val method: String, val headers: Map<String, String>, val body: String)

    private fun buildRequest(workflow: AutomationWorkflow, secret: String?, event: String, timestamp: String, payload: JSONObject): BuiltRequest? {
        val headers = runCatching {
            val obj = JSONObject(workflow.headersJson.ifBlank { "{}" })
            mutableMapOf<String, String>().apply { val keys = obj.keys(); while (keys.hasNext()) { val key = keys.next(); if (key.length in 1..80) put(key, obj.optString(key).take(2048)) } }
        }.getOrNull() ?: return null
        val sensitiveHeaderNames = setOf("authorization", "proxy-authorization", "x-api-key", "api-key", "apikey", "x-auth-token", "token", "secret")
        if (headers.keys.any { it.lowercase() in sensitiveHeaderNames }) return null
        val templatedBody = workflow.bodyTemplate
            .replace("{{event}}", event)
            .replace("{{timestamp}}", timestamp)
            .replace("{{payload}}", payload.toString())
        return when (workflow.actionType.lowercase()) {
            "http", "n8n" -> BuiltRequest(workflow.url.trim(), workflow.method, headers, templatedBody)
            "github_dispatch" -> {
                if (workflow.owner.isBlank() || workflow.repo.isBlank() || workflow.workflow.isBlank() || secret.isNullOrBlank()) return null
                val pathSafe = Regex("[A-Za-z0-9_.-]{1,100}")
                if (!pathSafe.matches(workflow.owner.trim()) || !pathSafe.matches(workflow.repo.trim()) || !pathSafe.matches(workflow.workflow.trim())) return null
                headers["Accept"] = "application/vnd.github+json"
                headers["X-GitHub-Api-Version"] = "2022-11-28"
                val refValue = workflow.ref.ifBlank { "main" }.take(200)
                val inputs = runCatching {
                    val inputConfig = JSONObject(workflow.bodyTemplate)
                    inputConfig.optJSONObject("inputs") ?: inputConfig
                }.getOrElse { JSONObject() }
                val body = JSONObject().put("ref", refValue).put("inputs", inputs).toString()
                BuiltRequest("https://api.github.com/repos/${workflow.owner.trim()}/${workflow.repo.trim()}/actions/workflows/${workflow.workflow.trim()}/dispatches", "POST", headers, body)
            }
            "resend_email" -> {
                if (workflow.to.isBlank() || workflow.from.isBlank() || workflow.subject.isBlank() || secret.isNullOrBlank()) return null
                val to = JSONArray().put(workflow.to.trim())
                val body = JSONObject().put("from", workflow.from.trim()).put("to", to).put("subject", workflow.subject.take(200))
                    .put("text", workflow.content.replace("{{event}}", event).replace("{{timestamp}}", timestamp).take(20000)).toString()
                BuiltRequest("https://api.resend.com/emails", "POST", headers, body)
            }
            "notion_page" -> {
                if (workflow.databaseId.isBlank() || workflow.title.isBlank() || secret.isNullOrBlank()) return null
                headers["Notion-Version"] = "2022-06-28"
                val property = workflow.titleProperty.ifBlank { "Name" }
                val body = JSONObject().put("parent", JSONObject().put("database_id", workflow.databaseId.trim()))
                    .put("properties", JSONObject().put(property, JSONObject().put("title", JSONArray().put(JSONObject().put("text", JSONObject().put("content", workflow.title.take(180)))))))
                    .put("children", JSONArray().put(JSONObject().put("object", "block").put("type", "paragraph").put("paragraph", JSONObject().put("rich_text", JSONArray().put(JSONObject().put("type", "text").put("text", JSONObject().put("content", workflow.content.replace("{{event}}", event).replace("{{timestamp}}", timestamp).take(1800))))))))
                    .toString()
                BuiltRequest("https://api.notion.com/v1/pages", "POST", headers, body)
            }
            else -> null
        }
    }
}
