package com.altayhr.kairo.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.PixelFormat
import android.animation.ValueAnimator
import android.view.animation.LinearInterpolator
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.graphics.Point
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.SystemClock
import android.text.TextUtils
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.altayhr.kairo.MainActivity
import com.altayhr.kairo.R
import com.altayhr.kairo.data.SecureApiKeyStore

/** User-opt-in top capsule. It remains available outside the app while its foreground service runs. */
class IslandOverlayService : Service() {
    companion object {
        const val ACTION_START = "com.altayhr.kairo.island.START"
        const val ACTION_STOP = "com.altayhr.kairo.island.STOP"
        const val ACTION_UPDATE = "com.altayhr.kairo.island.UPDATE"
        const val ACTION_REPOSITION = "com.altayhr.kairo.island.REPOSITION"
        private const val CHANNEL = "kairo_island_status"
        private const val NOTIFICATION_ID = 4101
        private const val PREFS = "kairo_preferences"

        fun isEnabled(context: Context) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean("island_enabled", false)
        fun hasPermission(context: Context) = Build.VERSION.SDK_INT < 23 || Settings.canDrawOverlays(context)
        fun start(context: Context): Boolean {
            if (!hasPermission(context)) return false
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean("island_enabled", true).apply()
            val intent = Intent(context, IslandOverlayService::class.java).setAction(ACTION_START)
            if (Build.VERSION.SDK_INT >= 26) context.startForegroundService(intent) else context.startService(intent)
            return true
        }
        fun stop(context: Context) {
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean("island_enabled", false).apply()
            context.startService(Intent(context, IslandOverlayService::class.java).setAction(ACTION_STOP))
        }
        fun saveGeometry(context: Context, geometry: CapsuleGeometry) {
            CapsuleGeometry.write(context, geometry)
            context.sendBroadcast(Intent(ACTION_REPOSITION).setPackage(context.packageName))
        }
        fun publish(context: Context, title: String, detail: String, kind: String = "working") {
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                .putString("island_title", title.take(80)).putString("island_detail", detail.take(240))
                .putString("island_kind", kind.take(24)).putLong("island_updated_at", System.currentTimeMillis()).apply()
            context.sendBroadcast(Intent(ACTION_UPDATE).setPackage(context.packageName))
        }
    }

    private lateinit var windowManager: WindowManager
    private val handler = Handler(Looper.getMainLooper())
    private var root: LinearLayout? = null
    private var isAttached = false
    private var expandedByUser = false
    private var autoExpanded = false
    private var capsulePage = 1
    private var pageTouchDownX = 0f
    private var titleView: TextView? = null
    private var subtitleView: TextView? = null
    private var badgeView: ImageView? = null
    private var panel: LinearLayout? = null
    private var spotifyActionView: TextView? = null
    private var headerView: LinearLayout? = null
    private var layoutParams: WindowManager.LayoutParams? = null
    private var dragDownRawX = 0f
    private var dragDownRawY = 0f
    private var dragStartX = 0
    private var dragStartY = 0
    private var capsuleDragged = false
    private var holdPressed = false
    private var holdTriggered = false
    private var holdAnimator: ValueAnimator? = null
    private val holdDrawable = CapsuleHoldProgressDrawable()
    private val prefs by lazy { getSharedPreferences(PREFS, Context.MODE_PRIVATE) }
    private val updateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                ACTION_UPDATE -> refresh(animate = true)
                ACTION_REPOSITION -> applySavedGeometry()
            }
        }
    }
    private val collapse = Runnable { autoExpanded = false; if (!expandedByUser) setExpanded(false) }

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        createChannel()
        val filter = IntentFilter().apply { addAction(ACTION_UPDATE); addAction(ACTION_REPOSITION) }
        if (Build.VERSION.SDK_INT >= 33) registerReceiver(updateReceiver, filter, RECEIVER_NOT_EXPORTED)
        else @Suppress("DEPRECATION") registerReceiver(updateReceiver, filter)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> { prefs.edit().putBoolean("island_enabled", false).apply(); removeOverlay(); stopForeground(STOP_FOREGROUND_REMOVE); stopSelf(); return START_NOT_STICKY }
            ACTION_UPDATE -> { refresh(true); return START_STICKY }
            else -> {
                if (!isEnabled(this) || !hasPermission(this)) { stopSelf(); return START_NOT_STICKY }
                startOngoingNotification()
                attachOverlay()
                refresh(false)
            }
        }
        return START_STICKY
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= 26) getSystemService(NotificationManager::class.java).createNotificationChannel(
            NotificationChannel(CHANNEL, "Etkinlik kapsülü", NotificationManager.IMPORTANCE_LOW).apply {
                description = "Kairo arka plan etkinlik göstergesi"; setShowBadge(false)
            }
        )
    }

    private fun startOngoingNotification() {
        val open = PendingIntent.getActivity(this, 1, Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val stop = PendingIntent.getService(this, 2, Intent(this, IslandOverlayService::class.java).setAction(ACTION_STOP), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val notification = NotificationCompat.Builder(this, CHANNEL).setSmallIcon(R.drawable.ic_status)
            .setContentTitle("Kairo etkinlik kapsülü açık")
            .setContentText("Görev ve sesli sohbet durumu üst kapsülde gösteriliyor.")
            .setContentIntent(open).setOngoing(true).setOnlyAlertOnce(true)
            .addAction(0, "Uygulamayı aç", open).addAction(0, "Kapsülü kapat", stop).build()
        if (Build.VERSION.SDK_INT >= 34) ServiceCompat.startForeground(this, NOTIFICATION_ID, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        else startForeground(NOTIFICATION_ID, notification)
    }

    private fun attachOverlay() {
        if (isAttached || !hasPermission(this)) return
        val d = resources.displayMetrics.density
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = rounded(0xFF08090D.toInt(), 22f, 0xFF292D39.toInt())
            setPadding((11 * d).toInt(), (7 * d).toInt(), (11 * d).toInt(), (7 * d).toInt())
            elevation = 20f
        }
        val header = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        headerView = header
        badgeView = ImageView(this).apply {
            setImageResource(R.drawable.ic_mochi)
            scaleType = ImageView.ScaleType.FIT_CENTER
            background = rounded(0xFF191725.toInt(), 11f, 0xFF60568D.toInt())
            val s = (32 * d).toInt(); layoutParams = LinearLayout.LayoutParams(s, s)
            setPadding((2 * d).toInt(), (2 * d).toInt(), (2 * d).toInt(), (2 * d).toInt())
        }
        val labels = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding((9 * d).toInt(), 0, (12 * d).toInt(), 0) }
        titleView = TextView(this).apply { text = "Kairo hazır"; textSize = 12f; setTextColor(0xFFF3F4F8.toInt()); typeface = Typeface.create("sans-serif-medium", Typeface.BOLD); maxLines = 1; ellipsize = TextUtils.TruncateAt.END }
        subtitleView = TextView(this).apply { text = "Etkinlikler izleniyor"; textSize = 10f; setTextColor(0xFFA1A6B5.toInt()); maxLines = 1; ellipsize = TextUtils.TruncateAt.END }
        labels.addView(titleView); labels.addView(subtitleView)
        labels.setOnTouchListener { _, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> { pageTouchDownX = event.rawX; true }
                MotionEvent.ACTION_UP -> {
                    val dx = event.rawX - pageTouchDownX
                    if (kotlin.math.abs(dx) > (48 * d)) {
                        capsulePage = (capsulePage + (if (dx < 0) 1 else -1) + 4) % 4
                        renderCapsulePage()
                    } else openApp(when(capsulePage) { 0 -> "chat"; 1 -> "agent"; 2 -> "media"; else -> "projects" })
                    true
                }
                MotionEvent.ACTION_CANCEL -> true
                else -> true
            }
        }
        header.addView(badgeView); header.addView(labels, LinearLayout.LayoutParams(0, -2, 1f))
        badgeView?.setOnTouchListener { _, event -> handleCapsuleDrag(event) }
        val more = TextView(this).apply { text = "···"; textSize = 18f; setTextColor(0xFFE5E7EB.toInt()); gravity = Gravity.CENTER; setPadding((9 * d).toInt(), 0, (1 * d).toInt(), 0); setOnClickListener { expandedByUser = !expandedByUser; setExpanded(expandedByUser) } }
        header.addView(View(this), LinearLayout.LayoutParams(0, 1, 1f)); header.addView(more)
        root!!.addView(header)
        root!!.foreground = holdDrawable
        panel = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; visibility = View.GONE; setPadding(0, (10 * d).toInt(), 0, 0) }
        panel!!.addView(View(this).apply { setBackgroundColor(0xFF2B2F3A.toInt()); layoutParams = LinearLayout.LayoutParams(-1, maxOf(1, (1 * d).toInt())) })
        panel!!.addView(TextView(this).apply { tag = "detail"; textSize = 11f; setTextColor(0xFFC1C5D0.toInt()); setPadding(0, (9 * d).toInt(), 0, (8 * d).toInt()); maxLines = 3 })
        val shortcutsOne = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        shortcutsOne.addView(actionText("Piyasa", false) { openApp("crypto") }, LinearLayout.LayoutParams(0, (30 * d).toInt(), 1f).apply { rightMargin = (4 * d).toInt() })
        shortcutsOne.addView(actionText("Portföy", false) { openApp("portfolio") }, LinearLayout.LayoutParams(0, (30 * d).toInt(), 1f).apply { rightMargin = (4 * d).toInt() })
        shortcutsOne.addView(actionText("E-posta", false) { openApp("email") }, LinearLayout.LayoutParams(0, (30 * d).toInt(), 1f))
        panel!!.addView(shortcutsOne)
        val shortcutsTwo = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL; setPadding(0, (4 * d).toInt(), 0, (5 * d).toInt()) }
        shortcutsTwo.addView(actionText("Sesli", false) { openApp("voice") }, LinearLayout.LayoutParams(0, (30 * d).toInt(), 1f).apply { rightMargin = (4 * d).toInt() })
        shortcutsTwo.addView(actionText("Notlar", false) { openApp("notes") }, LinearLayout.LayoutParams(0, (30 * d).toInt(), 1f).apply { rightMargin = (4 * d).toInt() })
        shortcutsTwo.addView(actionText("Servisler", false) { openApp("integrations") }, LinearLayout.LayoutParams(0, (30 * d).toInt(), 1f))
        panel!!.addView(shortcutsTwo)
        val shortcutsThree = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL; setPadding(0, (2 * d).toInt(), 0, (3 * d).toInt()) }
        shortcutsThree.addView(actionText("Görevler", false) { openApp("agent") }, LinearLayout.LayoutParams(0, (30 * d).toInt(), 1f).apply { rightMargin = (4 * d).toInt() })
        shortcutsThree.addView(actionText("Araçlar", false) { openApp("tools") }, LinearLayout.LayoutParams(0, (30 * d).toInt(), 1f).apply { rightMargin = (4 * d).toInt() })
        shortcutsThree.addView(actionText("Medya", false) { openApp("media") }, LinearLayout.LayoutParams(0, (30 * d).toInt(), 1f))
        panel!!.addView(shortcutsThree)
        val shortcutsFour = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL; setPadding(0, (2 * d).toInt(), 0, (5 * d).toInt()) }
        shortcutsFour.addView(actionText("Cihaz", false) { openApp("device") }, LinearLayout.LayoutParams(0, (30 * d).toInt(), 1f).apply { rightMargin = (4 * d).toInt() })
        shortcutsFour.addView(actionText("WhatsApp", false) { openApp("whatsapp") }, LinearLayout.LayoutParams(0, (30 * d).toInt(), 1f).apply { rightMargin = (4 * d).toInt() })
        shortcutsFour.addView(actionText("Ayarlar", false) { openApp("settings") }, LinearLayout.LayoutParams(0, (30 * d).toInt(), 1f))
        panel!!.addView(shortcutsFour)
        val shortcutsFive = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL; setPadding(0, (2 * d).toInt(), 0, (5 * d).toInt()) }
        shortcutsFive.addView(actionText("Spotify", false) { capsulePage = 2; renderCapsulePage(); openApp("media") }, LinearLayout.LayoutParams(0, (30 * d).toInt(), 1f).apply { rightMargin = (4 * d).toInt() })
        shortcutsFive.addView(actionText("AI sohbet", true) { openApp("chat") }, LinearLayout.LayoutParams(0, (30 * d).toInt(), 1f).apply { rightMargin = (4 * d).toInt() })
        shortcutsFive.addView(actionText("Güvenli kasa", false) { openApp("vault") }, LinearLayout.LayoutParams(0, (30 * d).toInt(), 1f))
        panel!!.addView(shortcutsFive)
        val actions = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        spotifyActionView = actionText("Spotify hesabını bağla", true) { openApp("media") }.apply { visibility = View.GONE }
        panel!!.addView(spotifyActionView, LinearLayout.LayoutParams(-1, (34 * d).toInt()).apply { bottomMargin = (5 * d).toInt() })
        actions.addView(actionText("Uygulamayı aç", true) { openApp("chat") }, LinearLayout.LayoutParams(0, (34 * d).toInt(), 1f).apply { rightMargin = (4 * d).toInt() })
        actions.addView(actionText("Gizle", false) { stop(this@IslandOverlayService) }, LinearLayout.LayoutParams(0, (34 * d).toInt(), 1f))
        panel!!.addView(actions); root!!.addView(panel)
        root!!.setOnClickListener { expandedByUser = !expandedByUser; setExpanded(expandedByUser) }

        val lp = WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT).apply { gravity = Gravity.TOP or Gravity.LEFT }
        layoutParams = lp
        applyGeometryToViews()
        runCatching { windowManager.addView(root, lp); isAttached = true }.onFailure { removeOverlay() }
    }

    private fun applySavedGeometry() {
        if (!isAttached) { attachOverlay(); return }
        applyGeometryToViews()
        val view = root ?: return
        val lp = layoutParams ?: return
        runCatching { windowManager.updateViewLayout(view, lp) }
    }

    private fun applyGeometryToViews() {
        val d = resources.displayMetrics.density
        val geometry = CapsuleGeometry.read(this)
        val metrics = resources.displayMetrics
        val maxWidthPx = (metrics.widthPixels - (24 * d).toInt()).coerceAtLeast((180 * d).toInt())
        val targetWidth = minOf((geometry.widthDp * d).toInt(), maxWidthPx)
        root?.minimumWidth = targetWidth
        root?.minimumHeight = (geometry.heightDp * d).toInt()
        root?.alpha = (prefs.getInt("island_opacity_percent", 100).coerceIn(55, 100) / 100f)
        root?.background = rounded(0xFF08090D.toInt(), capsuleCornerRadius(), 0xFF292D39.toInt())
        val badgeSize = minOf(42, geometry.heightDp - 14).coerceAtLeast(26)
        badgeView?.layoutParams = LinearLayout.LayoutParams((badgeSize * d).toInt(), (badgeSize * d).toInt())
        headerView?.layoutParams = LinearLayout.LayoutParams(-1, (geometry.heightDp * d).toInt() - (14 * d).toInt())
        layoutParams?.let { lp ->
            lp.width = targetWidth
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT
            lp.gravity = Gravity.TOP or Gravity.LEFT
            val maxX = (metrics.widthPixels - targetWidth).coerceAtLeast(0)
            val statusId = resources.getIdentifier("status_bar_height", "dimen", "android")
            val statusHeight = if (statusId > 0) resources.getDimensionPixelSize(statusId) else (24 * d).toInt()
            val requestedHeight = (geometry.heightDp * d).toInt()
            val measuredHeight = (root?.height ?: 0).takeIf { it > requestedHeight } ?: requestedHeight
            val maxY = (metrics.heightPixels - statusHeight - measuredHeight).coerceAtLeast(0)
            lp.x = (maxX * geometry.xPercent / 100f).toInt().coerceIn(0, maxX)
            lp.y = statusHeight + (maxY * geometry.yPercent / 100f).toInt().coerceIn(0, maxY)
        }
        root?.requestLayout()
    }

    private fun handleCapsuleDrag(event: MotionEvent): Boolean {
        val lp = layoutParams ?: return false
        val density = resources.displayMetrics.density
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                dragDownRawX = event.rawX; dragDownRawY = event.rawY
                dragStartX = lp.x; dragStartY = lp.y
                capsuleDragged = false
                holdPressed = true; holdTriggered = false
                stopHoldProgress(reset = true)
                holdAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
                    duration = 3000L
                    interpolator = LinearInterpolator()
                    addUpdateListener { holdDrawable.progress = it.animatedValue as Float; root?.invalidate() }
                    addListener(object : android.animation.AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: android.animation.Animator) {
                            if (holdPressed && !capsuleDragged) {
                                holdTriggered = true
                                holdDrawable.progress = 0f
                                startVoiceFromCapsule()
                            }
                        }
                    })
                    start()
                }
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = event.rawX - dragDownRawX
                val dy = event.rawY - dragDownRawY
                val slop = ViewConfiguration.get(this).scaledTouchSlop
                if (!capsuleDragged && kotlin.math.hypot(dx.toDouble(), dy.toDouble()) > slop) {
                    capsuleDragged = true
                    stopHoldProgress(reset = true)
                }
                if (capsuleDragged) {
                    val metrics = resources.displayMetrics
                    val statusId = resources.getIdentifier("status_bar_height", "dimen", "android")
                    val statusHeight = if (statusId > 0) resources.getDimensionPixelSize(statusId) else (24 * density).toInt()
                    val maxX = (metrics.widthPixels - (root?.width ?: lp.width)).coerceAtLeast(0)
                    val maxY = (metrics.heightPixels - statusHeight - (root?.height ?: lp.height)).coerceAtLeast(0)
                    lp.x = (dragStartX + dx.toInt()).coerceIn(0, maxX)
                    lp.y = (dragStartY + dy.toInt()).coerceIn(statusHeight, statusHeight + maxY)
                    runCatching { windowManager.updateViewLayout(root, lp) }
                }
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                val wasTap = !capsuleDragged && !holdTriggered && event.actionMasked == MotionEvent.ACTION_UP
                holdPressed = false
                if (!holdTriggered) stopHoldProgress(reset = true)
                if (capsuleDragged) {
                    val metrics = resources.displayMetrics
                    val statusId = resources.getIdentifier("status_bar_height", "dimen", "android")
                    val statusHeight = if (statusId > 0) resources.getDimensionPixelSize(statusId) else (24 * density).toInt()
                    val maxX = (metrics.widthPixels - (root?.width ?: lp.width)).coerceAtLeast(1)
                    val maxY = (metrics.heightPixels - statusHeight - (root?.height ?: lp.height)).coerceAtLeast(1)
                    val current = CapsuleGeometry.read(this).copy(
                        xPercent = (lp.x.toFloat() / maxX * 100f).coerceIn(0f, 100f),
                        yPercent = ((lp.y - statusHeight).toFloat() / maxY * 100f).coerceIn(0f, 100f)
                    )
                    CapsuleGeometry.write(this, current)
                    sendBroadcast(Intent(ACTION_REPOSITION).setPackage(packageName))
                } else if (wasTap) {
                    expandedByUser = !expandedByUser
                    setExpanded(expandedByUser)
                }
                return true
            }
        }
        return false
    }

    private fun stopHoldProgress(reset: Boolean) {
        holdAnimator?.removeAllUpdateListeners()
        holdAnimator?.removeAllListeners()
        holdAnimator?.cancel()
        holdAnimator = null
        if (reset) { holdDrawable.progress = 0f; root?.invalidate() }
    }

    private fun startVoiceFromCapsule() {
        val granted = Build.VERSION.SDK_INT < 23 || checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        val hasKey = !SecureApiKeyStore(this).getApiKey().isNullOrBlank()
        if (!granted || !hasKey) {
            publish(this, if (!granted) "Sesli sohbet için izin gerekli" else "Gemini API anahtarı gerekli", "Sesli sohbet kurulumunu tamamlamak için dokun.", "listening")
            openApp("voice")
            return
        }
        runCatching {
            com.altayhr.kairo.service.GeminiLiveVoiceService.start(this)
            publish(this, "Gemini Live başlatılıyor", "Üç saniyelik basılı tutma ile sesli sohbet açıldı.", "listening")
            if (!expandedByUser) { autoExpanded = true; setExpanded(true); handler.removeCallbacks(collapse); handler.postDelayed(collapse, 5000L) }
        }.onFailure {
            publish(this, "Sesli sohbet başlatılamadı", it.message ?: "İzinleri ve API anahtarını kontrol et.", "error")
            openApp("voice")
        }
    }

    private fun actionText(label: String, primary: Boolean, action: () -> Unit) = TextView(this).apply {
        val d = resources.displayMetrics.density
        text = label; textSize = 11f; gravity = Gravity.CENTER
        setTextColor(if (primary) 0xFF11131A.toInt() else 0xFFD4D7E0.toInt())
        background = rounded(if (primary) 0xFFB8AEFF.toInt() else 0xFF20232B.toInt(), 10f, if (primary) 0xFFB8AEFF.toInt() else 0xFF343846.toInt())
        setPadding((7 * d).toInt(), 0, (7 * d).toInt(), 0); setOnClickListener { action() }
    }

    private fun refresh(animate: Boolean) {
        if (!isAttached) attachOverlay()
        val title = prefs.getString("island_title", "Kairo hazır") ?: "Kairo hazır"
        val detail = prefs.getString("island_detail", "Etkinlikler izleniyor") ?: "Etkinlikler izleniyor"
        val kind = prefs.getString("island_kind", "idle") ?: "idle"
        titleView?.text = title
        subtitleView?.text = when (kind) { "success" -> "Tamamlandı"; "error" -> "İşlem tamamlanamadı"; "listening" -> "Sesli sohbet etkin"; "thinking" -> "Yanıt hazırlanıyor"; else -> detail.take(48) }
        badgeView?.setImageResource(when (kind) {
            "success" -> R.drawable.ic_mochi_success
            "error" -> R.drawable.ic_mochi_error
            "listening" -> R.drawable.ic_mochi_listening
            "thinking", "working" -> R.drawable.ic_mochi_thinking
            else -> R.drawable.ic_mochi
        })
        badgeView?.background = rounded(when (kind) { "success" -> 0xFF17332D.toInt(); "error" -> 0xFF351B25.toInt(); "listening" -> 0xFF172D42.toInt(); "thinking" -> 0xFF211D3F.toInt(); else -> 0xFF191725.toInt() }, 11f, when (kind) { "success" -> 0xFF5AC9A2.toInt(); "error" -> 0xFFFF7D88.toInt(); "listening" -> 0xFF63B8FF.toInt(); else -> 0xFF9585FF.toInt() })
        panel?.getChildAt(1)?.let { (it as? TextView)?.text = detail }
        if (animate && !expandedByUser) {
            autoExpanded = true; setExpanded(true); handler.removeCallbacks(collapse)
            val collapseEnabled = prefs.getBoolean("island_auto_collapse", true)
            if (kind != "listening" && collapseEnabled) {
                val delayMs = (prefs.getInt("island_auto_collapse_seconds", 4).coerceIn(1, 15) * 1000L)
                handler.postAtTime(collapse, this, SystemClock.uptimeMillis() + delayMs)
            }
        }
        renderCapsulePage()
    }

    private fun renderCapsulePage() {
        if (!isAttached) return
        val prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val title: String
        val subtitle: String
        val detail: String
        when (capsulePage) {
            0 -> { title = "Kairo AI"; subtitle = "AI sohbeti · dokunarak aç"; detail = "Sohbet, sesli komutlar ve yapay zekâ" }
            1 -> { title = prefs.getString("island_title", "Görev merkezi") ?: "Görev merkezi"; subtitle = prefs.getString("island_kind", "working")?.let { when(it) { "success" -> "Tamamlandı"; "error" -> "Hata · işlem durdu"; "thinking" -> "Düşünüyor"; "listening" -> "Sesli sohbet"; else -> "Görev durumu" } } ?: "Görev durumu"; detail = prefs.getString("island_detail", "Aktif görev yok") ?: "Aktif görev yok" }
            2 -> {
                val music = getSharedPreferences("kairo_spotify", Context.MODE_PRIVATE)
                if (!com.altayhr.kairo.oauth.OAuthFlowManager.isConnected(this, "spotify")) {
                    title = "Spotify hesabınızı bağlayın"
                    subtitle = "Spotify · güvenli hesap bağlantısı"
                    detail = "Spotify'ı bağlamak ve müzik kontrolünü açmak için aşağıdaki düğmeye dokunun."
                } else {
                    title = music.getString("last_title", "Spotify") ?: "Spotify"
                    subtitle = music.getString("last_artist", "Spotify hesabı") ?: "Spotify hesabı"
                    detail = if (music.getBoolean("is_playing", false)) "Spotify · çalıyor · dokunarak kontrol et" else "Spotify · duraklatıldı veya aktif parça yok"
                }
            }
            else -> { title = prefs.getString("active_project_name", "Projeler") ?: "Projeler"; subtitle = "Çalışma alanı"; detail = "Projeler, dosyalar ve görev bağlamı" }
        }
        titleView?.text = title
        subtitleView?.text = subtitle
        panel?.getChildAt(1)?.let { (it as? TextView)?.text = detail }
        spotifyActionView?.visibility = if (capsulePage == 2) View.VISIBLE else View.GONE
        spotifyActionView?.text = if (com.altayhr.kairo.oauth.OAuthFlowManager.isConnected(this, "spotify")) "Spotify kontrollerini aç" else "Spotify hesabını bağla"
        if (capsulePage == 2) {
            badgeView?.setImageResource(R.drawable.ic_spotify_capsule)
            badgeView?.background = rounded(0xFF163522.toInt(), 11f, 0xFF1ED760.toInt())
            badgeView?.setPadding((1 * resources.displayMetrics.density).toInt(), (1 * resources.displayMetrics.density).toInt(), (1 * resources.displayMetrics.density).toInt(), (1 * resources.displayMetrics.density).toInt())
        } else {
            val kind = prefs.getString("island_kind", "idle") ?: "idle"
            badgeView?.setImageResource(when (kind) {
                "success" -> R.drawable.ic_mochi_success
                "error" -> R.drawable.ic_mochi_error
                "listening" -> R.drawable.ic_mochi_listening
                "thinking", "working" -> R.drawable.ic_mochi_thinking
                else -> R.drawable.ic_mochi
            })
            badgeView?.background = rounded(when (kind) { "success" -> 0xFF17332D.toInt(); "error" -> 0xFF351B25.toInt(); "listening" -> 0xFF172D42.toInt(); "thinking" -> 0xFF211D3F.toInt(); else -> 0xFF191725.toInt() }, 11f, when (kind) { "success" -> 0xFF5AC9A2.toInt(); "error" -> 0xFFFF7D88.toInt(); "listening" -> 0xFF63B8FF.toInt(); else -> 0xFF9585FF.toInt() })
            val d = resources.displayMetrics.density.toInt().coerceAtLeast(1)
            badgeView?.setPadding(2 * d, 2 * d, 2 * d, 2 * d)
        }
    }

    private fun capsuleCornerRadius(): Float = prefs.getInt("island_corner_radius_dp", 22).coerceIn(12, 36).toFloat()

    private fun setExpanded(show: Boolean) {
        panel?.visibility = if (show) View.VISIBLE else View.GONE
        root?.background = rounded(0xFF08090D.toInt(), capsuleCornerRadius() + if (show) 2f else 0f, if (show) 0xFF5B5296.toInt() else 0xFF292D39.toInt())
        root?.requestLayout()
        // Wait for the expanded/collapsed view to be measured before clamping its screen position.
        root?.post { if (isAttached) applySavedGeometry() }
    }

    private fun openApp(route: String = "chat") {
        val launch = packageManager.getLaunchIntentForPackage(packageName) ?: Intent(this, MainActivity::class.java)
        launch.putExtra("kairo_route", route)
        launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivity(launch)
    }

    private fun rounded(color: Int, radiusDp: Float, stroke: Int = color) = GradientDrawable().apply {
        setColor(color); cornerRadius = radiusDp * resources.displayMetrics.density; setStroke(maxOf(1, resources.displayMetrics.density.toInt()), stroke)
    }

    private fun removeOverlay() {
        handler.removeCallbacks(collapse)
        val v = root
        if (isAttached && v != null) runCatching { windowManager.removeView(v) }
        root = null; panel = null; spotifyActionView = null; headerView = null; titleView = null; subtitleView = null; badgeView = null; layoutParams = null; isAttached = false
    }

    override fun onDestroy() { stopHoldProgress(reset = true); removeOverlay(); runCatching { unregisterReceiver(updateReceiver) }; super.onDestroy() }
    override fun onBind(intent: Intent?): IBinder? = null
}
