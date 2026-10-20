package com.altayhr.kairo.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.widget.RemoteViews
import com.altayhr.kairo.MainActivity
import com.altayhr.kairo.R
import com.altayhr.kairo.data.AgentRunRecord
import com.altayhr.kairo.data.AgentRunStore
import com.altayhr.kairo.oauth.OAuthFlowManager

/** A separately selectable Android launcher widget, backed by shared Kairo task/system state. */
data class KairoWidgetSpec(
    val key: String,
    val title: String,
    val category: String,
    val description: String,
    val route: String
)

object KairoWidgetCatalog {
    val all: List<KairoWidgetSpec> = listOf(
        KairoWidgetSpec("commands", "Komut akışı", "GELİŞTİRME", "Terminal komutları, onaylar ve gerçek çıkış kodları.", "terminal"),
        KairoWidgetSpec("progress", "Proje ilerlemesi", "PROJE", "Son AI görev kaydı ve proje aşamalarına hızlı erişim.", "projects"),
        KairoWidgetSpec("agents", "Ajan filosu", "AJANLAR", "Uzman ajan görev planlaması ve koordinasyon.", "agents"),
        KairoWidgetSpec("build", "Gradle derlemesi", "DERLEME", "Android derleme kontrollerini AI ile hazırla.", "widgets"),
        KairoWidgetSpec("npm", "NPM iş akışı", "GELİŞTİRME", "Node/npm projeleri için paket, test ve build akışı.", "widgets"),
        KairoWidgetSpec("release", "GitHub Release", "YAYIN", "APK imza, checksum ve GitHub Release kontrolü.", "widgets"),
        KairoWidgetSpec("expo", "Expo build", "YAYIN", "Expo/EAS derleme ve çıktı doğrulama adımları.", "widgets"),
        KairoWidgetSpec("download", "İndirme takibi", "DOSYALAR", "Dosya indirme ve bütünlük kontrolü görevleri.", "widgets"),
        KairoWidgetSpec("files", "Dosya denetimi", "DOSYALAR", "Proje dosyaları, kaynaklar ve referans tutarlılığı.", "projects"),
        KairoWidgetSpec("debug", "Hata ayıklama", "KALİTE", "Kök neden analizi ve güvenli hata düzeltme.", "widgets"),
        KairoWidgetSpec("tests", "Test laboratuvarı", "KALİTE", "Birim, entegrasyon ve regresyon test planları.", "widgets"),
        KairoWidgetSpec("security", "Güvenlik nöbetçisi", "GÜVENLİK", "Secret, OAuth, izin ve ağ güvenliği kontrolü.", "settings"),
        KairoWidgetSpec("review", "Kod incelemesi", "KALİTE", "Kod kalitesi, mimari ve risk incelemesi.", "widgets"),
        KairoWidgetSpec("integrations", "Entegrasyon durumu", "BAĞLANTILAR", "API ve hizmet entegrasyonlarının kontrolü.", "integrations"),
        KairoWidgetSpec("spotify", "Spotify canlı durumu", "MEDYA", "Gerçek Spotify oynatma bilgisi ve medya alanı.", "media"),
        KairoWidgetSpec("network", "Ağ ve çevrimdışı", "SİSTEM", "Android cihazın geçerli ağ bağlantısı durumu.", "settings"),
        KairoWidgetSpec("context", "Bağlam penceresi", "MODEL", "İstem kapsamı ve dosya bağlamı analizi.", "widgets"),
        KairoWidgetSpec("prompt", "İstem atölyesi", "MODEL", "İsteği adımlara ve kabul ölçütlerine dönüştür.", "widgets"),
        KairoWidgetSpec("approvals", "Onay kuyruğu", "GÜVENLİK", "Bekleyen görev/terminal onayları için hızlı erişim.", "terminal"),
        KairoWidgetSpec("delivery", "Teslim kartları", "TESLİM", "Çıktı, dosya teslimi ve doğrulama kontrolü.", "projects"),
        KairoWidgetSpec("activity", "Etkinlik günlüğü", "SİSTEM", "AI görev geçmişi ve son durum bilgisi.", "agents"),
        KairoWidgetSpec("filesync", "Proje bütünlüğü", "PROJE", "Dosya alanları ve proje bağlamını gözden geçir.", "projects"),
    )
    fun get(key: String): KairoWidgetSpec = all.firstOrNull { it.key == key } ?: all.first()
}

abstract class KairoAgentWidgetProvider : AppWidgetProvider() {
    protected abstract val widgetKey: String

    override fun onUpdate(context: Context, manager: AppWidgetManager, appWidgetIds: IntArray) {
        appWidgetIds.forEach { updateOne(context, manager, it) }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action in REFRESH_ACTIONS) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, javaClass))
            onUpdate(context, manager, ids)
        }
    }

    private fun updateOne(context: Context, manager: AppWidgetManager, appWidgetId: Int) {
        val spec = KairoWidgetCatalog.get(widgetKey)
        val prefs = context.getSharedPreferences("kairo_preferences", Context.MODE_PRIVATE)
        val recentRuns = AgentRunStore(context).recent(30)
        // Prompt titles are persisted in AgentRunStore; match this widget's own task history
        // instead of making every unrelated widget mirror the most recent global task.
        val widgetRuns = recentRuns.filter {
            it.chatTitle.startsWith("KAIRO WIDGET: ${spec.title}", ignoreCase = true)
        }
        val active = widgetRuns.firstOrNull { it.outcome == "running" }
        val latest = widgetRuns.firstOrNull()
        val views = RemoteViews(context.packageName, R.layout.widget_agent_tile)
        val (status, detail) = liveState(context, spec, active, latest, prefs.getBoolean("network_online", false))

        views.setTextViewText(R.id.agent_widget_brand, "KAIRO AI  /  ${spec.category}")
        views.setTextViewText(R.id.agent_widget_title, spec.title)
        views.setTextViewText(R.id.agent_widget_status, status)
        views.setTextViewText(R.id.agent_widget_detail, detail.take(150))
        views.setTextViewText(R.id.agent_widget_open, "AÇ")
        views.setTextViewText(R.id.agent_widget_ai, "AI İLE BAŞLAT  ↗")
        views.setOnClickPendingIntent(R.id.agent_widget_root, activityIntent(context, "widgets", null, widgetKey.hashCode()))
        views.setOnClickPendingIntent(R.id.agent_widget_open, activityIntent(context, spec.route, null, widgetKey.hashCode() + 1))
        views.setOnClickPendingIntent(R.id.agent_widget_ai, activityIntent(context, "widgets", spec.key, widgetKey.hashCode() + 2))
        manager.updateAppWidget(appWidgetId, views)
    }

    private fun activityIntent(context: Context, route: String, widgetId: String?, requestCode: Int): PendingIntent {
        val intent = Intent(context, MainActivity::class.java)
            .putExtra("kairo_route", route)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        widgetId?.let { intent.putExtra("kairo_widget_id", it) }
        return PendingIntent.getActivity(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun liveState(
        context: Context,
        spec: KairoWidgetSpec,
        active: AgentRunRecord?,
        latest: AgentRunRecord?,
        cachedNetworkOnline: Boolean
    ): Pair<String, String> {
        if (spec.key == "network") {
            val online = runCatching {
                val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val network = cm.activeNetwork
                network != null && cm.getNetworkCapabilities(network)?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) == true
            }.getOrDefault(cachedNetworkOnline)
            return (if (online) "● ÇEVRİMİÇİ" else "× ÇEVRİMDIŞI") to
                (if (online) "Android ağ bağlantısı doğrulandı." else "Ağ bağlantısı yok veya doğrulanamadı.")
        }
        if (spec.key == "spotify") {
            val p = context.getSharedPreferences("kairo_spotify", Context.MODE_PRIVATE)
            val title = p.getString("last_title", null).orEmpty()
            val artist = p.getString("last_artist", null).orEmpty()
            val playing = p.getBoolean("is_playing", false)
            val connected = OAuthFlowManager.isConnected(context, "spotify")
            val status = when {
                playing -> "● ÇALIYOR"
                connected -> "○ BAĞLI · DURAKLATILDI"
                else -> "○ BAĞLANTI BEKLENİYOR"
            }
            val track = listOf(title, artist).filter { it.isNotBlank() }.joinToString(" · ")
            return status to (track.ifBlank { "Parça bilgisi henüz mevcut değil. Aç düğmesi medya alanını açar." })
        }
        if (active != null) return "◌ AI GÖREVİ ÇALIŞIYOR" to "${active.chatTitle}: ${active.status}"
        return when (latest?.outcome) {
            "success" -> "✓ SON AI GÖREVİ TAMAMLANDI" to "${latest.chatTitle}: ${latest.status}"
            "error" -> "! SON AI GÖREVİNDE HATA" to "${latest.chatTitle}: ${latest.status}"
            "awaiting_approval" -> "◇ ONAY BEKLİYOR" to "${latest.chatTitle}: ${latest.status}"
            "running" -> "◌ AI GÖREVİ ÇALIŞIYOR" to "${latest.chatTitle}: ${latest.status}"
            else -> "HAZIR · AI GÖREVİ BEKLİYOR" to "${spec.description} Bu widget için henüz görev geçmişi yok."
        }
    }

    companion object {
        private val REFRESH_ACTIONS = setOf(
            "com.altayhr.kairo.widget.REFRESH",
            "com.altayhr.kairo.island.UPDATE",
            "com.altayhr.kairo.island.REPOSITION",
            "com.altayhr.kairo.task.UPDATE",
            "com.altayhr.kairo.spotify.UPDATE",
            "com.altayhr.kairo.network.STATUS"
        )
    }
}
