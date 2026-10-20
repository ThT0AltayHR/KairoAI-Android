package com.altayhr.kairo.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UnifiedDiffTest {
    @Test fun unchangedTextProducesOnlyContextRows() {
        val result = UnifiedDiff.compare("ilk\nikinci", "ilk\nikinci")
        assertEquals(listOf(DiffLine(' ', "ilk"), DiffLine(' ', "ikinci")), result)
    }

    @Test fun replacementProducesRemovalAndAddition() {
        val result = UnifiedDiff.compare("başlık\neski satır\nson", "başlık\nyeni satır\nson")
        assertTrue(result.contains(DiffLine('-', "eski satır")))
        assertTrue(result.contains(DiffLine('+', "yeni satır")))
        assertTrue(result.contains(DiffLine(' ', "son")))
    }

    @Test fun oversizedInputGetsLimitNotice() {
        val huge = (1..360).joinToString("\n") { "satır $it" }
        assertTrue(UnifiedDiff.compare(huge, "kısa").any { it.kind == '!' })
    }
}
