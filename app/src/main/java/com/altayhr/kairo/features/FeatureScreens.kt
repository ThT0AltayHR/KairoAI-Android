package com.altayhr.kairo.features

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowOutward
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.MicOff
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material.icons.outlined.StopCircle
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.altayhr.kairo.data.KairoNote
import com.altayhr.kairo.data.NotesRepository
import com.altayhr.kairo.data.SecureApiKeyStore
import com.altayhr.kairo.service.GeminiLiveVoiceService
import com.altayhr.kairo.service.IslandOverlayService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

private val CInk = Color(0xFF090A10)
private val CPanel = Color(0xFF12141D)
private val CCard = Color(0xFF191C27)
private val CStroke = Color(0xFF2B3040)
private val CText = Color(0xFFF3F4F8)
private val CSecondary = Color(0xFF9DA5B5)
private val CMuted = Color(0xFF72798A)
private val CAccent = Color(0xFF9989FF)
private val CGreen = Color(0xFF66D6C0)
private val CRed = Color(0xFFFF7D88)

@Composable
fun CryptoScreen() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("kairo_preferences", Context.MODE_PRIVATE) }
    var currency by remember { mutableStateOf(prefs.getString("crypto_currency", "USD") ?: "USD") }
    var symbols by remember { mutableStateOf((prefs.getString("crypto_symbols", "BTC,ETH,SOL") ?: "BTC,ETH,SOL").split(',').map(String::trim).filter(String::isNotBlank).distinct().take(6)) }
    var quotes by remember { mutableStateOf<Map<String, CryptoQuote>>(emptyMap()) }
    var loading by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("Hazır") }
    var selectedToAdd by remember { mutableStateOf("BTC") }
    var refreshKey by remember { mutableStateOf(0) }
    LaunchedEffect(currency, symbols, refreshKey) {
        loading = true; status = "Piyasa verileri alınıyor"
        val next = linkedMapOf<String, CryptoQuote>()
        symbols.forEach { symbol -> CryptoRepository.fetch(symbol, currency)?.let { next[symbol] = it } }
        quotes = next
        status = if (next.isEmpty()) "Şu anda fiyat verisi alınamadı" else "Son güncelleme: ${java.text.SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(java.util.Date())}"
        loading = false
        delay(60_000)
        refreshKey++
    }
    Column(Modifier.fillMaxSize().padding(horizontal = 17.dp, vertical = 14.dp), verticalArrangement = Arrangement.spacedBy(13.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Piyasa gözlemcisi", color = CText, fontSize = 25.sp, fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.Serif)
                Text("Bilgilendirme amaçlı fiyat ve 24 saatlik değişim", color = CSecondary, fontSize = 11.sp)
            }
            IconButton(onClick = { refreshKey++ }) { if (loading) CircularProgressIndicator(Modifier.size(20.dp), color = CAccent, strokeWidth = 2.dp) else Icon(Icons.Outlined.Refresh, "Fiyatları yenile", tint = CSecondary) }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            listOf("USD", "EUR").forEach { option ->
                val selected = currency == option
                Box(Modifier.clip(RoundedCornerShape(10.dp)).background(if (selected) CAccent else CCard).clickable { currency = option; prefs.edit().putString("crypto_currency", option).apply() }.padding(horizontal = 16.dp, vertical = 9.dp)) {
                    Text(option, color = if (selected) CInk else CSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(Modifier.weight(1f))
            Text(status, color = CMuted, fontSize = 10.sp, modifier = Modifier.align(Alignment.CenterVertically))
        }
        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            items(symbols, key = { it }) { symbol ->
                val quote = quotes[symbol]
                Card(colors = CardDefaults.cardColors(containerColor = CPanel), shape = RoundedCornerShape(17.dp), border = BorderStroke(1.dp, CStroke)) {
                    Row(Modifier.fillMaxWidth().padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(40.dp).clip(RoundedCornerShape(13.dp)).background(CCard), contentAlignment = Alignment.Center) {
                            Text(symbol.take(1), color = CAccent, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                        Column(Modifier.weight(1f).padding(start = 12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(symbol, color = CText, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text(quote?.let { "24 saatte ${if (it.change24h >= 0) "+" else ""}${"%.2f".format(Locale.US, it.change24h)}%" } ?: if (loading) "Veri yükleniyor" else "Veri yok", color = quote?.let { if (it.change24h >= 0) CGreen else CRed } ?: CMuted, fontSize = 11.sp)
                            quote?.let { LinearProgressIndicator(progress = { (0.5f + (it.change24h.coerceIn(-20.0, 20.0) / 40.0)).toFloat() }, modifier = Modifier.fillMaxWidth(.82f).height(3.dp).clip(CircleShape), color = if (it.change24h >= 0) CGreen else CRed, trackColor = CStroke) }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(quote?.let { CryptoRepository.formatPrice(it.price, currency) } ?: "—", color = CText, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text("spot veri", color = CMuted, fontSize = 10.sp)
                        }
                    }
                }
            }
        }
        Card(colors = CardDefaults.cardColors(containerColor = CPanel), shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, CStroke)) {
            Column(Modifier.fillMaxWidth().padding(13.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Text("İzleme listesi", color = CText, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val available = CryptoRepository.symbols().filterNot { it in symbols }
                    if (available.isNotEmpty()) {
                        Column(Modifier.weight(1f)) {
                            Text("Eklenebilir varlık", color = CMuted, fontSize = 10.sp)
                            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) { available.take(4).forEach { item -> Text(item, color = if (selectedToAdd == item) CText else CSecondary, fontSize = 11.sp, modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(if (selectedToAdd == item) CCard else Color.Transparent).clickable { selectedToAdd = item }.padding(horizontal = 8.dp, vertical = 6.dp)) } }
                        }
                        Button(onClick = { symbols = (symbols + selectedToAdd).take(6); prefs.edit().putString("crypto_symbols", symbols.joinToString(",")).apply() }, colors = ButtonDefaults.buttonColors(containerColor = CAccent, contentColor = CInk)) { Text("Ekle") }
                    } else Text("İzleme listesi dolu", color = CSecondary, fontSize = 11.sp)
                }
                Text("Kaynak: Coinbase Exchange genel piyasa uç noktası. Bu ekran yalnızca fiyat bilgisi sunar; alım-satım yapmaz ve yatırım tavsiyesi vermez.", color = CMuted, fontSize = 10.sp, lineHeight = 15.sp)
            }
        }
    }
}

@Composable
fun EmailComposerScreen() {
    val context = LocalContext.current
    var to by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var status by remember { mutableStateOf<String?>(null) }
    Column(Modifier.fillMaxSize().padding(17.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(CCard), contentAlignment = Alignment.Center) { Icon(Icons.Outlined.Email, null, tint = CAccent) }
            Column(Modifier.weight(1f).padding(start = 12.dp)) {
                Text("E-posta oluştur", color = CText, fontSize = 24.sp, fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.Serif)
                Text("Taslağı cihazındaki e-posta uygulamasına aktar", color = CSecondary, fontSize = 11.sp)
            }
        }
        OutlinedTextField(value = to, onValueChange = { to = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Alıcı") }, placeholder = { Text("ornek@posta.com") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), colors = fieldColors())
        OutlinedTextField(value = subject, onValueChange = { subject = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Konu") }, singleLine = true, colors = fieldColors())
        OutlinedTextField(value = body, onValueChange = { body = it }, modifier = Modifier.weight(1f).fillMaxWidth(), label = { Text("Mesaj") }, placeholder = { Text("Mesajını yaz…") }, colors = fieldColors())
        Button(onClick = {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:${Uri.encode(to.trim())}")
                putExtra(Intent.EXTRA_EMAIL, if (to.isBlank()) arrayOf<String>() else arrayOf(to.trim()))
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, body)
            }
            runCatching { context.startActivity(Intent.createChooser(intent, "E-posta uygulaması seç")); status = "Taslak e-posta uygulamasında açıldı; göndermeden önce kontrol et." }
                .onFailure { status = "Uygun e-posta istemcisi bulunamadı." }
        }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = CAccent, contentColor = CInk)) { Icon(Icons.Outlined.Send, null, modifier = Modifier.size(17.dp)); Spacer(Modifier.size(8.dp)); Text("E-posta uygulamasında aç") }
        status?.let { Text(it, color = CSecondary, fontSize = 11.sp) }
        Text("Güvenlik: mesaj doğrudan gönderilmez; e-posta istemcisi açılır ve son gönderim adımı sende kalır.", color = CMuted, fontSize = 10.sp, lineHeight = 15.sp)
    }
}

@Composable
fun VoiceScreen(apiConfigured: Boolean, liveModel: String, voice: String, onStart: () -> Unit, onStop: () -> Unit, onOpenSettings: () -> Unit) {
    val context = LocalContext.current
    var voiceStatus by remember { mutableStateOf(context.getSharedPreferences("kairo_preferences", Context.MODE_PRIVATE).getString("voice_status", "Sesli sohbet hazır") ?: "Sesli sohbet hazır") }
    var voiceDetail by remember { mutableStateOf(context.getSharedPreferences("kairo_preferences", Context.MODE_PRIVATE).getString("voice_detail", "Canlı sesli görüşme başlatılmadı") ?: "Canlı sesli görüşme başlatılmadı") }
    var running by remember { mutableStateOf(GeminiLiveVoiceService.isRunning(context)) }
    var lastUser by remember { mutableStateOf("") }
    var lastAssistant by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        while (true) {
            val p = context.getSharedPreferences("kairo_preferences", Context.MODE_PRIVATE)
            voiceStatus = p.getString("voice_status", "Sesli sohbet hazır") ?: "Sesli sohbet hazır"
            voiceDetail = p.getString("voice_detail", "Canlı sesli görüşme başlatılmadı") ?: "Canlı sesli görüşme başlatılmadı"
            running = p.getBoolean("voice_running", false)
            lastUser = p.getString("voice_last_user", "") ?: ""
            lastAssistant = p.getString("voice_last_assistant", "") ?: ""
            delay(700)
        }
    }
    Column(Modifier.fillMaxSize().padding(17.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(46.dp).clip(RoundedCornerShape(15.dp)).background(CCard), contentAlignment = Alignment.Center) { Icon(Icons.Outlined.GraphicEq, null, tint = CAccent, modifier = Modifier.size(25.dp)) }
            Column(Modifier.weight(1f).padding(start = 12.dp)) {
                Text("Canlı sesli sohbet", color = CText, fontSize = 24.sp, fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.Serif)
                Text("Gemini Live · çift yönlü ses", color = CSecondary, fontSize = 11.sp)
            }
        }
        Card(colors = CardDefaults.cardColors(containerColor = CPanel), border = BorderStroke(1.dp, CStroke), shape = RoundedCornerShape(19.dp)) {
            Column(Modifier.fillMaxWidth().padding(17.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(9.dp).clip(CircleShape).background(if (running) CGreen else CMuted))
                    Text(voiceStatus, color = CText, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 9.dp).weight(1f))
                    Text(if (running) "CANLI" else "BEKLEMEDE", color = if (running) CGreen else CMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
                Text(voiceDetail, color = CSecondary, fontSize = 12.sp, lineHeight = 18.sp)
                Divider(color = CStroke)
                Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Outlined.Settings, null, tint = CMuted, modifier = Modifier.size(16.dp)); Text("$liveModel · $voice sesi", color = CSecondary, fontSize = 11.sp, modifier = Modifier.padding(start = 7.dp)) }
                if (!running) Button(onClick = onStart, modifier = Modifier.fillMaxWidth(), enabled = apiConfigured, colors = ButtonDefaults.buttonColors(containerColor = CAccent, contentColor = CInk, disabledContainerColor = CStroke)) {
                    Icon(Icons.Outlined.Mic, null, modifier = Modifier.size(18.dp)); Spacer(Modifier.size(8.dp)); Text("Sesli sohbeti başlat")
                } else Button(onClick = onStop, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = CRed, contentColor = CInk)) {
                    Icon(Icons.Outlined.StopCircle, null, modifier = Modifier.size(18.dp)); Spacer(Modifier.size(8.dp)); Text("Görüşmeyi bitir")
                }
                if (!apiConfigured) Text("Başlatmadan önce Ayarlar bölümüne Gemini API anahtarını ekle.", color = CRed, fontSize = 11.sp)
                TextButton(onClick = onOpenSettings) { Text("Ses ve model ayarları", color = CAccent) }
            }
        }
        Text("SON KONUŞMA", color = CMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
        TranscriptCard("Sen", lastUser.ifBlank { "Söylediklerin burada görünecek." })
        TranscriptCard("Kairo", lastAssistant.ifBlank { "Asistanın konuşma özeti burada görünecek." })
        Text("Canlı mikrofon, bildirimdeki 'Görüşmeyi bitir' düğmesine basana kadar açık kalır. Android'in mikrofon göstergesi ve devam eden bildirim görünür kalır.", color = CMuted, fontSize = 10.sp, lineHeight = 15.sp)
    }
}

@Composable
private fun TranscriptCard(title: String, text: String) {
    Card(colors = CardDefaults.cardColors(containerColor = CPanel), border = BorderStroke(1.dp, CStroke), shape = RoundedCornerShape(14.dp)) {
        Column(Modifier.fillMaxWidth().padding(13.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(title, color = CAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Text(text, color = if (text.startsWith("Asistanın") || text.startsWith("Söylediklerin")) CMuted else CText, fontSize = 12.sp, lineHeight = 18.sp)
        }
    }
}

@Composable
fun NotesScreen(onUseNote: (String) -> Unit) {
    val context = LocalContext.current
    val repo = remember { NotesRepository(context) }
    var notes by remember { mutableStateOf(repo.getAll()) }
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var editing by remember { mutableStateOf<String?>(null) }
    Column(Modifier.fillMaxSize().padding(17.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Not defteri", color = CText, fontSize = 25.sp, fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.Serif)
                Text("Yerel notlar · projelerden ve sohbetlerden bağımsız", color = CSecondary, fontSize = 11.sp)
            }
        }
        Card(colors = CardDefaults.cardColors(containerColor = CPanel), border = BorderStroke(1.dp, CStroke), shape = RoundedCornerShape(17.dp)) {
            Column(Modifier.fillMaxWidth().padding(13.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Not başlığı") }, singleLine = true, colors = fieldColors())
                OutlinedTextField(value = body, onValueChange = { body = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Not içeriği") }, minLines = 3, colors = fieldColors())
                Button(onClick = {
                    val cleanTitle = title.trim().ifBlank { body.trim().take(32).ifBlank { "Yeni not" } }
                    if (body.isNotBlank() || title.isNotBlank()) {
                        val old = notes.firstOrNull { it.id == editing }
                        val note = KairoNote(id = old?.id ?: java.util.UUID.randomUUID().toString(), title = cleanTitle, body = body.trim(), createdAt = old?.createdAt ?: System.currentTimeMillis(), updatedAt = System.currentTimeMillis())
                        notes = (listOf(note) + notes.filterNot { it.id == note.id }).sortedByDescending { it.updatedAt }; repo.saveAll(notes)
                        title = ""; body = ""; editing = null
                    }
                }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = CAccent, contentColor = CInk)) { Icon(Icons.Outlined.Save, null, modifier = Modifier.size(17.dp)); Spacer(Modifier.size(7.dp)); Text(if (editing == null) "Notu kaydet" else "Değişiklikleri kaydet") }
            }
        }
        Text("KAYITLI NOTLAR · ${notes.size}", color = CMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.1.sp)
        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(notes, key = { it.id }) { note ->
                Card(colors = CardDefaults.cardColors(containerColor = CPanel), border = BorderStroke(1.dp, CStroke), shape = RoundedCornerShape(15.dp)) {
                    Column(Modifier.fillMaxWidth().padding(13.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                        Text(note.title, color = CText, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text(note.body, color = CSecondary, fontSize = 11.sp, lineHeight = 17.sp, maxLines = 5)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TextButton(onClick = { title = note.title; body = note.body; editing = note.id }) { Text("Düzenle", color = CAccent, fontSize = 11.sp) }
                            TextButton(onClick = { onUseNote("${note.title}\n\n${note.body}") }) { Text("Sohbette kullan", color = CAccent, fontSize = 11.sp) }
                            Spacer(Modifier.weight(1f))
                            IconButton(onClick = { notes = notes.filterNot { it.id == note.id }; repo.saveAll(notes) }) { Icon(Icons.Outlined.DeleteOutline, "Notu sil", tint = CMuted) }
                        }
                    }
                }
            }
        }
    }
}

private data class IntegrationDefinition(val id: String, val name: String, val category: String, val description: String, val docs: String, val endpoint: String, val tokenLabel: String = "API anahtarı", val needsToken: Boolean = true)

private val integrationDefinitions = listOf(
    IntegrationDefinition("github", "GitHub", "Geliştirme", "Hesap bilgisi ve bildirim uç noktası testi", "https://docs.github.com/rest", "https://api.github.com/user", "Personal access token"),
    IntegrationDefinition("vercel", "Vercel", "Geliştirme", "Proje/deployment API erişimi", "https://vercel.com/docs/rest-api", "https://api.vercel.com/v9/projects?limit=1", "Vercel token"),
    IntegrationDefinition("n8n", "n8n", "Otomasyon", "Kendi n8n sunucundaki iş akışlarına bağlantı", "https://docs.n8n.io/api/", "", "n8n API key"),
    IntegrationDefinition("notion", "Notion", "Çalışma", "Notion kullanıcı API'si bağlantı testi", "https://developers.notion.com/", "https://api.notion.com/v1/users/me", "Internal integration token"),
    IntegrationDefinition("calcom", "Cal.com", "Takvim", "Takvim API bağlantı testi", "https://cal.com/docs", "https://api.cal.com/v2/me", "Cal.com API key"),
    IntegrationDefinition("resend", "Resend", "E-posta", "Gönderim servisi alan adı uç noktası testi", "https://resend.com/docs/api-reference", "https://api.resend.com/domains", "Resend API key"),
    IntegrationDefinition("stripe", "Stripe", "Ödeme altyapısı", "Salt okunur hesap doğrulama isteği", "https://docs.stripe.com/api", "https://api.stripe.com/v1/account", "Secret key"),
    IntegrationDefinition("whatsapp", "WhatsApp Gateway", "Mesajlaşma", "Kullanıcının kendi gateway health uç noktasını denetler", "https://github.com/WhatsApp/WhatsApp-Web", "", "Gateway API key"),
    IntegrationDefinition("spotify", "Spotify", "Medya", "Spotify uygulaması ve web oynatıcısı kısayolu", "https://developer.spotify.com/documentation/web-api", "", "OAuth bağlantısı gerekir", false),
    IntegrationDefinition("music", "Apple Music", "Medya", "Apple Music web oynatıcısı kısayolu", "https://developer.apple.com/documentation/musickit", "", "", false),
    IntegrationDefinition("portfolio", "Portföy notları", "Kişisel", "Varlık bilgilerini yerel notlarla takip et; otomatik işlem yapılmaz", "", "", "", false),
    IntegrationDefinition("crypto", "Piyasa gözlemcisi", "Kişisel", "BTC, ETH, SOL ve diğer desteklenen varlıkların spot fiyatları", "https://docs.cdp.coinbase.com/exchange/reference/exchangerestapi_getproductstats", "", "", false),
    IntegrationDefinition("mail", "E-posta", "E-posta", "Taslakları Android e-posta uygulamasında hazırla", "", "", "", false)
)

@Composable
fun IntegrationHubScreen(secrets: SecureApiKeyStore, onNavigate: (String) -> Unit) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val prefs = remember { context.getSharedPreferences("kairo_integrations", Context.MODE_PRIVATE) }
    var editing by remember { mutableStateOf<IntegrationDefinition?>(null) }
    var token by remember(editing?.id) { mutableStateOf(editing?.let { secrets.getSecret("integration_${it.id}") }.orEmpty()) }
    var baseUrl by remember(editing?.id) { mutableStateOf(editing?.let { prefs.getString("url_${it.id}", "") }.orEmpty()) }
    val statuses = remember { mutableStateMapOf<String, String>() }
    val details = remember { mutableStateMapOf<String, List<String>>() }
    val busy = remember { mutableStateMapOf<String, Boolean>() }
    val scope = rememberCoroutineScope()
    val groups = integrationDefinitions.groupBy { it.category }
    Column(Modifier.fillMaxSize().padding(horizontal = 17.dp, vertical = 13.dp), verticalArrangement = Arrangement.spacedBy(11.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Entegrasyon merkezi", color = CText, fontSize = 25.sp, fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.Serif)
            Text("Servis durumları · anahtarlar şifreli tutulur · bağlantı testi salt okunurdur", color = CSecondary, fontSize = 10.sp, lineHeight = 15.sp)
        }
        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            groups.forEach { (group, itemsInGroup) ->
                item(key = "heading_$group") { Text(group.uppercase(Locale.ROOT), color = CMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp, modifier = Modifier.padding(top = 5.dp)) }
                items(itemsInGroup, key = { it.id }) { def ->
                    val configured = !def.needsToken || (!secrets.getSecret("integration_${def.id}").isNullOrBlank()) || (def.id == "n8n" && !prefs.getString("url_${def.id}", "").isNullOrBlank()) || (def.id == "whatsapp" && !prefs.getString("url_${def.id}", "").isNullOrBlank())
                    Card(colors = CardDefaults.cardColors(containerColor = CPanel), border = BorderStroke(1.dp, CStroke), shape = RoundedCornerShape(15.dp)) {
                        Column(Modifier.fillMaxWidth().padding(13.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(34.dp).clip(RoundedCornerShape(10.dp)).background(CCard), contentAlignment = Alignment.Center) { Text(def.name.take(1), color = CAccent, fontSize = 16.sp, fontWeight = FontWeight.Bold) }
                                Column(Modifier.weight(1f).padding(start = 10.dp)) {
                                    Text(def.name, color = CText, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                    Text(def.description, color = CSecondary, fontSize = 10.sp, lineHeight = 14.sp)
                                }
                                Box(Modifier.size(7.dp).clip(CircleShape).background(if (statuses[def.id]?.startsWith("Bağlandı") == true) CGreen else CMuted))
                            }
                            statuses[def.id]?.let { Text(it, color = if (it.startsWith("Bağlandı")) CGreen else if (it.contains("hata", true) || it.contains("başarısız", true)) CRed else CSecondary, fontSize = 10.sp) }
                            details[def.id]?.takeIf { it.isNotEmpty() }?.let { rows ->
                                Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(CCard).padding(10.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                    Text("Salt okunur servis özeti", color = CAccent, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                                    rows.take(5).forEach { row -> Text(row, color = CSecondary, fontSize = 10.sp, lineHeight = 14.sp) }
                                }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                if (def.needsToken) TextButton(onClick = { editing = def }) { Icon(Icons.Outlined.Settings, null, modifier = Modifier.size(14.dp)); Spacer(Modifier.size(4.dp)); Text("Yapılandır", fontSize = 10.sp, color = CAccent) }
                                if (def.id == "crypto") TextButton(onClick = { onNavigate("crypto") }) { Icon(Icons.Outlined.ShowChart, null, modifier = Modifier.size(14.dp)); Spacer(Modifier.size(4.dp)); Text("Piyasayı aç", fontSize = 10.sp, color = CAccent) }
                                if (def.id == "portfolio") TextButton(onClick = { onNavigate("portfolio") }) { Icon(Icons.Outlined.ShowChart, null, modifier = Modifier.size(14.dp)); Spacer(Modifier.size(4.dp)); Text("Portföyü aç", fontSize = 10.sp, color = CAccent) }
                                if (def.id == "mail") TextButton(onClick = { onNavigate("mail") }) { Icon(Icons.Outlined.Email, null, modifier = Modifier.size(14.dp)); Spacer(Modifier.size(4.dp)); Text("Taslak oluştur", fontSize = 10.sp, color = CAccent) }
                                if (def.id == "whatsapp") TextButton(onClick = { onNavigate("whatsapp") }) { Icon(Icons.Outlined.Phone, null, modifier = Modifier.size(14.dp)); Spacer(Modifier.size(4.dp)); Text("Mesaj hazırla", fontSize = 10.sp, color = CAccent) }
                                if (def.id == "spotify" || def.id == "music") TextButton(onClick = { onNavigate(def.id) }) { Icon(Icons.Outlined.ArrowOutward, null, modifier = Modifier.size(14.dp)); Spacer(Modifier.size(4.dp)); Text("Aç", fontSize = 10.sp, color = CAccent) }
                                Spacer(Modifier.weight(1f))
                                if (def.needsToken) TextButton(enabled = configured && busy[def.id] != true, onClick = {
                                    busy[def.id] = true; statuses[def.id] = "Bağlantı kontrol ediliyor"
                                    scope.launch {
                                        val result = withContext(Dispatchers.IO) {
                                            val secret = secrets.getSecret("integration_${def.id}").orEmpty()
                                            val url = prefs.getString("url_${def.id}", "").orEmpty()
                                            IntegrationTester.test(def, url, secret)
                                        }
                                        statuses[def.id] = result.status
                                        details[def.id] = result.details
                                        busy[def.id] = false
                                    }
                                }) { if (busy[def.id] == true) CircularProgressIndicator(Modifier.size(13.dp), color = CAccent, strokeWidth = 1.5.dp) else Text("Test", color = CSecondary, fontSize = 10.sp) }
                                if (def.docs.isNotBlank()) IconButton(onClick = { runCatching { uriHandler.openUri(def.docs) } }, modifier = Modifier.size(30.dp)) { Icon(Icons.Outlined.Link, "Belgeleri aç", tint = CMuted, modifier = Modifier.size(16.dp)) }
                            }
                        }
                    }
                }
            }
        }
    }
    editing?.let { def ->
        AlertDialog(onDismissRequest = { editing = null }, containerColor = CPanel,
            title = { Text("${def.name} bağlantısı", color = CText) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (def.id == "n8n" || def.id == "whatsapp") OutlinedTextField(value = baseUrl, onValueChange = { baseUrl = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Sunucu URL'si") }, placeholder = { Text("https://...") }, singleLine = true, colors = fieldColors())
                    OutlinedTextField(value = token, onValueChange = { token = it }, modifier = Modifier.fillMaxWidth(), label = { Text(def.tokenLabel) }, singleLine = true, visualTransformation = PasswordVisualTransformation(), colors = fieldColors())
                    Text("Anahtar, Android Keystore tarafından korunan bir AES anahtarıyla şifrelenerek cihazda tutulur. Bu bağlantı testi yalnızca okuma isteği gönderir.", color = CMuted, fontSize = 10.sp, lineHeight = 15.sp)
                }
            },
            confirmButton = { TextButton(onClick = {
                if (token.isNotBlank()) secrets.saveSecret("integration_${def.id}", token.trim())
                if (baseUrl.isNotBlank()) prefs.edit().putString("url_${def.id}", baseUrl.trim().trimEnd('/')).apply()
                editing = null; token = ""; baseUrl = ""
            }) { Text("Kaydet", color = CAccent) } },
            dismissButton = { TextButton(onClick = { editing = null }) { Text("Vazgeç", color = CSecondary) } }
        )
    }
}

private data class IntegrationResult(val status: String, val details: List<String> = emptyList())

/** Read-only provider snapshot. This never creates, sends, deletes or modifies provider data. */
private object IntegrationTester {
    fun test(def: IntegrationDefinition, baseUrl: String, token: String): IntegrationResult {
        if (token.isBlank() && def.needsToken) return IntegrationResult("API anahtarı eksik")
        val endpoint = when (def.id) {
            "n8n" -> baseUrl.trimEnd('/') + "/api/v1/workflows?limit=10"
            "whatsapp" -> baseUrl.trimEnd('/') + "/health"
            "calcom" -> "https://api.cal.com/v2/bookings?status=upcoming&limit=5"
            "vercel" -> "https://api.vercel.com/v9/projects?limit=5"
            "github" -> "https://api.github.com/user"
            "notion" -> "https://api.notion.com/v1/users/me"
            "resend" -> "https://api.resend.com/domains"
            "stripe" -> "https://api.stripe.com/v1/account"
            else -> def.endpoint
        }
        if (endpoint.isBlank()) return IntegrationResult("Bu entegrasyon için veri uç noktası yok")
        val parsed = runCatching { java.net.URI(endpoint) }.getOrNull()
            ?: return IntegrationResult("Geçersiz sunucu URL'si")
        if (parsed.scheme != "https" || parsed.host.isNullOrBlank() || parsed.userInfo != null) {
            return IntegrationResult("Güvenlik nedeniyle yalnızca kullanıcı bilgisi içermeyen HTTPS adresleri kabul edilir")
        }
        return runCatching {
            val body = request(endpoint, def.id, token)
            val details = when (def.id) {
                "github" -> {
                    val login = body.optString("login", "bilinmeyen hesap")
                    val publicRepos = body.optInt("public_repos", -1)
                    val followers = body.optInt("followers", -1)
                    buildList {
                        add("Hesap: $login")
                        if (publicRepos >= 0) add("Herkese açık depo: $publicRepos")
                        if (followers >= 0) add("Takipçi: $followers")
                        runCatching {
                            val arr = requestArray("https://api.github.com/user/repos?per_page=5&sort=updated", "github", token, extraAccept = "application/vnd.github+json")
                            if (arr.length() > 0) for (i in 0 until minOf(arr.length(), 5)) {
                                val r = arr.optJSONObject(i) ?: continue
                                add("Depo: ${r.optString("full_name", r.optString("name", "Depo"))} · ${r.optString("updated_at", "tarih yok").take(10)}")
                            }
                        }
                    }
                }
                "vercel" -> {
                    val arr = body.optJSONArray("projects") ?: JSONArray()
                    buildList {
                        add("Listelenen proje: ${arr.length()}")
                        for (i in 0 until minOf(arr.length(), 5)) {
                            val item = arr.optJSONObject(i) ?: continue
                            add("${item.optString("name", "Proje")} · ${item.optString("framework", "framework belirtilmemiş")} · ${item.optString("updatedAt", "").take(10)}")
                        }
                    }
                }
                "n8n" -> {
                    val arr = body.optJSONArray("data") ?: JSONArray()
                    val active = (0 until arr.length()).count { body.optJSONArray("data")?.optJSONObject(it)?.optBoolean("active", false) == true }
                    buildList {
                        add("İş akışı: ${arr.length()} · listede etkin: $active")
                        for (i in 0 until minOf(arr.length(), 5)) {
                            val item = arr.optJSONObject(i) ?: continue
                            add("${item.optString("name", "Adsız akış")} · ${if (item.optBoolean("active")) "etkin" else "pasif"} · düğüm ${item.optJSONArray("nodes")?.length() ?: 0}")
                        }
                    }
                }
                "notion" -> listOf(
                    "Bağlı kimlik: ${body.optString("name", body.optJSONObject("bot")?.optString("owner", "Notion entegrasyonu") ?: "Notion entegrasyonu")}",
                    "Nesne türü: ${body.optString("type", "kullanıcı/bot")}",
                    "Bu bağlantı kontrolünde sayfa içeriği değiştirilmedi."
                )
                "calcom" -> {
                    val arr = body.optJSONObject("data")?.optJSONArray("bookings") ?: body.optJSONArray("data") ?: JSONArray()
                    buildList {
                        add("Yaklaşan kayıt: ${arr.length()}")
                        for (i in 0 until minOf(arr.length(), 5)) {
                            val item = arr.optJSONObject(i) ?: continue
                            add("${item.optString("title", "Randevu")} · ${item.optString("start", "tarih yok").replace('T', ' ').take(16)} · ${item.optString("status", "durum yok")}")
                        }
                    }
                }
                "resend" -> {
                    val arr = body.optJSONArray("data") ?: JSONArray()
                    buildList {
                        add("E-posta alan adı: ${arr.length()}")
                        for (i in 0 until minOf(arr.length(), 5)) {
                            val item = arr.optJSONObject(i) ?: continue
                            add("${item.optString("name", "alan adı")} · ${item.optString("status", "durum belirtilmemiş")}")
                        }
                    }
                }
                "stripe" -> {
                    val business = body.optJSONObject("business_profile")
                    listOf(
                        "Hesap ülkesi: ${body.optString("country", "belirtilmemiş")}",
                        "Varsayılan para birimi: ${body.optString("default_currency", "belirtilmemiş")}",
                        "Hesap durumu: ${if (body.optBoolean("charges_enabled")) "ödeme alımı etkin" else "ödeme alımı etkin değil"}",
                        business?.optString("name")?.takeIf { it.isNotBlank() }?.let { "İşletme: $it" } ?: "Hesap bilgileri salt okunur alındı"
                    )
                }
                "whatsapp" -> listOf("Gateway sağlık yanıtı alındı", "Durum: ${body.optString("status", body.optString("state", "servis yanıt verdi"))}")
                else -> listOf("Servis geçerli bir JSON yanıtı verdi", body.keys().asSequence().take(8).joinToString(", ").let { "Alanlar: $it" })
            }
            IntegrationResult("Bağlandı · salt okunur özet hazır", details)
        }.getOrElse { IntegrationResult("Bağlantı hatası: ${it.message?.take(120) ?: "ağ hatası"}") }
    }

    private fun requestArray(url: String, provider: String, token: String, extraAccept: String? = null): JSONArray {
        val raw = requestBody(url, provider, token, extraAccept)
        return JSONArray(raw)
    }

    private fun request(url: String, provider: String, token: String, extraAccept: String? = null): JSONObject = JSONObject(requestBody(url, provider, token, extraAccept))

    private fun requestBody(url: String, provider: String, token: String, extraAccept: String? = null): String {
        val parsed = java.net.URI(url)
        require(parsed.scheme == "https" && !parsed.host.isNullOrBlank() && parsed.userInfo == null) { "Güvenli HTTPS URL gerekli" }
        val c = URL(url).openConnection() as HttpURLConnection
        c.instanceFollowRedirects = false
        c.connectTimeout = 9000; c.readTimeout = 12000; c.requestMethod = "GET"
        c.setRequestProperty("Accept", extraAccept ?: "application/json")
        c.setRequestProperty("User-Agent", "KairoAI-Android/0.7")
        when (provider) {
            "github" -> { c.setRequestProperty("Authorization", "Bearer $token"); c.setRequestProperty("X-GitHub-Api-Version", "2026-03-10") }
            "vercel", "resend", "stripe", "calcom", "notion" -> c.setRequestProperty("Authorization", "Bearer $token")
            "n8n" -> c.setRequestProperty("X-N8N-API-KEY", token)
            "whatsapp" -> if (token.isNotBlank()) c.setRequestProperty("Authorization", "Bearer $token")
        }
        if (provider == "notion") c.setRequestProperty("Notion-Version", "2022-06-28")
        if (provider == "calcom") c.setRequestProperty("cal-api-version", "2026-02-25")
        try {
            val code = c.responseCode
            val stream = if (code in 200..299) c.inputStream else c.errorStream
            val response = stream?.bufferedReader(Charsets.UTF_8)?.use { reader -> reader.readText().take(250_000) }.orEmpty()
            if (code !in 200..299) {
                val reason = when (code) {
                    401, 403 -> "Kimlik doğrulama başarısız · HTTP $code"
                    404 -> "Uç nokta bulunamadı · HTTP 404"
                    429 -> "Servis hız sınırı · HTTP 429"
                    else -> "Servis isteği başarısız · HTTP $code"
                }
                throw IllegalStateException(reason)
            }
            return response
        } finally { c.disconnect() }
    }
}

private fun fieldColors() = OutlinedTextFieldDefaults.colors(focusedBorderColor = CAccent, unfocusedBorderColor = CStroke, focusedTextColor = CText, unfocusedTextColor = CText, focusedLabelColor = CAccent, unfocusedLabelColor = CSecondary, cursorColor = CAccent)
