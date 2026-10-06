package io.github.halilozel1903.colorpicker.core

import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class PalettesTest {

    private val purple = 0xFF6750A4.toInt()

    private fun hueDistance(a: Float, b: Float): Float = abs(a - b).let { minOf(it, 360f - it) }

    @Test
    fun tintsGetLighterAndShadesDarker() {
        val tints = Palettes.tints(purple, 4)
        assertEquals(4, tints.size)
        val tintLuminance = tints.map { ColorContrast.relativeLuminance(it) }
        assertEquals(tintLuminance.sorted(), tintLuminance)
        assertTrue(tintLuminance.first() > ColorContrast.relativeLuminance(purple))
        assertTrue(tints.last() != ColorContrast.WHITE)

        val shades = Palettes.shades(purple, 4)
        val shadeLuminance = shades.map { ColorContrast.relativeLuminance(it) }
        assertEquals(shadeLuminance.sortedDescending(), shadeLuminance)
        assertTrue(shadeLuminance.first() < ColorContrast.relativeLuminance(purple))
    }

    @Test
    fun tintsAndShadesKeepAlphaAndCenterTheBase() {
        val translucent = ColorMath.withAlpha(purple, 0.5f)
        val scale = Palettes.tintsAndShades(translucent, 3)
        assertEquals(7, scale.size)
        assertEquals(translucent, scale[3])
        assertTrue(scale.all { ColorMath.alpha(it) == ColorMath.alpha(translucent) })
        assertEquals(emptyList(), Palettes.tints(purple, 0))
        assertFailsWith<IllegalArgumentException> { Palettes.shades(purple, -1) }
    }

    @Test
    fun harmonies() {
        val baseHue = ColorMath.toHsv(purple).hue
        val complementary = Palettes.complementary(purple)
        assertEquals(purple, complementary[0])
        assertTrue(hueDistance(baseHue + 180f, ColorMath.toHsv(complementary[1]).hue) < 1.5f)

        val analogous = Palettes.analogous(purple)
        assertEquals(purple, analogous[1])
        assertTrue(hueDistance(baseHue - 30f, ColorMath.toHsv(analogous[0]).hue) < 1.5f)
        assertTrue(hueDistance(baseHue + 30f, ColorMath.toHsv(analogous[2]).hue) < 1.5f)

        val triadic = Palettes.triadic(purple)
        assertEquals(3, triadic.size)
        assertTrue(hueDistance(baseHue + 120f, ColorMath.toHsv(triadic[1]).hue) < 1.5f)
        assertTrue(hueDistance(baseHue + 240f, ColorMath.toHsv(triadic[2]).hue) < 1.5f)

        assertEquals(3, Palettes.splitComplementary(purple).size)
        assertEquals(4, Palettes.tetradic(purple).size)
    }

    @Test
    fun rotateKeepsSaturationValueAndAlpha() {
        val color = 0x80FF0000.toInt()
        assertEquals(0x8000FF00.toInt(), Palettes.rotate(color, 120f))
        assertEquals(color, Palettes.rotate(color, 360f))
    }

    @Test
    fun materialPalette() {
        assertEquals(19, MaterialPalette.primaries.size)
        assertEquals(MaterialPalette.all.size, MaterialPalette.all.map { it.argb }.distinct().size)
        assertTrue(MaterialPalette.all.all { ColorMath.alpha(it.argb) == 255 })
    }
}
