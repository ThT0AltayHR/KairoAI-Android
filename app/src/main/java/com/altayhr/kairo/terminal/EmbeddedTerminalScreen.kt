package com.altayhr.kairo.terminal

import android.content.Context
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.altayhr.kairo.service.GeminiTaskService
import com.termux.view.TerminalView
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

private val TerminalBg = Color(0xFF0A0D13)
private val TerminalPanel = Color(0xFF131923)
private val TerminalBorder = Color(0xFF273242)
private val TerminalText = Color(0xFFE9EEF5)
private val TerminalMuted = Color(0xFF94A3B8)
private val TerminalGreen = Color(0xFF41D6A3)
private val TerminalRed = Color(0xFFFF6F7D)
private val TerminalGold = Color(0xFFFFC66D)
private val TerminalBlue = Color(0xFF77B8FF)

@Composable
fun EmbeddedTerminalScreen(sessionId: String) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var sessionGeneration by remember(sessionId) { mutableIntStateOf(0) }
    val terminal = remember(sessionId, sessionGeneration) { EmbeddedTerminalSessions.get(context, sessionId) }
    var commandText by remember(sessionId) { mutableStateOf("") }
    var rows by remember(sessionId) { mutableStateOf(EmbeddedTerminalEngine.history(context, sessionId, 30)) }
    var runningCommandId by remember(sessionId) { mutableStateOf<String?>(null) }
    var notice by remember(sessionId) { mutableStateOf<String?>(null) }
    var selectedApproval by remember { mutableStateOf<EmbeddedCommandRecord?>(null) }
    var terminalView by remember(sessionId, sessionGeneration) { mutableStateOf<TerminalView?>(null) }

    DisposableEffect(terminal) {
        onDispose {
            terminalView?.let { terminal.detach(it) }
            terminalView = null
        }
    }

    LaunchedEffect(sessionId) {
        while (isActive) {
            rows = EmbeddedTerminalEngine.history(context, sessionId, 30)
            delay(900)
        }
    }

    Column(
        Modifier.fillMaxSize().background(TerminalBg).padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                Modifier.size(45.dp).clip(RoundedCornerShape(14.dp)).background(TerminalGreen.copy(alpha = .12f))
                    .border(1.dp, TerminalGreen.copy(alpha = .25f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Outlined.Terminal, null, tint = TerminalGreen, modifier = Modifier.size(23.dp)) }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("Gömülü terminal", color = TerminalText, fontSize = 21.sp, fontWeight = FontWeight.Bold)
                Text("Kairo içinde · ayrı uygulama veya Termux bildirimi yok", color = TerminalMuted, fontSize = 10.sp)
            }
            IconButton(onClick = {
                runCatching { com.altayhr.kairo.data.WorkspaceStore(context).refreshTerminalWorkspace(sessionId) }
                rows = EmbeddedTerminalEngine.history(context, sessionId, 30)
                notice = "Komut günlüğü ve dosya envanteri yenilendi"
            }) {
                Icon(Icons.Outlined.Refresh, contentDescription = "Yenile", tint = TerminalMuted)
            }
        }

        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(TerminalPanel)
                .border(1.dp, TerminalBorder, RoundedCornerShape(12.dp)).padding(11.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(Modifier.size(8.dp).clip(CircleShape).background(if (terminal.terminalSession.isRunning) TerminalGreen else TerminalRed))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(if (terminal.terminalSession.isRunning) "Kabuk oturumu açık" else "Kabuk oturumu kapalı", color = TerminalText, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Text(EmbeddedTerminalEngine.workspace(context, sessionId).absolutePath, color = TerminalMuted, fontSize = 9.sp, maxLines = 2)
            }
            if (!terminal.terminalSession.isRunning) {
                IconButton(onClick = {
                    EmbeddedTerminalSessions.restart(context, sessionId)
                    sessionGeneration++
                    notice = "Yeni kabuk oturumu başlatıldı"
                }) { Icon(Icons.Outlined.PlayArrow, contentDescription = "Kabuk oturumunu başlat", tint = TerminalGreen) }
            }
        }

        Card(
            Modifier.fillMaxWidth().weight(1f, fill = true),
            colors = CardDefaults.cardColors(containerColor = TerminalPanel),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, TerminalBorder)
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize().padding(6.dp),
                factory = { ctx: Context ->
                    TerminalView(ctx, null).apply {
                        layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                        terminalView = this
                        terminal.attach(this)
                    }
                },
                update = { view ->
                    terminalView = view
                    if (view.mTermSession !== terminal.terminalSession) terminal.attach(view)
                }
            )
        }

        Text("AI komut kuyruğu", color = TerminalText, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        Text(
            "Gerçek PTY/kabuk Kairo'nun içine gömülüdür. Bu sürüm Android /system/bin/sh ve sistem yardımcılarını çalıştırır; Node/npm/Git/Gradle/Expo paketleri bu kaynak arşivinde henüz yerleşik değildir. AI komutları onaylanana kadar bekletilir.",
            color = TerminalMuted, fontSize = 10.sp, lineHeight = 15.sp
        )

        val waiting = rows.filter { it.state == "approval_required" }
        if (waiting.isNotEmpty()) {
            LazyColumn(
                Modifier.fillMaxWidth().heightIn(max = 210.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(waiting, key = { it.id }) { record ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A202B)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, TerminalGold.copy(alpha = .45f)),
                        shape = RoundedCornerShape(13.dp)
                    ) {
                        Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Outlined.Code, null, tint = TerminalGold, modifier = Modifier.size(15.dp))
                                Text("Onay bekleyen komut", color = TerminalGold, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Text(record.reason, color = TerminalMuted, fontSize = 10.sp, lineHeight = 14.sp)
                            Text(record.command, color = TerminalText, fontFamily = FontFamily.Monospace, fontSize = 11.sp, lineHeight = 17.sp)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { selectedApproval = record },
                                    colors = ButtonDefaults.buttonColors(containerColor = TerminalGreen, contentColor = Color(0xFF04130E)),
                                    enabled = runningCommandId == null
                                ) {
                                    Icon(Icons.Outlined.PlayArrow, null, modifier = Modifier.size(16.dp)); Spacer(Modifier.size(4.dp)); Text("İncele ve çalıştır", fontSize = 10.sp)
                                }
                                TextButton(onClick = { EmbeddedTerminalEngine.reject(context, record.id); notice = "Komut reddedildi" }) {
                                    Icon(Icons.Outlined.Close, null, tint = TerminalMuted, modifier = Modifier.size(15.dp)); Spacer(Modifier.size(3.dp)); Text("Reddet", color = TerminalMuted, fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            Text("Bekleyen AI komutu yok.", color = TerminalMuted, fontSize = 10.sp)
        }

        OutlinedTextField(
            value = commandText,
            onValueChange = { commandText = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Örn. pwd  veya  ls -la", fontSize = 12.sp, color = TerminalMuted) },
            leadingIcon = { Icon(Icons.Outlined.Terminal, null, tint = TerminalGreen) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TerminalText, unfocusedTextColor = TerminalText,
                focusedBorderColor = TerminalGreen, unfocusedBorderColor = TerminalBorder,
                cursorColor = TerminalGreen, focusedContainerColor = TerminalPanel, unfocusedContainerColor = TerminalPanel
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Button(
                onClick = {
                    val command = commandText.trim()
                    if (command.isNotEmpty()) {
                        try {
                            val record = EmbeddedTerminalEngine.propose(context, sessionId, command, "Kullanıcı terminal ekranında çalıştırılmasını istedi")
                            commandText = ""
                            selectedApproval = record
                            notice = "Komut hazırlandı; çalıştırmadan önce gözden geçir."
                        } catch (error: Exception) { notice = error.message ?: "Komut hazırlanamadı." }
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = commandText.isNotBlank() && runningCommandId == null,
                colors = ButtonDefaults.buttonColors(containerColor = TerminalGreen, contentColor = Color(0xFF04130E), disabledContainerColor = TerminalBorder)
            ) { Icon(Icons.Outlined.PlayArrow, null, modifier = Modifier.size(17.dp)); Spacer(Modifier.size(5.dp)); Text("Komut öner ve onayla", fontSize = 11.sp) }

            if (EmbeddedTerminalEngine.hasUnconsumedResult(context, sessionId) && sessionId != "personal") {
                Button(
                    onClick = {
                        try {
                            GeminiTaskService.enqueue(context, sessionId, false)
                            notice = "Terminal sonucu yapay zekâ görevine gönderildi."
                        } catch (error: Exception) { notice = error.message ?: "AI görevi başlatılamadı." }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TerminalBlue, contentColor = Color(0xFF061321))
                ) { Icon(Icons.Outlined.CheckCircle, null, modifier = Modifier.size(16.dp)); Spacer(Modifier.size(3.dp)); Text("AI ile devam", fontSize = 10.sp) }
            }
        }

        if (runningCommandId != null) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CircularProgressIndicator(Modifier.size(16.dp), color = TerminalGreen, strokeWidth = 2.dp)
                Text("Komut çalışıyor…", color = TerminalGreen, fontSize = 11.sp)
            }
        }

        val recentResults = rows.filter { it.state in setOf("success", "error", "timeout", "blocked") }.take(3)
        if (recentResults.isNotEmpty()) {
            Text("Son komutlar", color = TerminalText, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            LazyColumn(Modifier.fillMaxWidth().heightIn(max = 260.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                items(recentResults, key = { it.id }) { row ->
                    Card(colors = CardDefaults.cardColors(containerColor = TerminalPanel), border = androidx.compose.foundation.BorderStroke(1.dp, TerminalBorder), shape = RoundedCornerShape(12.dp)) {
                        Column(Modifier.fillMaxWidth().padding(11.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(row.command, color = TerminalText, fontFamily = FontFamily.Monospace, fontSize = 10.sp, modifier = Modifier.weight(1f), maxLines = 2)
                                Text(
                                    when (row.state) { "success" -> "BAŞARILI"; "timeout" -> "ZAMAN AŞIMI"; "blocked" -> "ENGELLENDİ"; else -> "HATA" },
                                    color = if (row.state == "success") TerminalGreen else TerminalRed,
                                    fontSize = 8.sp, fontWeight = FontWeight.Bold
                                )
                            }
                            Text("${row.message}${row.exitCode?.let { " · çıkış $it" }.orEmpty()}", color = TerminalMuted, fontSize = 10.sp)
                            if (row.stdout.isNotBlank()) Text(row.stdout.takeLast(2400), color = TerminalText, fontFamily = FontFamily.Monospace, fontSize = 9.sp, lineHeight = 13.sp, maxLines = 9)
                            if (row.stderr.isNotBlank()) Text(row.stderr.takeLast(2000), color = TerminalRed.copy(alpha = .9f), fontFamily = FontFamily.Monospace, fontSize = 9.sp, lineHeight = 13.sp, maxLines = 8)
                        }
                    }
                }
            }
        }
        notice?.let { message -> Text(message, color = TerminalBlue, fontSize = 10.sp, lineHeight = 14.sp) }
    }

    selectedApproval?.let { record ->
        AlertDialog(
            onDismissRequest = { selectedApproval = null },
            containerColor = TerminalPanel,
            title = { Text("Komutu çalıştır?", color = TerminalText) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(record.reason, color = TerminalMuted, fontSize = 12.sp)
                    Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(TerminalBg).padding(10.dp)) {
                        Text(record.command, color = TerminalText, fontFamily = FontFamily.Monospace, fontSize = 11.sp, lineHeight = 17.sp)
                    }
                    Text("Komut, Kairo'nun özel oturum klasöründe Android uygulama yetkileriyle çalışır. Komutun sistemi değiştirebileceğini düşünüyorsan reddet.", color = TerminalGold, fontSize = 10.sp, lineHeight = 14.sp)
                }
            },
            confirmButton = {
                TextButton(
                    enabled = runningCommandId == null,
                    onClick = {
                        selectedApproval = null
                        runningCommandId = record.id
                        scope.launch {
                            try {
                                val result = EmbeddedTerminalEngine.runApproved(context, record.id)
                                notice = result.message
                            } catch (error: Exception) { notice = error.message ?: "Terminal komutu tamamlanamadı." }
                            rows = EmbeddedTerminalEngine.history(context, sessionId, 30)
                            runningCommandId = null
                        }
                    }
                ) { Text("Onayla ve çalıştır", color = TerminalGreen) }
            },
            dismissButton = { TextButton(onClick = { selectedApproval = null }) { Text("Vazgeç", color = TerminalMuted) } }
        )
    }
}
