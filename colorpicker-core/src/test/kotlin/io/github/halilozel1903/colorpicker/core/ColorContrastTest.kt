package io.github.halilozel1903.colorpicker.core

import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ColorContrastTest {

    private fun assertClose(expected: Double, actual: Double, tolerance: Double = 0.01) {
        assertTrue(abs(expected - actual) <= tolerance, "expected $expected but was $actual")
    }

    @Test
    fun luminanceExtremes() {
        assertClose(0.0, ColorContrast.relativeLuminance(ColorContrast.BLACK), 1e-9)
        assertClose(1.0, ColorContrast.relativeLuminance(ColorContrast.WHITE), 1e-9)
    }

    @Test
    fun knownRatios() {
        assertClose(21.0, ColorContrast.contrastRatio(ColorContrast.BLACK, ColorContrast.WHITE))
        assertClose(21.0, ColorContrast.contrastRatio(ColorContrast.WHITE, ColorContrast.BLACK))
        assertClose(1.0, ColorContrast.contrastRatio(0xFF6750A4.toInt(), 0xFF6750A4.toInt()))
        // #767676 is the classic lightest grey that passes AA on white.
        assertClose(4.54, ColorContrast.contrastRatio(0xFF767676.toInt(), ColorContrast.WHITE))
    }

    @Test
    fun translucentForegroundIsComposited() {
        // Black at alpha 128/255 over white is #7F7F7F.
        assertClose(
            ColorContrast.contrastRatio(0xFF7F7F7F.toInt(), ColorContrast.WHITE),
            ColorContrast.contrastRatio(0x80000000.toInt(), ColorContrast.WHITE),
        )
    }

    @Test
    fun levels() {
        assertEquals(WcagLevel.AAA, ColorContrast.level(7.0))
        assertEquals(WcagLevel.AA, ColorContrast.level(4.5))
        assertEquals(WcagLevel.Fail, ColorContrast.level(4.49))
        assertEquals(WcagLevel.AAA, ColorContrast.level(4.5, largeText = true))
        assertEquals(WcagLevel.AA, ColorContrast.level(3.0, largeText = true))
        assertEquals(WcagLevel.Fail, ColorContrast.level(2.9, largeText = true))
    }

    @Test
    fun bestOnColor() {
        assertEquals(ColorContrast.WHITE, ColorContrast.bestOnColor(0xFF6750A4.toInt()))
        assertEquals(ColorContrast.BLACK, ColorContrast.bestOnColor(0xFFFFEB3B.toInt()))
        assertEquals(ColorContrast.WHITE, ColorContrast.bestOnColor(ColorContrast.BLACK))
        assertEquals(ColorContrast.BLACK, ColorContrast.bestOnColor(ColorContrast.WHITE))
        assertTrue(ColorContrast.isLight(0xFFF5F5F5.toInt()))
        assertFalse(ColorContrast.isLight(0xFF1A1C1E.toInt()))
        val custom = ColorContrast.bestOnColor(0xFF6750A4.toInt(), light = 0xFFEADDFF.toInt(), dark = 0xFF21005D.toInt())
        assertEquals(0xFFEADDFF.toInt(), custom)
    }

    @Test
    fun formatsRatios() {
        assertEquals("21.0:1", ColorContrast.formatRatio(21.0))
        assertEquals("4.5:1", ColorContrast.formatRatio(4.54))
        assertEquals("1.0:1", ColorContrast.formatRatio(1.0))
    }
}
