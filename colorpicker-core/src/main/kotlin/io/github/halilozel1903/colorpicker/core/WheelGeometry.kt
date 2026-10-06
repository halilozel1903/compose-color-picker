package io.github.halilozel1903.colorpicker.core

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

/** A point in screen coordinates (x to the right, y down). */
public data class WheelPoint(public val x: Float, public val y: Float)

/** A position on a color wheel: [hue] in degrees 0 until 360 and [saturation] 0..1 (center to edge). */
public data class HueSaturation(public val hue: Float, public val saturation: Float)

/**
 * Maps between points on a hue/saturation wheel and colors.
 *
 * Screen coordinates are used (y grows downward), so hue 0 (red) is at 3 o'clock and hue grows clockwise, which
 * matches a sweep gradient drawn on Android. Saturation is the distance from the center divided by the radius.
 */
public object WheelGeometry {

    /** The hue and saturation at ([x], [y]). Points outside the circle are clamped to its edge (saturation 1). */
    public fun hueSaturationAt(x: Float, y: Float, centerX: Float, centerY: Float, radius: Float): HueSaturation {
        if (radius <= 0f) return HueSaturation(0f, 0f)
        val dx = (x - centerX).toDouble()
        val dy = (y - centerY).toDouble()
        val distance = hypot(dx, dy)
        if (distance == 0.0) return HueSaturation(0f, 0f)
        val degrees = Math.toDegrees(atan2(dy, dx)).toFloat()
        return HueSaturation(ColorMath.normalizeHue(degrees), (distance / radius).coerceAtMost(1.0).toFloat())
    }

    /** The point for [hue] and [saturation] (clamped to 0..1) on a wheel. */
    public fun positionOf(hue: Float, saturation: Float, centerX: Float, centerY: Float, radius: Float): WheelPoint {
        val angle = Math.toRadians(ColorMath.normalizeHue(hue).toDouble())
        val distance = saturation.coerceIn(0f, 1f) * radius.coerceAtLeast(0f)
        return WheelPoint(
            (centerX + distance * cos(angle)).toFloat(),
            (centerY + distance * sin(angle)).toFloat(),
        )
    }

    /** True when ([x], [y]) is inside the circle or on its edge. */
    public fun isInside(x: Float, y: Float, centerX: Float, centerY: Float, radius: Float): Boolean =
        hypot((x - centerX).toDouble(), (y - centerY).toDouble()) <= radius

    /** Returns ([x], [y]) unchanged when it is inside the circle, otherwise the nearest point on its edge. */
    public fun clampToCircle(x: Float, y: Float, centerX: Float, centerY: Float, radius: Float): WheelPoint {
        val dx = (x - centerX).toDouble()
        val dy = (y - centerY).toDouble()
        val distance = hypot(dx, dy)
        val r = radius.coerceAtLeast(0f).toDouble()
        if (distance <= r) return WheelPoint(x, y)
        val scale = r / distance
        return WheelPoint((centerX + dx * scale).toFloat(), (centerY + dy * scale).toFloat())
    }

    /** The color at ([x], [y]) on a wheel drawn at [value] (brightness) and [alpha]. */
    public fun colorAt(
        x: Float,
        y: Float,
        centerX: Float,
        centerY: Float,
        radius: Float,
        value: Float = 1f,
        alpha: Float = 1f,
    ): Int {
        val hs = hueSaturationAt(x, y, centerX, centerY, radius)
        return ColorMath.hsvToArgb(hs.hue, hs.saturation, value, alpha)
    }
}
