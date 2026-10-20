package com.altayhr.kairo.features

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.TextSnippet
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.altayhr.kairo.data.ChatSession
import com.altayhr.kairo.data.FlowToolkitStore
import com.altayhr.kairo.data.DiffLine
import com.altayhr.kairo.data.UnifiedDiff
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val TInk = Color(0xFF090A10)
private val TPanel = Color(0xFF12141D)
private val TCard = Color(0xFF191C27)
private val TBorder = Color(0xFF2B3040)
private val TText = Color(0xFFF4F5F7)
private val TSecondary = Color(0xFF9DA5B5)
private val TMuted = Color(0xFF72798A)
private val TAccent = Color(0xFFAA9BFF)
private val TGreen = Color(0xFF66D6C0)

private data class ToolkitTab(val id: String, val title: String)
private val toolkitTabs = listOf(
    ToolkitTab("dictionary", "Sözlük"),
    ToolkitTab("snippets", "Metin parçaları"),
    ToolkitTab("styles", "Yanıt stili"),
    ToolkitTab("diff", "Fark karşılaştırma"),
    ToolkitTab("insights", "İçgörüler"),
    ToolkitTab("shortcuts", "Kısayollar")
)

@Composable
fun FlowToolkitScreen(
    chats: List<ChatSession>,
    store: FlowToolkitStore,
    onUseSnippet: (String) -> Unit,
    onRunPrompt: (String, Boolean) -> Unit
) {
    var selectedTab by remember { mutableStateOf("dictionary") }
    Column(Modifier.fillMaxSize().background(TInk)) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 17.dp, vertical = 14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(43.dp).clip(RoundedCornerShape(13.dp)).background(TAccent.copy(alpha = .15f)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.AutoAwesome, null, tint = TAccent, modifier = Modifier.size(23.dp))
                }
                Column(Modifier.padding(start = 11.dp).weight(1f)) {
                    Text("Akıllı araçlar", color = TText, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
                    Text("Yazım tercihleri, yeniden kullanılabilir komutlar ve kullanım istatistikleri", color = TSecondary, fontSize = 10.sp)
                }
            }
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                toolkitTabs.forEach { tab ->
                    FilterChip(
                        selected = selectedTab == tab.id,
                        onClick = { selectedTab = tab.id },
                        label = { Text(tab.title, fontSize = 10.sp) }
                    )
                }
            }
        }
        Box(Modifier.weight(1f).fillMaxWidth()) {
            when (selectedTab) {
                "dictionary" -> DictionaryToolScreen(store)
                "snippets" -> SnippetToolScreen(store, onUseSnippet)
                "styles" -> ResponseStyleToolScreen(store)
                "diff" -> DiffToolScreen()
                "insights" -> ActivityInsightsToolScreen(chats)
                else -> QuickActionsToolScreen(onRunPrompt)
            }
        }
    }
}

@Composable
private fun DictionaryToolScreen(store: FlowToolkitStore) {
    var words by remember { mutableStateOf(store.words()) }
    var replacements by remember { mutableStateOf(store.replacements()) }
    var newWord by remember { mutableStateOf("") }
    var from by remember { mutableStateOf("") }
    var to by remember { mutableStateOf("") }
    var search by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ToolkitCard {
            ToolHeading(Icons.Outlined.Book, "Özel sözlük", "Özel ürün adları, isimler ve teknik terimler yazım tercihlerinde korunur.")
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                OutlinedTextField(value = newWord, onValueChange = { newWord = it }, modifier = Modifier.weight(1f), singleLine = true, label = { Text("Yeni kelime") })
                Button(onClick = { words = store.addWord(newWord); newWord = "" }, enabled = newWord.isNotBlank(), colors = ButtonDefaults.buttonColors(containerColor = TAccent, contentColor = TInk)) {
                    Icon(Icons.Outlined.Add, null, modifier = Modifier.size(17.dp))
                }
            }
            OutlinedTextField(value = search, onValueChange = { search = it }, modifier = Modifier.fillMaxWidth(), singleLine = true, label = { Text("Sözlükte ara") }, leadingIcon = { Icon(Icons.Outlined.Search, null) })
            if (words.isEmpty()) Text("Henüz özel kelime yok. Ürün adlarını ve sık kullanılan terimleri buraya ekleyebilirsin.", color = TMuted, fontSize = 11.sp, lineHeight = 17.sp)
            val shownWords = words.filter { search.isBlank() || it.contains(search, true) }
            shownWords.chunked(2).forEach { rowWords ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    rowWords.forEach { word ->
                        Row(Modifier.weight(1f).clip(RoundedCornerShape(10.dp)).background(TCard).border(1.dp, TBorder, RoundedCornerShape(10.dp)).padding(start = 10.dp, end = 3.dp, top = 3.dp, bottom = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(word, color = TText, fontSize = 11.sp, modifier = Modifier.weight(1f), maxLines = 2, overflow = TextOverflow.Ellipsis)
                            IconButton(onClick = { words = store.removeWord(word) }, modifier = Modifier.size(29.dp)) { Icon(Icons.Outlined.DeleteOutline, "Kelimeyi sil", tint = TSecondary, modifier = Modifier.size(15.dp)) }
                        }
                    }
                    if (rowWords.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }
        ToolkitCard {
            ToolHeading(Icons.Outlined.Tune, "Dönüşüm kuralları", "Sık yanlış tanınan bir sözcüğü tercih ettiğin yazıma eşleştir.")
            OutlinedTextField(value = from, onValueChange = { from = it }, modifier = Modifier.fillMaxWidth(), singleLine = true, label = { Text("Gelen / yanlış yazım") })
            OutlinedTextField(value = to, onValueChange = { to = it }, modifier = Modifier.fillMaxWidth(), singleLine = true, label = { Text("Tercih edilen yazım") })
            Button(onClick = { replacements = store.saveReplacement(from, to); from = ""; to = "" }, enabled = from.isNotBlank() && to.isNotBlank(), colors = ButtonDefaults.buttonColors(containerColor = TAccent, contentColor = TInk)) { Text("Dönüşüm ekle") }
            if (replacements.isEmpty()) Text("Henüz dönüşüm kuralı yok.", color = TMuted, fontSize = 11.sp)
            replacements.forEach { replacement ->
                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(TCard).padding(horizontal = 10.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(replacement.from, color = TSecondary, fontSize = 11.sp, modifier = Modifier.weight(1f), maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text("→", color = TAccent, fontSize = 14.sp, modifier = Modifier.padding(horizontal = 8.dp))
                    Text(replacement.to, color = TText, fontSize = 11.sp, modifier = Modifier.weight(1f), maxLines = 2, overflow = TextOverflow.Ellipsis)
                    IconButton(onClick = { replacements = store.removeReplacement(replacement.id) }, modifier = Modifier.size(30.dp)) { Icon(Icons.Outlined.DeleteOutline, "Dönüşümü sil", tint = TSecondary, modifier = Modifier.size(15.dp)) }
                }
            }
            Text("Güvenlik: Kod blokları, URL'ler, dosya yolları ve tanımlayıcılar otomatik değiştirilmez. Tercihler Gemini'ye ek bağlam olarak iletilir.", color = TMuted, fontSize = 10.sp, lineHeight = 15.sp)
        }
    }
}

@Composable
private fun SnippetToolScreen(store: FlowToolkitStore, onUseSnippet: (String) -> Unit) {
    var snippets by remember { mutableStateOf(store.snippets()) }
    var trigger by remember { mutableStateOf("") }
    var text by remember { mutableStateOf("") }
    var editingId by remember { mutableStateOf<String?>(null) }
    var search by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ToolkitCard {
            ToolHeading(Icons.Outlined.TextSnippet, if (editingId == null) "Metin parçası oluştur" else "Metin parçasını düzenle", "Tekrar kullandığın talimatları kaydet; listeden tek dokunuşla sohbete aktar.")
            OutlinedTextField(value = trigger, onValueChange = { trigger = it }, modifier = Modifier.fillMaxWidth(), singleLine = true, label = { Text("Kısa ad / tetikleyici") })
            OutlinedTextField(value = text, onValueChange = { text = it }, modifier = Modifier.fillMaxWidth(), minLines = 4, maxLines = 10, label = { Text("Tam metin") })
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { snippets = store.saveSnippet(trigger, text, editingId); trigger = ""; text = ""; editingId = null }, enabled = trigger.isNotBlank() && text.isNotBlank(), colors = ButtonDefaults.buttonColors(containerColor = TAccent, contentColor = TInk)) {
                    Text(if (editingId == null) "Kaydet" else "Değişiklikleri kaydet")
                }
                if (editingId != null) TextButton(onClick = { trigger = ""; text = ""; editingId = null }) { Text("Vazgeç", color = TSecondary) }
            }
        }
        OutlinedTextField(value = search, onValueChange = { search = it }, modifier = Modifier.fillMaxWidth(), singleLine = true, label = { Text("Metin parçalarında ara") }, leadingIcon = { Icon(Icons.Outlined.Search, null) })
        val visible = snippets.filter { search.isBlank() || it.trigger.contains(search, true) || it.text.contains(search, true) }
        if (visible.isEmpty()) Text("Kaydedilmiş metin parçası yok. Sık kullandığın istemleri buraya ekleyebilirsin.", color = TMuted, fontSize = 11.sp, lineHeight = 17.sp)
        visible.forEach { snippet ->
            ToolkitCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(snippet.trigger, color = TText, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text(snippet.text, color = TSecondary, fontSize = 11.sp, lineHeight = 16.sp, maxLines = 5, overflow = TextOverflow.Ellipsis)
                    }
                    IconButton(onClick = { editingId = snippet.id; trigger = snippet.trigger; text = snippet.text }, modifier = Modifier.size(34.dp)) { Icon(Icons.Outlined.Edit, "Düzenle", tint = TAccent, modifier = Modifier.size(17.dp)) }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { onUseSnippet(snippet.text) }, colors = ButtonDefaults.buttonColors(containerColor = TAccent, contentColor = TInk)) { Icon(Icons.Outlined.ContentCopy, null, modifier = Modifier.size(15.dp)); Spacer(Modifier.width(6.dp)); Text("Sohbete aktar") }
                    TextButton(onClick = { snippets = store.removeSnippet(snippet.id); if (editingId == snippet.id) { editingId = null; trigger = ""; text = "" } }) { Icon(Icons.Outlined.DeleteOutline, null, modifier = Modifier.size(15.dp)); Spacer(Modifier.width(4.dp)); Text("Sil", color = TSecondary) }
                }
            }
        }
    }
}

@Composable
private fun ResponseStyleToolScreen(store: FlowToolkitStore) {
    var selectedStyle by remember { mutableStateOf(store.responseStyle()) }
    var cleanup by remember { mutableStateOf(store.cleanupEnabled()) }
    data class StyleOption(val id: String, val title: String, val detail: String)
    val options = listOf(
        StyleOption("natural", "Doğal", "Dengeli, anlaşılır Türkçe; konuşma diline yakın."),
        StyleOption("concise", "Kısa ve doğrudan", "Önce cevap, ardından yalnızca gerekli ayrıntılar."),
        StyleOption("deep", "Derin ve kapsamlı", "Riskler, seçenekler, varsayımlar ve doğrulama adımları."),
        StyleOption("professional", "Profesyonel", "Düzenli, tarafsız ve teslimata odaklı anlatım."),
        StyleOption("friendly", "Samimi", "Sıcak ton, sade dil ve gerektiğinde örnekler."),
        StyleOption("technical", "Teknik", "Dosya yolları, hata koşulları ve API ayrıntıları." )
    )
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ToolkitCard {
            ToolHeading(Icons.Outlined.Tune, "Varsayılan yanıt stili", "Bu tercih yeni Gemini yanıtlarının sistem yönergesine eklenir; modelin desteklemediği davranışlar garanti edilmez.")
            options.forEach { option ->
                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(if (selectedStyle == option.id) TAccent.copy(alpha = .10f) else TCard).border(1.dp, if (selectedStyle == option.id) TAccent.copy(alpha = .65f) else TBorder, RoundedCornerShape(12.dp)).clickable { selectedStyle = option.id; store.saveResponseStyle(option.id) }.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(option.title, color = TText, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Text(option.detail, color = TSecondary, fontSize = 10.sp, lineHeight = 15.sp)
                    }
                    if (selectedStyle == option.id) Icon(Icons.Outlined.Check, null, tint = TAccent, modifier = Modifier.size(19.dp))
                }
            }
        }
        ToolkitCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text("Yazım / dikte temizliği", color = TText, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Text("Sözlük ve dönüşüm kurallarını belirsiz yazımlarda yardımcı bağlam olarak kullan.", color = TSecondary, fontSize = 10.sp, lineHeight = 15.sp)
                }
                Switch(checked = cleanup, onCheckedChange = { cleanup = it; store.saveCleanupEnabled(it) })
            }
            Text("Kod, URL, dosya yolu, komut ve değişken adlarını otomatik değiştirme talimatı ayrıca uygulanır.", color = TMuted, fontSize = 10.sp, lineHeight = 15.sp)
        }
    }
}

@Composable
private fun DiffToolScreen() {
    val clipboard = LocalClipboardManager.current
    var oldText by remember { mutableStateOf("") }
    var newText by remember { mutableStateOf("") }
    var diff by remember { mutableStateOf<List<DiffLine>>(emptyList()) }
    var compared by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ToolkitCard {
            ToolHeading(Icons.Outlined.Code, "Yerel metin / kod farkı", "İki sürümü karşılaştır. İçerik ağ üzerinden gönderilmez ve hiçbir kod çalıştırılmaz.")
            OutlinedTextField(value = oldText, onValueChange = { oldText = it; compared = false }, modifier = Modifier.fillMaxWidth(), minLines = 4, maxLines = 9, label = { Text("Önceki sürüm") }, placeholder = { Text("Önceki kodu veya metni yapıştır") })
            OutlinedTextField(value = newText, onValueChange = { newText = it; compared = false }, modifier = Modifier.fillMaxWidth(), minLines = 4, maxLines = 9, label = { Text("Yeni sürüm") }, placeholder = { Text("Yeni kodu veya metni yapıştır") })
            Button(onClick = { diff = UnifiedDiff.compare(oldText, newText); compared = true; status = "" }, modifier = Modifier.fillMaxWidth(), enabled = oldText.isNotEmpty() || newText.isNotEmpty(), colors = ButtonDefaults.buttonColors(containerColor = TAccent, contentColor = TInk)) { Text("Farkları karşılaştır") }
        }
        if (compared) {
            val additions = diff.count { it.kind == '+' }
            val removals = diff.count { it.kind == '-' }
            ToolkitCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text("Karşılaştırma sonucu", color = TText, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text("+$additions ekleme · −$removals çıkarma · ${diff.size} satır", color = TSecondary, fontSize = 10.sp)
                    }
                    TextButton(onClick = { clipboard.setText(AnnotatedString(diff.joinToString("\n") { row -> "${row.kind} ${row.text}" })); status = "Fark çıktısı panoya kopyalandı" }) {
                        Icon(Icons.Outlined.ContentCopy, null, modifier = Modifier.size(15.dp)); Spacer(Modifier.width(4.dp)); Text("Kopyala", color = TAccent, fontSize = 10.sp)
                    }
                }
                Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color(0xFF08090D)).padding(8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    diff.take(700).forEach { line ->
                        val (prefix, color, tint) = when (line.kind) {
                            '+' -> Triple("+", Color(0xFF17352D), TGreen)
                            '-' -> Triple("−", Color(0xFF3B2029), Color(0xFFFF8E9A))
                            '!' -> Triple("!", Color(0xFF3A3020), Color(0xFFFFD28C))
                            else -> Triple(" ", Color.Transparent, TSecondary)
                        }
                        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(4.dp)).background(color).padding(horizontal = 6.dp, vertical = 3.dp), verticalAlignment = Alignment.Top) {
                            Text(prefix, color = tint, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(14.dp))
                            Text(line.text, color = if (line.kind == ' ') TSecondary else tint, fontSize = 10.sp, lineHeight = 14.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                        }
                    }
                }
                if (diff.size > 700) Text("Görünüm ilk 700 satırla sınırlandı; panoya kopyalama tam karşılaştırmayı içerir.", color = TMuted, fontSize = 10.sp)
                if (status.isNotBlank()) Text(status, color = TGreen, fontSize = 10.sp)
            }
        }
    }
}

@Composable
private fun ActivityInsightsToolScreen(chats: List<ChatSession>) {
    var period by remember { mutableStateOf("week") }
    val now = System.currentTimeMillis()
    val start = when (period) {
        "week" -> now - 7L * 24 * 60 * 60 * 1000
        "month" -> now - 30L * 24 * 60 * 60 * 1000
        else -> 0L
    }
    val allMessages = chats.flatMap { it.messages }
    val messages = allMessages.filter { it.createdAt >= start }
    val userMessages = messages.filter { it.role == "user" }
    val assistantMessages = messages.filter { it.role == "assistant" }
    val words = userMessages.sumOf { it.content.trim().split(Regex("\\s+")).count(String::isNotBlank) }
    val responseWords = assistantMessages.sumOf { it.content.trim().split(Regex("\\s+")).count(String::isNotBlank) }
    val files = chats.sumOf { chat -> chat.files.count { it.createdAt >= start } }
    val sources = messages.sumOf { it.sources.size }
    val activeDays = messages.map { it.createdAt / (24L * 60 * 60 * 1000) }.distinct().size
    val dateFormat = remember { SimpleDateFormat("dd MMM", Locale("tr", "TR")) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ToolkitCard {
            ToolHeading(Icons.Outlined.BarChart, "Kullanım içgörüleri", "Bu sayılar yalnızca cihazında kayıtlı Kairo sohbetlerinden hesaplanır; sesli dikte geçmişi veya başka uygulamalar izlenmez.")
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                listOf("week" to "7 gün", "month" to "30 gün", "all" to "Tümü").forEach { (id, label) ->
                    FilterChip(selected = period == id, onClick = { period = id }, label = { Text(label, fontSize = 11.sp) })
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricCard("Sohbet", chats.count { it.updatedAt >= start }.toString(), Modifier.weight(1f))
                MetricCard("Mesaj", messages.size.toString(), Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricCard("Kullanıcı kelimesi", words.toString(), Modifier.weight(1f))
                MetricCard("Yanıt kelimesi", responseWords.toString(), Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricCard("Aktif gün", activeDays.toString(), Modifier.weight(1f))
                MetricCard("Kaynak / dosya", "$sources / $files", Modifier.weight(1f))
            }
            Text("Güncel dönem: ${if (start == 0L) "Tüm kayıtlar" else dateFormat.format(Date(start)) + " – " + dateFormat.format(Date(now))}", color = TMuted, fontSize = 10.sp)
        }
        ToolkitCard {
            Text("Son konuşmalar", color = TText, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            val recent = chats.filter { it.updatedAt >= start }.sortedByDescending { it.updatedAt }.take(8)
            if (recent.isEmpty()) Text("Bu dönemde kayıtlı sohbet yok.", color = TMuted, fontSize = 11.sp)
            recent.forEach { chat ->
                Row(Modifier.fillMaxWidth().padding(vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(chat.title, color = TText, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("${chat.messages.size} mesaj · ${chat.files.size} dosya", color = TSecondary, fontSize = 10.sp)
                    }
                    Text(dateFormat.format(Date(chat.updatedAt)), color = TMuted, fontSize = 10.sp)
                }
            }
        }
    }
}

@Composable
private fun QuickActionsToolScreen(onRunPrompt: (String, Boolean) -> Unit) {
    data class QuickAction(val title: String, val detail: String, val prompt: String, val search: Boolean = false)
    val actions = listOf(
        QuickAction("Kod incelemesi", "Bir diff veya dosya ekleyip hataları, riskleri ve iyileştirmeleri bul.", "Eklediğim kodu incele. Önce yüksek riskli hata ve güvenlik sorunlarını, sonra bakım ve performans önerilerini sırala. Mümkünse dosya yolları ve düzeltme kodları ver."),
        QuickAction("Test üret", "Koddan birim testleri ve uç durumlar tasarla.", "Bu proje/dosya için anlamlı birim ve entegrasyon testleri tasarla. Normal akış, sınır değerleri, hatalı girdi ve regresyonları kapsa. Test dosyalarını ayrı kod blokları halinde ver."),
        QuickAction("Dokümantasyon", "API, kurulum ve kullanım adımlarını hazırla.", "Ekli proje bağlamına göre Türkçe README ve gerekli API/kurulum dokümantasyonunu hazırla. Komutları çalıştırılmış gibi gösterme; gerekli ortam değişkenlerini ve doğrulama adımlarını belirt."),
        QuickAction("Mimari plan", "Büyük bir işi küçük teslimatlara böl.", "Bu hedef için uygulanabilir mimari plan hazırla. Modülleri, veri akışını, dosya yollarını, bağımlılıkları, kabul ölçütlerini ve riskleri sırala. Her aşama için doğrulama ölçütü ekle."),
        QuickAction("Araştırma notu", "Güncel kaynaklardan kanıta dayalı cevap oluştur.", "Konuyu güncel ve güvenilir kaynaklarla araştır. Temel iddiaların yanında kaynak URL'leri ve yayın tarihlerini ver; çelişen bulguları belirt, sonunda uygulanabilir kısa öneri hazırla.", true),
        QuickAction("Hata raporu", "Hata iletisi üzerinden kök neden ve düzeltme üret.", "Aşağıdaki hata/proje bağlamını analiz et. Kanıtları varsayımlardan ayır, en olası kök nedenleri sırala, en küçük güvenli düzeltmeyi kodla ve gerçek ortamda uygulanması gereken testleri listele. Komut veya test çalıştırdığını iddia etme."),
        QuickAction("Performans kontrolü", "Yavaş noktaları ve ölçüm planını incele.", "Kod tabanında performans risklerini ara. Gereksiz IO/ağ çağrıları, pahalı döngüler, bellek ömrü ve eşzamanlılık sorunlarını dosya yollarıyla belirt. Önce ölçüm planı, sonra en çok fayda sağlayacak düzeltmeleri öner.")
    )
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        ToolkitCard {
            ToolHeading(Icons.Outlined.AutoAwesome, "Tek dokunuşla iş akışları", "Bir kısayol yeni sohbet açar ve seçtiğin görevi Gemini görev kuyruğuna gönderir. Web isteyen kısayol kaynak aramasını da etkinleştirir.")
        }
        actions.forEach { action ->
            ToolkitCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(action.title, color = TText, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text(action.detail, color = TSecondary, fontSize = 10.sp, lineHeight = 15.sp)
                    }
                    if (action.search) Icon(Icons.Outlined.Language, "Web araması", tint = TAccent, modifier = Modifier.size(19.dp))
                }
                Button(onClick = { onRunPrompt(action.prompt, action.search) }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = TAccent, contentColor = TInk)) {
                    Text("Bu görevi başlat")
                }
            }
        }
    }
}

@Composable
private fun ToolkitCard(content: @Composable ColumnScope.() -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = TPanel), border = androidx.compose.foundation.BorderStroke(1.dp, TBorder), shape = RoundedCornerShape(17.dp)) {
        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp), content = content)
    }
}

@Composable
private fun ToolHeading(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, detail: String) {
    Row(verticalAlignment = Alignment.Top) {
        Box(Modifier.size(34.dp).clip(RoundedCornerShape(10.dp)).background(TAccent.copy(alpha = .13f)), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = TAccent, modifier = Modifier.size(18.dp))
        }
        Column(Modifier.padding(start = 9.dp).weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(title, color = TText, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text(detail, color = TSecondary, fontSize = 10.sp, lineHeight = 15.sp)
        }
    }
}

@Composable
private fun MetricCard(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier.clip(RoundedCornerShape(12.dp)).background(TCard).border(1.dp, TBorder, RoundedCornerShape(12.dp)).padding(11.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text(label, color = TSecondary, fontSize = 10.sp)
        Text(value, color = TAccent, fontSize = 21.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}
