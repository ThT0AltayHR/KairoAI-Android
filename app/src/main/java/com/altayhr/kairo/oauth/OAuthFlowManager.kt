package com.altayhr.kairo.oauth

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.altayhr.kairo.data.SecureApiKeyStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import java.util.concurrent.TimeUnit

/** Direct OAuth 2.0 Authorization Code + PKCE for public clients. No hosted broker or server dependency. */
data class OAuthProfile(
    val id: String,
    val name: String,
    val clientId: String = "",
    val authorizeUrl: String = "",
    val tokenUrl: String = "",
    val scopes: String = "",
    val redirectUri: String = DEFAULT_REDIRECT
) {
    fun toJson() = JSONObject().put("id", id).put("name", name).put("clientId", clientId).put("authorizeUrl", authorizeUrl).put("tokenUrl", tokenUrl).put("scopes", scopes).put("redirectUri", redirectUri)
    companion object {
        const val DEFAULT_REDIRECT = "com.altayhr.kairo:/oauth2redirect"
        fun fromJson(o: JSONObject) = OAuthProfile(o.optString("id"), o.optString("name"), o.optString("clientId"), o.optString("authorizeUrl"), o.optString("tokenUrl"), o.optString("scopes"), o.optString("redirectUri", DEFAULT_REDIRECT))
        // Keep only public-client OAuth candidates here. GitHub/Notion automation can use narrowly scoped tokens stored in the local vault.
        fun presets() = listOf(
            OAuthProfile("spotify", "Spotify", authorizeUrl = "https://accounts.spotify.com/authorize", tokenUrl = "https://accounts.spotify.com/api/token", scopes = "user-read-private user-read-email user-read-playback-state user-read-currently-playing user-modify-playback-state"),
            OAuthProfile("custom", "Özel OAuth 2.0")
        )
    }
}

object OAuthFlowManager {
    const val ACTION_RESULT = "com.altayhr.kairo.oauth.RESULT"
    private const val PREFS = "kairo_oauth"
    private const val PENDING_SECRET = "oauth_pending_transaction"
    private val client = OkHttpClient.Builder().connectTimeout(12, TimeUnit.SECONDS).readTimeout(20, TimeUnit.SECONDS).callTimeout(25, TimeUnit.SECONDS).build()

    fun loadProfiles(context: Context): List<OAuthProfile> {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val custom = runCatching { org.json.JSONArray(prefs.getString("profiles", "[]")) }.getOrNull()
        val saved = if (custom != null) (0 until custom.length()).mapNotNull { i -> custom.optJSONObject(i)?.let(OAuthProfile::fromJson) } else emptyList()
        return (OAuthProfile.presets().map { preset -> saved.firstOrNull { it.id == preset.id } ?: preset } + saved.filter { p -> OAuthProfile.presets().none { it.id == p.id } }).distinctBy { it.id }
    }
    fun saveProfile(context: Context, profile: OAuthProfile) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val existing = loadProfiles(context).filterNot { it.id == profile.id }
        prefs.edit().putString("profiles", org.json.JSONArray().apply { (existing + profile).forEach { put(it.toJson()) } }.toString()).apply()
    }
    fun isConnected(context: Context, id: String) = !SecureApiKeyStore(context).getSecret("oauth_${id}_access_token").isNullOrBlank()
    fun accessToken(context: Context, id: String): String? = SecureApiKeyStore(context).getSecret("oauth_${id}_access_token")

    /** Use only on a background dispatcher. Public OAuth clients may refresh without a client secret if provider policy permits it. */
    fun accessTokenForUse(context: Context, id: String): String? {
        val secrets = SecureApiKeyStore(context)
        val current = secrets.getSecret("oauth_${id}_access_token") ?: return null
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (prefs.getLong("token_${id}_expiry", 0L) > System.currentTimeMillis() + 90_000L) return current
        val refresh = secrets.getSecret("oauth_${id}_refresh_token") ?: return current
        val profile = loadProfiles(context).firstOrNull { it.id == id } ?: return current
        return runCatching {
            val body = FormBody.Builder().add("grant_type", "refresh_token").add("refresh_token", refresh).add("client_id", profile.clientId).build()
            val request = Request.Builder().url(profile.tokenUrl).header("Accept", "application/json").post(body).build()
            val json = client.newCall(request).execute().use { response ->
                val responseText = response.body?.string().orEmpty()
                if (!response.isSuccessful) error("OAuth token yenileme başarısız (HTTP ${response.code}).")
                JSONObject(responseText)
            }
            val access = json.optString("access_token")
            if (access.isBlank()) error("Yenileme access token döndürmedi.")
            secrets.saveSecret("oauth_${id}_access_token", access)
            json.optString("refresh_token").takeIf { it.isNotBlank() }?.let { secrets.saveSecret("oauth_${id}_refresh_token", it) }
            prefs.edit().putLong("token_${id}_expiry", System.currentTimeMillis() + json.optLong("expires_in", 3600) * 1000L).apply()
            access
        }.getOrNull() ?: current
    }

    fun disconnect(context: Context, id: String) {
        SecureApiKeyStore(context).clearSecret("oauth_${id}_access_token")
        SecureApiKeyStore(context).clearSecret("oauth_${id}_refresh_token")
        SecureApiKeyStore(context).clearSecret(PENDING_SECRET)
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().remove("token_${id}_expiry").remove("token_${id}_scope").apply()
    }

    fun authorizationIntent(activity: Activity, profile: OAuthProfile): Intent {
        require(profile.clientId.isNotBlank()) { "OAuth istemci kimliği gerekli." }
        require(isHttpsEndpoint(profile.authorizeUrl) && isHttpsEndpoint(profile.tokenUrl)) { "OAuth uç noktaları güvenli HTTPS olmalı." }
        require(profile.redirectUri == OAuthProfile.DEFAULT_REDIRECT) { "Bu build yalnızca uygulamanın özel URI dönüşünü destekler." }
        val verifier = randomUrlSafe(64)
        val challenge = Base64.getUrlEncoder().withoutPadding().encodeToString(MessageDigest.getInstance("SHA-256").digest(verifier.toByteArray()))
        val state = randomUrlSafe(32)
        val pending = JSONObject().put("id", profile.id).put("clientId", profile.clientId)
            .put("tokenUrl", profile.tokenUrl).put("redirectUri", profile.redirectUri).put("verifier", verifier)
            .put("state", state).put("startedAt", System.currentTimeMillis())
        SecureApiKeyStore(activity).saveSecret(PENDING_SECRET, pending.toString())
        val uri = Uri.parse(profile.authorizeUrl).buildUpon().appendQueryParameter("response_type", "code").appendQueryParameter("client_id", profile.clientId)
            .appendQueryParameter("redirect_uri", profile.redirectUri).appendQueryParameter("state", state)
            .appendQueryParameter("code_challenge", challenge).appendQueryParameter("code_challenge_method", "S256")
            .also { if (profile.scopes.isNotBlank()) it.appendQueryParameter("scope", profile.scopes) }.build()
        return Intent(Intent.ACTION_VIEW, uri)
    }

    suspend fun handleRedirect(context: Context, uri: Uri): String = withContext(Dispatchers.IO) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val secrets = SecureApiKeyStore(context)
        val raw = secrets.getSecret(PENDING_SECRET) ?: return@withContext finish(context, "Bekleyen OAuth oturumu bulunamadı.", false)
        val pending = runCatching { JSONObject(raw) }.getOrNull() ?: run { secrets.clearSecret(PENDING_SECRET); return@withContext finish(context, "OAuth oturumu okunamadı.", false) }
        if (System.currentTimeMillis() - pending.optLong("startedAt") > 10 * 60_000) { secrets.clearSecret(PENDING_SECRET); return@withContext finish(context, "OAuth süresi doldu. Yeniden bağlan.", false) }
        if (!constantTimeEquals(uri.getQueryParameter("state").orEmpty(), pending.optString("state"))) {
            secrets.clearSecret(PENDING_SECRET); return@withContext finish(context, "OAuth güvenlik doğrulaması başarısız (state uyuşmuyor).", false)
        }
        uri.getQueryParameter("error")?.let { error -> secrets.clearSecret(PENDING_SECRET); return@withContext finish(context, "Yetkilendirme reddedildi: $error", false) }
        val code = uri.getQueryParameter("code") ?: run { secrets.clearSecret(PENDING_SECRET); return@withContext finish(context, "OAuth geri dönüşünde code bulunamadı.", false) }
        try {
            val profile = loadProfiles(context).firstOrNull { it.id == pending.optString("id") } ?: error("OAuth profili bulunamadı.")
            val form = FormBody.Builder().add("grant_type", "authorization_code").add("code", code)
                .add("client_id", pending.optString("clientId")).add("redirect_uri", pending.optString("redirectUri"))
                .add("code_verifier", pending.optString("verifier")).build()
            val request = Request.Builder().url(pending.optString("tokenUrl")).header("Accept", "application/json").post(form).build()
            val token = client.newCall(request).execute().use { response ->
                val text = response.body?.string().orEmpty()
                val json = runCatching { JSONObject(text) }.getOrElse { JSONObject().put("error_description", text.take(180)) }
                if (!response.isSuccessful) error("Token değişimi başarısız (HTTP ${response.code}): ${json.optString("error_description", json.optString("error", "sağlayıcı hatası"))}")
                json
            }
            val access = token.optString("access_token")
            if (access.isBlank()) error(token.optString("error_description", token.optString("error", "Sağlayıcı access_token döndürmedi.")))
            secrets.saveSecret("oauth_${profile.id}_access_token", access)
            token.optString("refresh_token").takeIf { it.isNotBlank() }?.let { secrets.saveSecret("oauth_${profile.id}_refresh_token", it) }
            prefs.edit().putLong("token_${profile.id}_expiry", System.currentTimeMillis() + token.optLong("expires_in", 3600) * 1000L).putString("token_${profile.id}_scope", token.optString("scope")).apply()
            secrets.clearSecret(PENDING_SECRET)
            finish(context, "${profile.name} bağlantısı başarıyla tamamlandı.", true)
        } catch (e: Exception) {
            secrets.clearSecret(PENDING_SECRET)
            finish(context, "OAuth hatası: ${e.message?.take(180) ?: "bağlantı hatası"}", false)
        }
    }

    private fun finish(context: Context, message: String, success: Boolean): String {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString("last_result", message).putBoolean("last_success", success).putLong("last_result_at", System.currentTimeMillis()).apply()
        context.sendBroadcast(Intent(ACTION_RESULT).setPackage(context.packageName).putExtra("message", message).putExtra("success", success))
        com.altayhr.kairo.automation.AutomationEvents.emit(context, if (success) "oauth.connected" else "oauth.failed", message, JSONObject().put("success", success).put("message", message.take(160)).toString())
        return message
    }
    fun lastResult(context: Context) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString("last_result", "OAuth bağlantı sonucu burada gösterilir.").orEmpty()
    fun lastSuccess(context: Context) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean("last_success", false)
    private fun randomUrlSafe(bytes: Int): String = Base64.getUrlEncoder().withoutPadding().encodeToString(ByteArray(bytes).also { SecureRandom().nextBytes(it) })
    private fun isHttpsEndpoint(value: String): Boolean = runCatching { val u = java.net.URI(value); u.scheme == "https" && !u.host.isNullOrBlank() && u.userInfo == null && u.query == null && u.fragment == null }.getOrDefault(false)
    private fun constantTimeEquals(a: String, b: String): Boolean { if (a.length != b.length) return false; var x = 0; for (i in a.indices) x = x or (a[i].code xor b[i].code); return x == 0 }
}
