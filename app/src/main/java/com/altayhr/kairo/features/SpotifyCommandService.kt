package com.altayhr.kairo.features

import android.content.Context
import com.altayhr.kairo.oauth.OAuthFlowManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/** Explicit Turkish Spotify playback intents; only runs after a user message and an existing Spotify connection. */
object SpotifyCommandService {
    private val http = OkHttpClient.Builder().connectTimeout(10,TimeUnit.SECONDS).readTimeout(15,TimeUnit.SECONDS).callTimeout(20,TimeUnit.SECONDS).build()
    fun isCommand(text: String): Boolean {
        val s=text.lowercase()
        val musicContext = s.contains("spotify") || s.contains("şarkı") || s.contains("müziği") || s.contains("parça")
        return musicContext && listOf("çal","aç","başlat","oynat","duraklat","sonraki parça","önceki parça","karıştır","tekrar et","sürdür").any { s.contains(it) }
    }
    suspend fun execute(context: Context, text: String): String = withContext(Dispatchers.IO) {
        val token=OAuthFlowManager.accessTokenForUse(context,"spotify") ?: return@withContext "Spotify hesabı bağlı değil. Medya merkezinden hesabını bağla."
        val lower=text.lowercase()
        try {
            when {
                listOf("duraklat","durdur").any { lower.contains(it) } -> { call(token,"PUT","me/player/pause"); context.getSharedPreferences("kairo_spotify",Context.MODE_PRIVATE).edit().putBoolean("is_playing",false).apply(); "Spotify oynatması duraklatıldı." }
                lower.contains("sürdür") || lower.contains("oynatmayı başlat") -> { call(token,"PUT","me/player/play"); context.getSharedPreferences("kairo_spotify",Context.MODE_PRIVATE).edit().putBoolean("is_playing",true).apply(); "Spotify oynatması sürdürüldü." }
                lower.contains("sonraki parça") || lower.contains("sonraki şarkı") -> { call(token,"POST","me/player/next"); "Spotify'da sonraki parçaya geçildi." }
                lower.contains("önceki parça") || lower.contains("önceki şarkı") -> { call(token,"POST","me/player/previous"); "Spotify'da önceki parçaya geçildi." }
                lower.contains("karıştır") -> {
                    val current=call(token,"GET","me/player")
                    val next=!current.optBoolean("shuffle_state")
                    call(token,"PUT","me/player/shuffle?state=$next")
                    context.getSharedPreferences("kairo_spotify",Context.MODE_PRIVATE).edit().putBoolean("shuffle", next).apply()
                    "Spotify karıştırma ${if(next) "açıldı" else "kapatıldı"}."
                }
                lower.contains("tekrar") -> {
                    val current=call(token,"GET","me/player")
                    val mode=when(current.optString("repeat_state","off")){"off"->"context";"context"->"track";else->"off"}
                    call(token,"PUT","me/player/repeat?state=$mode")
                    context.getSharedPreferences("kairo_spotify",Context.MODE_PRIVATE).edit().putString("repeat", mode).apply()
                    "Spotify tekrar modu: ${when(mode){"track"->"tek parça";"context"->"albüm/çalma listesi";else->"kapalı"}}."
                }
                else -> {
                    var query=text.replace(Regex("(?i)spotify('da|da)?")," ")
                    query=query.replace(Regex("(?i)\\b(lütfen|bana|şunu|şu müziği|müziği|şarkıyı|parçayı|aç|çal|başlat|oynat|dinlet|bul|spotify'dan)\\b")," ").replace(Regex("\\s+")," ").trim()
                    if(query.length<2) return@withContext "Hangi şarkıyı veya sanatçıyı açmamı istediğini belirt. Örnek: Spotify'da Daft Punk Get Lucky çal."
                    val search=call(token,"GET","search?type=track&limit=1&q="+java.net.URLEncoder.encode(query,"UTF-8"))
                    val tracks=search.optJSONObject("tracks")?.optJSONArray("items")
                    val track=tracks?.optJSONObject(0) ?: return@withContext "Spotify'da '$query' için eşleşen parça bulunamadı."
                    val uri=track.optString("uri")
                    val name=track.optString("name")
                    val artists=track.optJSONArray("artists")
                    val artist=(0 until (artists?.length()?:0)).mapNotNull { artists?.optJSONObject(it)?.optString("name") }.joinToString(", ")
                    val body=JSONObject().put("uris",org.json.JSONArray().put(uri)).toString()
                    val started=runCatching { call(token,"PUT","me/player/play",body); true }.getOrDefault(false)
                    if(!started) {
                        val devices=call(token,"GET","me/player/devices").optJSONArray("devices")
                        val device=(0 until (devices?.length()?:0)).mapNotNull { devices?.optJSONObject(it) }.firstOrNull { it.optBoolean("is_active") } ?: devices?.optJSONObject(0)
                        val id=device?.optString("id").orEmpty()
                        if(id.isBlank()) return@withContext "Parça bulundu: $name — $artist. Spotify'da önce bir cihazda oynatmayı başlat, sonra tekrar dene."
                        call(token,"PUT","me/player/play?device_id="+java.net.URLEncoder.encode(id,"UTF-8"),body)
                    }
                    context.getSharedPreferences("kairo_spotify",Context.MODE_PRIVATE).edit().putString("last_title",name).putString("last_artist",artist).putString("last_image",track.optJSONObject("album")?.optJSONArray("images")?.optJSONObject(0)?.optString("url").orEmpty()).putBoolean("is_playing",true).apply()
                    context.sendBroadcast(android.content.Intent("com.altayhr.kairo.spotify.UPDATE").setPackage(context.packageName))
                    "Spotify'da çalınıyor: $name — $artist."
                }
            }
        } catch(e:Exception) { "Spotify işlemi tamamlanamadı: ${e.message?.take(160) ?: "API hatası"}" }
    }
    private fun call(token:String,method:String,path:String,body:String?=null):JSONObject {
        val req=Request.Builder().url("https://api.spotify.com/v1/$path").header("Authorization","Bearer $token").apply {
            when(method) { "GET" -> get(); "POST" -> post((body ?: "{}").toRequestBody("application/json; charset=utf-8".toMediaType())); "PUT" -> put((body ?: "{}").toRequestBody("application/json; charset=utf-8".toMediaType())); else -> error("Unsupported method") }
        }.build()
        return http.newCall(req).execute().use { r ->
            val raw=r.body?.string().orEmpty()
            if(!r.isSuccessful && r.code!=204) error(when(r.code){401->"Spotify oturumu sona erdi; yeniden bağlan.";403->"Spotify bu eyleme izin vermedi. Hesap/izin veya Premium koşullarını kontrol et.";404->"Aktif Spotify cihazı bulunamadı.";429->"Spotify hız sınırına ulaşıldı; biraz bekle.";else->"Spotify HTTP ${r.code}"})
            if(raw.isBlank()) JSONObject() else JSONObject(raw)
        }
    }
}
