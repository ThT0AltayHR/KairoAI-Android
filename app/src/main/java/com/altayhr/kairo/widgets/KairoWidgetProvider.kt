package com.altayhr.kairo.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import com.altayhr.kairo.features.SpotifyCommandService
import java.util.concurrent.TimeUnit
import java.util.concurrent.ConcurrentHashMap
import com.altayhr.kairo.MainActivity
import com.altayhr.kairo.R
import com.altayhr.kairo.service.GeminiTaskService
import com.altayhr.kairo.service.IslandOverlayService

/** Home-screen companion widget with task status and direct Kairo routes. */
class KairoWidgetProvider : AppWidgetProvider() {
    companion object {
        private const val PREFS = "kairo_preferences"
        private const val ACTION_WIDGET_REFRESH = "com.altayhr.kairo.widget.REFRESH"
        private const val ACTION_SPOTIFY_PREV = "com.altayhr.kairo.widget.SPOTIFY_PREV"
        private const val ACTION_SPOTIFY_TOGGLE = "com.altayhr.kairo.widget.SPOTIFY_TOGGLE"
        private const val ACTION_SPOTIFY_NEXT = "com.altayhr.kairo.widget.SPOTIFY_NEXT"
        private const val ACTION_SPOTIFY_SHUFFLE = "com.altayhr.kairo.widget.SPOTIFY_SHUFFLE"
        private const val ACTION_SPOTIFY_REPEAT = "com.altayhr.kairo.widget.SPOTIFY_REPEAT"
        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        private val artworkCache = ConcurrentHashMap<String, Bitmap>()
        private val artworkLoading = ConcurrentHashMap<String, Boolean>()
        private val artworkClient = OkHttpClient.Builder().connectTimeout(6, TimeUnit.SECONDS).readTimeout(8, TimeUnit.SECONDS).callTimeout(10, TimeUnit.SECONDS).build()

        fun requestRefresh(context: Context) {
            context.sendBroadcast(Intent(ACTION_WIDGET_REFRESH).setPackage(context.packageName))
        }

        private fun routeIntent(context: Context, route: String, requestCode: Int): PendingIntent {
            val intent = Intent(context, MainActivity::class.java)
                .putExtra("kairo_route", route)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            return PendingIntent.getActivity(
                context, requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
    }

    override fun onUpdate(context: Context, manager: AppWidgetManager, appWidgetIds: IntArray) {
        appWidgetIds.forEach { id -> updateOne(context, manager, id) }
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action in setOf(ACTION_SPOTIFY_PREV, ACTION_SPOTIFY_TOGGLE, ACTION_SPOTIFY_NEXT, ACTION_SPOTIFY_SHUFFLE, ACTION_SPOTIFY_REPEAT)) {
            val pending = goAsync()
            scope.launch {
                try {
                    val prefs = context.getSharedPreferences("kairo_spotify", Context.MODE_PRIVATE)
                    val command = when (action) {
                        ACTION_SPOTIFY_PREV -> "Spotify'da önceki parça"
                        ACTION_SPOTIFY_NEXT -> "Spotify'da sonraki parça"
                        ACTION_SPOTIFY_SHUFFLE -> "Spotify'da karıştırmayı değiştir"
                        ACTION_SPOTIFY_REPEAT -> "Spotify'da tekrar etme modunu değiştir"
                        else -> if (prefs.getBoolean("is_playing", false)) "Spotify'da duraklat" else "Spotify'da oynatmayı sürdür"
                    }
                    val result = SpotifyCommandService.execute(context, command)
                    com.altayhr.kairo.service.IslandOverlayService.publish(context, "Spotify", result, if (result.contains("başarılı") || result.contains("sürdürüldü") || result.contains("geçildi")) "success" else "working")
                } finally {
                    val manager = AppWidgetManager.getInstance(context)
                    val ids = manager.getAppWidgetIds(ComponentName(context, KairoWidgetProvider::class.java))
                    onUpdate(context, manager, ids)
                    requestRefresh(context)
                    pending.finish()
                }
            }
            return
        }
        super.onReceive(context, intent)
        when (intent.action) {
            ACTION_WIDGET_REFRESH,
            IslandOverlayService.ACTION_UPDATE,
            IslandOverlayService.ACTION_REPOSITION,
            GeminiTaskService.ACTION_TASK_UPDATE,
            "com.altayhr.kairo.spotify.UPDATE",
            "com.altayhr.kairo.network.STATUS" -> {
                val manager = AppWidgetManager.getInstance(context)
                val ids = manager.getAppWidgetIds(ComponentName(context, KairoWidgetProvider::class.java))
                onUpdate(context, manager, ids)
            }
        }
    }

    private fun broadcastIntent(context: Context, action: String, requestCode: Int): PendingIntent = PendingIntent.getBroadcast(
        context, requestCode, Intent(context, KairoWidgetProvider::class.java).setAction(action),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    private fun updateOne(context: Context, manager: AppWidgetManager, widgetId: Int) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val voiceRunning = prefs.getBoolean("voice_running", false)
        val kind = prefs.getString("island_kind", "idle").orEmpty()
        val title = when {
            voiceRunning -> prefs.getString("voice_status", "Sesli sohbet açık")
            else -> prefs.getString("island_title", "Kairo hazır")
        }.orEmpty().take(54)
        val musicPrefs = context.getSharedPreferences("kairo_spotify", Context.MODE_PRIVATE)
        val musicTitle = musicPrefs.getString("last_title", null)
        val musicArtist = musicPrefs.getString("last_artist", null)
        val musicPlaying = musicPrefs.getBoolean("is_playing", false)
        val musicImage = musicPrefs.getString("last_image", null)
        val networkOnline = prefs.getBoolean("network_online", true)
        val detail = when {
            !networkOnline -> "Çevrimdışı · bağlantı bekleniyor"
            musicPlaying && !musicTitle.isNullOrBlank() -> "$musicTitle · ${musicArtist.orEmpty()}"
            voiceRunning -> prefs.getString("voice_detail", "Canlı ses oturumu etkin")
            else -> prefs.getString("island_detail", "Yeni bir görev başlat")
        }.orEmpty().take(100)
        val status = when {
            !networkOnline -> "Çevrimdışı · ağ bekleniyor"
            kind == "success" -> "Tamamlandı"
            kind == "error" -> "Kontrol gerekiyor"
            kind == "thinking" -> "Görev işleniyor"
            kind == "listening" -> "Sesli görüşme açık"
            else -> if (voiceRunning) "Sesli görüşme açık" else "Hazır"
        }
        val views = RemoteViews(context.packageName, R.layout.widget_kairo)
        views.setImageViewResource(R.id.widget_mochi, R.drawable.ic_mochi)
        views.setTextViewText(R.id.widget_title, "Kairo AI")
        views.setTextViewText(R.id.widget_status, "$status · $title")
        views.setTextViewText(R.id.widget_detail, detail)
        views.setImageViewResource(R.id.widget_album_cover, R.drawable.ic_spotify_mark)
        musicImage?.takeIf { it.startsWith("https://", true) }?.let { url ->
            artworkCache[url]?.let { views.setImageViewBitmap(R.id.widget_album_cover, it) }
                ?: loadArtwork(context, url)
        }
        views.setTextViewText(R.id.widget_spotify_toggle, if (musicPlaying) "Ⅱ Duraklat" else "▶ Oynat")
        views.setTextViewText(R.id.widget_spotify_shuffle, if (musicPrefs.getBoolean("shuffle", false)) "Karıştır ✓" else "Karıştır")
        views.setTextViewText(R.id.widget_spotify_repeat, when (musicPrefs.getString("repeat", "off")) { "track" -> "Tekrar 1"; "context" -> "Tekrar ✓"; else -> "Tekrar" })
        views.setOnClickPendingIntent(R.id.widget_spotify_prev, broadcastIntent(context, ACTION_SPOTIFY_PREV, 7201))
        views.setOnClickPendingIntent(R.id.widget_spotify_toggle, broadcastIntent(context, ACTION_SPOTIFY_TOGGLE, 7202))
        views.setOnClickPendingIntent(R.id.widget_spotify_next, broadcastIntent(context, ACTION_SPOTIFY_NEXT, 7203))
        views.setOnClickPendingIntent(R.id.widget_spotify_shuffle, broadcastIntent(context, ACTION_SPOTIFY_SHUFFLE, 7204))
        views.setOnClickPendingIntent(R.id.widget_spotify_repeat, broadcastIntent(context, ACTION_SPOTIFY_REPEAT, 7205))
        views.setOnClickPendingIntent(R.id.widget_header, routeIntent(context, "chat", 7101))
        views.setOnClickPendingIntent(R.id.widget_chat, routeIntent(context, "chat", 7102))
        views.setOnClickPendingIntent(R.id.widget_agent, routeIntent(context, "agent", 7103))
        views.setOnClickPendingIntent(R.id.widget_voice, routeIntent(context, "voice", 7104))
        views.setOnClickPendingIntent(R.id.widget_detail, routeIntent(context, "media", 7110))
        views.setOnClickPendingIntent(R.id.widget_tools, routeIntent(context, "tools", 7105))
        views.setOnClickPendingIntent(R.id.widget_settings, routeIntent(context, "settings", 7106))
        manager.updateAppWidget(widgetId, views)
    }

    private fun loadArtwork(context: Context, url: String) {
        // RemoteViews cannot load a URL itself; fetch art asynchronously and update the widget once decoded.
        if (artworkCache.containsKey(url) || artworkLoading.putIfAbsent(url, true) != null) return
        scope.launch {
            try {
                val bitmap = runCatching {
                    artworkClient.newCall(Request.Builder().url(url).get().build()).execute().use { response ->
                        if (!response.isSuccessful) null else response.body?.byteStream()?.use { BitmapFactory.decodeStream(it) }
                    }
                }.getOrNull()
                if (bitmap != null) {
                    artworkCache[url] = bitmap
                    if (artworkCache.size > 12) artworkCache.keys.firstOrNull { it != url }?.let { artworkCache.remove(it) }
                    withContext(Dispatchers.Main) {
                        val manager = AppWidgetManager.getInstance(context)
                        val ids = manager.getAppWidgetIds(ComponentName(context, KairoWidgetProvider::class.java))
                        ids.forEach { id ->
                            val views = RemoteViews(context.packageName, R.layout.widget_kairo)
                            views.setImageViewBitmap(R.id.widget_album_cover, bitmap)
                            manager.partiallyUpdateAppWidget(id, views)
                        }
                    }
                }
            } finally { artworkLoading.remove(url) }
        }
    }
}
