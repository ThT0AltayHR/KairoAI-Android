package com.altayhr.kairo.data

/** A single line in a bounded, local-only diff: ' ' unchanged, '+' added, '-' removed, '!' limit notice. */
data class DiffLine(val kind: Char, val text: String)

/**
 * Line-based LCS diff for comparing local text or source code.
 * Inputs are bounded to keep an accidentally huge paste from allocating an unbounded matrix.
 * Returned text is never interpreted as a command or executed.
 */
object UnifiedDiff {
    private const val MAX_CHARS = 40_000
    private const val MAX_LINES = 350

    fun compare(oldText: String, newText: String): List<DiffLine> {
        val oldTruncated = oldText.length > MAX_CHARS || oldText.lineSequence().take(MAX_LINES + 1).count() > MAX_LINES
        val newTruncated = newText.length > MAX_CHARS || newText.lineSequence().take(MAX_LINES + 1).count() > MAX_LINES
        val old = oldText.take(MAX_CHARS).lines().take(MAX_LINES)
        val fresh = newText.take(MAX_CHARS).lines().take(MAX_LINES)
        val dp = Array(old.size + 1) { IntArray(fresh.size + 1) }
        for (i in old.size - 1 downTo 0) {
            for (j in fresh.size - 1 downTo 0) {
                dp[i][j] = if (old[i] == fresh[j]) dp[i + 1][j + 1] + 1 else maxOf(dp[i + 1][j], dp[i][j + 1])
            }
        }
        val rows = ArrayList<DiffLine>(old.size + fresh.size + 1)
        var i = 0
        var j = 0
        while (i < old.size && j < fresh.size) {
            when {
                old[i] == fresh[j] -> { rows += DiffLine(' ', old[i]); i++; j++ }
                dp[i + 1][j] >= dp[i][j + 1] -> { rows += DiffLine('-', old[i]); i++ }
                else -> { rows += DiffLine('+', fresh[j]); j++ }
            }
        }
        while (i < old.size) rows += DiffLine('-', old[i++])
        while (j < fresh.size) rows += DiffLine('+', fresh[j++])
        if (oldTruncated || newTruncated) rows += DiffLine('!', "Karşılaştırma sınırına ulaşıldı; ilk $MAX_LINES satır / $MAX_CHARS karakter işlendi.")
        return rows
    }
}
