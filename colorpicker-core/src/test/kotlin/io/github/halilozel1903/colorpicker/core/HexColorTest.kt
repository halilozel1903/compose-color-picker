package io.github.halilozel1903.colorpicker.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class HexColorTest {

    @Test
    fun parsesAllFormats() {
        assertEquals(0xFFFF0000.toInt(), HexColor.parse("#F00"))
        assertEquals(0xFF112233.toInt(), HexColor.parse("123"))
        assertEquals(0xFF6750A4.toInt(), HexColor.parse("#6750A4"))
        assertEquals(0xFF6750A4.toInt(), HexColor.parse("6750a4"))
        assertEquals(0x806750A4.toInt(), HexColor.parse("#806750A4"))
        assertEquals(0x00000000, HexColor.parse("#00000000"))
        assertEquals(0xFFFFFFFF.toInt(), HexColor.parse("  #ffffffff  "))
    }

    @Test
    fun rejectsInvalidInput() {
        assertNull(HexColor.parse(""))
        assertNull(HexColor.parse("#"))
        assertNull(HexColor.parse("#12"))
        assertNull(HexColor.parse("#12345"))
        assertNull(HexColor.parse("#1234567"))
        assertNull(HexColor.parse("#123456789"))
        assertNull(HexColor.parse("#GGHHII"))
        assertNull(HexColor.parse("##123456"))
        assertNull(HexColor.parse("12 34 56"))
    }

    @Test
    fun explainsValidation() {
        assertEquals(HexValidation.Valid, HexColor.validate("#abc"))
        assertEquals(HexValidation.Empty, HexColor.validate("  # "))
        assertEquals(HexValidation.InvalidCharacter, HexColor.validate("#12345z"))
        assertEquals(HexValidation.InvalidLength, HexColor.validate("#1234"))
        assertTrue(HexColor.isValid("FFF"))
        assertFalse(HexColor.isValid("FFFF"))
        assertTrue(HexColor.hasAlpha("#80FFFFFF"))
        assertFalse(HexColor.hasAlpha("#FFFFFF"))
    }

    @Test
    fun formats() {
        assertEquals("#6750A4", HexColor.format(0xFF6750A4.toInt()))
        assertEquals("#806750A4", HexColor.format(0x806750A4.toInt()))
        assertEquals("#FF6750A4", HexColor.format(0xFF6750A4.toInt(), includeAlpha = true))
        assertEquals("6750a4", HexColor.format(0xFF6750A4.toInt(), withHash = false, uppercase = false))
        assertEquals("#000000", HexColor.format(0xFF000000.toInt()))
        assertEquals("#00000000", HexColor.format(0))
        assertEquals("#0000FF", HexColor.format(0x800000FF.toInt(), includeAlpha = false))
    }

    @Test
    fun formatParseRoundTrip() {
        val samples = listOf(0, -1, 0x12345678, 0x7FFFFFFF, Int.MIN_VALUE, 0xFF6750A4.toInt(), 0x01020304)
        for (color in samples) {
            assertEquals(color, HexColor.parse(HexColor.format(color, includeAlpha = true)))
        }
        for (r in 0..255 step 51) for (g in 0..255 step 51) for (b in 0..255 step 51) {
            val color = ColorMath.rgb(r, g, b)
            assertEquals(color, HexColor.parse(HexColor.format(color)))
        }
    }

    @Test
    fun sanitizesInput() {
        assertEquals("ABC123", HexColor.sanitize("#abc 123"))
        assertEquals("ABCDEF12", HexColor.sanitize("abcdef1234"))
        assertEquals("ABC", HexColor.sanitize("xyzabc", maxLength = 6))
    }
}
