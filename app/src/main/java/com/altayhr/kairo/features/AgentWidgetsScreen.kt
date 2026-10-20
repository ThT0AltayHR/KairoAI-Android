package com.altayhr.kairo.features

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountTree
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.DataObject
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.NetworkCheck
import androidx.compose.material.icons.outlined.PendingActions
import androidx.compose.material.icons.outlined.Preview
import androidx.compose.material.icons.outlined.Rule
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.altayhr.kairo.data.AgentRunRecord
import com.altayhr.kairo.data.AgentRunStore
import com.altayhr.kairo.terminal.EmbeddedTerminalEngine
import com.altayhr.kairo.terminal.EmbeddedCommandRecord
import kotlinx.coroutines.delay

private val WInk = Color(0xFF090A10)
private val WPanel = Color(0xFF12141D)
private val WCard = Color(0xFF191C27)
private val WStroke = Color(0xFF2B3040)
private val WText = Color(0xFFF4F5F7)
private val WSecond = Color(0xFF9DA5B5)
private val WMuted = Color(0xFF747D90)
private val WAccent = Color(0xFFAA9BFF)
private val WGreen = Color(0xFF66D6C0)
private val WGold = Color(0xFFFFC86B)
private val WRed = Color(0xFFFF7D88)

private data class AgentWidgetDefinition(
    val id: String,
    val title: String,
    val category: String,
    val description: String,
    val icon: ImageVector,
    val prompt: String,
    val preview: List<String>
)

private val agentWidgets = listOf(
    AgentWidgetDefinition("commands", "Komut akışı", "GELİŞTİRME", "Komut önizlemelerini ve gerçek ajan aşamalarını ağaç görünümünde takip et.", Icons.Outlined.Terminal,
        "Bu görev için bir komut/iş akışı taslağı üret. Komutları terminalde çalıştırılmış gibi gösterme. Her satırda amaç, risk, beklenen çıktı ve çalıştırma ön koşulunu belirt.", listOf("İstek analiz ediliyor", "Planlama ajanı adımları hazırlıyor", "npm run build · öneri", "./gradlew assembleRelease · öneri", "git status --short · kontrol önerisi")),
    AgentWidgetDefinition("progress", "Proje ilerlemesi", "PROJE", "Gerçek görev kaydındaki durumu ve sonraki kilometre taşını özetle.", Icons.Outlined.Timeline,
        "Proje için doğrulanabilir kilometre taşları ve bağımlılıkları üret. Ölçüm verisi yoksa yüzde veya kalan süre uydurma.", listOf("Görev kapsamı", "Bağımlılıklar", "Kilometre taşları", "Kabul ölçütleri")),
    AgentWidgetDefinition("agents", "Ajan filosu", "AJANLAR", "Planlama, kodlama, inceleme ve entegrasyon rolleri için görev dağılımı.", Icons.Outlined.AccountTree,
        "Bu görevi Kairo'nun uzman ajan rollerine böl: ana dağıtıcı, planlama, kodlama, terminal hazırlığı, hata ayıklama, entegrasyon, sohbet ve kapsül/widget raporlama. Her rol için girdi ve teslim çıktısını belirt; çalıştırılmayan işleri tamamlandı işaretleme.", listOf("Ana ajan · dağıtım", "Planlama ajanı", "Kodlama ajanı", "İnceleme ajanı")),
    AgentWidgetDefinition("build", "Gradle derlemesi", "DERLEME", "Android derleme ön kontrol listesi ve gerçek sonuçların ayrımı.", Icons.Outlined.Code,
        "Android Gradle derlemesi için SDK, JDK, bağımlılık, lint, test ve release imza ön kontrollerini hazırla. Derleme gerçekten çalıştırılmadıysa başarılı deme.", listOf("JDK / Android SDK", "Gradle sync", "test + lint", "assembleDebug / assembleRelease")),
    AgentWidgetDefinition("npm", "NPM iş akışı", "GELİŞTİRME", "Node projeleri için paket, test ve build komutlarını planla.", Icons.Outlined.DataObject,
        "Bu projeye uygun npm komutlarını package.json dosyasına göre tasarla. package.json görülmediyse genel öneri olduğunu belirt; komutları çalıştırma.", listOf("npm install · koşula bağlı", "npm run test · script varsa", "npm run build · script varsa", "çıktı dosyalarını kontrol et")),
    AgentWidgetDefinition("release", "GitHub Release", "YAYIN", "Etiket, imza, APK/AAB ve checksum yayın kontrolü.", Icons.Outlined.UploadFile,
        "GitHub Release pipeline'ı incele veya tasarla: tag doğrulaması, secrets, APK imzası, SHA-256 ve release asset kontrolü. İmza sırrını kaynak dosyaya yazma.", listOf("Sürüm etiketi", "CI ve test", "APK imza doğrulama", "SHA-256 ve Release")),
    AgentWidgetDefinition("expo", "Expo build", "YAYIN", "Expo/EAS derleme adımlarını, profilleri ve çıktı doğrulamasını hazırla.", Icons.Outlined.CloudDownload,
        "Expo/EAS build için app config, build profile, credentials, platform seçimi ve çıktı doğrulamasını planla. Gerçek EAS erişiminin olup olmadığını ayrı belirt.", listOf("app.json / app.config", "eas.json profili", "credential kontrolü", "APK/AAB indirme ve doğrulama")),
    AgentWidgetDefinition("download", "İndirme takibi", "DOSYALAR", "İndirme görevlerinin yüzde, boyut ve bütünlük kontrolü için görünür plan.", Icons.Outlined.CloudDownload,
        "İndirme akışı tasarla: kaynağı doğrula, boyut/yüzde ilerlemesini ölç, iptali destekle, tamamlandığında SHA-256 ve içerik tipini denetle. Gerçek indirme verisi yoksa yüzde uydurma.", listOf("Kaynak URL doğrulaması", "İndirme başlatma", "Gerçek byte/yüzde", "Checksum ve hedef konum")),
    AgentWidgetDefinition("files", "Dosya denetimi", "DOSYALAR", "Eksik, yetim veya birbirine uymayan dosya referanslarını denetle.", Icons.Outlined.FolderOpen,
        "Bu proje dosyaları için manifest/resource/import/referans tutarlılığı denetim planı çıkar; erişemediğin dosyalar için kontrol edildi deme.", listOf("Dosya envanteri", "Import / referans", "Manifest ve kaynaklar", "Eksik dosya raporu")),
    AgentWidgetDefinition("debug", "Hata ayıklama", "KALİTE", "Hata belirtilerinden kök neden ve güvenli düzeltme önerileri üret.", Icons.Outlined.BugReport,
        "Hata ayıklama planı oluştur. Bulguları kanıt, olası neden, düzeltme ve regresyon testi olarak ayır. Log yoksa varsayımları açıkça söyle.", listOf("Hata belirtisi", "Kök neden hipotezleri", "Düzeltme önerisi", "Regresyon testi")),
    AgentWidgetDefinition("tests", "Test laboratuvarı", "KALİTE", "Birim, entegrasyon, görsel ve regresyon testlerini takip et.", Icons.Outlined.Rule,
        "Görev için bir test matrisi oluştur: birim, entegrasyon, izin/arka plan, hata senaryoları ve kabul ölçütleri. Çalıştırılmış test sonuçlarıyla önerilen testleri ayır.", listOf("Birim testleri", "Entegrasyon testleri", "İzin/arka plan", "Sonuç ve regresyon")),
    AgentWidgetDefinition("security", "Güvenlik nöbetçisi", "GÜVENLİK", "Token, log, dış URL, yetki ve izin kullanımını gözden geçir.", Icons.Outlined.Security,
        "Bu özellik için secret sızıntısı, URL doğrulaması, OAuth state/PKCE, log redaksiyonu ve en az ayrıcalık açısından güvenlik incelemesi yap.", listOf("Secret/log taraması", "OAuth ve izinler", "URL / ağ güvenliği", "Risk önem derecesi")),
    AgentWidgetDefinition("review", "Kod incelemesi", "KALİTE", "Mimari, okunabilirlik, hata yönetimi ve kaynak kullanımı.", Icons.Outlined.Preview,
        "Kod incelemesi yap: gerçek hataları öncele, dosya ve satır bağlamını destekle, performans ve erişilebilirliği de değerlendir. Değişiklik yapılmadıysa yapılmış gibi sunma.", listOf("Kritik hatalar", "Bakım kolaylığı", "Erişilebilirlik", "Önerilen patch")),
    AgentWidgetDefinition("integrations", "Entegrasyon durumu", "BAĞLANTILAR", "API uç noktaları, token kapsamları ve entegrasyon sınırları.", Icons.Outlined.Language,
        "İstenen entegrasyonları sunucu olmadan cihaz içinden hangi akışların yapabileceğini, hangi OAuth sağlayıcısının client secret/backend istediğini ayırarak planla.", listOf("Kimlik doğrulama", "API kapsamları", "Hata / hız sınırı", "Geri alma yolu")),
    AgentWidgetDefinition("spotify", "Spotify canlı durumu", "MEDYA", "Spotify bağlantı ve oynatma durumunu açıklayan AI yardımcı kartı.", Icons.Outlined.MusicNote,
        "Spotify bağlantısının güvenli kontrol listesi ve hata teşhisini hazırla. Gerçek oynatma API yanıtına erişimin yoksa mevcut şarkı çalıyor iddiasında bulunma.", listOf("OAuth / token", "Aktif cihaz", "Oynatma kontrolü", "Kapsül / widget durumu")),
    AgentWidgetDefinition("network", "Ağ ve çevrimdışı", "SİSTEM", "Bağlantı kaybı, tekrar bağlanma ve kuyruk davranışını gözden geçir.", Icons.Outlined.NetworkCheck,
        "Ağ kesintisi için çevrimdışı davranış tasarla: yerel kayıt, başarısız istek kuyruğu, idempotency, geri deneme ve tekrar bağlanma bildirimi. İnternet testi yapılmadıysa çevrimiçi olduğunu varsayma.", listOf("Ağ doğrulama", "Yerel kayıt", "Bekleyen istek", "Yeniden bağlantı")),
    AgentWidgetDefinition("context", "Bağlam penceresi", "MODEL", "İstek boyutu, dosya bağlamı ve yanıt bütçesi için AI tahmini.", Icons.Outlined.Insights,
        "Bu görev için hangi dosya ve bağlamın gerekli olduğunu, bağlam maliyetini ve hangi bilgilerin eksik olduğunu özetle. Ölçülmemiş token sayısını gerçek ölçüm gibi sunma.", listOf("Gerekli bağlam", "Dosya önceliği", "Tahmini token bütçesi", "Eksik bilgi")),
    AgentWidgetDefinition("prompt", "İstem atölyesi", "MODEL", "İsteği açık kabul ölçütlerine ve aşamalara dönüştür.", Icons.Outlined.AutoAwesome,
        "Kullanıcının isteğini iyi bir görev istemine dönüştür: hedef, kapsam, kapsam dışı, bağımlılıklar, teslim dosyaları, kabul ölçütleri ve riskler. Bilinmeyenleri belirt.", listOf("Hedef", "Kapsam", "Kabul ölçütleri", "Teslim biçimi")),
    AgentWidgetDefinition("approvals", "Onay kuyruğu", "GÜVENLİK", "Yıkıcı, dış servise veri gönderen veya yayın yapan adımlar için onay kontrolü.", Icons.Outlined.PendingActions,
        "Bu görevde kullanıcının açık onayını gerektiren adımları belirle. Dosya silme, yayınlama, ücretli API, mesaj gönderme ve dış servise secret gönderme için güvenli onay kapıları yaz.", listOf("Onay gerektiren adım", "Etki kapsamı", "Geri alma", "Kullanıcı onayı")),
    AgentWidgetDefinition("delivery", "Teslim kartları", "TESLİM", "Dosya adı, uzantı, hedef, checksum ve teslim notunu hazırla.", Icons.Outlined.CheckCircle,
        "Teslim edilecek çıktı kartlarını tanımla: dosya adı, uzantı, kısa içerik özeti, hedef, SHA-256 gerektiğinde checksum ve doğrulama durumu. Var olmayan dosyaya link üretme.", listOf("Çıktı listesi", "Uzantı ve boyut", "Kaydetme hedefi", "Teslim doğrulaması")),
    AgentWidgetDefinition("activity", "Etkinlik günlüğü", "SİSTEM", "Ajan aşamalarını ve sonuçlarını tarih sırasıyla özetle.", Icons.Outlined.Schedule,
        "Görev günlüğü için kişisel veriyi ve secret değerlerini dışarıda bırakan olay şeması öner: zaman, ajan rolü, aşama, gerçek durum, hata kodu ve tekrar deneme.", listOf("Görev alındı", "Ajan aşaması", "Gerçek sonuç", "Hata / tekrar deneme")),
    AgentWidgetDefinition("filesync", "Proje bütünlüğü", "PROJE", "Dosya yolları, uzantılar ve proje/sohbet alanı ayrımını izle.", Icons.Outlined.Tune,
        "Sohbet dosyaları ile proje ortak dosyalarını ayrı tutan bir veri bütünlüğü kontrolü tasarla. Dosya kopyalama, yeniden adlandırma, silme ve dışa aktarmada kayıp risklerini azalt.", listOf("Sohbet alanı", "Proje ortak alanı", "Dosya kimliği", "Yedek/geri alma"))
)

@Composable
fun AgentWidgetsScreen(
    onRunPrompt: (String, Boolean) -> Unit,
    isBusy: Boolean,
    selectedProjectName: String? = null,
    terminalSessionId: String = "personal",
    launchWidgetId: String? = null,
    onWidgetLaunchHandled: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val runStore = remember(context) { AgentRunStore(context) }
    var records by remember { mutableStateOf(runStore.recent(20)) }
    var terminalRows by remember(terminalSessionId) { mutableStateOf(EmbeddedTerminalEngine.history(context, terminalSessionId, 8)) }
    var selectedCategory by remember { mutableStateOf("Tümü") }
    val categories = listOf("Tümü", "GELİŞTİRME", "PROJE", "AJANLAR", "DERLEME", "YAYIN", "DOSYALAR", "KALİTE", "GÜVENLİK", "BAĞLANTILAR", "MEDYA", "SİSTEM", "MODEL", "TESLİM")
    LaunchedEffect(terminalSessionId) {
        while (true) {
            records = runStore.recent(20)
            terminalRows = EmbeddedTerminalEngine.history(context, terminalSessionId, 8)
            delay(1_000)
        }
    }
    val latest = records.firstOrNull()
    val active = records.firstOrNull { it.outcome == "running" }
    val visibleWidgets = remember(selectedCategory) { if (selectedCategory == "Tümü") agentWidgets else agentWidgets.filter { it.category == selectedCategory } }

    // The launcher-widget AI button arrives with a specific key; turn it into the same
    // prompt that the corresponding in-app card uses, rather than a decorative-only tap.
    LaunchedEffect(launchWidgetId, isBusy, active?.taskId) {
        val requestedId = launchWidgetId ?: return@LaunchedEffect
        if (isBusy || active != null) return@LaunchedEffect
        val widget = agentWidgets.firstOrNull { it.id == requestedId }
        if (widget == null) {
            onWidgetLaunchHandled(requestedId)
            return@LaunchedEffect
        }
        val activeContext = latest?.let { "\n\nSON GÖREV DURUMU\nBaşlık: ${it.chatTitle}\nAşama: ${it.stage}\nDurum: ${it.status}\nSonuç: ${it.outcome}" }.orEmpty()
        onRunPrompt("KAIRO WIDGET: ${widget.title}\nKategori: ${widget.category}\nAmaç: ${widget.description}\n\n${widget.prompt}$activeContext\n\nÇıktıyı Türkçe ve uygulanabilir biçimde ver. Çalıştırılmamış terminal komutlarını ve doğrulanmamış sonuçları açıkça öneri olarak etiketle.", false)
        onWidgetLaunchHandled(requestedId)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(WInk),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(start = 16.dp, end = 16.dp, top = 13.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(11.dp)
    ) {
        item {
            Card(colors = CardDefaults.cardColors(containerColor = WPanel), border = androidx.compose.foundation.BorderStroke(1.dp, WStroke), shape = RoundedCornerShape(23.dp)) {
                Column(Modifier.fillMaxWidth().padding(17.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(42.dp).clip(RoundedCornerShape(14.dp)).background(WAccent.copy(alpha = .16f)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Outlined.GraphicEq, null, tint = WAccent, modifier = Modifier.size(23.dp))
                        }
                        Column(Modifier.weight(1f).padding(start = 11.dp)) {
                            Text("KAIRO LIVE WIDGETS", color = WText, fontSize = 15.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
                            Text("${agentWidgets.size} yapay zekâ destekli kontrol kartı", color = WSecond, fontSize = 11.sp)
                        }
                        Box(Modifier.clip(CircleShape).background(if (isBusy || active != null) WGold.copy(alpha = .14f) else WGreen.copy(alpha = .12f)).padding(horizontal = 10.dp, vertical = 6.dp)) {
                            Text(if (isBusy || active != null) "ÇALIŞIYOR" else "HAZIR", color = if (isBusy || active != null) WGold else WGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Text("Kartlar aynı Gemini görev hattına bağlanır. Dokunduğunda kartın konusuna göre bir görev istemi hazırlanır ve sohbet ajanına gönderilir.", color = WSecond, fontSize = 11.sp, lineHeight = 17.sp)
                    if (!selectedProjectName.isNullOrBlank()) Text("Aktif proje: $selectedProjectName", color = WAccent, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(7.dp), verticalAlignment = Alignment.CenterVertically) {
                        StatPill("Son görev", latest?.status ?: "Henüz görev yok", WAccent)
                        StatPill("Kuyruk", if (active != null || isBusy) "İşleniyor" else "Boş", if (active != null || isBusy) WGold else WGreen)
                    }
                }
            }
        }
        item {
            CommandTimelineCard(latest = latest, active = active, isBusy = isBusy, terminalRows = terminalRows,
                onRunPrompt = { onRunPrompt(agentWidgets.first { it.id == "commands" }.prompt, false) })
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                Text("WIDGET KATALOĞU", color = WSecond, fontSize = 10.sp, letterSpacing = 1.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterVertically).weight(1f))
                Text("${visibleWidgets.size} kart", color = WMuted, fontSize = 10.sp)
            }
            androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp), contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 4.dp)) {
                items(categories) { category ->
                    val activeCategory = selectedCategory == category
                    Box(Modifier.clip(CircleShape).background(if (activeCategory) WAccent else WPanel).border(1.dp, if (activeCategory) WAccent else WStroke, CircleShape).clickable { selectedCategory = category }.padding(horizontal = 11.dp, vertical = 8.dp)) {
                        Text(category, color = if (activeCategory) WInk else WSecond, fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
        items(visibleWidgets, key = { it.id }) { widget ->
            AgentWidgetCard(widget = widget, isBusy = isBusy || active != null, latest = latest, onRun = {
                val activeContext = latest?.let { "\n\nSON GÖREV DURUMU\nBaşlık: ${it.chatTitle}\nAşama: ${it.stage}\nDurum: ${it.status}\nSonuç: ${it.outcome}" }.orEmpty()
                onRunPrompt("KAIRO WIDGET: ${widget.title}\nKategori: ${widget.category}\nAmaç: ${widget.description}\n\n${widget.prompt}$activeContext\n\nÇıktıyı Türkçe ve uygulanabilir biçimde ver. Çalıştırılmamış terminal komutlarını ve doğrulanmamış sonuçları açıkça öneri olarak etiketle.", false)
            })
        }
    }
}

@Composable
private fun CommandTimelineCard(
    latest: AgentRunRecord?,
    active: AgentRunRecord?,
    isBusy: Boolean,
    terminalRows: List<EmbeddedCommandRecord>,
    onRunPrompt: () -> Unit
) {
    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF10151A)), border = androidx.compose.foundation.BorderStroke(1.dp, WGreen.copy(alpha = .34f)), shape = RoundedCornerShape(21.dp)) {
        Column(Modifier.fillMaxWidth().padding(15.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(37.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFF17352B)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.Terminal, null, tint = WGreen, modifier = Modifier.size(20.dp))
                }
                Column(Modifier.weight(1f).padding(start = 10.dp)) {
                    Text("KOMUT ZAMAN ÇİZELGESİ", color = WText, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = .8.sp)
                    Text("Gerçek terminal kayıtları ve sonraki önerilen adımlar", color = WSecond, fontSize = 9.sp)
                }
                Text(if (terminalRows.any { it.state == "running" } || active != null || isBusy) "LIVE" else "LOCAL", color = WGreen, fontSize = 9.sp, fontWeight = FontWeight.Black)
            }
            val currentStatus = when {
                terminalRows.any { it.state == "running" } -> "Terminal komutu çalışıyor"
                terminalRows.any { it.state == "approval_required" } -> "Komut kullanıcı onayını bekliyor"
                active != null -> active.status
                isBusy -> "Gemini görevi işleniyor"
                else -> latest?.status ?: "Beklemede"
            }
            val currentColor = when {
                terminalRows.any { it.state == "running" } || active != null || isBusy -> WGold
                terminalRows.firstOrNull()?.state == "success" -> WGreen
                terminalRows.firstOrNull()?.state in setOf("error", "timeout") -> WRed
                else -> WAccent
            }
            TimelineLine("|_", "Görev / istek", currentStatus, currentColor, active != null || isBusy || terminalRows.any { it.state == "running" })
            TimelineLine("|__", "Planlama ve komut izni", when {
                terminalRows.any { it.state == "approval_required" } -> "Kullanıcı incelemesi bekleniyor"
                terminalRows.isNotEmpty() -> "Komut geçmişi yerel olarak kaydedildi"
                else -> "AI görev istemiyle hazırlanır"
            }, if (terminalRows.any { it.state == "approval_required" }) WGold else WMuted, terminalRows.any { it.state == "approval_required" })

            if (terminalRows.isNotEmpty()) {
                terminalRows.take(5).forEachIndexed { index, record ->
                    val label = when (record.state) {
                        "approval_required" -> "ONAY BEKLİYOR"
                        "running" -> "ÇALIŞIYOR"
                        "success" -> "BAŞARILI · çıkış ${record.exitCode ?: 0}"
                        "timeout" -> "ZAMAN AŞIMI"
                        "rejected" -> "REDDEDİLDİ"
                        "blocked" -> "ENGELLENDİ"
                        else -> "HATA · çıkış ${record.exitCode ?: "yok"}"
                    }
                    val tint = when (record.state) {
                        "success" -> WGreen
                        "approval_required", "running" -> WGold
                        "rejected", "error", "timeout", "blocked" -> WRed
                        else -> WMuted
                    }
                    TimelineLine(if (index == terminalRows.lastIndex) "└──" else "|___", record.command, "$label · ${record.reason.take(72)}", tint, record.state == "running")
                }
            } else {
                TimelineLine("|___", "npm run build", "ÖNERİ · Node ve script varlığı henüz terminal tarafından doğrulanmadı", WGold, false)
                TimelineLine("|____", "./gradlew assembleRelease", "ÖNERİ · Projede wrapper ve Android SDK olmalı", WGold, false)
                TimelineLine("|_____", "git status --short", "ÖNERİ · Çalışma klasörü Git deposu olmalı", WGold, false)
            }
            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(WGold.copy(alpha = .08f)).padding(10.dp), verticalAlignment = Alignment.Top) {
                Icon(Icons.Outlined.Security, null, tint = WGold, modifier = Modifier.size(15.dp))
                Text("AI'nin terminal komutları onay isteği olarak görünür. Yalnızca kullanıcı tarafından onaylanan komut gerçek çıktı ve çıkış kodu üretir. Bu Android shell'inde Node/npm/Git/Gradle araçları ayrıca kurulu değilse kullanılamaz.", color = WSecond, fontSize = 9.sp, lineHeight = 14.sp, modifier = Modifier.padding(start = 7.dp))
            }
            Button(onClick = onRunPrompt, enabled = !isBusy && active == null, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = WGreen, contentColor = WInk), shape = RoundedCornerShape(11.dp)) {
                Icon(Icons.Outlined.AutoAwesome, null, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(7.dp)); Text(if (isBusy || active != null) "Ajanlar dolu · bekle" else "Komut akışını AI ile hazırla", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun TimelineLine(prefix: String, title: String, detail: String, color: Color, active: Boolean) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Text(prefix, color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(43.dp))
        Box(Modifier.padding(top = 5.dp).size(7.dp).clip(CircleShape).background(color))
        Column(Modifier.weight(1f).padding(start = 9.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, color = WText, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            Text(detail, color = if (active) color else WSecond, fontSize = 9.sp, lineHeight = 13.sp)
        }
        if (active) androidx.compose.material3.CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 1.6.dp, color = color)
    }
}

@Composable
private fun AgentWidgetCard(widget: AgentWidgetDefinition, isBusy: Boolean, latest: AgentRunRecord?, onRun: () -> Unit) {
    val currentColor = when (latest?.outcome) { "success" -> WGreen; "error" -> WRed; "running", "awaiting_approval" -> WGold; else -> WAccent }
    Card(colors = CardDefaults.cardColors(containerColor = WPanel), border = androidx.compose.foundation.BorderStroke(1.dp, WStroke), shape = RoundedCornerShape(17.dp)) {
        Column(Modifier.fillMaxWidth().padding(13.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(36.dp).clip(RoundedCornerShape(11.dp)).background(WCard), contentAlignment = Alignment.Center) {
                    Icon(widget.icon, null, tint = WAccent, modifier = Modifier.size(19.dp))
                }
                Column(Modifier.weight(1f).padding(start = 10.dp)) {
                    Text(widget.title, color = WText, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Text(widget.category, color = WMuted, fontSize = 8.sp, letterSpacing = 1.1.sp)
                }
                Box(Modifier.clip(CircleShape).background(currentColor.copy(alpha = .12f)).padding(horizontal = 8.dp, vertical = 5.dp)) {
                    Text(if (latest == null) "HAZIR" else when (latest.outcome) { "success" -> "SON: OK"; "error" -> "SON: HATA"; "running" -> "ÇALIŞIYOR"; "awaiting_approval" -> "ONAY BEKLİYOR"; else -> "HAZIR" }, color = currentColor, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                }
            }
            Text(widget.description, color = WSecond, fontSize = 10.sp, lineHeight = 15.sp)
            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(WInk.copy(alpha = .72f)).padding(10.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                widget.preview.forEachIndexed { i, item ->
                    Row(verticalAlignment = Alignment.Top) {
                        Text(if (i == widget.preview.lastIndex) "└" else "├", color = WAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text(item, color = WText.copy(alpha = .9f), fontSize = 9.sp, lineHeight = 13.sp, modifier = Modifier.padding(start = 7.dp))
                    }
                }
            }
            OutlinedButton(onClick = onRun, enabled = !isBusy, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.outlinedButtonColors(contentColor = WAccent), shape = RoundedCornerShape(10.dp)) {
                Icon(Icons.Outlined.AutoAwesome, null, modifier = Modifier.size(15.dp)); Spacer(Modifier.width(7.dp)); Text(if (isBusy) "Ajanlar dolu · bekle" else "AI ile çalıştır", fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun StatPill(label: String, value: String, color: Color) {
    Row(Modifier.clip(CircleShape).background(color.copy(alpha = .1f)).padding(horizontal = 9.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Text("$label: ", color = WSecond, fontSize = 9.sp)
        Text(value, color = color, fontSize = 9.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
    }
}
