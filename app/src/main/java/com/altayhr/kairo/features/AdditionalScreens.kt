package com.altayhr.kairo.features

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.net.Uri
import org.json.JSONArray
import org.json.JSONObject
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.BatteryChargingFull
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.Headphones
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.Memory
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.NumberFormat
import java.util.Locale

private val AInk = Color(0xFF090A10)
private val APanel = Color(0xFF12141D)
private val ACard = Color(0xFF191C27)
private val AStroke = Color(0xFF2B3040)
private val AText = Color(0xFFF3F4F8)
private val ASecondary = Color(0xFF9DA5B5)
private val AMuted = Color(0xFF72798A)
private val AAccent = Color(0xFFAA9BFF)
private val AGreen = Color(0xFF66D6C0)
private val ARed = Color(0xFFFF7D88)

private data class TrackedPosition(val symbol: String, val amount: Double)

@Composable
fun PortfolioScreen() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("kairo_preferences", Context.MODE_PRIVATE) }
    var positions by remember { mutableStateOf(loadPositions(prefs.getString("portfolio_positions", "[]") ?: "[]")) }
    var symbol by remember { mutableStateOf("BTC") }
    var amount by remember { mutableStateOf("0.01") }
    var currency by remember { mutableStateOf(prefs.getString("portfolio_currency", "USD") ?: "USD") }
    var quotes by remember { mutableStateOf<Map<String, CryptoQuote>>(emptyMap()) }
    var busy by remember { mutableStateOf(false) }
    var refreshNo by remember { mutableStateOf(0) }
    var message by remember { mutableStateOf("Portföyünü elle girilen miktarlar ve genel piyasa fiyatlarıyla takip et.") }
    val scope = rememberCoroutineScope()

    fun persist(next: List<TrackedPosition>) {
        positions = next
        prefs.edit().putString("portfolio_positions", JSONArray().also { a -> next.forEach { a.put(JSONObject().put("symbol", it.symbol).put("amount", it.amount)) } }.toString()).apply()
    }

    LaunchedEffect(Unit) {
        while (true) { delay(60_000); refreshNo++ }
    }
    LaunchedEffect(refreshNo, positions, currency) {
        busy = true
        val next = linkedMapOf<String, CryptoQuote>()
        positions.map { it.symbol }.distinct().forEach { ticker -> CryptoRepository.fetch(ticker, currency)?.let { next[ticker] = it } }
        quotes = next
        busy = false
        message = if (positions.isEmpty()) "Henüz portföy varlığı eklenmedi." else if (next.isEmpty()) "Fiyatlar alınamadı; ağ bağlantısını kontrol et." else "Fiyatlar güncel · ${java.text.SimpleDateFormat("HH:mm", Locale.getDefault()).format(java.util.Date())}"
    }
    Column(Modifier.fillMaxSize().padding(17.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Portföy", color = AText, fontSize = 26.sp, fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.SansSerif)
                Text("Yalnızca izleme · işlem ve cüzdan imzalama yok", color = ASecondary, fontSize = 11.sp)
            }
            IconButton(onClick = { refreshNo++ }) { if (busy) CircularProgressIndicator(Modifier.size(19.dp), color = AAccent, strokeWidth = 2.dp) else Icon(Icons.Outlined.Refresh, "Fiyatları yenile", tint = ASecondary) }
        }
        val total = positions.sumOf { pos -> (quotes[pos.symbol]?.price ?: 0.0) * pos.amount }
        Card(colors = CardDefaults.cardColors(containerColor = APanel), border = BorderStroke(1.dp, AStroke), shape = RoundedCornerShape(18.dp)) {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Text("GÖZLEMLENEN TOPLAM", color = AMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(CryptoRepository.formatPrice(total, currency), color = AText, fontSize = 28.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                    listOf("USD", "EUR").forEach { option ->
                        Text(option, color = if (currency == option) AInk else ASecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 5.dp).clip(RoundedCornerShape(7.dp)).background(if (currency == option) AAccent else ACard).clickable { currency = option; prefs.edit().putString("portfolio_currency", option).apply() }.padding(horizontal = 9.dp, vertical = 6.dp))
                    }
                }
                Text(message, color = AMuted, fontSize = 10.sp)
            }
        }
        Card(colors = CardDefaults.cardColors(containerColor = APanel), border = BorderStroke(1.dp, AStroke), shape = RoundedCornerShape(17.dp)) {
            Column(Modifier.fillMaxWidth().padding(13.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Text("Varlık ekle", color = AText, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(0.8f)) {
                        Text("Sembol", color = AMuted, fontSize = 10.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            CryptoRepository.symbols().take(6).forEach { candidate ->
                                Text(candidate, color = if (symbol == candidate) AInk else ASecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold,
                                    modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(if (symbol == candidate) AAccent else ACard).clickable { symbol = candidate }.padding(horizontal = 5.dp, vertical = 7.dp))
                            }
                        }
                    }
                    OutlinedTextField(value = amount, onValueChange = { amount = it.filter { ch -> ch.isDigit() || ch == '.' || ch == ',' } }, modifier = Modifier.weight(0.7f), label = { Text("Miktar") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), colors = inputColors())
                    IconButton(onClick = {
                        val value = amount.replace(',', '.').toDoubleOrNull()
                        if (value == null || value <= 0.0) message = "Pozitif bir miktar gir."
                        else { persist((positions.filterNot { it.symbol == symbol } + TrackedPosition(symbol, value)).sortedBy { it.symbol }); message = "$symbol portföye eklendi."; refreshNo++ }
                    }) { Icon(Icons.Outlined.Add, "Varlık ekle", tint = AAccent) }
                }
            }
        }
        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (positions.isEmpty()) item { Text("Portföyün boş. Yukarıdan bir varlık ve miktar ekleyebilirsin.", color = AMuted, fontSize = 12.sp, modifier = Modifier.padding(8.dp)) }
            items(positions, key = { it.symbol }) { pos ->
                val quote = quotes[pos.symbol]
                Card(colors = CardDefaults.cardColors(containerColor = APanel), border = BorderStroke(1.dp, AStroke), shape = RoundedCornerShape(14.dp)) {
                    Row(Modifier.fillMaxWidth().padding(13.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(38.dp).clip(RoundedCornerShape(11.dp)).background(ACard), contentAlignment = Alignment.Center) { Text(pos.symbol.take(1), color = AAccent, fontSize = 16.sp, fontWeight = FontWeight.Bold) }
                        Column(Modifier.weight(1f).padding(start = 10.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(pos.symbol, color = AText, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text("${"%.8f".format(Locale.US, pos.amount)} ${pos.symbol}", color = ASecondary, fontSize = 10.sp)
                            Text(quote?.let { "24 saatte ${if (it.change24h >= 0) "+" else ""}${"%.2f".format(Locale.US, it.change24h)}%" } ?: "Fiyat bulunamadı", color = quote?.let { if (it.change24h >= 0) AGreen else ARed } ?: AMuted, fontSize = 10.sp)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(quote?.let { CryptoRepository.formatPrice(it.price * pos.amount, currency) } ?: "—", color = AText, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text("adet fiyatı ${quote?.let { CryptoRepository.formatPrice(it.price, currency) } ?: "—"}", color = AMuted, fontSize = 9.sp)
                        }
                        IconButton(onClick = { persist(positions.filterNot { it.symbol == pos.symbol }); refreshNo++ }, modifier = Modifier.size(32.dp)) { Icon(Icons.Outlined.DeleteOutline, "Portföyden kaldır", tint = AMuted, modifier = Modifier.size(17.dp)) }
                    }
                }
            }
        }
        Text("Bu ekran yerel miktarları ve Coinbase Exchange'in genel spot fiyatlarını kullanır. Borsa hesabına bağlanmaz, özel anahtar istemez, alım-satım emri göndermez ve yatırım tavsiyesi vermez.", color = AMuted, fontSize = 10.sp, lineHeight = 15.sp)
    }
}

private fun loadPositions(raw: String): List<TrackedPosition> = runCatching {
    val array = JSONArray(raw)
    (0 until array.length()).mapNotNull { i -> array.optJSONObject(i)?.let { obj ->
        val symbol = obj.optString("symbol").uppercase(Locale.ROOT)
        val amount = obj.optDouble("amount", -1.0)
        if (symbol in CryptoRepository.symbols() && amount > 0.0) TrackedPosition(symbol, amount) else null
    } }.distinctBy { it.symbol }
}.getOrDefault(emptyList())

@Composable
fun DeviceStatusScreen() {
    val context = LocalContext.current
    var cpu by remember { mutableStateOf(0f) }
    var memory by remember { mutableStateOf(0f) }
    var battery by remember { mutableStateOf(0) }
    var charging by remember { mutableStateOf(false) }
    var usedStorage by remember { mutableStateOf(0L) }
    var totalStorage by remember { mutableStateOf(1L) }
    var updated by remember { mutableStateOf("Ölçüm bekleniyor") }
    var previousTicks by remember { mutableStateOf<Pair<Long, Long>?>(null) }
    LaunchedEffect(Unit) {
        while (true) {
            val sample = withContext(Dispatchers.IO) { readCpuTicks() }
            val prev = previousTicks
            if (sample != null && prev != null && sample.second > prev.second) cpu = (((sample.first - prev.first).toDouble() / (sample.second - prev.second).toDouble()) * 100.0).toFloat().coerceIn(0f, 100f)
            if (sample != null) previousTicks = sample
            val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val mem = ActivityManager.MemoryInfo().also { manager.getMemoryInfo(it) }
            memory = ((mem.totalMem - mem.availMem).toDouble() / mem.totalMem.coerceAtLeast(1L).toDouble()).toFloat().coerceIn(0f, 1f)
            val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
            battery = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY).coerceAtLeast(0)
            val batteryIntent = context.registerReceiver(null, android.content.IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
            charging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
            val fs = StatFs(Environment.getDataDirectory().path)
            totalStorage = fs.totalBytes.coerceAtLeast(1L); usedStorage = (fs.totalBytes - fs.availableBytes).coerceAtLeast(0L)
            updated = java.text.SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(java.util.Date())
            delay(3000)
        }
    }
    val runtime = Runtime.getRuntime()
    val processRam = runtime.totalMemory() - runtime.freeMemory()
    Column(Modifier.fillMaxSize().padding(17.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) { Text("Cihaz durumu", color = AText, fontSize = 25.sp, fontWeight = FontWeight.SemiBold); Text("Android sistem ölçümleri · yerel", color = ASecondary, fontSize = 11.sp) }
            Icon(Icons.Outlined.Info, null, tint = AAccent)
        }
        Text("Cihaz bilgisi", color = AMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
        Card(colors = CardDefaults.cardColors(containerColor = APanel), border = BorderStroke(1.dp, AStroke), shape = RoundedCornerShape(17.dp)) {
            Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusLine("Model", "${Build.MANUFACTURER} ${Build.MODEL}")
                StatusLine("Android", "${Build.VERSION.RELEASE} · API ${Build.VERSION.SDK_INT}")
                StatusLine("Bellek", "${formatBytes(Runtime.getRuntime().maxMemory())} uygulama sınırı")
                StatusLine("Uygulama yığını", formatBytes(processRam))
                StatusLine("Son ölçüm", updated)
            }
        }
        StatusMeter(Icons.Outlined.Tune, "İşlemci", "Yaklaşık toplam sistem kullanımı", cpu / 100f, "${cpu.toInt()}%", AAccent)
        StatusMeter(Icons.Outlined.Memory, "RAM", "Kullanılan sistem belleği", memory, "${(memory * 100).toInt()}%", if (memory > .85f) ARed else AGreen)
        StatusMeter(Icons.Outlined.BatteryChargingFull, "Pil", if (charging) "Şarj oluyor" else "Pil seviyesi", battery / 100f, "$battery%", if (battery < 20) ARed else AGreen)
        StatusMeter(Icons.Outlined.Storage, "Depolama", "Cihaz içi veri alanı", usedStorage.toFloat() / totalStorage.toFloat(), "${formatBytes(usedStorage)} / ${formatBytes(totalStorage)}", AAccent)
        Text("CPU ölçümü /proc/stat üzerinden yaklaşık hesaplanır; bazı üreticiler, güç tasarrufu ve Android arka plan kısıtları değerleri etkileyebilir. Uygulama diğer uygulamaların özel işlem ayrıntılarını okumaz.", color = AMuted, fontSize = 10.sp, lineHeight = 15.sp)
    }
}

private fun readCpuTicks(): Pair<Long, Long>? = runCatching {
    val fields = File("/proc/stat").bufferedReader().use { it.readLine() }.trim().split(Regex("\\s+")).drop(1).mapNotNull { it.toLongOrNull() }
    if (fields.size < 7) return null
    val idle = fields[3] + fields.getOrElse(4) { 0L }
    val total = fields.sum()
    Pair(total - idle, total)
}.getOrNull()

@Composable
private fun StatusMeter(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, detail: String, fraction: Float, value: String, color: Color) {
    Card(colors = CardDefaults.cardColors(containerColor = APanel), border = BorderStroke(1.dp, AStroke), shape = RoundedCornerShape(15.dp)) {
        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = color, modifier = Modifier.size(18.dp))
                Column(Modifier.weight(1f).padding(start = 9.dp)) { Text(title, color = AText, fontSize = 13.sp, fontWeight = FontWeight.SemiBold); Text(detail, color = AMuted, fontSize = 10.sp) }
                Text(value, color = AText, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
            LinearProgressIndicator(progress = { fraction.coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape), color = color, trackColor = AStroke)
        }
    }
}

@Composable
private fun StatusLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { Text(label, color = AMuted, fontSize = 11.sp, modifier = Modifier.weight(1f)); Text(value, color = AText, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) }
}

@Composable
fun MediaHubScreen() {
    val context = LocalContext.current
    var status by remember { mutableStateOf("Medya uygulamasını açabilir veya bağlantılı hesabını uygulamanın kendi izinleriyle yönetebilirsin.") }
    Column(Modifier.fillMaxSize().padding(17.dp), verticalArrangement = Arrangement.spacedBy(13.dp)) {
        Text("Medya merkezi", color = AText, fontSize = 25.sp, fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.SansSerif)
        Text("Apple Music ve Spotify kısayolları", color = ASecondary, fontSize = 11.sp)
        MediaCard("Spotify", "spotify: ile uygulamayı aç; kurulu değilse web oynatıcıyı aç.", Icons.Outlined.Headphones) {
            openExternal(context, listOf("spotify:", "https://open.spotify.com")); status = "Spotify açılma isteği gönderildi. Uygulama içi oynatma kontrolü için ayrıca medya oturumu izni gerekir."
        }
        MediaCard("Apple Music", "Apple Music web oynatıcısına git.", Icons.Outlined.MusicNote) {
            openExternal(context, listOf("music://", "https://music.apple.com")); status = "Apple Music bağlantısı açıldı."
        }
        MediaCard("YouTube Music", "Desteklenen cihazlarda varsayılan uygulamayı aç.", Icons.Outlined.GraphicEq) {
            openExternal(context, listOf("https://music.youtube.com")); status = "YouTube Music bağlantısı açıldı."
        }
        Card(colors = CardDefaults.cardColors(containerColor = APanel), border = BorderStroke(1.dp, AStroke), shape = RoundedCornerShape(15.dp)) { Text(status, color = ASecondary, fontSize = 11.sp, lineHeight = 17.sp, modifier = Modifier.padding(14.dp)) }
        Text("Not: macOS'taki Apple Events ve çalışan uygulamayı doğrudan kontrol etme mekanizması Android'de aynı şekilde bulunmaz. Bu ekran güvenilir uygulama/deep-link devrini yapar; arka plandaki tüm medya oturumlarını kontrol ettiğini iddia etmez.", color = AMuted, fontSize = 10.sp, lineHeight = 15.sp)
    }
}

@Composable
private fun MediaCard(title: String, detail: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onOpen: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onOpen), colors = CardDefaults.cardColors(containerColor = APanel), border = BorderStroke(1.dp, AStroke), shape = RoundedCornerShape(17.dp)) {
        Row(Modifier.fillMaxWidth().padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(42.dp).clip(RoundedCornerShape(13.dp)).background(ACard), contentAlignment = Alignment.Center) { Icon(icon, null, tint = AAccent, modifier = Modifier.size(23.dp)) }
            Column(Modifier.weight(1f).padding(start = 12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) { Text(title, color = AText, fontSize = 14.sp, fontWeight = FontWeight.SemiBold); Text(detail, color = ASecondary, fontSize = 11.sp, lineHeight = 15.sp) }
            Icon(Icons.Outlined.CheckCircle, null, tint = AAccent, modifier = Modifier.size(18.dp))
        }
    }
}

private fun openExternal(context: Context, candidates: List<String>) {
    candidates.forEach { target ->
        if (runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(target)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)); true }.getOrDefault(false)) return
    }
}

@Composable
fun WhatsAppScreen() {
    val context = LocalContext.current
    var phone by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var state by remember { mutableStateOf<String?>(null) }
    Column(Modifier.fillMaxSize().padding(17.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(45.dp).clip(RoundedCornerShape(14.dp)).background(ACard), contentAlignment = Alignment.Center) { Icon(Icons.Outlined.Phone, null, tint = AAccent, modifier = Modifier.size(23.dp)) }
            Column(Modifier.weight(1f).padding(start = 12.dp)) { Text("WhatsApp", color = AText, fontSize = 24.sp, fontWeight = FontWeight.SemiBold); Text("Güvenli mesaj taslağı", color = ASecondary, fontSize = 11.sp) }
        }
        OutlinedTextField(value = phone, onValueChange = { phone = it.filter { ch -> ch.isDigit() || ch == '+' } }, modifier = Modifier.fillMaxWidth(), label = { Text("Telefon numarası") }, placeholder = { Text("+90 5xx xxx xx xx") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), colors = inputColors())
        OutlinedTextField(value = message, onValueChange = { message = it }, modifier = Modifier.weight(1f).fillMaxWidth(), label = { Text("Mesaj") }, placeholder = { Text("Mesaj taslağını yaz…") }, colors = inputColors())
        Button(onClick = {
            val digits = phone.filter(Char::isDigit)
            if (digits.length < 8) state = "Geçerli ülke koduyla birlikte bir numara gir."
            else if (message.isBlank()) state = "Önce mesajını yaz."
            else runCatching {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$digits?text=${Uri.encode(message)}")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                state = "WhatsApp sohbeti açıldı. Mesajı gözden geçirip uygulamanın içinden sen gönderebilirsin."
            }.onFailure { state = "WhatsApp bağlantısı açılamadı." }
        }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = AAccent, contentColor = AInk)) { Icon(Icons.Outlined.Phone, null, modifier = Modifier.size(17.dp)); Spacer(Modifier.width(8.dp)); Text("WhatsApp'ta aç") }
        state?.let { Text(it, color = ASecondary, fontSize = 11.sp, lineHeight = 16.sp) }
        Text("Bu ekran otomatik mesaj göndermez, sohbetleri okuyamaz ve mesajı cihazdan göndermeden önce WhatsApp'ın kontrolünü sana bırakır. Mac sürümündeki yerel GOWA/OpenWA gateway işlemi ayrı sunucu kurulumu ve doğrulanmış API sözleşmesi gerektirir.", color = AMuted, fontSize = 10.sp, lineHeight = 15.sp)
    }
}

@Composable
private fun inputColors() = OutlinedTextFieldDefaults.colors(focusedBorderColor = AAccent, unfocusedBorderColor = AStroke, focusedTextColor = AText, unfocusedTextColor = AText, focusedLabelColor = AAccent, unfocusedLabelColor = ASecondary, cursorColor = AAccent)

private fun formatBytes(bytes: Long): String {
    if (bytes < 1024) return "$bytes B"
    val units = listOf("KB", "MB", "GB", "TB")
    var value = bytes.toDouble()
    var index = -1
    do { value /= 1024.0; index++ } while (value >= 1024.0 && index < units.lastIndex)
    return String.format(Locale.getDefault(), "%.1f %s", value, units[index.coerceIn(0, units.lastIndex)])
}
