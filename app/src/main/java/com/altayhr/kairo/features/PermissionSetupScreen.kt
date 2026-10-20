package com.altayhr.kairo.features

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BatteryAlert
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PermissionSetupScreen(
    notificationsGranted: Boolean,
    overlayGranted: Boolean,
    microphoneGranted: Boolean,
    batteryUnrestricted: Boolean,
    onRequestNotifications: () -> Unit,
    onOpenOverlaySettings: () -> Unit,
    onRequestMicrophone: () -> Unit,
    onOpenBatterySettings: () -> Unit,
    onComplete: () -> Unit
) {
    val primary = Color(0xFFF1F2F8)
    val secondary = Color(0xFFA3A9B8)
    val accent = Color(0xFFAA9BFF)
    val border = Color(0xFF303344)
    Column(
        Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(13.dp)
    ) {
        Box(Modifier.size(58.dp).background(accent, RoundedCornerShape(19.dp)), contentAlignment = Alignment.Center) {
            Text("K", color = Color(0xFF11121A), fontSize = 34.sp, fontWeight = FontWeight.Black)
        }
        Text("Kairo'yu hazırlayalım", color = primary, fontSize = 27.sp, fontWeight = FontWeight.Bold)
        Text(
            "Arka plan kapsülü ve sesli sohbet için izinleri adım adım gözden geçir. İzinleri daha sonra Ayarlar bölümünden değiştirebilirsin.",
            color = secondary, fontSize = 13.sp, lineHeight = 20.sp
        )
        PermissionCard(
            title = "Bildirimler",
            description = "Arka plandaki görevlerin ilerlemesini ve tamamlanmasını gör.",
            granted = notificationsGranted,
            requiredForFeature = "Önerilir",
            icon = { Icon(Icons.Outlined.Notifications, null, tint = accent) },
            actionLabel = if (notificationsGranted) "İzin verildi" else "Bildirim iznini aç",
            onClick = onRequestNotifications
        )
        PermissionCard(
            title = "Ekranın üzerinde göster",
            description = "Kairo kapsülünün diğer uygulamaların üstünde görünmesi için gerekir.",
            granted = overlayGranted,
            requiredForFeature = "Kapsül için gerekli",
            icon = { Icon(Icons.Outlined.Layers, null, tint = accent) },
            actionLabel = if (overlayGranted) "İzin verildi" else "Sistem ayarlarını aç",
            onClick = onOpenOverlaySettings
        )
        PermissionCard(
            title = "Mikrofon",
            description = "Yalnızca sen canlı sesli sohbeti başlattığında kullanılır.",
            granted = microphoneGranted,
            requiredForFeature = "Sesli sohbet için",
            icon = { Icon(Icons.Outlined.Mic, null, tint = accent) },
            actionLabel = if (microphoneGranted) "İzin verildi" else "Mikrofon izni ver",
            onClick = onRequestMicrophone
        )
        PermissionCard(
            title = "Arka plan kısıtlamaları",
            description = "Bazı telefon üreticileri kapsülün ve görev bildirimlerinin arka planda çalışmasını sınırlayabilir. Bu adım isteğe bağlıdır.",
            granted = batteryUnrestricted,
            requiredForFeature = "İsteğe bağlı",
            icon = { Icon(Icons.Outlined.BatteryAlert, null, tint = accent) },
            actionLabel = if (batteryUnrestricted) "Kısıtlama yok" else "Pil ayarlarını aç",
            onClick = onOpenBatterySettings
        )
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF141620)),
            border = androidx.compose.foundation.BorderStroke(1.dp, border),
            shape = RoundedCornerShape(15.dp)
        ) {
            Row(Modifier.padding(13.dp), verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(Icons.Outlined.Settings, null, tint = secondary, modifier = Modifier.padding(top = 1.dp))
                Text(
                    "Dosya erişimi için tüm depolamayı açan geniş bir izin istemiyoruz. Dosya eklediğinde Android'in kendi dosya seçicisi kullanılır; yalnızca seçtiğin dosyalar içe aktarılır.",
                    color = secondary, fontSize = 11.sp, lineHeight = 17.sp
                )
            }
        }
        Button(
            onClick = onComplete,
            modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
            colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = Color(0xFF11121A)),
            shape = RoundedCornerShape(14.dp)
        ) { Text("Kurulumu tamamla", fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 4.dp)) }
        Text("İzinlerin bazıları isteğe bağlıdır. Kapsül ve mikrofon, yalnızca ilgili özelliği kullandığında çalışır.", color = secondary, fontSize = 10.sp, lineHeight = 15.sp)
    }
}

@Composable
private fun PermissionCard(
    title: String,
    description: String,
    granted: Boolean,
    requiredForFeature: String,
    icon: @Composable () -> Unit,
    actionLabel: String,
    onClick: () -> Unit
) {
    val primary = Color(0xFFF1F2F8)
    val secondary = Color(0xFFA3A9B8)
    val border = Color(0xFF303344)
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF141620)),
        border = androidx.compose.foundation.BorderStroke(1.dp, border),
        shape = RoundedCornerShape(15.dp)
    ) {
        Column(Modifier.fillMaxWidth().padding(13.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(Modifier.size(40.dp).background(Color(0xFF211F32), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) { icon() }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(title, color = primary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Text(requiredForFeature, color = secondary, fontSize = 10.sp)
                }
                Icon(
                    if (granted) Icons.Outlined.CheckCircle else Icons.Outlined.Settings,
                    contentDescription = if (granted) "İzin verildi" else "İzin ayarlanmadı",
                    tint = if (granted) Color(0xFF70D8BB) else secondary,
                    modifier = Modifier.size(19.dp)
                )
            }
            Text(description, color = secondary, fontSize = 11.sp, lineHeight = 16.sp)
            OutlinedButton(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = !granted || title == "Arka plan kısıtlamaları",
                shape = RoundedCornerShape(10.dp)
            ) { Text(actionLabel, fontSize = 11.sp) }
        }
    }
}
