package com.altayhr.kairo.features

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DragIndicator
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import com.altayhr.kairo.service.CapsuleGeometry
import kotlin.math.roundToInt

@Composable
fun CapsuleSettingsSection(
    savedGeometry: CapsuleGeometry,
    overlayAllowed: Boolean,
    capsuleEnabled: Boolean,
    onToggleCapsule: (Boolean) -> Unit,
    onOpenOverlaySettings: () -> Unit,
    onApplyGeometry: (CapsuleGeometry) -> Unit
) {
    val context = LocalContext.current
    val prefs = remember(context) { context.getSharedPreferences("kairo_preferences", android.content.Context.MODE_PRIVATE) }
    var geometry by remember(savedGeometry) { mutableStateOf(savedGeometry.normalized()) }
    var autoCollapse by remember(context) { mutableStateOf(prefs.getBoolean("island_auto_collapse", true)) }
    var collapseSeconds by remember(context) { mutableStateOf(prefs.getInt("island_auto_collapse_seconds", 4).toFloat().coerceIn(1f, 15f)) }
    var cornerRadius by remember(context) { mutableStateOf(prefs.getInt("island_corner_radius_dp", 22).toFloat().coerceIn(12f, 36f)) }
    var opacity by remember(context) { mutableStateOf(prefs.getInt("island_opacity_percent", 100).toFloat().coerceIn(55f, 100f)) }
    val accent = Color(0xFFAA9BFF)
    val background = Color(0xFF12141D)
    val border = Color(0xFF2B3040)
    val primary = Color(0xFFF4F5F7)
    val secondary = Color(0xFF9DA5B5)
    val geometryForDrag by rememberUpdatedState(geometry)

    Card(
        colors = CardDefaults.cardColors(containerColor = background),
        border = androidx.compose.foundation.BorderStroke(1.dp, border),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(Modifier.fillMaxWidth().padding(15.dp), verticalArrangement = Arrangement.spacedBy(13.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(39.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFF211F32)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.Layers, null, tint = accent, modifier = Modifier.size(21.dp))
                }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Dinamik kapsül", color = primary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    Text("Konumunu ve boyutlarını dokunarak ayarla", color = secondary, fontSize = 10.sp)
                }
                Switch(checked = capsuleEnabled, onCheckedChange = onToggleCapsule)
            }
            if (!overlayAllowed) {
                Text("Kapsülün diğer uygulamaların üzerinde görünmesi için sistem izni gerekiyor.", color = secondary, fontSize = 11.sp, lineHeight = 16.sp)
                Button(onClick = onOpenOverlaySettings, colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = Color(0xFF11121A))) {
                    Text("Kapsül iznini aç")
                }
            }
            Text("ÖN İZLEME", color = secondary, fontSize = 9.sp, letterSpacing = 1.5.sp, fontWeight = FontWeight.Bold)
            BoxWithConstraints(
                Modifier.fillMaxWidth().height(244.dp).clip(RoundedCornerShape(17.dp))
                    .background(Color(0xFF090A10)).border(1.dp, border, RoundedCornerShape(17.dp))
                    .padding(8.dp)
            ) {
                val actualWidth = geometry.widthDp.dp.coerceAtMost(maxWidth - 16.dp).coerceAtLeast(150.dp)
                val actualHeight = geometry.heightDp.dp.coerceAtMost(76.dp).coerceAtLeast(40.dp)
                val maxX = (maxWidth - actualWidth).coerceAtLeast(1.dp)
                val maxY = (maxHeight - actualHeight).coerceAtLeast(1.dp)
                val density = LocalDensity.current
                val maxXPx = with(density) { maxX.toPx() }
                val maxYPx = with(density) { maxY.toPx() }
                val pillX = with(density) { (maxX * (geometry.xPercent / 100f)).toPx() }.roundToInt()
                val pillY = with(density) { (maxY * (geometry.yPercent / 100f)).toPx() }.roundToInt()
                Box(
                    Modifier.offset { IntOffset(pillX, pillY) }
                        .width(actualWidth).height(actualHeight)
                        .clip(RoundedCornerShape(100.dp)).background(Color(0xFF08090D))
                        .border(1.dp, accent.copy(alpha = .9f), RoundedCornerShape(100.dp)),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 7.dp)) {
                        Box(
                            Modifier.size((geometry.heightDp - 12).coerceIn(28, 42).dp)
                                .clip(CircleShape).background(Color(0xFF28243F))
                                .border(1.dp, accent.copy(alpha = .75f), CircleShape)
                                .pointerInput(maxXPx, maxYPx) {
                                    detectDragGestures(
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            val current = geometryForDrag
                                            val nextX = (current.xPercent + dragAmount.x / maxXPx.coerceAtLeast(1f) * 100f).coerceIn(0f, 100f)
                                            val nextY = (current.yPercent + dragAmount.y / maxYPx.coerceAtLeast(1f) * 100f).coerceIn(0f, 100f)
                                            geometry = current.copy(xPercent = nextX, yPercent = nextY).normalized()
                                        }
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Outlined.DragIndicator, contentDescription = "Kapsülü sürükle", tint = accent, modifier = Modifier.size(20.dp))
                        }
                        Spacer(Modifier.width(8.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                            Text("Kairo etkin", color = primary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                            Text("Sürükleyerek konumlandır", color = secondary, fontSize = 8.sp, maxLines = 1)
                        }
                    }
                }
            }
            Text("Yuvarlak tutamacı sürükleyerek kapsülü ekranda istediğin yere taşı. X ve Y değerleri ön izlemedeki kullanılabilir alana göredir.", color = secondary, fontSize = 10.sp, lineHeight = 15.sp)
            SliderRow("Yatay konum", "${geometry.xPercent.roundToInt()}%", geometry.xPercent, 0f..100f) { geometry = geometry.copy(xPercent = it).normalized() }
            SliderRow("Dikey konum", "${geometry.yPercent.roundToInt()}%", geometry.yPercent, 0f..100f) { geometry = geometry.copy(yPercent = it).normalized() }
            SliderRow("Yatay genişlik", "${geometry.widthDp} dp", geometry.widthDp.toFloat(), 180f..360f) { geometry = geometry.copy(widthDp = it.roundToInt()).normalized() }
            SliderRow("Dikey yükseklik", "${geometry.heightDp} dp", geometry.heightDp.toFloat(), 40f..76f) { geometry = geometry.copy(heightDp = it.roundToInt()).normalized() }
            Text("HIZLI KONUM", color = secondary, fontSize = 9.sp, letterSpacing = 1.5.sp, fontWeight = FontWeight.Bold)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                listOf("Sol" to 0f, "Orta" to 50f, "Sağ" to 100f).forEach { (label, value) ->
                    TextButton(onClick = { geometry = geometry.copy(xPercent = value).normalized() }, modifier = Modifier.weight(1f)) { Text(label, color = if (geometry.xPercent == value) accent else secondary, fontSize = 10.sp) }
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                listOf("Üst" to 0f, "Orta" to 50f, "Alt" to 100f).forEach { (label, value) ->
                    TextButton(onClick = { geometry = geometry.copy(yPercent = value).normalized() }, modifier = Modifier.weight(1f)) { Text(label, color = if (geometry.yPercent == value) accent else secondary, fontSize = 10.sp) }
                }
            }
            DividerLine()
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Otomatik daralma", color = primary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Text("Görev durumu kısa süre genişleyip sonra küçülür", color = secondary, fontSize = 10.sp)
                }
                Switch(checked = autoCollapse, onCheckedChange = { autoCollapse = it })
            }
            SliderRow("Daralma gecikmesi", "${collapseSeconds.roundToInt()} sn", collapseSeconds, 1f..15f) { collapseSeconds = it }
            SliderRow("Köşe yuvarlaklığı", "${cornerRadius.roundToInt()} dp", cornerRadius, 12f..36f) { cornerRadius = it }
            SliderRow("Kapsül opaklığı", "${opacity.roundToInt()}%", opacity, 55f..100f) { opacity = it }
            Text("Canlı ses oturumu sürerken kapsül otomatik kapanmaz. Boyut ve görünüm değişiklikleri izin verildiğinde diğer uygulamaların üzerinde uygulanır.", color = secondary, fontSize = 10.sp, lineHeight = 15.sp)
            Button(
                onClick = {
                    prefs.edit().putBoolean("island_auto_collapse", autoCollapse)
                        .putInt("island_auto_collapse_seconds", collapseSeconds.roundToInt())
                        .putInt("island_corner_radius_dp", cornerRadius.roundToInt())
                        .putInt("island_opacity_percent", opacity.roundToInt()).apply()
                    onApplyGeometry(geometry.normalized())
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = Color(0xFF11121A)),
                shape = RoundedCornerShape(12.dp)
            ) { Text("Konum ve boyutu uygula", fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable
private fun DividerLine() {
    Box(Modifier.fillMaxWidth().height(1.dp).background(Color(0xFF2B3040)))
}

@Composable
private fun SliderRow(label: String, valueLabel: String, value: Float, range: ClosedFloatingPointRange<Float>, onChange: (Float) -> Unit) {
    val secondary = Color(0xFF9DA5B5)
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, color = Color(0xFFF4F5F7), fontSize = 11.sp, modifier = Modifier.weight(1f))
            Text(valueLabel, color = secondary, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
        }
        Slider(value = value.coerceIn(range.start, range.endInclusive), onValueChange = onChange, valueRange = range)
    }
}
