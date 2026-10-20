package com.altayhr.kairo.automation

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.work.WorkManager
import com.altayhr.kairo.data.SecureApiKeyStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

private val LInk = Color(0xFF090A10)
private val LPanel = Color(0xFF12141D)
private val LCard = Color(0xFF191C27)
private val LStroke = Color(0xFF2B3040)
private val LText = Color(0xFFF4F5F7)
private val LSecond = Color(0xFF9DA5B5)
private val LAccent = Color(0xFFAA9BFF)
private val LGreen = Color(0xFF66D6C0)
private val LGold = Color(0xFFFFC86B)
private val LRed = Color(0xFFFF7D88)

private val actionOptions = listOf(
    "http" to "HTTP isteği",
    "n8n" to "n8n webhook'a istek",
    "github_dispatch" to "GitHub Actions çalıştır",
    "resend_email" to "Resend e-posta gönder",
    "notion_page" to "Notion sayfası oluştur"
)

@Composable
fun AutomationCenterScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val store = remember(context) { AutomationStore(context) }
    val secrets = remember(context) { SecureApiKeyStore(context) }
    var refresh by remember { mutableStateOf(0) }
    val flows = remember(refresh) { store.workflows().sortedByDescending { it.updatedAt } }
    val events = remember(refresh) { store.events() }
    var status by remember { mutableStateOf("") }
    var editorOpen by remember { mutableStateOf(false) }
    var editingId by remember { mutableStateOf<String?>(null) }
    var name by remember { mutableStateOf("") }
    var actionType by remember { mutableStateOf("http") }
    var trigger by remember { mutableStateOf("manual") }
    var triggerValue by remember { mutableStateOf("chat.message.sent") }
    var interval by remember { mutableStateOf(15f) }
    var method by remember { mutableStateOf("POST") }
    var url by remember { mutableStateOf("") }
    var headers by remember { mutableStateOf("{}") }
    var body by remember { mutableStateOf("{\"event\":\"{{event}}\",\"timestamp\":\"{{timestamp}}\",\"payload\":{{payload}}}") }
    var secretName by remember { mutableStateOf("") }
    var notify by remember { mutableStateOf(true) }
    var owner by remember { mutableStateOf("") }
    var repo by remember { mutableStateOf("") }
    var workflow by remember { mutableStateOf("") }
    var ref by remember { mutableStateOf("main") }
    var to by remember { mutableStateOf("") }
    var from by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var databaseId by remember { mutableStateOf("") }
    var titleProperty by remember { mutableStateOf("Name") }
    var pageTitle by remember { mutableStateOf("") }
    var revealSecretsHint by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (true) { refresh++; delay(1500) }
    }
    fun resetEditor() {
        editingId = null; name = ""; actionType = "http"; trigger = "manual"; triggerValue = "chat.message.sent"; interval = 15f
        method = "POST"; url = ""; headers = "{}"; body = "{\"event\":\"{{event}}\",\"timestamp\":\"{{timestamp}}\",\"payload\":{{payload}}}"
        secretName = ""; notify = true; owner = ""; repo = ""; workflow = ""; ref = "main"; to = ""; from = ""; subject = ""; content = ""; databaseId = ""; titleProperty = "Name"; pageTitle = ""
    }
    fun editFlow(w: AutomationWorkflow) {
        editingId = w.id; name = w.name; actionType = w.actionType; trigger = w.trigger; triggerValue = w.triggerValue; interval = w.intervalMinutes.toFloat()
        method = w.method; url = w.url; headers = w.headersJson; body = w.bodyTemplate; secretName = w.secretName; notify = w.notifyOnResult
        owner = w.owner; repo = w.repo; workflow = w.workflow; ref = w.ref; to = w.to; from = w.from; subject = w.subject; content = w.content
        databaseId = w.databaseId; titleProperty = w.titleProperty; pageTitle = w.title; editorOpen = true
    }
    fun saveFlow() {
        if (actionType in setOf("http", "n8n")) {
            val headerObject = runCatching { org.json.JSONObject(headers.ifBlank { "{}" }) }.getOrNull()
            if (headerObject == null) { status = "Başlıklar geçerli bir JSON nesnesi olmalı."; return }
            val sensitiveNames = setOf("authorization", "proxy-authorization", "x-api-key", "api-key", "apikey", "x-auth-token", "token", "secret")
            val iterator = headerObject.keys()
            while (iterator.hasNext()) {
                val key = iterator.next()
                if (key.lowercase() in sensitiveNames) {
                    status = "Gizli başlık değeri düz metin akış ayarında saklanamaz. Kasa etiketi kullan ve Authorization/API anahtarı başlığını uygulamanın üretmesine izin ver."
                    return
                }
            }
        }
        if (actionType in setOf("github_dispatch", "resend_email", "notion_page") && secretName.isBlank()) {
            status = "Önce Güvenli Kasa'ya erişim tokenını kaydet ve kasa etiketini seç."; return
        }
        if (actionType in setOf("github_dispatch", "resend_email", "notion_page") && secretName.isNotBlank()) {
            val exists = if (secretName.startsWith("oauth:")) com.altayhr.kairo.oauth.OAuthFlowManager.isConnected(context, secretName.removePrefix("oauth:")) else !secrets.getSecret(secretName).isNullOrBlank()
            if (!exists) { status = "Güvenli Kasa'da/bağlı OAuth hesaplarında '$secretName' bulunamadı. Önce bağlantıyı kur veya tokenı kasaya kaydet."; return }
        }
        val wf = AutomationWorkflow(
            id = editingId ?: UUID.randomUUID().toString(), name = name.trim().take(100).ifBlank { "Yeni otomasyon" }, enabled = true,
            trigger = trigger, triggerValue = triggerValue.trim(), intervalMinutes = interval.toInt().coerceIn(15, 10080), method = method,
            url = url.trim(), headersJson = headers, bodyTemplate = body, secretName = secretName.trim(), notifyOnResult = notify,
            actionType = actionType, owner = owner.trim(), repo = repo.trim(), workflow = workflow.trim(), ref = ref.trim().ifBlank { "main" },
            to = to.trim(), from = from.trim(), subject = subject.trim(), content = content, databaseId = databaseId.trim(), titleProperty = titleProperty.trim().ifBlank { "Name" }, title = pageTitle.trim()
        )
        store.save(wf); AutomationWorker.schedule(context, wf); refresh++; editorOpen = false; status = "Akış cihazda kaydedildi${if (trigger == "schedule") " ve WorkManager zamanlamasına eklendi" else ""}."
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(LInk),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(11.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Yerel otomasyon", color = LText, fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
                    Text("Cihaz üzerinde zamanlama · dış API'lere güvenli istek · kalıcı olay günlüğü", color = LSecond, fontSize = 10.sp, lineHeight = 15.sp)
                }
                Box(Modifier.size(40.dp).clip(RoundedCornerShape(13.dp)).background(LCard), contentAlignment = Alignment.Center) { Icon(Icons.Outlined.Bolt, null, tint = LAccent) }
            }
        }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = LPanel), border = androidx.compose.foundation.BorderStroke(1.dp, LStroke), shape = RoundedCornerShape(17.dp)) {
                Column(Modifier.fillMaxWidth().padding(13.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Outlined.Security, null, tint = LGreen, modifier = Modifier.size(18.dp)); Text("Sunucusuz çalışma modeli", color = LText, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, modifier = Modifier.padding(start = 8.dp)) }
                    Text("Akışlar bu Android cihazında saklanır ve WorkManager ile çalışır. HTTP/n8n, GitHub Actions dispatch, Resend ve Notion eylemleri doğrudan sağlayıcı API'sine gider; gerekli tokenları Güvenli Kasa'da tut. Gelen genel HTTPS webhook'u, cihaz kapalıyken olay alma ve sürekli sunucu cron'u sunucu olmadan mümkün değildir.", color = LSecond, fontSize = 10.sp, lineHeight = 15.sp)
                    Button(onClick = { resetEditor(); editorOpen = true }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = LAccent, contentColor = LInk), shape = RoundedCornerShape(11.dp)) { Icon(Icons.Outlined.Add, null, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(7.dp)); Text("Yeni iş akışı oluştur", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                }
            }
        }
        if (status.isNotBlank()) item {
            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(11.dp)).background(LCard).padding(11.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(7.dp).clip(CircleShape).background(if (status.contains("hata", true) || status.contains("başarısız", true)) LRed else LGreen))
                Text(status, color = LSecond, fontSize = 10.sp, modifier = Modifier.weight(1f).padding(start = 8.dp))
                TextButton(onClick = { status = "" }) { Text("Kapat", color = LSecond, fontSize = 9.sp) }
            }
        }
        item { Text("İŞ AKIŞLARI · ${flows.size}", color = LSecond, fontSize = 10.sp, letterSpacing = 1.4.sp, fontWeight = FontWeight.Bold) }
        if (flows.isEmpty()) item {
            Card(colors = CardDefaults.cardColors(containerColor = LPanel), border = androidx.compose.foundation.BorderStroke(1.dp, LStroke), shape = RoundedCornerShape(15.dp)) {
                Text("Henüz otomasyon yok. Yeni iş akışı oluşturup HTTP, n8n, GitHub Actions, Resend veya Notion eylemi seçebilirsin.", color = LSecond, fontSize = 11.sp, lineHeight = 17.sp, modifier = Modifier.padding(15.dp))
            }
        }
        items(flows, key = { it.id }) { flow ->
            Card(colors = CardDefaults.cardColors(containerColor = LPanel), border = androidx.compose.foundation.BorderStroke(1.dp, LStroke), shape = RoundedCornerShape(15.dp)) {
                Column(Modifier.fillMaxWidth().padding(13.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(flow.name, color = LText, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text("${actionOptions.firstOrNull { it.first == flow.actionType }?.second ?: flow.actionType} · ${triggerLabel(flow.trigger)}", color = LSecond, fontSize = 9.sp)
                        }
                        Switch(checked = flow.enabled, onCheckedChange = { enabled -> val changed = flow.copy(enabled = enabled); store.save(changed); AutomationWorker.schedule(context, changed); refresh++; status = if (enabled) "Akış etkinleştirildi." else "Akış duraklatıldı." })
                    }
                    Text(flow.lastStatus, color = if (flow.lastStatus.contains("başarılı", true) || flow.lastStatus.contains("HTTP 2", true)) LGreen else LSecond, fontSize = 10.sp, lineHeight = 14.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        Button(onClick = { AutomationWorker.oneTime(context, flow, "manual"); store.appendEvent(AutomationEvent(UUID.randomUUID().toString(), "workflow.manual", "Manuel çalıştırma kuyruğa alındı: ${flow.name}")); refresh++; status = "Akış yerel sıraya alındı." }, enabled = flow.enabled, colors = ButtonDefaults.buttonColors(containerColor = LGreen, contentColor = LInk), shape = RoundedCornerShape(9.dp), modifier = Modifier.weight(1f)) { Icon(Icons.Outlined.PlayArrow, null, modifier = Modifier.size(15.dp)); Spacer(Modifier.width(5.dp)); Text("Çalıştır", fontSize = 10.sp) }
                        OutlinedButton(onClick = { editFlow(flow) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(9.dp)) { Text("Düzenle", fontSize = 10.sp) }
                        IconButton(onClick = { WorkManager.getInstance(context).cancelUniqueWork("kairo-schedule-${flow.id}"); store.delete(flow.id); refresh++; status = "Akış silindi." }) { Icon(Icons.Outlined.DeleteOutline, "Akışı sil", tint = LRed) }
                    }
                }
            }
        }
        item { Text("YEREL OLAY GÜNLÜĞÜ · ${events.size}", color = LSecond, fontSize = 10.sp, letterSpacing = 1.4.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 6.dp)) }
        items(events.take(30), key = { it.id }) { event ->
            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(LPanel).border(1.dp, LStroke, RoundedCornerShape(10.dp)).padding(10.dp), verticalAlignment = Alignment.Top) {
                Box(Modifier.padding(top = 3.dp).size(6.dp).clip(CircleShape).background(LAccent))
                Column(Modifier.weight(1f).padding(start = 8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(event.type, color = LText, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                    Text(event.detail, color = LSecond, fontSize = 9.sp, lineHeight = 13.sp)
                }
                Text(java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date(event.timestamp)), color = LSecond, fontSize = 8.sp)
            }
        }
    }

    if (editorOpen) AlertDialog(
        onDismissRequest = { editorOpen = false }, containerColor = LPanel,
        title = { Text(if (editingId == null) "Yeni yerel iş akışı" else "İş akışını düzenle", color = LText) },
        text = {
            LazyColumn(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item { Field(name, { name = it }, "Akış adı") }
                item { Text("EYLEM TÜRÜ", color = LSecond, fontSize = 9.sp, letterSpacing = 1.2.sp) }
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        actionOptions.forEach { (id, label) ->
                            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(9.dp)).background(if (actionType == id) LAccent.copy(alpha = .13f) else LCard).clickable {
                                actionType = id
                                when (id) {
                                    "n8n" -> { if (url.isBlank()) url = "https://" }
                                    "github_dispatch" -> { method = "POST"; body = "{}" }
                                    "resend_email", "notion_page" -> method = "POST"
                                }
                            }.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(7.dp).clip(CircleShape).background(if (actionType == id) LAccent else LStroke))
                                Text(label, color = LText, fontSize = 10.sp, modifier = Modifier.padding(start = 8.dp))
                            }
                        }
                    }
                }
                item { Text("TETİKLEYİCİ", color = LSecond, fontSize = 9.sp, letterSpacing = 1.2.sp) }
                item { Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) { listOf("manual" to "Manuel", "event" to "Yerel olay", "schedule" to "Zamanla").forEach { (id, label) -> OutlinedButton(onClick = { trigger = id }, colors = ButtonDefaults.outlinedButtonColors(containerColor = if (trigger == id) LAccent.copy(alpha = .17f) else Color.Transparent, contentColor = if (trigger == id) LAccent else LSecond), modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp)) { Text(label, fontSize = 9.sp) } } } }
                if (trigger == "event") item { Field(triggerValue, { triggerValue = it }, "Olay kimliği (örn. chat.message.sent)") }
                if (trigger == "schedule") item { Column { Text("Aralık: ${interval.toInt()} dakika (Android WorkManager alt sınırı 15 dakika)", color = LSecond, fontSize = 10.sp); Slider(value = interval, onValueChange = { interval = it }, valueRange = 15f..1440f) } }
                if (actionType in setOf("http", "n8n")) {
                    item { Field(url, { url = it }, "HTTPS endpoint", keyboard = KeyboardType.Uri) }
                    item { Field(method, { method = it.uppercase().take(8) }, "HTTP yöntemi (GET/POST/PUT/PATCH/DELETE") }
                    item { Field(headers, { headers = it }, "İstek başlıkları JSON", minLines = 2) }
                    item { Field(body, { body = it }, "JSON gövdesi · {{event}}, {{timestamp}}, {{payload}}", minLines = 4) }
                }
                if (actionType == "github_dispatch") {
                    item { Field(owner, { owner = it }, "GitHub kullanıcı/organizasyon") }
                    item { Field(repo, { repo = it }, "Depo adı") }
                    item { Field(workflow, { workflow = it }, "Workflow dosyası veya ID") }
                    item { Field(ref, { ref = it }, "Branch/tag (ref)") }
                    item { Field(body, { body = it }, "Workflow inputs JSON (opsiyonel; ör. {\"environment\":\"test\"})", minLines = 3) }
                    item { Field(secretName, { secretName = it }, "Güvenli Kasa etiketi (repo/workflow yetkili token)") }
                    item { Text("Token'ı mesaja değil Güvenli Kasa'ya ekle. Token'a yalnızca gerekli Actions yazma iznini ver.", color = LGold, fontSize = 9.sp, lineHeight = 14.sp) }
                }
                if (actionType == "resend_email") {
                    item { Field(from, { from = it }, "Gönderen e-posta / doğrulanmış domain") }
                    item { Field(to, { to = it }, "Alıcı e-posta") }
                    item { Field(subject, { subject = it }, "Konu") }
                    item { Field(content, { content = it }, "E-posta metni ({{event}}, {{timestamp}})", minLines = 4) }
                    item { Field(secretName, { secretName = it }, "Güvenli Kasa etiketi (Resend API key)") }
                }
                if (actionType == "notion_page") {
                    item { Field(databaseId, { databaseId = it }, "Notion database ID") }
                    item { Field(titleProperty, { titleProperty = it }, "Title property adı") }
                    item { Field(pageTitle, { pageTitle = it }, "Yeni sayfanın başlığı") }
                    item { Field(content, { content = it }, "Sayfa içeriği", minLines = 4) }
                    item { Field(secretName, { secretName = it }, "Güvenli Kasa etiketi (Notion integration token)") }
                }
                if (actionType in setOf("http", "n8n")) item { Field(secretName, { secretName = it }, "Kasa etiketi (gerekliyse; oauth:spotify gibi isim desteklenir)") }
                item { Row(verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text("Sonuç bildirimi", color = LText, fontSize = 11.sp); Text("Başarı/hata sonucunu bildirimde göster", color = LSecond, fontSize = 9.sp) }; Switch(checked = notify, onCheckedChange = { notify = it }) } }
                item { Text("Kaydetmeden önce hedef URL, token izni ve eylem etkisini kontrol et. E-posta gönderme ve GitHub workflow çalıştırma dış sistemde gerçek etki oluşturur.", color = LGold, fontSize = 9.sp, lineHeight = 14.sp) }
            }
        },
        confirmButton = { Button(onClick = { saveFlow() }, enabled = name.isNotBlank() && (actionType !in setOf("http", "n8n") || url.startsWith("https://")), colors = ButtonDefaults.buttonColors(containerColor = LAccent, contentColor = LInk)) { Icon(Icons.Outlined.Save, null, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(5.dp)); Text("Kaydet") } },
        dismissButton = { TextButton(onClick = { editorOpen = false }) { Text("İptal", color = LSecond) } }
    )
}

@Composable
private fun Field(value: String, onChange: (String) -> Unit, label: String, keyboard: KeyboardType = KeyboardType.Text, minLines: Int = 1) {
    OutlinedTextField(value = value, onValueChange = onChange, modifier = Modifier.fillMaxWidth(), label = { Text(label, fontSize = 10.sp) }, minLines = minLines, maxLines = if (minLines > 1) 8 else 1, keyboardOptions = KeyboardOptions(keyboardType = keyboard), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LAccent, unfocusedBorderColor = LStroke, focusedTextColor = LText, unfocusedTextColor = LText, focusedLabelColor = LAccent, unfocusedLabelColor = LSecond))
}

private fun triggerLabel(trigger: String) = when (trigger) { "schedule" -> "Zamanlanmış"; "event" -> "Yerel olay"; else -> "Manuel" }
