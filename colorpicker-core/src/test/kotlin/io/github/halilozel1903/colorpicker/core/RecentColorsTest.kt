package io.github.halilozel1903.colorpicker.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RecentColorsTest {

    private val red = 0xFFFF0000.toInt()
    private val green = 0xFF00FF00.toInt()
    private val blue = 0xFF0000FF.toInt()

    @Test
    fun mostRecentFirst() {
        val recent = RecentColors(maxSize = 5).add(red).add(green).add(blue)
        assertEquals(listOf(blue, green, red), recent.colors)
        assertEquals(3, recent.size)
    }

    @Test
    fun dedupesByMovingToFront() {
        val recent = RecentColors(maxSize = 5).add(red).add(green).add(red)
        assertEquals(listOf(red, green), recent.colors)
    }

    @Test
    fun dropsOldestWhenFull() {
        val recent = RecentColors(maxSize = 2).add(red).add(green).add(blue)
        assertEquals(listOf(blue, green), recent.colors)
        assertEquals(listOf(blue), recent.withMaxSize(1).colors)
    }

    @Test
    fun constructorDedupesAndTrims() {
        val recent = RecentColors(listOf(red, red, green, blue), maxSize = 2)
        assertEquals(listOf(red, green), recent.colors)
    }

    @Test
    fun addAllRemoveAndClear() {
        val recent = RecentColors(maxSize = 4).addAll(listOf(red, green, blue))
        assertEquals(listOf(blue, green, red), recent.colors)
        assertTrue(green in recent)
        assertFalse(green in recent.remove(green))
        assertTrue(recent.clear().isEmpty())
        assertEquals(4, recent.clear().maxSize)
    }

    @Test
    fun alphaMakesADifferentColor() {
        val recent = RecentColors().add(red).add(ColorMath.withAlpha(red, 0.5f))
        assertEquals(2, recent.size)
    }

    @Test
    fun equalityAndValidation() {
        assertEquals(RecentColors(listOf(red), 3), RecentColors(maxSize = 3).add(red))
        assertFailsWith<IllegalArgumentException> { RecentColors(maxSize = 0) }
    }
}
