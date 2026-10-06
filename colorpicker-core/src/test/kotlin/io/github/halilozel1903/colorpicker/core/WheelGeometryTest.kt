package io.github.halilozel1903.colorpicker.core

import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WheelGeometryTest {

    private fun assertClose(expected: Float, actual: Float, tolerance: Float = 0.01f) {
        assertTrue(abs(expected - actual) <= tolerance, "expected $expected but was $actual")
    }

    @Test
    fun hueGrowsClockwiseOnScreen() {
        // Center (100, 100), radius 100. Right is red (0), down is 90, left is 180, up is 270.
        assertClose(0f, WheelGeometry.hueSaturationAt(200f, 100f, 100f, 100f, 100f).hue)
        assertClose(90f, WheelGeometry.hueSaturationAt(100f, 200f, 100f, 100f, 100f).hue)
        assertClose(180f, WheelGeometry.hueSaturationAt(0f, 100f, 100f, 100f, 100f).hue)
        assertClose(270f, WheelGeometry.hueSaturationAt(100f, 0f, 100f, 100f, 100f).hue)
    }

    @Test
    fun saturationIsDistanceOverRadiusClamped() {
        assertEquals(HueSaturation(0f, 0f), WheelGeometry.hueSaturationAt(100f, 100f, 100f, 100f, 100f))
        assertClose(0.5f, WheelGeometry.hueSaturationAt(150f, 100f, 100f, 100f, 100f).saturation)
        assertEquals(1f, WheelGeometry.hueSaturationAt(500f, 100f, 100f, 100f, 100f).saturation)
        assertEquals(HueSaturation(0f, 0f), WheelGeometry.hueSaturationAt(5f, 5f, 0f, 0f, 0f))
    }

    @Test
    fun positionRoundTrip() {
        for (hue in 0 until 360 step 15) for (step in 0..10) {
            val saturation = step / 10f
            val point = WheelGeometry.positionOf(hue.toFloat(), saturation, 120f, 80f, 64f)
            val back = WheelGeometry.hueSaturationAt(point.x, point.y, 120f, 80f, 64f)
            assertClose(saturation, back.saturation, 0.001f)
            if (saturation > 0f) {
                val diff = abs(hue - back.hue).let { minOf(it, 360f - it) }
                assertTrue(diff < 0.05f, "hue $hue came back as ${back.hue}")
            }
        }
    }

    @Test
    fun positionClampsSaturation() {
        val point = WheelGeometry.positionOf(0f, 3f, 0f, 0f, 10f)
        assertClose(10f, point.x)
        assertClose(0f, point.y)
    }

    @Test
    fun clampsToCircle() {
        assertEquals(WheelPoint(3f, 4f), WheelGeometry.clampToCircle(3f, 4f, 0f, 0f, 10f))
        val clamped = WheelGeometry.clampToCircle(30f, 40f, 0f, 0f, 10f)
        assertClose(6f, clamped.x)
        assertClose(8f, clamped.y)
        assertTrue(WheelGeometry.isInside(6f, 8f, 0f, 0f, 10f))
        assertFalse(WheelGeometry.isInside(8f, 8f, 0f, 0f, 10f))
    }

    @Test
    fun colorAtEdgeAndCenter() {
        assertEquals(0xFFFF0000.toInt(), WheelGeometry.colorAt(200f, 100f, 100f, 100f, 100f))
        assertEquals(0xFFFFFFFF.toInt(), WheelGeometry.colorAt(100f, 100f, 100f, 100f, 100f))
        assertEquals(0xFF000000.toInt(), WheelGeometry.colorAt(200f, 100f, 100f, 100f, 100f, value = 0f))
    }
}
