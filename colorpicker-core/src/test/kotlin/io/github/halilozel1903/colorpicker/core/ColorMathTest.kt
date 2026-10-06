package io.github.halilozel1903.colorpicker.core

import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ColorMathTest {

    private fun assertClose(expected: Float, actual: Float, tolerance: Float = 0.002f) {
        assertTrue(abs(expected - actual) <= tolerance, "expected $expected but was $actual")
    }

    @Test
    fun packsAndUnpacksChannels() {
        val color = ColorMath.argb(0x80, 0x12, 0x34, 0x56)
        assertEquals(0x80123456.toInt(), color)
        assertEquals(0x80, ColorMath.alpha(color))
        assertEquals(0x12, ColorMath.red(color))
        assertEquals(0x34, ColorMath.green(color))
        assertEquals(0x56, ColorMath.blue(color))
    }

    @Test
    fun clampsChannels() {
        assertEquals(0xFFFF0000.toInt(), ColorMath.argb(300, 999, -5, 0))
    }

    @Test
    fun primaryColorsToHsv() {
        assertEquals(Hsv(0f, 1f, 1f), ColorMath.toHsv(0xFFFF0000.toInt()))
        assertEquals(Hsv(120f, 1f, 1f), ColorMath.toHsv(0xFF00FF00.toInt()))
        assertEquals(Hsv(240f, 1f, 1f), ColorMath.toHsv(0xFF0000FF.toInt()))
        assertEquals(Hsv(0f, 0f, 0f), ColorMath.toHsv(0xFF000000.toInt()))
        assertEquals(Hsv(0f, 0f, 1f), ColorMath.toHsv(0xFFFFFFFF.toInt()))
    }

    @Test
    fun hsvToArgbKnownValues() {
        assertEquals(0xFFFFFF00.toInt(), ColorMath.hsvToArgb(60f, 1f, 1f))
        assertEquals(0xFF00FFFF.toInt(), ColorMath.hsvToArgb(180f, 1f, 1f))
        assertEquals(0xFFFF00FF.toInt(), ColorMath.hsvToArgb(300f, 1f, 1f))
        assertEquals(0xFF808080.toInt(), ColorMath.hsvToArgb(0f, 0f, 0.5f))
        // Hue wraps around.
        assertEquals(ColorMath.hsvToArgb(30f, 1f, 1f), ColorMath.hsvToArgb(390f, 1f, 1f))
        assertEquals(ColorMath.hsvToArgb(330f, 1f, 1f), ColorMath.hsvToArgb(-30f, 1f, 1f))
        assertEquals(0x80FF0000.toInt(), ColorMath.hsvToArgb(0f, 1f, 1f, alpha = 128 / 255f))
    }

    @Test
    fun hsvRoundTripForEveryStepOfTheCube() {
        for (r in 0..255 step 15) for (g in 0..255 step 15) for (b in 0..255 step 15) {
            val color = ColorMath.argb(0xCC, r, g, b)
            assertEquals(color, ColorMath.toHsv(color).toArgb(), HexColor.format(color))
        }
    }

    @Test
    fun hslRoundTripForEveryStepOfTheCube() {
        for (r in 0..255 step 17) for (g in 0..255 step 17) for (b in 0..255 step 17) {
            val color = ColorMath.rgb(r, g, b)
            assertEquals(color, ColorMath.toHsl(color).toArgb(), HexColor.format(color))
        }
    }

    @Test
    fun hslKnownValues() {
        val hsl = ColorMath.toHsl(0xFF6750A4.toInt())
        assertClose(256.43f, hsl.hue, 0.05f)
        assertClose(0.343f, hsl.saturation, 0.002f)
        assertClose(0.478f, hsl.lightness, 0.002f)
        assertEquals(0xFF808080.toInt(), ColorMath.hslToArgb(200f, 0f, 0.5f))
        assertEquals(0xFFFF0000.toInt(), ColorMath.hslToArgb(0f, 1f, 0.5f))
        assertEquals(0xFFFFFFFF.toInt(), ColorMath.hslToArgb(0f, 1f, 1f))
    }

    @Test
    fun rgbRoundTrip() {
        val color = 0x7F102030
        assertEquals(Rgb(0x10, 0x20, 0x30, 0x7F), Rgb.fromArgb(color))
        assertEquals(color, Rgb.fromArgb(color).toArgb())
    }

    @Test
    fun normalizesHue() {
        assertEquals(0f, ColorMath.normalizeHue(360f))
        assertEquals(10f, ColorMath.normalizeHue(370f))
        assertEquals(350f, ColorMath.normalizeHue(-10f))
        assertEquals(0f, ColorMath.normalizeHue(Float.NaN))
    }

    @Test
    fun alphaHelpers() {
        assertEquals(0x80FF0000.toInt(), ColorMath.withAlpha(0xFFFF0000.toInt(), 128 / 255f))
        assertEquals(0, ColorMath.alphaToInt(-1f))
        assertEquals(255, ColorMath.alphaToInt(2f))
        assertClose(0.5f, ColorMath.alphaFraction(0x80000000.toInt()), 0.003f)
    }

    @Test
    fun mixesAndComposites() {
        assertEquals(0xFF808080.toInt(), ColorMath.mix(0xFF000000.toInt(), 0xFFFFFFFF.toInt(), 0.5f))
        assertEquals(0xFF000000.toInt(), ColorMath.mix(0xFF000000.toInt(), 0xFFFFFFFF.toInt(), -1f))
        // Black at alpha 128/255 over white leaves 127/255 of the white.
        assertEquals(0xFF7F7F7F.toInt(), ColorMath.compositeOver(0x80000000.toInt(), 0xFFFFFFFF.toInt()))
        assertEquals(0, ColorMath.compositeOver(0, 0))
    }
}
