package com.altayhr.kairo.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Brand animation; the Android system splash uses a transparent icon so the launcher glyph never flashes. */
@Composable
fun KairoSplashScreen() {
    val transition = rememberInfiniteTransition(label = "kairo_splash")
    val orbit = transition.animateFloat(0f, 360f, infiniteRepeatable(tween(2600, easing = FastOutSlowInEasing)), label = "orbit")
    val reverse = transition.animateFloat(360f, 0f, infiniteRepeatable(tween(3600, easing = FastOutSlowInEasing)), label = "orbit_reverse")
    val pulse = transition.animateFloat(0.88f, 1.08f, infiniteRepeatable(tween(1150, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "pulse")
    val sweep = transition.animateFloat(18f, 312f, infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing)), label = "sweep")
    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF07080D), Color(0xFF10111B), Color(0xFF08090E)))), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(280.dp)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = size.minDimension * 0.32f
            drawCircle(Brush.radialGradient(listOf(Color(0x223F358C), Color.Transparent), center, radius * 2.0f), radius = radius * 2.0f, center = center)
            drawCircle(Color(0xFF26243B), radius * 1.34f, center, style = Stroke(width = 1.dp.toPx()))
            rotate(orbit.value, center) {
                drawArc(Brush.sweepGradient(listOf(Color.Transparent, Color(0xFFA99BFF), Color(0xFFE7C98B), Color.Transparent), center), -22f, sweep.value, false, topLeft = Offset(center.x - radius * 1.34f, center.y - radius * 1.34f), size = Size(radius * 2.68f, radius * 2.68f), style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round))
                drawCircle(Color(0xFFF0D49B), radius = 4.2.dp.toPx(), center = Offset(center.x + radius * 1.34f, center.y))
            }
            rotate(reverse.value, center) {
                drawArc(Color(0x668E83F4), 155f, 78f, false, topLeft = Offset(center.x - radius, center.y - radius), size = Size(radius * 2, radius * 2), style = Stroke(width = 1.2.dp.toPx(), cap = StrokeCap.Round))
                drawCircle(Color(0xFF9A90F7), radius = 2.5.dp.toPx(), center = Offset(center.x, center.y - radius))
            }
            // Hand-drawn four-point star, rather than an emoji or system icon.
            val starR = 25.dp.toPx() * pulse.value
            val star = PathBuilder.star(center, starR)
            drawPath(star, Brush.linearGradient(listOf(Color(0xFFFFE5AB), Color(0xFFB7ACFF), Color(0xFF8778DE))), style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round))
            drawCircle(Color(0xFFF7E2AE), radius = 2.dp.toPx(), center = center)
            val tailY = center.y + radius * 1.62f
            for (i in 0..2) {
                val w = (15 - i * 4).dp.toPx()
                drawLine(Color(0xFF8E83F4).copy(alpha = 0.48f - i * 0.1f), Offset(center.x - w, tailY + i * 5.dp.toPx()), Offset(center.x + w, tailY + i * 5.dp.toPx()), strokeWidth = 1.dp.toPx(), cap = StrokeCap.Round)
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Spacer(Modifier.height(185.dp))
            Text("K A I R O", color = Color(0xFFF4F0E5), fontSize = 22.sp, fontWeight = FontWeight.Light, fontFamily = FontFamily.SansSerif, letterSpacing = 5.sp)
            Spacer(Modifier.height(7.dp))
            Text("DÜŞÜN · ÜRET · OTOMATİZE ET", color = Color(0xFF898DA4), fontSize = 8.sp, letterSpacing = 2.4.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(35.dp))
            Canvas(Modifier.size(width = 70.dp, height = 3.dp)) {
                drawRoundRect(Color(0xFF252638), cornerRadius = androidx.compose.ui.geometry.CornerRadius(size.height))
                drawRoundRect(Brush.horizontalGradient(listOf(Color(0xFF8C81E8), Color(0xFFE9CB8B), Color(0xFF8C81E8))), topLeft = Offset(size.width * ((sweep.value % 100f) / 100f - .25f).coerceIn(0f, .78f), 0f), size = Size(size.width * .22f, size.height), cornerRadius = androidx.compose.ui.geometry.CornerRadius(size.height))
            }
        }
    }
}

private object PathBuilder {
    fun star(center: Offset, r: Float): androidx.compose.ui.graphics.Path = androidx.compose.ui.graphics.Path().apply {
        val points = listOf(Offset(center.x, center.y - r), Offset(center.x + r * .24f, center.y - r * .24f), Offset(center.x + r, center.y), Offset(center.x + r * .24f, center.y + r * .24f), Offset(center.x, center.y + r), Offset(center.x - r * .24f, center.y + r * .24f), Offset(center.x - r, center.y), Offset(center.x - r * .24f, center.y - r * .24f))
        moveTo(points.first().x, points.first().y); points.drop(1).forEach { lineTo(it.x, it.y) }; close()
    }
}
