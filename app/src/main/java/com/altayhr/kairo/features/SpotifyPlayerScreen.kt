package com.altayhr.kairo.features

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.altayhr.kairo.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.altayhr.kairo.data.SecureApiKeyStore
import com.altayhr.kairo.oauth.OAuthFlowManager
import com.altayhr.kairo.oauth.OAuthProfile
import com.altayhr.kairo.service.IslandOverlayService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaType
import java.util.concurrent.TimeUnit
import org.json.JSONObject

private val SpotifyGreen = Color(0xFF1ED760)
private val SpotifyPanel = Color(0xFF12141D)
private val SpotifyText = Color(0xFFF4F5F7)
private val SpotifyMuted = Color(0xFF9DA5B5)

private data class SpotifyPlayback(val title:String, val artist:String, val album:String, val image:String?, val progress:Int, val duration:Int, val playing:Boolean, val device:String?)

@Composable
fun SpotifyPlayerScreen() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("kairo_spotify", Context.MODE_PRIVATE) }
    val secrets = remember { SecureApiKeyStore(context) }
    var clientId by remember { mutableStateOf(prefs.getString("client_id", "") ?: "") }
    var showClientId by remember { mutableStateOf(clientId.isNotBlank()) }
    var playback by remember { mutableStateOf<SpotifyPlayback?>(null) }
    var busy by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf(if (OAuthFlowManager.isConnected(context,"spotify")) "Spotify bağlantısı kontrol ediliyor…" else "Spotify hesabını bağla; sonra Kairo'dan müzik isteyebilirsin.") }
    var error by remember { mutableStateOf<String?>(null) }
    var refresh by remember { mutableIntStateOf(0) }
    var repeat by remember { mutableStateOf("off") }
    var shuffle by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun profile() = OAuthProfile("spotify", "Spotify", clientId = clientId.trim(), authorizeUrl = "https://accounts.spotify.com/authorize", tokenUrl = "https://accounts.spotify.com/api/token", scopes = "user-read-private user-read-email user-read-playback-state user-read-currently-playing user-modify-playback-state", redirectUri = OAuthProfile.DEFAULT_REDIRECT)
    suspend fun api(method:String, path:String, body:String?=null): JSONObject? = withContext(Dispatchers.IO) {
        val token = OAuthFlowManager.accessTokenForUse(context,"spotify") ?: error("Spotify hesabını bağla.")
        val req = Request.Builder().url("https://api.spotify.com/v1/$path").header("Authorization","Bearer $token").apply {
            when(method) { "GET" -> get(); "PUT" -> put((body ?: "{}").toRequestBody("application/json; charset=utf-8".toMediaType())); "POST" -> post((body ?: "{}").toRequestBody("application/json; charset=utf-8".toMediaType())); "DELETE" -> delete() }
        }.build()
        OkHttpClient.Builder().connectTimeout(10,TimeUnit.SECONDS).readTimeout(12,TimeUnit.SECONDS).build().newCall(req).execute().use { r ->
            val raw = r.body?.string().orEmpty()
            if (r.code == 204 || r.code == 202) JSONObject() else {
                if (!r.isSuccessful) error(when(r.code) { 401 -> "Spotify oturumu sona erdi. Yeniden bağlan."; 403 -> "Spotify bu işlemi hesabın/cihazın için kabul etmedi. Oynatma kontrolü Premium gerektirebilir."; 404 -> "Aktif Spotify cihazı bulunamadı. Spotify'da bir cihazda müzik başlat."; else -> "Spotify API hatası (HTTP ${r.code})." })
                JSONObject(raw)
            }
        }
    }
    suspend fun load() {
        if (!OAuthFlowManager.isConnected(context,"spotify")) {
            status = "Spotify hesabınızı bağlayın"; playback = null
            if (IslandOverlayService.isEnabled(context)) IslandOverlayService.publish(context, "Spotify hesabınızı bağlayın", "Dinamik kapsülde Spotify sayfasını açıp bağlan düğmesine dokunun.", "music")
            return
        }
        busy = true; error = null
        if (IslandOverlayService.isEnabled(context)) IslandOverlayService.publish(context, "Spotify'a bağlanılıyor", "Oynatma durumu Spotify Web API üzerinden kontrol ediliyor.", "working")
        try {
            val json = api("GET","me/player")
            if (json == null || json.length() == 0) { playback = null; status = "Spotify bağlı · henüz aktif oynatma yok" }
            else {
                val item = json.optJSONObject("item")
                val artists = item?.optJSONArray("artists")
                val artist = (0 until (artists?.length() ?: 0)).mapNotNull { artists?.optJSONObject(it)?.optString("name") }.joinToString(", ")
                val images = item?.optJSONObject("album")?.optJSONArray("images")
                playback = SpotifyPlayback(item?.optString("name").orEmpty(),artist,item?.optJSONObject("album")?.optString("name").orEmpty(),images?.optJSONObject(0)?.optString("url")?.takeIf { !it.isNullOrBlank() },json.optInt("progress_ms"),item?.optInt("duration_ms") ?: 0,json.optBoolean("is_playing"),json.optJSONObject("device")?.optString("name"))
                status = if (json.optBoolean("is_playing")) "Şu anda çalıyor" else "Oynatma duraklatıldı"
                repeat = json.optString("repeat_state","off"); shuffle = json.optBoolean("shuffle_state")
                prefs.edit().putString("last_title",playback?.title).putString("last_artist",playback?.artist).putString("last_image",playback?.image).putBoolean("is_playing",playback?.playing == true).putBoolean("shuffle", shuffle).putString("repeat", repeat).apply()
                context.sendBroadcast(Intent("com.altayhr.kairo.spotify.UPDATE").setPackage(context.packageName))
                if (IslandOverlayService.isEnabled(context)) IslandOverlayService.publish(context, "Spotify", "${playback?.title.orEmpty()} · ${playback?.artist.orEmpty()}", "music")
            }
            if (json == null || json.length() == 0) {
                context.getSharedPreferences("kairo_spotify", Context.MODE_PRIVATE).edit().putBoolean("is_playing", false).apply()
                context.sendBroadcast(Intent("com.altayhr.kairo.spotify.UPDATE").setPackage(context.packageName))
                if (IslandOverlayService.isEnabled(context)) IslandOverlayService.publish(context, "Spotify bağlı", "Aktif parça yok · dokunarak Spotify kontrollerini aç.", "music")
            }
        } catch (e:Exception) { error = e.message ?: "Spotify bağlantısı başarısız"; status = "Spotify bağlantısı kurulamadı"
            if (IslandOverlayService.isEnabled(context)) IslandOverlayService.publish(context, "Spotify bağlantısı gerekli", "Spotify hesabını bağla", "error")
        }
        finally { busy = false }
    }
    LaunchedEffect(refresh) { load() }
    LaunchedEffect(Unit) { while (true) { delay(15_000); if (OAuthFlowManager.isConnected(context,"spotify")) refresh++ } }

    Column(Modifier.fillMaxSize().background(Color(0xFF090A10)).padding(17.dp), verticalArrangement = Arrangement.spacedBy(13.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(43.dp).clip(RoundedCornerShape(14.dp)).background(SpotifyGreen), contentAlignment = Alignment.Center) { SpotifyMark(Modifier.size(31.dp)) }
            Column(Modifier.weight(1f).padding(start=11.dp)) { Text("Spotify", color=SpotifyText,fontSize=25.sp,fontWeight=FontWeight.Bold); Text("Kairo AI · canlı müzik kontrolü",color=SpotifyMuted,fontSize=11.sp) }
            IconButton(onClick={refresh++}) { Icon(Icons.Outlined.Refresh,null,tint=SpotifyMuted) }
        }
        if (!OAuthFlowManager.isConnected(context,"spotify")) {
            Card(colors=CardDefaults.cardColors(containerColor=SpotifyPanel),border=BorderStroke(1.dp,SpotifyGreen.copy(alpha=.55f)),shape=RoundedCornerShape(19.dp)) {
                Column(Modifier.fillMaxWidth().padding(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)) {
                    Text("Spotify hesabınızı bağlayın",color=SpotifyText,fontSize=18.sp,fontWeight=FontWeight.SemiBold)
                    Text("Bağlantı kurulduğunda yapay zekâ müziği arayabilir, oynatmayı ve geçişleri Spotify Web API izinleri kapsamında kontrol edebilir.",color=SpotifyMuted,fontSize=12.sp,lineHeight=18.sp)
                    OutlinedTextField(value=clientId,onValueChange={clientId=it},modifier=Modifier.fillMaxWidth(),label={Text("Spotify Developer Client ID")},singleLine=true,colors=OutlinedTextFieldDefaults.colors(focusedBorderColor=SpotifyGreen,unfocusedBorderColor=Color(0xFF2B3040),focusedTextColor=SpotifyText,unfocusedTextColor=SpotifyText))
                    Text("Spotify Developer Dashboard’da Client ID oluştur ve Redirect URI olarak ${OAuthProfile.DEFAULT_REDIRECT} adresini tam eşleşmeyle kaydet. Kairo, Authorization Code + PKCE kullanır; gizli Client Secret uygulamaya gömülmez. Bazı geliştirici hesaplarında özel URI şeması reddedilirse HTTPS App Link desteği olan bir alan adı gerekir.",color=SpotifyMuted,fontSize=10.sp,lineHeight=15.sp)
                    Button(onClick={
                        prefs.edit().putString("client_id",clientId.trim()).apply()
                        busy = true; status = "Spotify'a bağlanılıyor…"; error = null
                        if (IslandOverlayService.isEnabled(context)) IslandOverlayService.publish(context, "Spotify'a bağlanılıyor", "Güvenli hesap izni için Spotify açılıyor.", "working")
                        try { require(context is Activity) { "Bu ekranı doğrudan uygulamada aç." }; OAuthFlowManager.saveProfile(context, profile()); context.startActivity(OAuthFlowManager.authorizationIntent(context,profile())) }
                        catch(e:Exception) { busy = false; error=e.message ?: "Spotify OAuth başlatılamadı"; if (IslandOverlayService.isEnabled(context)) IslandOverlayService.publish(context, "Spotify bağlantısı başarısız", error ?: "Bağlantı kurulamadı", "error") }
                    },modifier=Modifier.fillMaxWidth(),colors=ButtonDefaults.buttonColors(containerColor=SpotifyGreen,contentColor=Color.Black)) { Icon(Icons.Outlined.Link,null); Spacer(Modifier.width(8.dp)); Text("Spotify'a bağlan",fontWeight=FontWeight.Bold) }
                }
            }
        } else {
            Card(colors=CardDefaults.cardColors(containerColor=SpotifyPanel),border=BorderStroke(1.dp,Color(0xFF2B3040)),shape=RoundedCornerShape(21.dp)) {
                Column(Modifier.fillMaxWidth().padding(16.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(12.dp)) {
                    val bitmap = remember(playback?.image) { mutableStateOf<android.graphics.Bitmap?>(null) }
                    LaunchedEffect(playback?.image) { bitmap.value = withContext(Dispatchers.IO) { playback?.image?.let { runCatching { OkHttpClient().newCall(Request.Builder().url(it).build()).execute().use { response -> response.body?.byteStream()?.use { stream -> BitmapFactory.decodeStream(stream) } } }.getOrNull() } } }
                    if (bitmap.value != null) Image(bitmap.value!!.asImageBitmap(),null,Modifier.fillMaxWidth(.72f).aspectRatio(1f).clip(RoundedCornerShape(16.dp)))
                    else Box(Modifier.fillMaxWidth(.72f).aspectRatio(1f).clip(RoundedCornerShape(16.dp)).background(Color(0xFF1A3828)),contentAlignment=Alignment.Center) { Text("SPOTIFY",color=SpotifyGreen,fontWeight=FontWeight.Black,fontSize=22.sp) }
                    Text(playback?.title ?: "Çalan parça yok",color=SpotifyText,fontSize=19.sp,fontWeight=FontWeight.Bold)
                    Text(playback?.artist ?: "Spotify bağlı",color=SpotifyMuted,fontSize=13.sp)
                    Text(playback?.album.orEmpty(),color=SpotifyMuted,fontSize=11.sp)
                    LinearProgressIndicator(progress={ if ((playback?.duration ?: 0)>0) (playback!!.progress.toFloat()/playback!!.duration).coerceIn(0f,1f) else 0f },modifier=Modifier.fillMaxWidth(),color=SpotifyGreen,trackColor=Color(0xFF30333C))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = {
                            scope.launch {
                                try { api("POST", "me/player/previous"); delay(350); refresh++ } catch (e: Exception) { error = e.message }
                            }
                        }) { Icon(Icons.Outlined.SkipPrevious, "Önceki parça", tint = SpotifyText, modifier = Modifier.size(29.dp)) }
                        IconButton(
                            onClick = {
                                scope.launch {
                                    try {
                                        if (playback?.playing == true) api("PUT", "me/player/pause") else api("PUT", "me/player/play")
                                        delay(300); refresh++
                                    } catch (e: Exception) { error = e.message }
                                }
                            },
                            modifier = Modifier.size(58.dp)
                        ) { Icon(if (playback?.playing == true) Icons.Outlined.Pause else Icons.Outlined.PlayArrow, "Oynat veya duraklat", tint = SpotifyGreen, modifier = Modifier.size(52.dp)) }
                        IconButton(onClick = {
                            scope.launch {
                                try { api("POST", "me/player/next"); delay(350); refresh++ } catch (e: Exception) { error = e.message }
                            }
                        }) { Icon(Icons.Outlined.SkipNext, "Sonraki parça", tint = SpotifyText, modifier = Modifier.size(29.dp)) }
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        TextButton(onClick = {
                            scope.launch {
                                try { shuffle = !shuffle; api("PUT", "me/player/shuffle?state=$shuffle"); refresh++ } catch (e: Exception) { error = e.message }
                            }
                        }) {
                            Icon(Icons.Outlined.Shuffle, null, tint = if (shuffle) SpotifyGreen else SpotifyMuted)
                            Spacer(Modifier.width(4.dp)); Text("Karıştır", color = if (shuffle) SpotifyGreen else SpotifyMuted)
                        }
                        TextButton(onClick = {
                            scope.launch {
                                try {
                                    repeat = when (repeat) { "off" -> "context"; "context" -> "track"; else -> "off" }
                                    api("PUT", "me/player/repeat?state=$repeat"); refresh++
                                } catch (e: Exception) { error = e.message }
                            }
                        }) {
                            Icon(Icons.Outlined.Repeat, null, tint = if (repeat != "off") SpotifyGreen else SpotifyMuted)
                            Spacer(Modifier.width(4.dp))
                            Text(when (repeat) { "track" -> "Tekrar: parça"; "context" -> "Tekrar: liste"; else -> "Tekrar kapalı" }, color = if (repeat != "off") SpotifyGreen else SpotifyMuted, fontSize = 11.sp)
                        }
                    }
                    Text(status,color=SpotifyMuted,fontSize=11.sp)
                    TextButton(onClick={OAuthFlowManager.disconnect(context,"spotify"); playback=null; refresh++}) { Text("Spotify bağlantısını kes",color=Color(0xFFFF7D88)) }
                }
            }
        }
        if (busy) LinearProgressIndicator(Modifier.fillMaxWidth(),color=SpotifyGreen)
        error?.let { Text(it,color=Color(0xFFFF7D88),fontSize=11.sp,lineHeight=16.sp) }
        Text("Not: Spotify oynatma API'si uygun bir aktif cihaz gerektirir; bazı kontrol özellikleri Premium hesabı isteyebilir. Kairo parolayı saklamaz. Client ID gizli değildir; erişim/yenileme tokenları Android Keystore ile şifrelenir.",color=SpotifyMuted,fontSize=10.sp,lineHeight=15.sp)
    }
}

@Composable
private fun SpotifyMark(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val unit = size.minDimension / 32f
        drawCircle(color = Color(0xFF07150B), radius = size.minDimension * .48f)
        val waves = listOf(
            floatArrayOf(6.0f, 11.5f, 13.5f, 8.8f, 21.7f, 10.0f, 26.0f, 13.0f),
            floatArrayOf(7.2f, 16.0f, 13.5f, 13.6f, 21.0f, 14.5f, 25.0f, 17.0f),
            floatArrayOf(8.5f, 20.4f, 14.5f, 18.4f, 20.5f, 19.0f, 23.5f, 21.1f)
        )
        waves.forEachIndexed { idx, a ->
            val path = Path().apply {
                moveTo(a[0] * unit, a[1] * unit)
                cubicTo(a[2] * unit, a[3] * unit, a[4] * unit, a[5] * unit, a[6] * unit, a[7] * unit)
            }
            drawPath(path, color = SpotifyGreen, style = Stroke(width = (if (idx == 0) 2.15f else 1.85f) * unit, cap = StrokeCap.Round))
        }
    }
}
