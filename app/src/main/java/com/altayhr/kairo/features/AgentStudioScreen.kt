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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
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
import androidx.compose.foundation.verticalScroll
import com.altayhr.kairo.data.AgentRunRecord
import com.altayhr.kairo.data.AgentRunStore
import com.altayhr.kairo.data.ChatSession
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val AInk = Color(0xFF090A10)
private val APanel = Color(0xFF12141D)
private val ACard = Color(0xFF191C27)
private val AStroke = Color(0xFF2B3040)
private val AText = Color(0xFFF4F5F7)
private val ASecondary = Color(0xFF9DA5B5)
private val AAccent = Color(0xFFAA9BFF)
private val AGreen = Color(0xFF66D6C0)

private data class AgentMode(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val instruction: String,
    val visibleSteps: List<String>
)

private val modes = listOf(
    AgentMode("build", "Geliştir", "Özellik tasarla ve dosya çıktıları üret.", Icons.Outlined.Code,
        "Bir kıdemli Android/Kotlin geliştiricisi gibi çalış. Önce kısa bir çözüm planı sun; ardından tam dosyaları, yollarıyla kod bloklarında üret. Değişiklikleri küçük ve bütünlüklü tut.",
        listOf("Hedefi ve dosya bağlamını belirle", "Çözüm planını oluştur", "Dosyaları ve uygulama adımlarını hazırla", "Riskleri ve testleri özetle")),
    AgentMode("review", "Kod incele", "Hataları, güvenlik ve mimari sorunlarını bul.", Icons.Outlined.Security,
        "Bir kod inceleme ajanı gibi davran. Öncelikle gerçek kusurları önem sırasına göre açıkla; dosya ve satır bağlamı varsa belirt. Kanıtlayamadığın sorunları kesin gerçek gibi sunma. Gerekirse düzeltilmiş dosyaları eksiksiz üret.",
        listOf("Kaynakları ve varsayımları incele", "Hataları önem derecesine göre sırala", "Düzeltmeleri öner ve uygula", "Regresyon testlerini belirt")),
    AgentMode("debug", "Hata ayıkla", "Belirtiyi daraltıp kök nedeni araştır.", Icons.Outlined.BugReport,
        "Hata ayıklama ajanı gibi çalış. Eldeki hata iletileri ve dosya bağlamına dayan. Kök neden hipotezlerini ayır, en olası nedeni açıkla ve düzeltilmiş kodu üret. Çalıştırılmayan komut veya testleri çalıştırılmış gibi gösterme.",
        listOf("Belirtiyi ve kanıtı ayır", "Kök nedenleri sınırla", "En küçük güvenli düzeltmeyi hazırla", "Doğrulama adımlarını listele")),
    AgentMode("research", "Araştır", "Web kaynaklarını karşılaştırıp bulguları toparla.", Icons.Outlined.Language,
        "Araştırma ajanı gibi çalış. Web araması etkinse kaynak bağlantıları ve tarihleriyle kanıt topla. Kaynaklar çelişiyorsa bunu açıkça belirt. Sonucu özet, kanıtlar ve uygulanabilir öneriler halinde düzenle.",
        listOf("Soruyu alt başlıklara ayır", "Kaynakları ve güncelliği kontrol et", "Bulguları karşılaştır", "Kaynaklı sonuç ve öneri üret")),
    AgentMode("plan", "Planla", "Büyük görevleri izlenebilir adımlara böl.", Icons.Outlined.Tune,
        "Bir proje planlama ajanı gibi çalış. Kısa görev kartı üret: hedef, kapsam, bağımlılıklar, aşamalar, kabul ölçütleri ve riskler. Gizli iç düşünce dökümü verme; yalnızca kullanıcıya yararlı plan ve gerekçeleri paylaş.",
        listOf("Kapsamı ve tamamlanma ölçütünü netleştir", "Bağımlılık ve riskleri belirle", "Aşamaları ve çıktıları planla", "Kontrol listesini hazırla"))
)

@Composable
fun AgentStudioScreen(
    chats: List<ChatSession>,
    loadingChatId: String?,
    onOpenChat: (ChatSession) -> Unit,
    onRun: (String, Boolean) -> Unit
) {
    var selectedModeId by remember { mutableStateOf("build") }
    var goal by remember { mutableStateOf("") }
    var useWebSearch by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val runStore = remember(context) { AgentRunStore(context) }
    var taskRuns by remember { mutableStateOf(runStore.recent(15)) }
    val timeFormat = remember { SimpleDateFormat("HH:mm · dd MMM", Locale("tr", "TR")) }
    LaunchedEffect(runStore) { while (true) { taskRuns = runStore.recent(15); delay(1200) } }
    val selectedMode = modes.first { it.id == selectedModeId }
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(17.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(46.dp).clip(RoundedCornerShape(14.dp)).background(AAccent.copy(alpha = .14f)), contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.Tune, null, tint = AAccent, modifier = Modifier.size(25.dp))
            }
            Column(Modifier.weight(1f).padding(start = 12.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text("Görev merkezi", color = AText, fontSize = 25.sp, fontWeight = FontWeight.SemiBold)
                Text("Planla · üret · kontrol et", color = ASecondary, fontSize = 11.sp)
            }
        }
        Card(colors = CardDefaults.cardColors(containerColor = APanel), border = androidx.compose.foundation.BorderStroke(1.dp, AStroke), shape = RoundedCornerShape(18.dp)) {
            Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(11.dp)) {
                Text("ÇALIŞMA MODU", color = ASecondary, fontSize = 9.sp, letterSpacing = 1.5.sp, fontWeight = FontWeight.Bold)
                modes.forEach { mode ->
                    val selected = mode.id == selectedModeId
                    Row(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(13.dp))
                            .background(if (selected) AAccent.copy(alpha = .10f) else ACard)
                            .border(1.dp, if (selected) AAccent.copy(alpha = .7f) else AStroke, RoundedCornerShape(13.dp))
                            .clickable { selectedModeId = mode.id }.padding(11.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(if (selected) AAccent.copy(alpha = .17f) else APanel), contentAlignment = Alignment.Center) {
                            Icon(mode.icon, null, tint = if (selected) AAccent else ASecondary, modifier = Modifier.size(19.dp))
                        }
                        Column(Modifier.weight(1f).padding(start = 10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(mode.title, color = AText, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Text(mode.description, color = ASecondary, fontSize = 10.sp, lineHeight = 14.sp)
                        }
                        if (selected) Box(Modifier.size(7.dp).clip(CircleShape).background(AAccent))
                    }
                }
                Text("GÖREV HEDEFİ", color = ASecondary, fontSize = 9.sp, letterSpacing = 1.5.sp, fontWeight = FontWeight.Bold)
                Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(13.dp)).background(AInk).border(1.dp, AStroke, RoundedCornerShape(13.dp)).padding(13.dp)) {
                    if (goal.isBlank()) Text("Örn. Projedeki giriş akışını incele, güvenlik açıklarını düzelt ve değişen dosyaları hazırla…", color = ASecondary.copy(alpha = .8f), fontSize = 12.sp, lineHeight = 18.sp)
                    BasicTextField(value = goal, onValueChange = { goal = it }, modifier = Modifier.fillMaxWidth(), textStyle = androidx.compose.ui.text.TextStyle(color = AText, fontSize = 12.sp, lineHeight = 19.sp), minLines = 4, maxLines = 8)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = useWebSearch, onCheckedChange = { useWebSearch = it })
                    Icon(Icons.Outlined.Search, null, tint = ASecondary, modifier = Modifier.size(16.dp))
                    Text("Web araması ve kaynaklar", color = ASecondary, fontSize = 11.sp, modifier = Modifier.weight(1f))
                }
                Button(
                    onClick = {
                        val prompt = buildString {
                            append("KAIRO GÖREV KARTI\nÇalışma modu: ${selectedMode.title}\nHedef: ${goal.trim()}\n\n")
                            append("İŞ AKIŞI\n")
                            selectedMode.visibleSteps.forEachIndexed { index, step -> append("${index + 1}. $step\n") }
                            append("\nÇALIŞMA TALİMATI\n${selectedMode.instruction}\n")
                            append("\nSonuçta kısa bir görünür plan, yapılan analiz/çıktıların özeti, varsa tam dosya içerikleri (dosya yolu kod bloğu başlığında), doğrulama adımları ve açık kalan kısıtları ver. Gizli iç düşünce dökümü verme. Terminal veya gerçek cihaz testi gerekiyorsa bunu yapılmış gibi sunma.")
                        }
                        onRun(prompt, useWebSearch)
                    },
                    enabled = goal.isNotBlank() && loadingChatId == null,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = AAccent, contentColor = AInk),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowForward, null, modifier = Modifier.size(17.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(if (loadingChatId != null) "Görev çalışıyor…" else "Görevi başlat", fontWeight = FontWeight.Bold)
                }
            }
        }
        Card(colors = CardDefaults.cardColors(containerColor = APanel), border = androidx.compose.foundation.BorderStroke(1.dp, AStroke), shape = RoundedCornerShape(18.dp)) {
            Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Text("GÖREV AŞAMALARI", color = ASecondary, fontSize = 9.sp, letterSpacing = 1.5.sp, fontWeight = FontWeight.Bold)
                selectedMode.visibleSteps.forEachIndexed { index, step ->
                    Row(verticalAlignment = Alignment.Top) {
                        Box(Modifier.size(22.dp).clip(CircleShape).background(if (index == 0) AAccent.copy(alpha = .19f) else ACard).border(1.dp, if (index == 0) AAccent else AStroke, CircleShape), contentAlignment = Alignment.Center) {
                            Text("${index + 1}", color = if (index == 0) AAccent else ASecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Text(step, color = AText, fontSize = 11.sp, modifier = Modifier.padding(start = 9.dp, top = 4.dp), lineHeight = 15.sp)
                    }
                }
                Text("Bu kart, kullanıcıya gösterilen görev planıdır. Modelin gizli iç düşünce metni gösterilmez.", color = ASecondary, fontSize = 10.sp, lineHeight = 15.sp)
            }
        }
        if (chats.isNotEmpty()) {
            Text("SON GÖREVLER", color = ASecondary, fontSize = 9.sp, letterSpacing = 1.5.sp, fontWeight = FontWeight.Bold)
            chats.sortedByDescending { it.updatedAt }.take(8).forEach { chat ->
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(APanel).border(1.dp, AStroke, RoundedCornerShape(12.dp))
                        .clickable { onOpenChat(chat) }.padding(12.dp), verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(chat.title, color = AText, fontSize = 12.sp, fontWeight = FontWeight.Medium, maxLines = 1)
                        Text("${chat.messages.size} mesaj · ${if (chat.pinned) "Sabitlenmiş" else "Sohbet arşivi"}", color = ASecondary, fontSize = 10.sp)
                    }
                    Icon(Icons.AutoMirrored.Outlined.ArrowForward, null, tint = AAccent, modifier = Modifier.size(17.dp))
                }
            }
        }
        if (taskRuns.isNotEmpty()) {
            Text("KALICI GÖREV GEÇMİŞİ", color = ASecondary, fontSize = 9.sp, letterSpacing = 1.5.sp, fontWeight = FontWeight.Bold)
            taskRuns.take(10).forEach { run ->
                val runChat = chats.firstOrNull { it.id == run.chatId }
                val statusColor = when (run.outcome) { "success" -> AGreen; "error" -> Color(0xFFFF7D88); else -> AAccent }
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(13.dp)).background(APanel).border(1.dp, AStroke, RoundedCornerShape(13.dp))
                        .clickable(enabled = runChat != null) { runChat?.let(onOpenChat) }.padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(Modifier.padding(top = 4.dp).size(8.dp).clip(CircleShape).background(statusColor))
                    Column(Modifier.weight(1f).padding(start = 10.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(run.chatTitle, color = AText, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                        Text("${run.stage} · ${run.status}", color = statusColor, fontSize = 10.sp, maxLines = 2)
                        Text("${timeFormat.format(Date(run.updatedAt))}${if (run.webSearch) " · Web araması" else " · Web kapalı"}", color = ASecondary, fontSize = 9.sp)
                    }
                }
            }
        }
        Text("Güvenlik sınırı: Bu görev merkezi Gemini ile plan ve dosya çıktısı üretir. Terminal komutları, masaüstü ajanları veya dış servislerde onay olmadan eylem yürütmez.", color = ASecondary, fontSize = 10.sp, lineHeight = 15.sp)
    }
}
