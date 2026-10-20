package com.altayhr.kairo.service

import android.content.Context
import kotlin.math.roundToInt

/** Persisted geometry for Kairo's Android overlay capsule. Percentages are relative to available display space. */
data class CapsuleGeometry(
    val xPercent: Float = 50f,
    val yPercent: Float = 0f,
    val widthDp: Int = 242,
    val heightDp: Int = 48
) {
    fun normalized(): CapsuleGeometry = copy(
        xPercent = xPercent.coerceIn(0f, 100f),
        yPercent = yPercent.coerceIn(0f, 100f),
        widthDp = widthDp.coerceIn(180, 360),
        heightDp = heightDp.coerceIn(40, 76)
    )

    companion object {
        private const val PREFS = "kairo_preferences"
        fun read(context: Context): CapsuleGeometry {
            val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            return CapsuleGeometry(
                p.getFloat("capsule_x_percent", 50f),
                p.getFloat("capsule_y_percent", 0f),
                p.getInt("capsule_width_dp", 242),
                p.getInt("capsule_height_dp", 48)
            ).normalized()
        }

        fun write(context: Context, value: CapsuleGeometry) {
            val g = value.normalized()
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                .putFloat("capsule_x_percent", g.xPercent)
                .putFloat("capsule_y_percent", g.yPercent)
                .putInt("capsule_width_dp", g.widthDp)
                .putInt("capsule_height_dp", g.heightDp)
                .apply()
        }
    }
}
