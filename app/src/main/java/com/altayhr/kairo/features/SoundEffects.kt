package com.altayhr.kairo.features

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.altayhr.kairo.R

/** Locally bundled Kairo sound motifs, gated by the user's sound preference. */
object SoundEffects {
    private var pool: SoundPool? = null
    private val soundIds = mutableMapOf<String, Int>()
    private val loaded = mutableSetOf<Int>()
    private val pending = mutableListOf<String>()
    private val sounds = mapOf(
        "send" to R.raw.sound_send,
        "finish" to R.raw.sound_finish,
        "error" to R.raw.sound_error,
        "think" to R.raw.sound_think,
        "attach" to R.raw.sound_attach,
        "approve" to R.raw.sound_approve,
        "approval" to R.raw.sound_approval,
        "greeting" to R.raw.sound_greeting,
        "greet" to R.raw.sound_greet,
        "pop" to R.raw.sound_pop,
        "blip" to R.raw.sound_blip,
        "search" to R.raw.sound_search,
        "proud" to R.raw.sound_proud,
        "question" to R.raw.sound_question,
        "open" to R.raw.sound_open,
        "close" to R.raw.sound_close,
        "annoyed" to R.raw.sound_annoyed,
        "dizzy" to R.raw.sound_dizzy,
        "gulp" to R.raw.sound_gulp,
        "hover" to R.raw.sound_hover,
        "love" to R.raw.sound_love,
        "peek" to R.raw.sound_peek,
        "rate" to R.raw.sound_rate,
        "slap" to R.raw.sound_slap,
        "sleep" to R.raw.sound_sleep,
        "tick" to R.raw.sound_tick,
        "wink" to R.raw.sound_wink,
        "work" to R.raw.sound_work,
        "yawn" to R.raw.sound_yawn
    )

    @Synchronized private fun ensurePool(context: Context) {
        if (pool != null) return
        val attributes = AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build()
        pool = SoundPool.Builder().setMaxStreams(3).setAudioAttributes(attributes).build().also { p ->
            p.setOnLoadCompleteListener { _, sampleId, status ->
                val waiting = synchronized(this@SoundEffects) {
                    if (status != 0) emptyList() else {
                        loaded.add(sampleId)
                        pending.filter { soundIds[it] == sampleId }.also { matches -> pending.removeAll(matches.toSet()) }
                    }
                }
                waiting.forEach { p.play(sampleId, .45f, .45f, 1, 0, 1f) }
            }
            sounds.forEach { (name, resource) -> soundIds[name] = p.load(context.applicationContext, resource, 1) }
        }
    }

    fun play(context: Context, name: String) {
        val prefs = context.getSharedPreferences("kairo_preferences", Context.MODE_PRIVATE)
        if (!prefs.getBoolean("sound_enabled", true)) return
        ensurePool(context)
        val sample = soundIds[name] ?: soundIds["blip"] ?: return
        if (sample in loaded) pool?.play(sample, .45f, .45f, 1, 0, 1f) else synchronized(this) { pending.add(name) }
    }

    @Synchronized fun release() { pool?.release(); pool = null; soundIds.clear(); loaded.clear(); pending.clear() }
}
