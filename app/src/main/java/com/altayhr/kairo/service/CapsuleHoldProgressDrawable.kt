package com.altayhr.kairo.service

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PathMeasure
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.drawable.Drawable

/** A short gold light-trail that makes one lap around the capsule during the 3-second press. */
internal class CapsuleHoldProgressDrawable : Drawable() {
    /** 0..1 is the position of the moving highlight along the capsule's perimeter. */
    var progress: Float = 0f
        set(value) { field = value.coerceIn(0f, 1f); invalidateSelf() }

    private val path = Path()
    private val segment = Path()
    private val rect = RectF()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        strokeWidth = 2.8f
    }

    override fun draw(canvas: Canvas) {
        if (progress < 0f || progress > 1f) return
        val b = bounds
        val inset = paint.strokeWidth / 2f + 1.5f
        rect.set(b.left + inset, b.top + inset, b.right - inset, b.bottom - inset)
        if (rect.width() <= 1f || rect.height() <= 1f) return

        val radius = minOf(rect.height() / 2f, 28f)
        path.reset()
        path.addRoundRect(rect, radius, radius, Path.Direction.CW)
        val measure = PathMeasure(path, false)
        val length = measure.length
        if (length <= 0f) return

        // A compact head/trail rotates around the capsule instead of filling the whole border.
        val segmentLength = length * 0.19f
        val start = length * (progress % 1f)
        val end = start + segmentLength
        segment.reset()
        if (end <= length) {
            measure.getSegment(start, end, segment, true)
        } else {
            measure.getSegment(start, length, segment, true)
            measure.getSegment(0f, end - length, segment, true)
        }
        paint.shader = LinearGradient(
            b.left.toFloat(), b.top.toFloat(), b.right.toFloat(), b.bottom.toFloat(),
            intArrayOf(Color.TRANSPARENT, 0xFFB98125.toInt(), 0xFFFFE7A0.toInt(), 0xFFFFF2C8.toInt(), 0xFFD39B3B.toInt()),
            floatArrayOf(0f, 0.18f, 0.47f, 0.68f, 1f), Shader.TileMode.CLAMP
        )
        canvas.drawPath(segment, paint)
    }

    override fun setAlpha(alpha: Int) { paint.alpha = alpha }
    override fun setColorFilter(colorFilter: android.graphics.ColorFilter?) { paint.colorFilter = colorFilter }
    @Deprecated("Deprecated in Android") override fun getOpacity(): Int = android.graphics.PixelFormat.TRANSLUCENT
}
