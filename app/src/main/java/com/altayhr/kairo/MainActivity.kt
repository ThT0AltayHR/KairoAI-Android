package com.altayhr.kairo

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.PowerManager
import android.provider.OpenableColumns
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.UploadFile
import androidx.compose.material.icons.outlined.Workspaces
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.content.ContextCompat
import com.altayhr.kairo.data.ChatMessage
import com.altayhr.kairo.data.FlowToolkitStore
import com.altayhr.kairo.data.ChatSession
import com.altayhr.kairo.data.SecureApiKeyStore
import com.altayhr.kairo.data.WebSource
import com.altayhr.kairo.data.WorkspaceFile
import com.altayhr.kairo.data.WorkspaceProject
import com.altayhr.kairo.data.WorkspaceStore
import com.altayhr.kairo.automation.AutomationCenterScreen
import com.altayhr.kairo.oauth.OAuthFlowManager
import com.altayhr.kairo.ui.KairoSplashScreen
import com.altayhr.kairo.network.GeminiClient
import com.altayhr.kairo.terminal.EmbeddedTerminalScreen
import com.altayhr.kairo.features.CryptoScreen
import com.altayhr.kairo.features.EmailComposerScreen
import com.altayhr.kairo.features.DeviceStatusScreen
import com.altayhr.kairo.features.MediaHubScreen
import com.altayhr.kairo.features.PortfolioScreen
import com.altayhr.kairo.features.WhatsAppScreen
import com.altayhr.kairo.features.IntegrationHubScreen
import com.altayhr.kairo.features.NotesScreen
import com.altayhr.kairo.features.PermissionSetupScreen
import com.altayhr.kairo.features.CapsuleSettingsSection
import com.altayhr.kairo.features.AgentStudioScreen
import com.altayhr.kairo.features.AgentWidgetsScreen
import com.altayhr.kairo.features.FlowToolkitScreen
import com.altayhr.kairo.features.SoundEffects
import com.altayhr.kairo.features.VoiceScreen
import com.altayhr.kairo.features.SpotifyPlayerScreen
import com.altayhr.kairo.features.SpotifyCommandService
import com.altayhr.kairo.features.SecureVaultScreen
import com.altayhr.kairo.features.AgentPoolScreen
import com.altayhr.kairo.service.GeminiLiveVoiceService
import com.altayhr.kairo.automation.AutomationEvents
import org.json.JSONObject
import com.altayhr.kairo.service.GeminiTaskService
import com.altayhr.kairo.service.IslandOverlayService
import com.altayhr.kairo.service.CapsuleGeometry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale
import java.util.UUID

private val Ink = Color(0xFF090A10)
private val Panel = Color(0xFF12141D)
private val CardColor = Color(0xFF191C27)
private val FieldColor = Color(0xFF232638)
private val Stroke = Color(0xFF2B3040)
private val PrimaryText = Color(0xFFF4F5F7)
private val SecondaryText = Color(0xFF9DA5B5)
private val MutedText = Color(0xFF72798A)
private val Accent = Color(0xFFAA9BFF)
private val Green = Color(0xFF66D6C0)
private val Danger = Color(0xFFFF7D88)

class MainActivity : ComponentActivity() {
    companion object { const val ACTION_CAPSULE_NAVIGATE = "com.altayhr.kairo.capsule.NAVIGATE" }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        processOAuthRedirect(intent)
        intent.getStringExtra("kairo_route")?.let { route ->
            val nav = Intent(ACTION_CAPSULE_NAVIGATE).setPackage(packageName).putExtra("destination", route)
            intent.getStringExtra("chat_id")?.let { nav.putExtra("chat_id", it) }
            intent.getStringExtra("kairo_widget_id")?.let { nav.putExtra("kairo_widget_id", it) }
            sendBroadcast(nav)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        processOAuthRedirect(intent)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.statusBarColor = android.graphics.Color.rgb(9, 10, 12)
        window.navigationBarColor = android.graphics.Color.rgb(9, 10, 12)
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }
        setContent {
            var showBrandSplash by remember { mutableStateOf(true) }
            LaunchedEffect(Unit) { delay(2150); showBrandSplash = false }
            MaterialTheme(colorScheme = darkColorScheme(
                primary = Accent, onPrimary = Color(0xFF211207), background = Ink, surface = Panel,
                onSurface = PrimaryText, secondary = SecondaryText, outline = Stroke, error = Danger
            )) {
                Box(Modifier.fillMaxSize()) {
                    KairoApp(WorkspaceStore(this@MainActivity), SecureApiKeyStore(this@MainActivity))
                    AnimatedVisibility(visible = showBrandSplash, modifier = Modifier.fillMaxSize(), exit = fadeOut(tween(420))) {
                        KairoSplashScreen()
                    }
                }
            }
        }
    }

    private fun processOAuthRedirect(source: Intent?) {
        val uri = source?.data ?: return
        if (uri.scheme != "com.altayhr.kairo" || uri.path != "/oauth2redirect") return
        lifecycleScope.launch { OAuthFlowManager.handleRedirect(this@MainActivity, uri) }
    }
}

private enum class Destination { ONBOARDING, AGENT, AGENT_POOL, AGENT_WIDGETS, VAULT, TOOLS, CHAT, PROJECTS, PROJECT_FILES, SETTINGS, TERMINAL, VOICE, CRYPTO, PORTFOLIO, EMAIL, NOTES, INTEGRATIONS, AUTOMATION, DEVICE, MEDIA, WHATSAPP }
private enum class ChatPane { CHAT, WORKSPACE }
private data class SecretReview(val original: String, val redacted: String, val suggestedName: String, val value: String)

private fun destinationForRoute(route: String?): Destination = when (route?.lowercase(Locale.ROOT)) {
    "onboarding" -> Destination.ONBOARDING
    "agent" -> Destination.AGENT
    "agents" -> Destination.AGENT_POOL
    "widgets" -> Destination.AGENT_WIDGETS
    "vault" -> Destination.VAULT
    "tools" -> Destination.TOOLS
    "chat" -> Destination.CHAT
    "projects" -> Destination.PROJECTS
    "settings" -> Destination.SETTINGS
    "voice" -> Destination.VOICE
    "crypto" -> Destination.CRYPTO
    "portfolio" -> Destination.PORTFOLIO
    "email" -> Destination.EMAIL
    "notes" -> Destination.NOTES
    "integrations" -> Destination.INTEGRATIONS
    "automation" -> Destination.AUTOMATION
    "device" -> Destination.DEVICE
    "media" -> Destination.MEDIA
    "whatsapp" -> Destination.WHATSAPP
    else -> Destination.CHAT
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun KairoApp(store: WorkspaceStore, secrets: SecureApiKeyStore) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val flowToolkitStore = remember(context) { FlowToolkitStore(context) }

    var chats by remember { mutableStateOf(store.getChats().sortedWith(compareByDescending<ChatSession> { it.pinned }.thenByDescending { it.updatedAt })) }
    var projects by remember { mutableStateOf(store.getProjects().sortedByDescending { it.createdAt }) }
    val initialChatId = remember { (context as? MainActivity)?.intent?.getStringExtra("chat_id") }
    var selectedChatId by remember { mutableStateOf(initialChatId?.takeIf { id -> chats.any { it.id == id } } ?: chats.firstOrNull()?.id.orEmpty()) }
    val initialCapsuleRoute = remember { (context as? MainActivity)?.intent?.getStringExtra("kairo_route") }
    var setupComplete by remember { mutableStateOf(context.getSharedPreferences("kairo_preferences", Context.MODE_PRIVATE).getBoolean("permission_setup_complete", false)) }
    var destination by remember { mutableStateOf(if (setupComplete) destinationForRoute(initialCapsuleRoute) else Destination.ONBOARDING) }
    var pendingWidgetId by remember { mutableStateOf((context as? MainActivity)?.intent?.getStringExtra("kairo_widget_id")) }
    var chatPane by remember { mutableStateOf(ChatPane.CHAT) }
    var input by remember { mutableStateOf("") }
    var pendingFiles by remember { mutableStateOf<List<WorkspaceFile>>(emptyList()) }
    var loadingChatId by remember { mutableStateOf<String?>(null) }
    var statusNotice by remember { mutableStateOf<String?>(null) }
    var secretReview by remember { mutableStateOf<SecretReview?>(null) }
    var secretReviewName by remember { mutableStateOf("API_TOKEN") }
    var secretReviewValue by remember { mutableStateOf("") }
    var secretReviewReveal by remember { mutableStateOf(false) }
    var networkOnline by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
        var previous: Boolean? = null
        while (true) {
            val current = runCatching { cm.activeNetwork?.let { cm.getNetworkCapabilities(it)?.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_VALIDATED) } == true }.getOrDefault(false)
            networkOnline = current
            if ((previous != null && previous != current) || (previous == null && !current)) {
                val msg = if (current) "Ağa yeniden bağlanıldı" else "Çevrimdışı · bağlantı bekleniyor"
                context.getSharedPreferences("kairo_preferences", Context.MODE_PRIVATE).edit().putBoolean("network_online", current).putString("network_status", msg).apply()
                statusNotice = msg
                context.sendBroadcast(Intent("com.altayhr.kairo.network.STATUS").setPackage(context.packageName).putExtra("online", current))
                if (IslandOverlayService.isEnabled(context)) {
                    IslandOverlayService.publish(context, if (current) "Çevrimiçi" else "Çevrimdışı", if (current) "Ağa yeniden bağlanıldı · görevler devam edebilir" else "Bağlantı yok · yeniden bağlanma bekleniyor", if (current) "success" else "error")
                }
                com.altayhr.kairo.widgets.KairoWidgetProvider.requestRefresh(context)
            }
            previous = current
            delay(2500)
        }
    }
    var webSearch by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var searchVisible by remember { mutableStateOf(false) }
    var exportFile by remember { mutableStateOf<WorkspaceFile?>(null) }
    var renameChatId by remember { mutableStateOf<String?>(null) }
    var renameText by remember { mutableStateOf("") }
    var deleteChatId by remember { mutableStateOf<String?>(null) }
    var newProjectOpen by remember { mutableStateOf(false) }
    var projectName by remember { mutableStateOf("") }
    var projectDescription by remember { mutableStateOf("") }
    var selectedProjectId by remember { mutableStateOf<String?>(null) }
    var apiKeyPresent by remember { mutableStateOf(!secrets.getApiKey().isNullOrBlank()) }
    var modelName by remember { mutableStateOf(secrets.model()) }
    var thinkingLevel by remember { mutableStateOf(secrets.thinkingLevel()) }
    var islandEnabled by remember { mutableStateOf(IslandOverlayService.isEnabled(context)) }
    var soundEnabled by remember { mutableStateOf(context.getSharedPreferences("kairo_preferences", Context.MODE_PRIVATE).getBoolean("sound_enabled", true)) }
    var notificationsGranted by remember { mutableStateOf(android.os.Build.VERSION.SDK_INT < 33 || ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) }
    var microphoneGranted by remember { mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) }
    var overlayPermissionGranted by remember { mutableStateOf(IslandOverlayService.hasPermission(context)) }
    var batteryUnrestricted by remember { mutableStateOf((context.getSystemService(Context.POWER_SERVICE) as PowerManager).isIgnoringBatteryOptimizations(context.packageName)) }
    var shouldAutoStartCapsule by remember { mutableStateOf(false) }
    var lastOverlayPermission by remember { mutableStateOf(overlayPermissionGranted) }
    var capsuleGeometry by remember { mutableStateOf(CapsuleGeometry.read(context)) }
    val microphonePermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        microphoneGranted = granted
        if (granted) { statusNotice = "Mikrofon izni verildi; canlı sohbeti başlatabilirsin" }
        else statusNotice = "Sesli sohbet için mikrofon izni gerekiyor"
    }
    val notificationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        notificationsGranted = granted
        statusNotice = if (granted) "Bildirim izni verildi" else "Bildirim izni verilmedi; arka plan durumu sınırlı görünebilir"
    }
    fun openOverlayPermissionSettings() {
        shouldAutoStartCapsule = true
        runCatching {
            context.startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }.onFailure { statusNotice = "Sistem kapsül ayarları açılamadı" }
    }
    fun requestNotifications() {
        if (android.os.Build.VERSION.SDK_INT >= 33 && !notificationsGranted) notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        else statusNotice = "Bildirim izni zaten kullanılabilir"
    }
    fun requestMicrophone() {
        if (!microphoneGranted) microphonePermission.launch(Manifest.permission.RECORD_AUDIO)
        else statusNotice = "Mikrofon izni zaten verildi"
    }
    fun openBatterySettings() {
        runCatching { context.startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
            .onFailure { statusNotice = "Pil ayarları açılamadı" }
    }

    fun persistChats(next: List<ChatSession>) {
        chats = next.sortedWith(compareByDescending<ChatSession> { it.pinned }.thenByDescending { it.updatedAt })
        store.saveChats(chats)
    }
    fun updateChat(updated: ChatSession) {
        val next = chats.toMutableList()
        val index = next.indexOfFirst { it.id == updated.id }
        if (index >= 0) next[index] = updated else next.add(updated)
        persistChats(next)
    }
    fun makeNewChat(projectId: String? = selectedProjectId): ChatSession {
        val chat = ChatSession(projectId = projectId)
        persistChats(listOf(chat) + chats)
        selectedChatId = chat.id
        destination = Destination.CHAT
        chatPane = ChatPane.CHAT
        pendingFiles = emptyList()
        selectedProjectId = projectId
        statusNotice = "Yeni sohbet hazır"
        return chat
    }
    fun selectChat(chat: ChatSession) {
        selectedChatId = chat.id
        selectedProjectId = chat.projectId
        destination = Destination.CHAT
        chatPane = ChatPane.CHAT
        pendingFiles = emptyList()
        input = ""
        searchVisible = false
    }

    fun submitMessage(rawText: String) {
        val trimmedInput = rawText.trim()
        if (secretReview == null && trimmedInput.isNotEmpty()) {
            val patterns = listOf(
                Regex("ghp_[A-Za-z0-9]{20,}"), Regex("github_pat_[A-Za-z0-9_]{20,}"),
                Regex("AIza[A-Za-z0-9_-]{30,}"), Regex("sk-[A-Za-z0-9_-]{24,}"),
                Regex("xox[baprs]-[A-Za-z0-9-]{15,}"),
                Regex("(?i)(api[_ -]?key|access[_ -]?token|secret|token)\\s*[:=]\\s*[^\\s,;]{12,}")
            )
            val match = patterns.firstNotNullOfOrNull { it.find(trimmedInput) }
            if (match != null) {
                val token = (when { ":" in match.value -> match.value.substringAfter(":"); "=" in match.value -> match.value.substringAfter("="); else -> match.value }).trim().trim('"', '\'')
                secretReview = SecretReview(trimmedInput, trimmedInput.replace(match.value, "[GÜVENLİ KASAYA TAŞINAN GİZLİ DEĞER]"), "API_TOKEN", token)
                secretReviewName = when { match.value.startsWith("ghp_") || match.value.startsWith("github_pat_") -> "GITHUB_TOKEN"; match.value.startsWith("AIza") -> "GOOGLE_API_KEY"; match.value.startsWith("xox") -> "SLACK_TOKEN"; else -> "API_TOKEN" }
                secretReviewValue = token
                secretReviewReveal = false
                statusNotice = "Gizli değer algılandı; göndermeden önce kontrol et"
                return
            }
        }
        val trimmed = rawText.trim().ifBlank { if (pendingFiles.isNotEmpty()) "Eklediğim dosyaları incele ve önemli noktaları özetle." else "" }
        if (trimmed.isNotEmpty() && loadingChatId == null) {
            val active = chats.firstOrNull { it.id == selectedChatId } ?: makeNewChat(selectedProjectId)
            val now = System.currentTimeMillis()
            val userMessage = ChatMessage(role = "user", content = trimmed, createdAt = now, attachments = pendingFiles)
            val title = if (active.messages.isEmpty() && active.title == "Yeni sohbet") {
                trimmed.replace('\n', ' ').take(38).let { if (trimmed.length > 38) "$it…" else it }
            } else active.title
            val requestChat = active.copy(title = title, messages = active.messages + userMessage, updatedAt = now)
            updateChat(requestChat)
            AutomationEvents.emit(context, "chat.message.sent", "Kullanıcı sohbet mesajı gönderdi", JSONObject()
                .put("chatId", requestChat.id).put("projectId", requestChat.projectId ?: "").put("characterCount", trimmed.length).put("attachmentCount", pendingFiles.size).toString())
            input = ""
            pendingFiles = emptyList()
            loadingChatId = requestChat.id
            statusNotice = "Görev gönderildi"
            SoundEffects.play(context, "send")
            IslandOverlayService.publish(context, "İstek alındı", "Sohbet bağlamı hazırlanıyor; yanıt arka plan görev kuyruğunda üretilecek.", "thinking")
            if (SpotifyCommandService.isCommand(trimmed) && com.altayhr.kairo.oauth.OAuthFlowManager.isConnected(context, "spotify")) {
                scope.launch {
                    val result = SpotifyCommandService.execute(context, trimmed)
                    val latest = chats.firstOrNull { it.id == requestChat.id } ?: requestChat
                    updateChat(latest.copy(messages = latest.messages + ChatMessage(role = "assistant", content = result, createdAt = System.currentTimeMillis()), updatedAt = System.currentTimeMillis()))
                    loadingChatId = null
                    statusNotice = result
                    IslandOverlayService.publish(context, "Spotify", result, if (result.startsWith("Spotify'da çalınıyor")) "success" else "error")
                }
            } else {
                GeminiTaskService.enqueue(context, requestChat.id, webSearch)
                statusNotice = "Görev arka planda başlatıldı"
            }
            destination = Destination.CHAT
            chatPane = ChatPane.CHAT
        }
    }

    DisposableEffect(context) {
        val taskReceiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                when (intent?.action) {
                    MainActivity.ACTION_CAPSULE_NAVIGATE -> {
                        val nextDestination = destinationForRoute(intent.getStringExtra("destination"))
                        val targetChatId = intent.getStringExtra("chat_id")
                        if (nextDestination == Destination.CHAT && !targetChatId.isNullOrBlank()) {
                            chats.firstOrNull { it.id == targetChatId }?.let { chat ->
                                selectedChatId = chat.id
                                selectedProjectId = chat.projectId
                            }
                        }
                        destination = nextDestination
                        intent.getStringExtra("kairo_widget_id")?.takeIf { it.isNotBlank() }?.let { pendingWidgetId = it }
                        if (destination == Destination.CHAT) chatPane = ChatPane.CHAT
                        statusNotice = if (intent.hasExtra("kairo_widget_id")) "Widget AI görevi hazırlanıyor" else "Kapsülden açıldı"
                    }
                    IslandOverlayService.ACTION_REPOSITION -> {
                        capsuleGeometry = CapsuleGeometry.read(context)
                        overlayPermissionGranted = IslandOverlayService.hasPermission(context)
                    }
                    GeminiTaskService.ACTION_TASK_UPDATE -> {
                        val chatId = intent.getStringExtra("chat_id") ?: return
                        val message = intent.getStringExtra("status").orEmpty()
                        chats = store.getChats().sortedWith(compareByDescending<ChatSession> { it.pinned }.thenByDescending { it.updatedAt })
                        if (loadingChatId == chatId && (intent.hasExtra("success") || intent.getBooleanExtra("terminal_waiting", false))) loadingChatId = null
                        statusNotice = message
                        islandEnabled = IslandOverlayService.isEnabled(context)
                    }
                }
            }
        }
        val filter = IntentFilter().apply {
            addAction(GeminiTaskService.ACTION_TASK_UPDATE)
            addAction(MainActivity.ACTION_CAPSULE_NAVIGATE)
            addAction(IslandOverlayService.ACTION_REPOSITION)
        }
        ContextCompat.registerReceiver(context, taskReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
        onDispose { runCatching { context.unregisterReceiver(taskReceiver) } }
    }

    LaunchedEffect(Unit) {
        if (chats.isEmpty()) {
            val firstChat = ChatSession()
            chats = listOf(firstChat)
            selectedChatId = firstChat.id
            store.saveChats(chats)
        }
        while (true) {
            islandEnabled = IslandOverlayService.isEnabled(context)
            soundEnabled = context.getSharedPreferences("kairo_preferences", Context.MODE_PRIVATE).getBoolean("sound_enabled", true)
            notificationsGranted = android.os.Build.VERSION.SDK_INT < 33 || ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            microphoneGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
            overlayPermissionGranted = IslandOverlayService.hasPermission(context)
            batteryUnrestricted = (context.getSystemService(Context.POWER_SERVICE) as PowerManager).isIgnoringBatteryOptimizations(context.packageName)
            if (!lastOverlayPermission && overlayPermissionGranted && shouldAutoStartCapsule) {
                islandEnabled = IslandOverlayService.start(context)
                shouldAutoStartCapsule = false
                statusNotice = if (islandEnabled) "Kapsül izni verildi ve kapsül açıldı" else "Kapsül başlatılamadı"
            }
            lastOverlayPermission = overlayPermissionGranted
            val latestGeometry = CapsuleGeometry.read(context)
            if (latestGeometry != capsuleGeometry) capsuleGeometry = latestGeometry
            delay(1200)
        }
    }
    LaunchedEffect(statusNotice) {
        if (statusNotice != null) {
            delay(3000)
            statusNotice = null
        }
    }

    val openFilePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                runCatching {
                    val name = displayName(context, uri)
                    val mime = context.contentResolver.getType(uri) ?: mimeFromName(name)
                    val bytes = withContext(Dispatchers.IO) {
                        val stream = context.contentResolver.openInputStream(uri) ?: throw IllegalStateException("Dosya okunamadı.")
                        stream.use { readLimited(it, 25 * 1024 * 1024) }
                    }
                    if (destination == Destination.PROJECT_FILES) {
                        val project = projects.firstOrNull { it.id == selectedProjectId }
                            ?: throw IllegalStateException("Proje bulunamadı.")
                        val stored = withContext(Dispatchers.IO) { store.storeProjectFile(project.id, name, mime, bytes) }
                        projects = projects.map { if (it.id == project.id) it.copy(files = it.files + stored) else it }
                        store.saveProjects(projects)
                        statusNotice = "Dosya proje alanına kopyalandı"
                    } else {
                        val current = chats.firstOrNull { it.id == selectedChatId } ?: makeNewChat(selectedProjectId)
                        val stored = withContext(Dispatchers.IO) { store.storeFile(current.id, name, mime, bytes, "uploaded") }
                        updateChat(current.copy(files = current.files + stored, updatedAt = System.currentTimeMillis()))
                        pendingFiles = pendingFiles + stored
                        statusNotice = "Dosya sohbet çalışma alanına kopyalandı"
                    }
                }.onFailure { statusNotice = it.message ?: "Dosya eklenemedi" }
            }
        }
    }
    val createDocument = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("*/*")) { uri: Uri? ->
        val fileToExport = exportFile
        if (uri != null && fileToExport != null) {
            scope.launch {
                runCatching {
                    withContext(Dispatchers.IO) {
                        store.readFile(fileToExport).inputStream().use { source ->
                            context.contentResolver.openOutputStream(uri)?.use { target -> source.copyTo(target) }
                                ?: throw IllegalStateException("Seçilen klasöre yazılamadı.")
                        }
                    }
                    statusNotice = "Dosya dışa aktarıldı"
                }.onFailure { statusNotice = it.message ?: "Dosya dışa aktarılamadı" }
                exportFile = null
            }
        } else exportFile = null
    }

    val currentChat = chats.firstOrNull { it.id == selectedChatId }
    val drawerChats = chats.filter { searchQuery.isBlank() || it.title.contains(searchQuery, true) }
    val drawerBody: @Composable ColumnScope.() -> Unit = {
        Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 18.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(38.dp).clip(RoundedCornerShape(13.dp)).background(Accent), contentAlignment = Alignment.Center) {
                Text("K", color = Color(0xFF11131A), fontSize = 22.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.SansSerif)
            }
            Spacer(Modifier.width(11.dp))
            Column {
                Text("Kairo AI", color = PrimaryText, fontFamily = FontFamily.SansSerif, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text("KİŞİSEL YAPAY ZEKA ÇALIŞMA ALANI", color = MutedText, fontSize = 8.sp, letterSpacing = 1.05.sp)
            }
        }
        DrawerAction(Icons.Outlined.Add, "Yeni sohbet", true) { makeNewChat(null); scope.launch { drawerState.close() } }
        Spacer(Modifier.height(6.dp))
        DrawerAction(Icons.Outlined.Workspaces, "Projeler") { destination = Destination.PROJECTS; scope.launch { drawerState.close() } }
        DrawerAction(Icons.Outlined.Tune, "Görev merkezi") { destination = Destination.AGENT; scope.launch { drawerState.close() } }
        DrawerAction(Icons.Outlined.Group, "Ajan filosu · 8 anahtar") { destination = Destination.AGENT_POOL; scope.launch { drawerState.close() } }
        DrawerAction(Icons.Outlined.AutoAwesome, "AI widget merkezi · 22 kart") { destination = Destination.AGENT_WIDGETS; scope.launch { drawerState.close() } }
        DrawerAction(Icons.Outlined.Lock, "Güvenli kasa") { destination = Destination.VAULT; scope.launch { drawerState.close() } }
        DrawerAction(Icons.Outlined.AutoAwesome, "Akıllı araçlar") { destination = Destination.TOOLS; scope.launch { drawerState.close() } }
        DrawerAction(Icons.Outlined.Tune, "Canlı sesli sohbet") { destination = Destination.VOICE; scope.launch { drawerState.close() } }
        DrawerAction(Icons.Outlined.History, "Kripto piyasası") { destination = Destination.CRYPTO; scope.launch { drawerState.close() } }
        DrawerAction(Icons.Outlined.AttachFile, "E-posta taslağı") { destination = Destination.EMAIL; scope.launch { drawerState.close() } }
        DrawerAction(Icons.Outlined.Description, "Not defteri") { destination = Destination.NOTES; scope.launch { drawerState.close() } }
        DrawerAction(Icons.Outlined.Tune, "Portföy") { destination = Destination.PORTFOLIO; scope.launch { drawerState.close() } }
        DrawerAction(Icons.Outlined.Language, "Spotify ve medya") { destination = Destination.MEDIA; scope.launch { drawerState.close() } }
        DrawerAction(Icons.Outlined.AttachFile, "WhatsApp mesajı") { destination = Destination.WHATSAPP; scope.launch { drawerState.close() } }
        DrawerAction(Icons.Outlined.History, "Cihaz durumu") { destination = Destination.DEVICE; scope.launch { drawerState.close() } }
        DrawerAction(Icons.Outlined.Language, "Entegrasyonlar") { destination = Destination.INTEGRATIONS; scope.launch { drawerState.close() } }
        DrawerAction(Icons.Outlined.Tune, "OAuth ve otomasyon") { destination = Destination.AUTOMATION; scope.launch { drawerState.close() } }
        DrawerAction(Icons.Outlined.Settings, "Ayarlar") { destination = Destination.SETTINGS; scope.launch { drawerState.close() } }
        DrawerAction(Icons.Outlined.Check, "İzinler ve ilk kurulum") { destination = Destination.ONBOARDING; scope.launch { drawerState.close() } }
        DrawerAction(Icons.Outlined.Code, "Terminal") { destination = Destination.TERMINAL; scope.launch { drawerState.close() } }
        Spacer(Modifier.height(18.dp))
        Divider(color = Stroke.copy(alpha = .7f), modifier = Modifier.padding(horizontal = 18.dp))
        Row(Modifier.fillMaxWidth().padding(start = 18.dp, end = 10.dp, top = 18.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("SON SOHBETLER", color = MutedText, fontSize = 10.sp, letterSpacing = 1.4.sp, modifier = Modifier.weight(1f))
            IconButton(onClick = { searchVisible = !searchVisible; searchQuery = "" }, modifier = Modifier.size(30.dp)) {
                Icon(Icons.Outlined.Search, contentDescription = "Sohbetlerde ara", tint = SecondaryText, modifier = Modifier.size(17.dp))
            }
        }
        AnimatedVisibility(searchVisible) {
            OutlinedTextField(value = searchQuery, onValueChange = { searchQuery = it }, modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp),
                placeholder = { Text("Sohbet ara", fontSize = 13.sp) }, singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Accent, unfocusedBorderColor = Stroke))
        }
        LazyColumn(Modifier.weight(1f).padding(horizontal = 8.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            items(drawerChats, key = { it.id }) { chat ->
                var menuOpen by remember(chat.id) { mutableStateOf(false) }
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                        .background(if (chat.id == selectedChatId && destination == Destination.CHAT) FieldColor else Color.Transparent)
                        .clickable { selectChat(chat); scope.launch { drawerState.close() } }
                        .padding(start = 12.dp, end = 6.dp, top = 10.dp, bottom = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (chat.pinned) Icon(Icons.Outlined.Bookmark, null, tint = Accent, modifier = Modifier.size(14.dp))
                    else Icon(Icons.Outlined.History, null, tint = MutedText, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(9.dp))
                    Text(chat.title, color = if (chat.id == selectedChatId) PrimaryText else SecondaryText, fontSize = 13.sp, maxLines = 1, modifier = Modifier.weight(1f))
                    Box {
                        IconButton(onClick = { menuOpen = true }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Outlined.MoreHoriz, null, tint = MutedText, modifier = Modifier.size(18.dp))
                        }
                        DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }, modifier = Modifier.background(CardColor)) {
                            DropdownMenuItem(text = { Text(if (chat.pinned) "Sabitlemeyi kaldır" else "Sohbeti sabitle") }, leadingIcon = { Icon(if (chat.pinned) Icons.Outlined.BookmarkBorder else Icons.Outlined.Bookmark, null) }, onClick = {
                                updateChat(chat.copy(pinned = !chat.pinned)); menuOpen = false
                            })
                            DropdownMenuItem(text = { Text("Yeniden adlandır") }, onClick = {
                                renameChatId = chat.id; renameText = chat.title; menuOpen = false
                            })
                            DropdownMenuItem(text = { Text("Sil", color = Danger) }, leadingIcon = { Icon(Icons.Outlined.DeleteOutline, null, tint = Danger) }, onClick = {
                                deleteChatId = chat.id; menuOpen = false
                            })
                        }
                    }
                }
            }
        }
        Text("Kairo AI · v1.0.0", color = MutedText, fontSize = 10.sp, modifier = Modifier.padding(start = 20.dp, top = 8.dp, bottom = 14.dp))
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = { ModalDrawerSheet(drawerContainerColor = Panel, modifier = Modifier.width(310.dp)) { drawerBody() } },
        scrimColor = Color.Black.copy(alpha = .65f)
    ) {
        Scaffold(
            containerColor = Ink,
            contentWindowInsets = WindowInsets.statusBars,
            topBar = {
                Column(Modifier.fillMaxWidth().background(Ink)) {
                    Row(Modifier.fillMaxWidth().background(if (networkOnline) Color(0xFF10382A) else Color(0xFF481D27)).padding(horizontal = 14.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(if (networkOnline) Icons.Outlined.CheckCircle else Icons.Outlined.CloudOff, null, tint = if (networkOnline) Color(0xFF69E3A2) else Color(0xFFFF6D7A), modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(7.dp))
                        Text(if (networkOnline) "ÇEVRİMİÇİ · Kairo hizmetleri hazır" else "ÇEVRİMDIŞI · bağlantı geri gelince görevler devam eder", color = if (networkOnline) Color(0xFF8BE6B3) else Color(0xFFFF9BA4), fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = .4.sp)
                    }
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { scope.launch { drawerState.open() } }, modifier = Modifier.size(42.dp)) {
                            Icon(Icons.Outlined.Menu, "Menüyü aç", tint = PrimaryText)
                        }
                        Spacer(Modifier.width(5.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                when (destination) {
                                    Destination.ONBOARDING -> "İlk kurulum"
                                    Destination.AGENT -> "Görev merkezi"
                                    Destination.AGENT_POOL -> "Ajan filosu"
                                    Destination.AGENT_WIDGETS -> "AI widget merkezi"
                                    Destination.VAULT -> "Güvenli kasa"
                                    Destination.TOOLS -> "Akıllı araçlar"
                                    Destination.CHAT -> currentChat?.title ?: "Yeni sohbet"
                                    Destination.PROJECTS -> "Projeler"
                                    Destination.PROJECT_FILES -> projects.firstOrNull { it.id == selectedProjectId }?.name ?: "Proje dosyaları"
                                    Destination.SETTINGS -> "Ayarlar"
                                    Destination.TERMINAL -> "Terminal"
                                    Destination.VOICE -> "Canlı sesli sohbet"
                                    Destination.CRYPTO -> "Kripto piyasası"
                                    Destination.EMAIL -> "E-posta taslağı"
                                    Destination.NOTES -> "Not defteri"
                                    Destination.INTEGRATIONS -> "Entegrasyon merkezi"
                                    Destination.AUTOMATION -> "Yerel otomasyon"
                                    Destination.PORTFOLIO -> "Portföy"
                                    Destination.DEVICE -> "Cihaz durumu"
                                    Destination.MEDIA -> "Medya merkezi"
                                    Destination.WHATSAPP -> "WhatsApp"
                                }, color = PrimaryText, fontFamily = FontFamily.Serif, fontWeight = FontWeight.SemiBold,
                                fontSize = 18.sp, maxLines = 1
                            )
                            Text(
                                when (destination) {
                                    Destination.ONBOARDING -> "İzinleri gözden geçir ve uygulamayı hazırla"
                                    Destination.AGENT -> "Görevleri planla, kod üret ve çıktıları incele"
                                    Destination.AGENT_POOL -> "Sekiz şifreli Gemini anahtar yuvası · rol dağılımı"
                                    Destination.AGENT_WIDGETS -> "22 AI destekli kart · komut akışı · ilerleme · derleme · dosyalar"
                                    Destination.VAULT -> "Tüm sohbetler ve projeler için ortak şifreli kasa"
                                    Destination.TOOLS -> "Sözlük · Metin parçaları · Yanıt stili · İçgörüler · Kısayollar"
                                    Destination.CHAT -> currentChat?.let { if (it.projectId != null) projects.firstOrNull { p -> p.id == it.projectId }?.name ?: "Çalışma alanı" else "Kişisel çalışma alanı" } ?: "Kişisel çalışma alanı"
                                    Destination.PROJECTS -> "Sohbetlerini projelere ayır"
                                    Destination.PROJECT_FILES -> "${projects.firstOrNull { it.id == selectedProjectId }?.files?.size ?: 0} proje dosyası · Sohbet dosyalarından ayrı"
                                    Destination.SETTINGS -> "Bağlantı, modeller ve arayüz tercihleri"
                                    Destination.TERMINAL -> "Kairo içine gömülü terminal · AI komut onayı"
                                    Destination.VOICE -> "Gemini Live · çift yönlü ses"
                                    Destination.CRYPTO -> "Anlık piyasa verileri · salt okunur"
                                    Destination.EMAIL -> "Güvenli e-posta taslağı"
                                    Destination.NOTES -> "Yerel notlar ve prompt parçaları"
                                    Destination.INTEGRATIONS -> "Servis bağlantıları ve durum kontrolü"
                                    Destination.AUTOMATION -> "Cihazda çalışan zamanlanmış ve olay tabanlı akışlar"
                                    Destination.PORTFOLIO -> "Yerel varlık takibi · sadece görüntüleme"
                                    Destination.DEVICE -> "Pil, depolama ve yaklaşık bellek/CPU ölçümü"
                                    Destination.MEDIA -> "Spotify ve Apple Music kısayolları"
                                    Destination.WHATSAPP -> "Mesajı uygulamaya aktar; son gönderim sende"
                                }, color = MutedText, fontSize = 10.sp, letterSpacing = .3.sp, maxLines = 1
                            )
                        }
                        if (destination == Destination.CHAT && currentChat != null) {
                            IconButton(onClick = { chatPane = if (chatPane == ChatPane.CHAT) ChatPane.WORKSPACE else ChatPane.CHAT }, modifier = Modifier.size(40.dp)) {
                                Icon(if (chatPane == ChatPane.CHAT) Icons.Outlined.FolderOpen else Icons.AutoMirrored.Outlined.ArrowBack,
                                    if (chatPane == ChatPane.CHAT) "Çalışma alanı" else "Sohbete dön", tint = if (chatPane == ChatPane.WORKSPACE) Accent else SecondaryText)
                            }
                            IconButton(onClick = { makeNewChat(currentChat.projectId) }, modifier = Modifier.size(40.dp)) {
                                Icon(Icons.Outlined.Add, "Yeni sohbet", tint = Accent)
                            }
                        } else if (destination == Destination.PROJECTS) {
                            IconButton(onClick = { newProjectOpen = true }, modifier = Modifier.size(40.dp)) { Icon(Icons.Outlined.Add, "Yeni proje", tint = Accent) }
                        } else if (destination == Destination.PROJECT_FILES) {
                            IconButton(onClick = { destination = Destination.PROJECTS }, modifier = Modifier.size(40.dp)) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Projelere dön", tint = SecondaryText) }
                            IconButton(onClick = { openFilePicker.launch(arrayOf("*/*")) }, modifier = Modifier.size(40.dp)) { Icon(Icons.Outlined.UploadFile, "Proje dosyası ekle", tint = Accent) }
                        }
                    }
                    AnimatedVisibility(visible = loadingChatId != null || statusNotice != null) {
                        Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 2.dp), horizontalArrangement = Arrangement.Center) {
                            Row(Modifier.clip(CircleShape).background(Color(0xFF191C20)).border(1.dp, Stroke, CircleShape).padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                if (loadingChatId != null) CircularProgressIndicator(Modifier.size(11.dp), strokeWidth = 1.5.dp, color = Accent)
                                else Box(Modifier.size(6.dp).clip(CircleShape).background(Green))
                                Spacer(Modifier.width(8.dp))
                                Text(if (loadingChatId != null) "Gemini yanıt hazırlıyor" else statusNotice.orEmpty(), color = SecondaryText, fontSize = 11.sp)
                            }
                        }
                    }
                    if (destination == Destination.CHAT && currentChat != null) {
                        Row(Modifier.fillMaxWidth().padding(start = 18.dp, end = 18.dp, bottom = 10.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            PaneToggle("Sohbet", chatPane == ChatPane.CHAT) { chatPane = ChatPane.CHAT }
                            PaneToggle("Çalışma alanı  ${currentChat.files.size}", chatPane == ChatPane.WORKSPACE) { chatPane = ChatPane.WORKSPACE }
                            Spacer(Modifier.weight(1f))
                            currentChat.projectId?.let { id -> projects.firstOrNull { it.id == id }?.let { Text(it.name, color = Accent, fontSize = 11.sp, modifier = Modifier.align(Alignment.CenterVertically)) } }
                        }
                    }
                    Divider(color = Stroke.copy(alpha = .65f))
                }
            },
            bottomBar = {
                if (destination == Destination.CHAT && chatPane == ChatPane.CHAT && currentChat != null) {
                    Composer(
                        text = input, onTextChange = { input = it }, pendingFiles = pendingFiles,
                        webSearch = webSearch, onWebToggle = { webSearch = !webSearch },
                        onAttach = { openFilePicker.launch(arrayOf("*/*")) },
                        onRemovePending = { file -> pendingFiles = pendingFiles.filterNot { it.id == file.id } },
                        onSend = { submitMessage(input) }, enabled = loadingChatId == null
                    )
                }
            }
        ) { padding ->
            Box(Modifier.fillMaxSize().padding(padding)) {
                when (destination) {
                    Destination.TOOLS -> FlowToolkitScreen(
                        chats = chats,
                        store = flowToolkitStore,
                        onUseSnippet = { snippet ->
                            if (currentChat == null) makeNewChat(null)
                            input = snippet
                            destination = Destination.CHAT
                            chatPane = ChatPane.CHAT
                        },
                        onRunPrompt = { prompt, useSearch ->
                            makeNewChat(null)
                            webSearch = useSearch
                            submitMessage(prompt)
                        }
                    )
                    Destination.AGENT_POOL -> AgentPoolScreen()
                    Destination.AGENT_WIDGETS -> AgentWidgetsScreen(
                        onRunPrompt = { prompt, useSearch ->
                            if (loadingChatId == null) {
                                makeNewChat(selectedProjectId)
                                webSearch = useSearch
                                submitMessage(prompt)
                            } else { statusNotice = "Tüm çalışma hattı meşgul; görev bitince yeniden dene." }
                        },
                        isBusy = loadingChatId != null,
                        selectedProjectName = projects.firstOrNull { it.id == selectedProjectId }?.name,
                        terminalSessionId = selectedChatId.ifBlank { "personal" },
                        launchWidgetId = pendingWidgetId,
                        onWidgetLaunchHandled = { consumed -> if (pendingWidgetId == consumed) pendingWidgetId = null }
                    )
                    Destination.VAULT -> SecureVaultScreen()
                    Destination.AGENT -> AgentStudioScreen(
                        chats = chats,
                        loadingChatId = loadingChatId,
                        onOpenChat = { selectChat(it) },
                        onRun = { prompt, useSearch ->
                            makeNewChat(null)
                            webSearch = useSearch
                            submitMessage(prompt)
                        }
                    )
                    Destination.ONBOARDING -> PermissionSetupScreen(
                        notificationsGranted = notificationsGranted,
                        overlayGranted = overlayPermissionGranted,
                        microphoneGranted = microphoneGranted,
                        batteryUnrestricted = batteryUnrestricted,
                        onRequestNotifications = { requestNotifications() },
                        onOpenOverlaySettings = { openOverlayPermissionSettings() },
                        onRequestMicrophone = { requestMicrophone() },
                        onOpenBatterySettings = { openBatterySettings() },
                        onComplete = {
                            context.getSharedPreferences("kairo_preferences", Context.MODE_PRIVATE).edit().putBoolean("permission_setup_complete", true).apply()
                            setupComplete = true
                            destination = Destination.CHAT
                            statusNotice = "Kairo kullanıma hazır"
                        }
                    )
                    Destination.CHAT -> {
                        val chat = currentChat
                        if (chat == null) EmptyLanding(onNewChat = { makeNewChat(null) })
                        else if (chatPane == ChatPane.WORKSPACE) {
                            WorkspaceScreen(chat, onAttach = { openFilePicker.launch(arrayOf("*/*")) }, onExport = { file -> exportFile = file; createDocument.launch(file.name) }, onDeleteFile = { file ->
                                SoundEffects.play(context, "close")
                                store.deleteWorkspaceFile(file)
                                updateChat(chat.copy(files = chat.files.filterNot { it.id == file.id }, messages = chat.messages.map { it.copy(attachments = it.attachments.filterNot { a -> a.id == file.id }) }))
                                pendingFiles = pendingFiles.filterNot { it.id == file.id }
                            })
                        } else ChatScreen(chat = chat, loading = loadingChatId == chat.id, onQuickPrompt = { input = it }, onExport = { file -> exportFile = file; createDocument.launch(file.name) })
                    }
                    Destination.PROJECTS -> ProjectsScreen(
                        projects = projects, chats = chats,
                        onNewProject = { newProjectOpen = true },
                        onOpenProject = { project -> selectedProjectId = project.id; destination = Destination.PROJECT_FILES },
                        onOpenChat = { selectChat(it) }
                    )
                    Destination.PROJECT_FILES -> {
                        val project = projects.firstOrNull { it.id == selectedProjectId }
                        if (project == null) ProjectsScreen(projects, chats, onNewProject = { newProjectOpen = true }, onOpenProject = { selectedProjectId = it.id }, onOpenChat = { selectChat(it) })
                        else ProjectDetailScreen(
                            project = project,
                            chats = chats.filter { it.projectId == project.id }.sortedByDescending { it.updatedAt },
                            onNewChat = { makeNewChat(project.id) },
                            onOpenChat = { selectChat(it) },
                            onAttach = { openFilePicker.launch(arrayOf("*/*")) },
                            onExport = { file -> exportFile = file; createDocument.launch(file.name) },
                            onDeleteFile = { file ->
                                SoundEffects.play(context, "close")
                                store.deleteWorkspaceFile(file)
                                projects = projects.map { if (it.id == project.id) it.copy(files = it.files.filterNot { f -> f.id == file.id }) else it }
                                store.saveProjects(projects)
                            }
                        )
                    }
                    Destination.SETTINGS -> SettingsScreen(
                        apiKeyPresent = apiKeyPresent, modelName = modelName, thinkingLevel = thinkingLevel,
                        islandEnabled = islandEnabled, soundEnabled = soundEnabled,
                        liveModel = secrets.liveModel(), liveVoice = secrets.liveVoice(),
                        capsuleGeometry = capsuleGeometry, overlayAllowed = overlayPermissionGranted,
                        notificationsGranted = notificationsGranted, microphoneGranted = microphoneGranted, batteryUnrestricted = batteryUnrestricted,
                        onRequestNotifications = { requestNotifications() }, onRequestMicrophone = { requestMicrophone() },
                        onOpenOverlaySettings = { openOverlayPermissionSettings() }, onOpenBatterySettings = { openBatterySettings() },
                        onSaveCapsuleGeometry = { geometry -> IslandOverlayService.saveGeometry(context, geometry); capsuleGeometry = geometry.normalized(); statusNotice = "Kapsül konumu ve boyutu uygulandı" },
                        onSaveApiKey = { key -> if (key.isNullOrBlank()) secrets.clearApiKey() else secrets.saveApiKey(key); apiKeyPresent = !key.isNullOrBlank(); statusNotice = if (apiKeyPresent) "API anahtarı güvenli saklandı" else "API anahtarı silindi" },
                        onSaveModel = { name -> secrets.saveModel(name); modelName = secrets.model(); statusNotice = "Model tercihi kaydedildi" },
                        onSaveThinking = { level -> secrets.saveThinkingLevel(level); thinkingLevel = secrets.thinkingLevel(); statusNotice = "Akıl yürütme tercihi kaydedildi" },
                        onSaveLive = { live, voice -> secrets.saveLiveModel(live); secrets.saveLiveVoice(voice); statusNotice = "Canlı ses tercihleri kaydedildi" },
                        onToggleSounds = { enabled -> context.getSharedPreferences("kairo_preferences", Context.MODE_PRIVATE).edit().putBoolean("sound_enabled", enabled).apply(); soundEnabled = enabled },
                        onToggleIsland = { enabled ->
                            if (enabled) {
                                if (IslandOverlayService.hasPermission(context)) {
                                    islandEnabled = IslandOverlayService.start(context)
                                    statusNotice = if (islandEnabled) "Etkinlik kapsülü açıldı" else "Kapsül başlatılamadı"
                                } else {
                                    statusNotice = "Kapsül için diğer uygulamaların üzerinde gösterme izni gerekli"
                                    openOverlayPermissionSettings()
                                }
                            } else { IslandOverlayService.stop(context); islandEnabled = false; statusNotice = "Etkinlik kapsülü kapatıldı" }
                        }
                    )
                    Destination.TERMINAL -> EmbeddedTerminalScreen(sessionId = selectedChatId.ifBlank { "personal" })
                    Destination.VOICE -> VoiceScreen(
                        apiConfigured = apiKeyPresent, liveModel = secrets.liveModel(), voice = secrets.liveVoice(),
                        onStart = {
                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) GeminiLiveVoiceService.start(context)
                            else microphonePermission.launch(Manifest.permission.RECORD_AUDIO)
                        },
                        onStop = { GeminiLiveVoiceService.stop(context) },
                        onOpenSettings = { destination = Destination.SETTINGS }
                    )
                    Destination.CRYPTO -> CryptoScreen()
                    Destination.EMAIL -> EmailComposerScreen()
                    Destination.NOTES -> NotesScreen { note -> input = note; destination = Destination.CHAT; chatPane = ChatPane.CHAT; statusNotice = "Not sohbet alanına aktarıldı" }
                    Destination.INTEGRATIONS -> IntegrationHubScreen(secrets) { key -> destination = when (key) { "crypto" -> Destination.CRYPTO; "mail" -> Destination.EMAIL; "portfolio" -> Destination.PORTFOLIO; "whatsapp" -> Destination.WHATSAPP; "spotify", "music" -> Destination.MEDIA; "automation" -> Destination.AUTOMATION; else -> Destination.INTEGRATIONS } }
                    Destination.AUTOMATION -> AutomationCenterScreen()
                    Destination.PORTFOLIO -> PortfolioScreen()
                    Destination.DEVICE -> DeviceStatusScreen()
                    Destination.MEDIA -> SpotifyPlayerScreen()
                    Destination.WHATSAPP -> WhatsAppScreen()
                }
            }
        }
    }

    if (secretReview != null) {
        AlertDialog(
            onDismissRequest = { secretReview = null; secretReviewValue = "" },
            containerColor = CardColor,
            title = { Text("Gizli değer algılandı") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Bu metin API anahtarı veya token içerebilir. Ham değer sohbet geçmişine gönderilmeyecek. İstersen tüm sohbetlerde ortak güvenli kasaya kaydet.", color = SecondaryText, fontSize = 12.sp)
                    OutlinedTextField(value = secretReviewName, onValueChange = { secretReviewName = it.uppercase().replace(Regex("[^A-Z0-9_]+"), "_").take(55) }, label = { Text("Kasa etiketi") }, singleLine = true)
                    OutlinedTextField(value = secretReviewValue, onValueChange = { secretReviewValue = it }, label = { Text("Algılanan değer") }, singleLine = true, visualTransformation = if (secretReviewReveal) VisualTransformation.None else PasswordVisualTransformation(), trailingIcon = { IconButton(onClick = { secretReviewReveal = !secretReviewReveal }) { Icon(if (secretReviewReveal) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility, "Değeri göster/gizle") } })
                }
            },
            confirmButton = { TextButton(onClick = {
                val review = secretReview
                if (!secretReviewName.isBlank() && secretReviewValue.isNotBlank()) {
                    secrets.saveSecret("vault_${secretReviewName}", secretReviewValue)
                    val redacted = review?.redacted ?: "[GİZLİ DEĞER KASAYA KAYDEDİLDİ]"
                    secretReview = null; secretReviewValue = ""
                    submitMessage(redacted.replace("[GÜVENLİ KASAYA TAŞINAN GİZLİ DEĞER]", "[${secretReviewName} güvenli kasaya kaydedildi]"))
                    statusNotice = "Gizli değer şifreli kasaya kaydedildi; ham token sohbetten çıkarıldı"
                }
            }) { Text("Kaydet ve gönder") } },
            dismissButton = { Row {
                TextButton(onClick = {
                    val redacted = secretReview?.redacted ?: ""
                    secretReview = null; secretReviewValue = ""
                    if (redacted.isNotBlank()) submitMessage(redacted)
                }) { Text("Kasaya kaydetmeden gönder") }
                TextButton(onClick = { secretReview = null; secretReviewValue = "" }) { Text("İptal") }
            } }
        )
    }

    if (renameChatId != null) {
        AlertDialog(onDismissRequest = { renameChatId = null }, containerColor = CardColor,
            title = { Text("Sohbeti yeniden adlandır") },
            text = { OutlinedTextField(value = renameText, onValueChange = { renameText = it }, singleLine = true, label = { Text("Sohbet adı") }) },
            confirmButton = { TextButton(onClick = {
                val id = renameChatId
                val clean = renameText.trim()
                if (id != null && clean.isNotBlank()) chats.firstOrNull { it.id == id }?.let { updateChat(it.copy(title = clean, updatedAt = System.currentTimeMillis())) }
                renameChatId = null
            }) { Text("Kaydet", color = Accent) } },
            dismissButton = { TextButton(onClick = { renameChatId = null }) { Text("Vazgeç") } }
        )
    }
    if (deleteChatId != null) {
        AlertDialog(onDismissRequest = { deleteChatId = null }, containerColor = CardColor,
            title = { Text("Sohbet silinsin mi?") }, text = { Text("Sohbetin mesajları ve o sohbete ait çalışma alanı dosyaları bu cihazdan kaldırılacak.", color = SecondaryText) },
            confirmButton = { TextButton(onClick = {
                val id = deleteChatId!!
                store.deleteChatFiles(id)
                persistChats(chats.filterNot { it.id == id })
                if (selectedChatId == id) {
                    val replacement = chats.firstOrNull()
                    if (replacement != null) selectChat(replacement) else makeNewChat(null)
                }
                deleteChatId = null
            }) { Text("Sil", color = Danger) } },
            dismissButton = { TextButton(onClick = { deleteChatId = null }) { Text("Vazgeç") } }
        )
    }
    if (newProjectOpen) {
        AlertDialog(onDismissRequest = { newProjectOpen = false }, containerColor = CardColor,
            title = { Text("Yeni proje") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(value = projectName, onValueChange = { projectName = it }, singleLine = true, label = { Text("Proje adı") })
                    OutlinedTextField(value = projectDescription, onValueChange = { projectDescription = it }, label = { Text("Açıklama (isteğe bağlı)") }, minLines = 2)
                }
            },
            confirmButton = { TextButton(onClick = {
                if (projectName.trim().isNotBlank()) {
                    val p = WorkspaceProject(name = projectName.trim(), description = projectDescription.trim(), colorIndex = projects.size % 5)
                    projects = listOf(p) + projects; store.saveProjects(projects)
                    projectName = ""; projectDescription = ""; newProjectOpen = false; selectedProjectId = p.id
                    makeNewChat(p.id)
                }
            }) { Text("Oluştur", color = Accent) } },
            dismissButton = { TextButton(onClick = { newProjectOpen = false }) { Text("Vazgeç") } }
        )
    }
}

@Composable
private fun DrawerAction(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, accent: Boolean = false, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 10.dp).clip(RoundedCornerShape(12.dp))
        .background(if (accent) Accent.copy(alpha = .11f) else Color.Transparent)
        .clickable(onClick = onClick).padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = if (accent) Accent else SecondaryText, modifier = Modifier.size(19.dp))
        Spacer(Modifier.width(12.dp))
        Text(title, color = if (accent) PrimaryText else SecondaryText, fontSize = 13.sp, fontWeight = if (accent) FontWeight.SemiBold else FontWeight.Normal)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PaneToggle(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(onClick = onClick, color = if (selected) FieldColor else Color.Transparent, shape = RoundedCornerShape(8.dp), border = if (selected) androidx.compose.foundation.BorderStroke(1.dp, Stroke) else null) {
        Text(label, color = if (selected) PrimaryText else MutedText, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp))
    }
}

@Composable
private fun ChatScreen(chat: ChatSession, loading: Boolean, onQuickPrompt: (String) -> Unit, onExport: (WorkspaceFile) -> Unit) {
    if (chat.messages.isEmpty()) {
        EmptyLanding(onNewChat = { onQuickPrompt("") }, onQuickPrompt = onQuickPrompt)
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize().imePadding(),
            contentPadding = PaddingValues(start = 17.dp, end = 17.dp, top = 18.dp, bottom = 18.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            items(chat.messages, key = { it.id }) { message -> MessageRow(message, onExport) }
            if (loading) item {
                Row(Modifier.fillMaxWidth().padding(start = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(28.dp).clip(RoundedCornerShape(10.dp)).background(Accent), contentAlignment = Alignment.Center) {
                        Text("c", color = Ink, fontSize = 18.sp, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.width(11.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Kairo AI", color = PrimaryText, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        Text("Yanıt hazırlanıyor…", color = SecondaryText, fontSize = 13.sp)
                    }
                    Spacer(Modifier.width(12.dp))
                    CircularProgressIndicator(Modifier.size(14.dp), strokeWidth = 1.6.dp, color = Accent)
                }
            }
        }
    }
}

@Composable
private fun MessageRow(message: ChatMessage, onExport: (WorkspaceFile) -> Unit) {
    val uriHandler = LocalUriHandler.current
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(9.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (message.role == "assistant") {
                Box(Modifier.size(27.dp).clip(RoundedCornerShape(9.dp)).background(Accent), contentAlignment = Alignment.Center) {
                    Text("c", color = Ink, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                }
                Spacer(Modifier.width(9.dp))
                Text("Kairo AI", color = PrimaryText, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Text("  ·  Gemini", color = MutedText, fontSize = 10.sp)
            } else {
                Spacer(Modifier.weight(1f))
                Text("SEN", color = MutedText, letterSpacing = 1.4.sp, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(7.dp))
                Box(Modifier.size(26.dp).clip(CircleShape).background(FieldColor), contentAlignment = Alignment.Center) {
                    Text("S", color = SecondaryText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        if (message.role == "user") {
            Column(Modifier.fillMaxWidth().padding(start = 20.dp).clip(RoundedCornerShape(17.dp, 4.dp, 17.dp, 17.dp))
                .background(Color(0xFF202329)).border(1.dp, Stroke.copy(alpha = .55f), RoundedCornerShape(17.dp, 4.dp, 17.dp, 17.dp)).padding(14.dp)) {
                SelectionContainer { MarkdownText(message.content, PrimaryText, 14.sp) }
                if (message.attachments.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    message.attachments.forEach { FileChip(it.name, it.mimeType, false) }
                }
            }
        } else {
            Column(Modifier.fillMaxWidth().padding(start = 3.dp, end = 4.dp)) {
                val generatedFiles = message.attachments.filter { it.origin == "generated" }
                val visibleBody = if (generatedFiles.isEmpty()) message.content else {
                    var artifactIndex = 0
                    fencedCodeRegex.replace(message.content) {
                        val artifact = generatedFiles.getOrNull(artifactIndex++)
                        if (artifact != null) "\nDosya oluşturuldu: **${artifact.name}**. Dosya kartı aşağıda.\n" else it.value
                    }
                }
                SelectionContainer {
                    MarkdownText(visibleBody, if (message.isError) Danger else Color(0xFFE3E6EA), 14.sp)
                }
                if (message.attachments.isNotEmpty()) {
                    Spacer(Modifier.height(10.dp))
                    message.attachments.filter { it.origin == "generated" }.forEach { file -> GeneratedFileCard(file, onExport) }
                }
                if (message.sources.isNotEmpty()) {
                    Spacer(Modifier.height(11.dp))
                    Text("KAYNAKLAR", color = MutedText, fontSize = 9.sp, letterSpacing = 1.5.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    message.sources.forEachIndexed { i, source ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 3.dp).clip(RoundedCornerShape(9.dp)).background(CardColor)
                            .clickable { runCatching { uriHandler.openUri(source.url) } }.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(24.dp).clip(RoundedCornerShape(7.dp)).background(FieldColor), contentAlignment = Alignment.Center) {
                                Icon(Icons.Outlined.Language, null, tint = Accent, modifier = Modifier.size(13.dp))
                            }
                            Spacer(Modifier.width(9.dp))
                            Column(Modifier.weight(1f)) {
                                Text(source.title, color = PrimaryText, fontSize = 12.sp, maxLines = 2)
                                Text(source.url.substringAfter("//").substringBefore('/'), color = MutedText, fontSize = 10.sp, maxLines = 1)
                            }
                            Text("${i + 1}", color = MutedText, fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }
}


private val fencedCodeRegex = Regex("```([^\\n`]*)\\n([\\s\\S]*?)```")

@Composable
private fun MarkdownText(content: String, color: Color, fontSize: androidx.compose.ui.unit.TextUnit) {
    val formatted = remember(content, color, fontSize) {
        buildAnnotatedString {
            content.lines().forEachIndexed { index, rawLine ->
                if (index > 0) append("\n")
                val line = rawLine.trimStart()
                when {
                    line.startsWith("### ") -> withStyle(SpanStyle(fontWeight = FontWeight.SemiBold, fontSize = fontSize * 1.05f)) { appendInlineMarkdown(line.removePrefix("### ")) }
                    line.startsWith("## ") -> withStyle(SpanStyle(fontWeight = FontWeight.SemiBold, fontSize = fontSize * 1.12f)) { appendInlineMarkdown(line.removePrefix("## ")) }
                    line.startsWith("# ") -> withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = fontSize * 1.2f)) { appendInlineMarkdown(line.removePrefix("# ")) }
                    line.startsWith("> ") -> withStyle(SpanStyle(color = SecondaryText, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)) { appendInlineMarkdown(line.removePrefix("> ")) }
                    line.startsWith("- ") || line.startsWith("* ") -> { append("•  "); appendInlineMarkdown(line.drop(2)) }
                    else -> appendInlineMarkdown(rawLine)
                }
            }
        }
    }
    Text(formatted, color = color, fontSize = fontSize, lineHeight = fontSize * 1.62f)
}

private fun AnnotatedString.Builder.appendInlineMarkdown(line: String) {
    val expression = Regex("\\*\\*(.+?)\\*\\*|`([^`]+)`")
    var cursor = 0
    expression.findAll(line).forEach { match ->
        if (match.range.first > cursor) append(line.substring(cursor, match.range.first))
        val full = match.value
        if (full.startsWith("**")) {
            withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) { append(full.removePrefix("**").removeSuffix("**")) }
        } else {
            withStyle(SpanStyle(fontFamily = FontFamily.Monospace, color = Accent, background = FieldColor)) {
                append(" ${full.removePrefix("`").removeSuffix("`")} ")
            }
        }
        cursor = match.range.last + 1
    }
    if (cursor < line.length) append(line.substring(cursor))
}

@Composable
private fun GeneratedFileCard(file: WorkspaceFile, onExport: (WorkspaceFile) -> Unit) {
    val extension = file.name.substringAfterLast('.', "FILE").uppercase(Locale.ROOT).take(6)
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp).clip(RoundedCornerShape(13.dp))
        .background(Panel).border(1.dp, Stroke, RoundedCornerShape(13.dp)).padding(11.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(39.dp).clip(RoundedCornerShape(10.dp)).background(Accent.copy(alpha = .14f)), contentAlignment = Alignment.Center) {
            Text(extension, color = Accent, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(file.name, color = PrimaryText, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 2)
            Text("Dosya hazır · ${formatBytes(file.size)}", color = MutedText, fontSize = 10.sp)
        }
        TextButton(onClick = { onExport(file) }) { Text("Dışa aktar", color = Accent, fontSize = 11.sp) }
    }
}

@Composable
private fun EmptyLanding(onNewChat: () -> Unit, onQuickPrompt: (String) -> Unit = { onNewChat() }) {
    Column(Modifier.fillMaxSize().padding(horizontal = 25.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.Start, verticalArrangement = Arrangement.Center) {
        Box(Modifier.size(58.dp).clip(RoundedCornerShape(19.dp)).background(Accent), contentAlignment = Alignment.Center) {
            Text("c", color = Ink, fontSize = 36.sp, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(21.dp))
        Text("Fikirden, çalışan projeye.", color = PrimaryText, fontFamily = FontFamily.Serif, fontSize = 31.sp, lineHeight = 36.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(9.dp))
        Text("Kod yaz, dosya incele, araştırma yap. Her sohbetin kendi dosyaları ve çalışma alanı olsun.", color = SecondaryText, fontSize = 14.sp, lineHeight = 22.sp, modifier = Modifier.widthIn(max = 380.dp))
        Spacer(Modifier.height(25.dp))
        Text("NEREDEN BAŞLAYALIM?", color = MutedText, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
        Spacer(Modifier.height(10.dp))
        val suggestions = listOf(
            "Kod incele" to "Eklediğim dosyaları incele ve önemli sorunları özetle.",
            "Bir proje tasarla" to "Bir Android uygulaması için mimari ve dosya yapısı öner.",
            "Hata ayıkla" to "Bir hata ayıklama planı oluştur. Gerekli dosyaları benden iste."
        )
        suggestions.forEach { (title, prompt) ->
            Row(Modifier.fillMaxWidth().padding(vertical = 4.dp).clip(RoundedCornerShape(13.dp)).background(Panel)
                .border(1.dp, Stroke.copy(alpha = .8f), RoundedCornerShape(13.dp)).clickable { onQuickPrompt(prompt) }.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(title, color = PrimaryText, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Text(prompt, color = MutedText, fontSize = 11.sp, maxLines = 2, lineHeight = 16.sp)
                }
                Icon(Icons.Outlined.ChevronRight, null, tint = MutedText, modifier = Modifier.size(19.dp))
            }
        }
        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.Tune, null, tint = Accent, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(7.dp))
            Text("Başlamak için Ayarlar bölümüne Gemini API anahtarını ekle.", color = MutedText, fontSize = 11.sp)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Composer(
    text: String, onTextChange: (String) -> Unit, pendingFiles: List<WorkspaceFile>, webSearch: Boolean,
    onWebToggle: () -> Unit, onAttach: () -> Unit, onRemovePending: (WorkspaceFile) -> Unit,
    onSend: () -> Unit, enabled: Boolean
) {
    val focusManager = LocalFocusManager.current
    Column(Modifier.fillMaxWidth().background(Ink).imePadding().padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 8.dp)) {
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(Panel).border(1.dp, Stroke, RoundedCornerShape(22.dp)).padding(10.dp)) {
            if (pendingFiles.isNotEmpty()) {
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    pendingFiles.forEach { file ->
                        Surface(color = FieldColor, shape = RoundedCornerShape(10.dp)) {
                            Row(Modifier.padding(start = 8.dp, end = 4.dp, top = 5.dp, bottom = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.Description, null, tint = Accent, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(5.dp))
                                Text(file.name, color = PrimaryText, fontSize = 10.sp, maxLines = 1)
                                IconButton(onClick = { onRemovePending(file) }, modifier = Modifier.size(22.dp)) { Icon(Icons.Outlined.Close, null, tint = MutedText, modifier = Modifier.size(13.dp)) }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(7.dp))
            }
            BasicTextField(
                value = text, onValueChange = onTextChange, modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp),
                textStyle = TextStyle(color = PrimaryText, fontSize = 14.sp, lineHeight = 21.sp),
                cursorBrush = androidx.compose.ui.graphics.SolidColor(Accent),
                decorationBox = { inner -> Box { if (text.isEmpty()) Text("Bir görev tanımla veya soru sor…", color = MutedText, fontSize = 14.sp); inner() } },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { focusManager.clearFocus(); onSend() })
            )
            Spacer(Modifier.height(6.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onAttach, modifier = Modifier.size(34.dp)) { Icon(Icons.Outlined.AttachFile, "Dosya ekle", tint = SecondaryText, modifier = Modifier.size(18.dp)) }
                Surface(onClick = onWebToggle, color = if (webSearch) Accent.copy(alpha = .15f) else Color.Transparent,
                    shape = RoundedCornerShape(9.dp), border = if (webSearch) androidx.compose.foundation.BorderStroke(1.dp, Accent.copy(alpha = .45f)) else null) {
                    Row(Modifier.padding(horizontal = 9.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Language, null, tint = if (webSearch) Accent else MutedText, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(5.dp))
                        Text("Web", color = if (webSearch) Accent else MutedText, fontSize = 11.sp)
                    }
                }
                Spacer(Modifier.weight(1f))
                Text("${pendingFiles.size} ek", color = MutedText, fontSize = 10.sp)
                Spacer(Modifier.width(8.dp))
                FilledIconButton(onClick = { focusManager.clearFocus(); onSend() }, enabled = enabled && (text.isNotBlank() || pendingFiles.isNotEmpty()),
                    modifier = Modifier.size(37.dp), colors = IconButtonDefaults.filledIconButtonColors(containerColor = Accent, contentColor = Ink, disabledContainerColor = FieldColor, disabledContentColor = MutedText)) {
                    Icon(Icons.AutoMirrored.Outlined.Send, "Gönder", modifier = Modifier.size(17.dp))
                }
            }
        }
        Text("Kairo yanıtları hata içerebilir. Kritik değişiklikleri uygulamadan önce gözden geçir.", color = MutedText, fontSize = 9.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 7.dp, bottom = 1.dp))
    }
}

@Composable
private fun ProjectsScreen(
    projects: List<WorkspaceProject>, chats: List<ChatSession>, onNewProject: () -> Unit,
    onOpenProject: (WorkspaceProject) -> Unit, onOpenChat: (ChatSession) -> Unit
) {
    Column(Modifier.fillMaxSize().padding(horizontal = 17.dp, vertical = 15.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Projelerin", color = PrimaryText, fontFamily = FontFamily.Serif, fontSize = 26.sp)
                Text("Her proje kendi sohbetlerini toplar; her sohbetin dosyaları ayrı kalır.", color = SecondaryText, fontSize = 12.sp, lineHeight = 18.sp)
            }
            FilledIconButton(onClick = onNewProject, colors = IconButtonDefaults.filledIconButtonColors(containerColor = Accent, contentColor = Ink)) { Icon(Icons.Outlined.Add, "Proje ekle") }
        }
        Spacer(Modifier.height(18.dp))
        if (projects.isEmpty()) {
            InfoCard(icon = Icons.Outlined.Workspaces, title = "İlk projen", detail = "Kod tabanını, görev sohbetlerini ve proje bağlamını düzenli tutmak için yeni bir proje oluştur.")
            Spacer(Modifier.height(10.dp))
            Button(onClick = onNewProject, colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = Ink)) { Text("Proje oluştur") }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(11.dp), contentPadding = PaddingValues(bottom = 24.dp)) {
                items(projects, key = { it.id }) { project ->
                    val projectChats = chats.filter { it.projectId == project.id }.sortedByDescending { it.updatedAt }
                    Card(colors = CardDefaults.cardColors(containerColor = Panel), border = androidx.compose.foundation.BorderStroke(1.dp, Stroke), shape = RoundedCornerShape(17.dp)) {
                        Column(Modifier.fillMaxWidth().padding(15.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(projectColor(project.colorIndex).copy(alpha = .17f)), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Outlined.FolderOpen, null, tint = projectColor(project.colorIndex), modifier = Modifier.size(20.dp))
                                }
                                Spacer(Modifier.width(11.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(project.name, color = PrimaryText, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                    Text("${projectChats.size} sohbet · ${projectChats.sumOf { it.files.size }} dosya", color = MutedText, fontSize = 10.sp)
                                }
                                TextButton(onClick = { onOpenProject(project) }) { Text("Aç", color = Accent) }
                            }
                            if (project.description.isNotBlank()) {
                                Spacer(Modifier.height(10.dp)); Text(project.description, color = SecondaryText, fontSize = 12.sp)
                            }
                            if (projectChats.isNotEmpty()) {
                                Spacer(Modifier.height(9.dp)); Divider(color = Stroke.copy(alpha = .7f)); Spacer(Modifier.height(5.dp))
                                projectChats.take(3).forEach { chat ->
                                    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).clickable { onOpenChat(chat) }.padding(vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Outlined.History, null, tint = MutedText, modifier = Modifier.size(15.dp))
                                        Spacer(Modifier.width(8.dp)); Text(chat.title, color = SecondaryText, fontSize = 12.sp, maxLines = 1, modifier = Modifier.weight(1f))
                                        Icon(Icons.Outlined.ChevronRight, null, tint = MutedText, modifier = Modifier.size(15.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProjectDetailScreen(
    project: WorkspaceProject,
    chats: List<ChatSession>,
    onNewChat: () -> Unit,
    onOpenChat: (ChatSession) -> Unit,
    onAttach: () -> Unit,
    onExport: (WorkspaceFile) -> Unit,
    onDeleteFile: (WorkspaceFile) -> Unit
) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 17.dp, vertical = 15.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(project.name, color = PrimaryText, fontFamily = FontFamily.Serif, fontSize = 26.sp)
                Text("Proje dosyaları ve bu projeye bağlı sohbetler", color = SecondaryText, fontSize = 12.sp, lineHeight = 18.sp)
            }
            FilledIconButton(onClick = onNewChat, colors = IconButtonDefaults.filledIconButtonColors(containerColor = Accent, contentColor = Ink)) {
                Icon(Icons.Outlined.Add, "Bu projede sohbet oluştur")
            }
        }
        if (project.description.isNotBlank()) {
            Spacer(Modifier.height(9.dp)); Text(project.description, color = SecondaryText, fontSize = 12.sp)
        }
        Spacer(Modifier.height(15.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MetricCard("PROJE DOSYALARI", project.files.size.toString(), Modifier.weight(1f))
            MetricCard("SOHBET", chats.size.toString(), Modifier.weight(1f))
            MetricCard("SOHBET DOSYALARI", chats.sumOf { it.files.size }.toString(), Modifier.weight(1f))
        }
        Spacer(Modifier.height(17.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("PROJE DOSYALARI", color = MutedText, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.3.sp, modifier = Modifier.weight(1f))
            TextButton(onClick = onAttach) { Icon(Icons.Outlined.Add, null, tint = Accent, modifier = Modifier.size(15.dp)); Spacer(Modifier.width(4.dp)); Text("Dosya ekle", color = Accent, fontSize = 11.sp) }
        }
        if (project.files.isEmpty()) {
            InfoCard(Icons.Outlined.FolderOpen, "Ortak proje alanı boş", "Buradaki dosyalar proje düzeyinde saklanır. Tek tek sohbetlerin dosyaları ayrı çalışma alanlarında tutulmaya devam eder.")
        } else {
            project.files.sortedByDescending { it.createdAt }.forEach { file ->
                Row(Modifier.fillMaxWidth().padding(vertical = 4.dp).clip(RoundedCornerShape(13.dp)).background(Panel)
                    .border(1.dp, Stroke, RoundedCornerShape(13.dp)).padding(11.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(38.dp).clip(RoundedCornerShape(10.dp)).background(Color(0xFF14251F)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Outlined.Description, null, tint = Green, modifier = Modifier.size(20.dp))
                    }
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(file.name, color = PrimaryText, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 2)
                        Text("${formatBytes(file.size)} · Proje dosyası", color = MutedText, fontSize = 10.sp)
                    }
                    IconButton(onClick = { onExport(file) }, modifier = Modifier.size(36.dp)) { Icon(Icons.Outlined.UploadFile, "Dışa aktar", tint = Accent, modifier = Modifier.size(18.dp)) }
                    IconButton(onClick = { onDeleteFile(file) }, modifier = Modifier.size(36.dp)) { Icon(Icons.Outlined.DeleteOutline, "Proje alanından sil", tint = Danger, modifier = Modifier.size(18.dp)) }
                }
            }
        }
        Spacer(Modifier.height(18.dp))
        Text("SOHBETLER", color = MutedText, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.3.sp)
        Spacer(Modifier.height(6.dp))
        if (chats.isEmpty()) {
            Text("Bu projeye henüz sohbet bağlanmamış.", color = SecondaryText, fontSize = 12.sp)
        } else {
            chats.forEach { chat ->
                Row(Modifier.fillMaxWidth().padding(vertical = 4.dp).clip(RoundedCornerShape(11.dp)).background(Panel)
                    .clickable { onOpenChat(chat) }.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.History, null, tint = MutedText, modifier = Modifier.size(17.dp))
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(chat.title, color = PrimaryText, fontSize = 12.sp, maxLines = 1)
                        Text("${chat.messages.size} mesaj · ${chat.files.size} sohbet dosyası", color = MutedText, fontSize = 10.sp)
                    }
                    Icon(Icons.Outlined.ChevronRight, null, tint = MutedText, modifier = Modifier.size(17.dp))
                }
            }
        }
    }
}

@Composable
private fun WorkspaceScreen(chat: ChatSession, onAttach: () -> Unit, onExport: (WorkspaceFile) -> Unit, onDeleteFile: (WorkspaceFile) -> Unit) {
    Column(Modifier.fillMaxSize().padding(horizontal = 17.dp, vertical = 15.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Bu sohbetin dosyaları", color = PrimaryText, fontFamily = FontFamily.Serif, fontSize = 24.sp)
                Text("Başka sohbetlerin dosyalarından ayrıdır.", color = SecondaryText, fontSize = 12.sp)
            }
            FilledIconButton(onClick = onAttach, colors = IconButtonDefaults.filledIconButtonColors(containerColor = Accent, contentColor = Ink)) { Icon(Icons.Outlined.UploadFile, "Dosya ekle") }
        }
        Spacer(Modifier.height(18.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MetricCard("DOSYA", chat.files.size.toString(), Modifier.weight(1f))
            MetricCard("OLUŞTURULAN", chat.files.count { it.origin == "generated" }.toString(), Modifier.weight(1f))
            MetricCard("YÜKLENEN", chat.files.count { it.origin != "generated" }.toString(), Modifier.weight(1f))
        }
        Spacer(Modifier.height(17.dp))
        if (chat.files.isEmpty()) {
            InfoCard(Icons.Outlined.FolderOpen, "Çalışma alanı boş", "Dosya ekleyebilir veya sohbetten kod istediğinde oluşturulan dosyaları burada saklayabilirsin.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(bottom = 22.dp)) {
                items(chat.files.sortedByDescending { it.createdAt }, key = { it.id }) { file ->
                    var menuOpen by remember(file.id) { mutableStateOf(false) }
                    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Panel).border(1.dp, Stroke, RoundedCornerShape(14.dp)).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(42.dp).clip(RoundedCornerShape(11.dp)).background(if (file.origin == "generated") Accent.copy(alpha = .15f) else FieldColor), contentAlignment = Alignment.Center) {
                            Icon(Icons.Outlined.Description, null, tint = if (file.origin == "generated") Accent else SecondaryText, modifier = Modifier.size(21.dp))
                        }
                        Spacer(Modifier.width(11.dp))
                        Column(Modifier.weight(1f)) {
                            Text(file.name, color = PrimaryText, fontSize = 12.sp, fontWeight = FontWeight.Medium, maxLines = 2)
                            Spacer(Modifier.height(3.dp))
                            Text("${formatBytes(file.size)} · ${if (file.origin == "generated") "Kairo tarafından oluşturuldu" else "Yüklenen dosya"}", color = MutedText, fontSize = 10.sp)
                        }
                        Box {
                            IconButton(onClick = { menuOpen = true }) { Icon(Icons.Outlined.MoreHoriz, null, tint = SecondaryText) }
                            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }, modifier = Modifier.background(CardColor)) {
                                DropdownMenuItem(text = { Text("Cihaza dışa aktar") }, leadingIcon = { Icon(Icons.Outlined.UploadFile, null) }, onClick = { menuOpen = false; onExport(file) })
                                DropdownMenuItem(text = { Text("Çalışma alanından kaldır", color = Danger) }, leadingIcon = { Icon(Icons.Outlined.DeleteOutline, null, tint = Danger) }, onClick = { menuOpen = false; onDeleteFile(file) })
                            }
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Text("Dışa aktar", color = Accent, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        Text("Dosya menüsünden Android'in klasör seçicisiyle cihazındaki istediğin konuma kopyalayabilirsin.", color = MutedText, fontSize = 11.sp, lineHeight = 17.sp)
    }
}

@Composable
private fun SettingsScreen(
    apiKeyPresent: Boolean,
    modelName: String,
    thinkingLevel: String,
    islandEnabled: Boolean,
    soundEnabled: Boolean,
    liveModel: String,
    liveVoice: String,
    capsuleGeometry: CapsuleGeometry,
    overlayAllowed: Boolean,
    notificationsGranted: Boolean,
    microphoneGranted: Boolean,
    batteryUnrestricted: Boolean,
    onRequestNotifications: () -> Unit,
    onRequestMicrophone: () -> Unit,
    onOpenOverlaySettings: () -> Unit,
    onOpenBatterySettings: () -> Unit,
    onSaveCapsuleGeometry: (CapsuleGeometry) -> Unit,
    onSaveApiKey: (String?) -> Unit,
    onSaveModel: (String) -> Unit,
    onSaveThinking: (String) -> Unit,
    onSaveLive: (String, String) -> Unit,
    onToggleSounds: (Boolean) -> Unit,
    onToggleIsland: (Boolean) -> Unit
) {
    var apiKey by remember { mutableStateOf("") }
    var showKey by remember { mutableStateOf(false) }
    var model by remember(modelName) { mutableStateOf(modelName) }
    var currentLiveModel by remember(liveModel) { mutableStateOf(liveModel) }
    var currentVoice by remember(liveVoice) { mutableStateOf(liveVoice) }
    var deleteConfirm by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().padding(horizontal = 17.dp, vertical = 15.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(15.dp)) {
        Text("Kişiselleştirme", color = PrimaryText, fontSize = 27.sp, fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold)
        Text("BAĞLANTI VE GİZLİLİK", color = MutedText, fontSize = 10.sp, letterSpacing = 1.6.sp, fontWeight = FontWeight.Bold)
        Card(colors = CardDefaults.cardColors(containerColor = Panel), border = androidx.compose.foundation.BorderStroke(1.dp, Stroke), shape = RoundedCornerShape(17.dp)) {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(11.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(37.dp).clip(RoundedCornerShape(11.dp)).background(Color(0xFF14251F)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Outlined.Check, null, tint = Green, modifier = Modifier.size(19.dp))
                    }
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Gemini API", color = PrimaryText, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text(if (apiKeyPresent) "Anahtar cihazda şifreli saklanıyor" else "API anahtarı eklenmedi", color = if (apiKeyPresent) Green else SecondaryText, fontSize = 11.sp)
                    }
                    if (apiKeyPresent) Box(Modifier.size(7.dp).clip(CircleShape).background(Green))
                }
                OutlinedTextField(value = apiKey, onValueChange = { apiKey = it }, modifier = Modifier.fillMaxWidth(), singleLine = true,
                    label = { Text(if (apiKeyPresent) "Yeni API anahtarı" else "API anahtarı") },
                    placeholder = { Text("Gemini API anahtarını gir") }, visualTransformation = if (showKey) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = { TextButton(onClick = { showKey = !showKey }) { Text(if (showKey) "Gizle" else "Göster", color = Accent) } },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Accent, unfocusedBorderColor = Stroke))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { if (apiKey.isNotBlank()) { onSaveApiKey(apiKey.trim()); apiKey = "" } }, enabled = apiKey.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = Ink)) { Text("Anahtarı kaydet") }
                    if (apiKeyPresent) TextButton(onClick = { deleteConfirm = true }) { Text("Anahtarı sil", color = Danger) }
                }
                Text("Anahtar, Android Keystore'da tutulan AES-GCM anahtarıyla şifrelenir. Bu, cihaz tamamen ele geçirilirse anahtarın çıkarılamayacağını garanti etmez; üretim kullanımı için kendi aracılık sunucunu değerlendirmelisin.", color = MutedText, fontSize = 10.sp, lineHeight = 16.sp)
            }
        }
        Card(colors = CardDefaults.cardColors(containerColor = Panel), border = androidx.compose.foundation.BorderStroke(1.dp, Stroke), shape = RoundedCornerShape(17.dp)) {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Model ve akıl yürütme", color = PrimaryText, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text("Kullandığın Google AI hesabında erişimi bulunan model kimliğini seç. Düşünce özeti kullanıcıya görev durumu olarak gösterilir; gizli iç akıl yürütme metni gösterilmez.", color = SecondaryText, fontSize = 11.sp, lineHeight = 17.sp)
                OutlinedTextField(value = model, onValueChange = { model = it }, modifier = Modifier.fillMaxWidth(), singleLine = true, label = { Text("Metin modeli kimliği") },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Accent, unfocusedBorderColor = Stroke))
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp), verticalAlignment = Alignment.CenterVertically) {
                    listOf("gemini-4-argon", "gemini-3.8-flash").forEach { suggestion ->
                        Text(suggestion, color = if (model == suggestion) Ink else SecondaryText, fontSize = 10.sp,
                            modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(if (model == suggestion) Accent else FieldColor).clickable { model = suggestion }.padding(horizontal = 9.dp, vertical = 7.dp))
                    }
                    Spacer(Modifier.weight(1f))
                    Button(onClick = { onSaveModel(model.trim()) }, enabled = model.isNotBlank(), colors = ButtonDefaults.buttonColors(containerColor = FieldColor, contentColor = PrimaryText)) { Text("Kaydet") }
                }
                Divider(color = Stroke)
                Text("Akıl yürütme yoğunluğu", color = PrimaryText, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    listOf("low" to "Hızlı", "medium" to "Dengeli", "high" to "Derin").forEach { (id, label) ->
                        Text(label, color = if (thinkingLevel.equals(id, true)) Ink else SecondaryText, fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f).clip(RoundedCornerShape(10.dp)).background(if (thinkingLevel.equals(id, true)) Accent else FieldColor).clickable { onSaveThinking(id) }.padding(vertical = 10.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    }
                }
                Text("Not: model sağlayıcısı bu alanı desteklemiyorsa istek hata döndürebilir.", color = MutedText, fontSize = 10.sp)
            }
        }
        Card(colors = CardDefaults.cardColors(containerColor = Panel), border = androidx.compose.foundation.BorderStroke(1.dp, Stroke), shape = RoundedCornerShape(17.dp)) {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Gemini Live ses", color = PrimaryText, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                OutlinedTextField(value = currentLiveModel, onValueChange = { currentLiveModel = it }, modifier = Modifier.fillMaxWidth(), singleLine = true, label = { Text("Canlı ses model kimliği") }, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Accent, unfocusedBorderColor = Stroke))
                OutlinedTextField(value = currentVoice, onValueChange = { currentVoice = it }, modifier = Modifier.fillMaxWidth(), singleLine = true, label = { Text("Ses adı (ör. Kore, Puck, Aoede)") }, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Accent, unfocusedBorderColor = Stroke))
                Button(onClick = { onSaveLive(currentLiveModel.trim(), currentVoice.trim()) }, enabled = currentLiveModel.isNotBlank() && currentVoice.isNotBlank(), colors = ButtonDefaults.buttonColors(containerColor = FieldColor, contentColor = PrimaryText)) { Text("Canlı ses tercihlerini kaydet") }
                Text("Mikrofon yalnızca sen sesli sohbeti başlattığında etkinleşir. Devam eden sistem bildirimi görüşmeyi bitirme düğmesi içerir.", color = MutedText, fontSize = 10.sp, lineHeight = 16.sp)
            }
        }
        Card(colors = CardDefaults.cardColors(containerColor = Panel), border = androidx.compose.foundation.BorderStroke(1.dp, Stroke), shape = RoundedCornerShape(17.dp)) {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Arayüz ve sistem", color = PrimaryText, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) { Text("Etkinlik kapsülü", color = PrimaryText, fontSize = 13.sp); Text("Diğer uygulamaların üzerinde kayan görev durumu", color = SecondaryText, fontSize = 10.sp) }
                    Switch(checked = islandEnabled, onCheckedChange = onToggleIsland)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) { Text("Durum sesleri", color = PrimaryText, fontSize = 13.sp); Text("Gönderim, başarı, hata ve ekleme sesleri", color = SecondaryText, fontSize = 10.sp) }
                    Switch(checked = soundEnabled, onCheckedChange = onToggleSounds)
                }
                Divider(color = Stroke)
                SettingBullet("Sohbetler, projeler, notlar ve dosyalar cihazda yerel olarak saklanır.")
                SettingBullet("Web arama kaynakları destek sağlandığında yanıtın altında gösterilir.")
                SettingBullet("E-posta entegrasyonu taslağı yerel posta uygulamasına hazırlar; otomatik gönderim yapmaz.")
                SettingBullet("Kripto ekranı genel piyasa fiyatlarını gösterir; alım-satım veya cüzdan erişimi yoktur.")
                SettingBullet("Sistem üstü kapsül Android'in özel iznine ve üretici arka plan politikalarına bağlıdır.")
            }
        }
        CapsuleSettingsSection(
            savedGeometry = capsuleGeometry,
            overlayAllowed = overlayAllowed,
            capsuleEnabled = islandEnabled,
            onToggleCapsule = onToggleIsland,
            onOpenOverlaySettings = onOpenOverlaySettings,
            onApplyGeometry = onSaveCapsuleGeometry
        )
        Card(colors = CardDefaults.cardColors(containerColor = Panel), border = androidx.compose.foundation.BorderStroke(1.dp, Stroke), shape = RoundedCornerShape(17.dp)) {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("İzin yöneticisi", color = PrimaryText, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                PermissionSettingRow("Bildirimler", if (notificationsGranted) "Etkin" else "Kapalı", notificationsGranted, "Aç", onRequestNotifications)
                PermissionSettingRow("Mikrofon", if (microphoneGranted) "Etkin" else "Kapalı", microphoneGranted, "İzin ver", onRequestMicrophone)
                PermissionSettingRow("Ekran üstü kapsül", if (overlayAllowed) "Etkin" else "Kapalı", overlayAllowed, "Sistem ayarları", onOpenOverlaySettings)
                PermissionSettingRow("Pil arka plan kısıtlaması", if (batteryUnrestricted) "Kısıtlama yok" else "Üreticiye bağlı", batteryUnrestricted, "Pil ayarları", onOpenBatterySettings)
                Text("İzinler yalnızca ilgili özelliği kullanmak için gereklidir. Android'in dosya seçicisiyle seçilen dosyalar için tüm depolama erişimi istenmez.", color = MutedText, fontSize = 10.sp, lineHeight = 16.sp)
            }
        }
        Card(colors = CardDefaults.cardColors(containerColor = Panel), border = androidx.compose.foundation.BorderStroke(1.dp, Stroke), shape = RoundedCornerShape(17.dp)) {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Kairo AI", color = PrimaryText, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                Text("Android için yerel çalışma alanı ve yapay zekâ yardımcı uygulaması", color = SecondaryText, fontSize = 11.sp)
                Text("Geliştirici: AltayHR & Muhammed", color = Accent, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Text("Bu port, macOS yeteneklerini Android'in izin ve servis modeliyle uyarlamayı amaçlar; dış servislerin uç noktaları, hesap yetkileri ve işletim sistemi davranışı gerçek cihazda doğrulanmalıdır.", color = MutedText, fontSize = 10.sp, lineHeight = 16.sp)
            }
        }
    }
    if (deleteConfirm) AlertDialog(onDismissRequest = { deleteConfirm = false }, containerColor = CardColor,
        title = { Text("API anahtarı silinsin mi?") }, text = { Text("Anahtar cihazdan kaldırılır. Gemini yanıtları için yeniden eklemen gerekir.", color = SecondaryText) },
        confirmButton = { TextButton(onClick = { onSaveApiKey(null); apiKey = ""; deleteConfirm = false }) { Text("Sil", color = Danger) } },
        dismissButton = { TextButton(onClick = { deleteConfirm = false }) { Text("Vazgeç") } })
}

@Composable
private fun PermissionSettingRow(title: String, status: String, granted: Boolean, buttonLabel: String, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, color = PrimaryText, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Text(status, color = if (granted) Green else SecondaryText, fontSize = 10.sp)
        }
        TextButton(onClick = onClick) { Text(buttonLabel, color = Accent, fontSize = 10.sp) }
    }
}

@Composable
private fun InfoCard(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, detail: String) {
    Card(colors = CardDefaults.cardColors(containerColor = Panel), border = androidx.compose.foundation.BorderStroke(1.dp, Stroke), shape = RoundedCornerShape(17.dp)) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Icon(icon, null, tint = Accent, modifier = Modifier.size(24.dp))
            Text(title, color = PrimaryText, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Text(detail, color = SecondaryText, fontSize = 12.sp, lineHeight = 19.sp)
        }
    }
}

@Composable
private fun MetricCard(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier.clip(RoundedCornerShape(12.dp)).background(Panel).border(1.dp, Stroke, RoundedCornerShape(12.dp)).padding(10.dp)) {
        Text(label, color = MutedText, fontSize = 8.sp, letterSpacing = .8.sp)
        Spacer(Modifier.height(5.dp)); Text(value, color = PrimaryText, fontSize = 21.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun FileChip(name: String, mimeType: String, generated: Boolean) {
    Row(Modifier.padding(top = 4.dp).clip(RoundedCornerShape(8.dp)).background(if (generated) Accent.copy(alpha = .10f) else FieldColor).padding(horizontal = 8.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Outlined.Description, null, tint = if (generated) Accent else SecondaryText, modifier = Modifier.size(13.dp))
        Spacer(Modifier.width(5.dp)); Text(name, color = if (generated) Accent else SecondaryText, fontSize = 10.sp, maxLines = 1)
    }
}

@Composable
private fun SettingBullet(text: String) {
    Row(verticalAlignment = Alignment.Top) {
        Box(Modifier.padding(top = 6.dp).size(5.dp).clip(CircleShape).background(Accent))
        Spacer(Modifier.width(8.dp)); Text(text, color = SecondaryText, fontSize = 11.sp, lineHeight = 17.sp)
    }
}

private fun projectColor(index: Int): Color = when (index % 5) {
    0 -> Accent
    1 -> Color(0xFF8B9DFF)
    2 -> Color(0xFF54D6C7)
    3 -> Color(0xFFE89BC6)
    else -> Color(0xFFF4C66A)
}

fun saveCodeArtifacts(context: Context, store: WorkspaceStore, chatId: String, response: String): List<WorkspaceFile> {
    val regex = Regex("```([^\\n`]*)\\n([\\s\\S]*?)```")
    val blocks = regex.findAll(response).toList()
    val files = mutableListOf<WorkspaceFile>()
    blocks.forEachIndexed { index, match ->
        val header = match.groupValues[1].trim()
        val code = match.groupValues[2].trimEnd()
        if (code.isBlank()) return@forEachIndexed
        val headerParts = header.split(Regex("\\s+"), limit = 2)
        val language = headerParts.firstOrNull().orEmpty().lowercase(Locale.ROOT)
        val rawName = headerParts.getOrNull(1)?.trim()?.trim('`', '"', '\'')
            ?: header.takeIf { it.contains('.') && !it.contains(' ') }
        val possibleName = rawName?.substringAfterLast('/')?.takeIf { it.contains('.') && !it.startsWith('.') }
        val extension = possibleName?.substringAfterLast('.') ?: when (language) {
            "kotlin", "kt" -> "kt"; "kts" -> "kts"; "swift" -> "swift"; "java" -> "java"; "python", "py" -> "py"
            "javascript", "js" -> "js"; "typescript", "ts" -> "ts"; "tsx" -> "tsx"; "jsx" -> "jsx"; "html" -> "html"
            "css" -> "css"; "json" -> "json"; "xml" -> "xml"; "sql" -> "sql"; "bash", "sh", "shell" -> "sh"
            "yaml", "yml" -> "yaml"; "markdown", "md" -> "md"; "rust", "rs" -> "rs"; "go" -> "go"
            "c" -> "c"; "cpp", "c++" -> "cpp"; "gradle" -> "gradle"; "toml" -> "toml"; "text", "txt" -> "txt"
            else -> "txt"
        }
        val name = possibleName ?: "kairo-output-${System.currentTimeMillis()}-${index + 1}.$extension"
        val stored = store.storeFile(chatId, name, mimeFromName(name), code.toByteArray(Charsets.UTF_8), "generated")
        files += stored
    }
    return files
}

private fun readLimited(input: java.io.InputStream, maxBytes: Int): ByteArray {
    val output = java.io.ByteArrayOutputStream()
    val buffer = ByteArray(16 * 1024)
    var total = 0
    while (true) {
        val count = input.read(buffer)
        if (count < 0) break
        total += count
        require(total <= maxBytes) { "Dosya sınırı 25 MB. Daha küçük bir dosya seç." }
        output.write(buffer, 0, count)
    }
    return output.toByteArray()
}

private fun displayName(context: Context, uri: Uri): String {
    val cursor = context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
    val queriedName = cursor?.use { c -> if (c.moveToFirst()) c.getString(0) else null }
    if (!queriedName.isNullOrBlank()) return queriedName
    return uri.lastPathSegment?.substringAfterLast('/') ?: "ek-dosya"
}

private fun mimeFromName(name: String): String = when (name.substringAfterLast('.', "").lowercase(Locale.ROOT)) {
    "txt", "md", "log", "csv", "kt", "kts", "swift", "java", "py", "js", "ts", "html", "css", "sql", "sh", "xml", "yaml", "yml", "toml", "gradle", "properties", "c", "h", "cpp", "rs", "go" -> "text/plain"
    "json" -> "application/json"; "pdf" -> "application/pdf"; "png" -> "image/png"; "jpg", "jpeg" -> "image/jpeg"; "webp" -> "image/webp"
    "gif" -> "image/gif"; "mp3" -> "audio/mpeg"; "wav" -> "audio/wav"; "m4a" -> "audio/mp4"; "mp4" -> "video/mp4"; "zip" -> "application/zip"
    else -> "application/octet-stream"
}

private fun formatBytes(size: Long): String = when {
    size < 1024 -> "$size B"
    size < 1024 * 1024 -> String.format(Locale.ROOT, "%.1f KB", size / 1024.0)
    else -> String.format(Locale.ROOT, "%.1f MB", size / (1024.0 * 1024.0))
}
