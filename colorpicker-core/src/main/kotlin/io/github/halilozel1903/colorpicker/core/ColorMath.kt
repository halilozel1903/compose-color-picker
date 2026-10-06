package io.github.halilozel1903.colorpicker.core

import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Packed ARGB helpers and conversions between RGB, HSV and HSL.
 *
 * Colors are 32 bit ARGB integers (`0xAARRGGBB`), the same packing as `android.graphics.Color` and
 * `androidx.compose.ui.graphics.toArgb()`, so values move between this module and Compose without conversion.
 */
public object ColorMath {

    /** Packs channels (each 0..255, clamped) into an ARGB integer. */
    public fun argb(alpha: Int, red: Int, green: Int, blue: Int): Int =
        (alpha.coerceIn(0, 255) shl 24) or
            (red.coerceIn(0, 255) shl 16) or
            (green.coerceIn(0, 255) shl 8) or
            blue.coerceIn(0, 255)

    /** Packs an opaque color from channels 0..255. */
    public fun rgb(red: Int, green: Int, blue: Int): Int = argb(255, red, green, blue)

    public fun alpha(argb: Int): Int = (argb ushr 24) and 0xFF
    public fun red(argb: Int): Int = (argb shr 16) and 0xFF
    public fun green(argb: Int): Int = (argb shr 8) and 0xFF
    public fun blue(argb: Int): Int = argb and 0xFF

    /** The alpha channel as a fraction 0..1. */
    public fun alphaFraction(argb: Int): Float = alpha(argb) / 255f

    /** Replaces the alpha channel; [alpha] is a fraction 0..1. */
    public fun withAlpha(argb: Int, alpha: Float): Int = (argb and 0x00FFFFFF) or (alphaToInt(alpha) shl 24)

    /** Converts an alpha fraction 0..1 to 0..255. */
    public fun alphaToInt(alpha: Float): Int = (alpha.coerceIn(0f, 1f) * 255f).roundToInt()

    /** Mixes two colors channel by channel (including alpha); [fraction] 0 gives [from], 1 gives [to]. */
    public fun mix(from: Int, to: Int, fraction: Float): Int {
        val t = fraction.coerceIn(0f, 1f)
        fun lerp(a: Int, b: Int): Int = (a + (b - a) * t).roundToInt()
        return argb(
            lerp(alpha(from), alpha(to)),
            lerp(red(from), red(to)),
            lerp(green(from), green(to)),
            lerp(blue(from), blue(to)),
        )
    }

    /** Draws [foreground] over [background] with source-over blending. The result is opaque if [background] is. */
    public fun compositeOver(foreground: Int, background: Int): Int {
        val fa = alpha(foreground) / 255.0
        val ba = alpha(background) / 255.0
        val outA = fa + ba * (1 - fa)
        if (outA <= 0.0) return 0
        fun channel(f: Int, b: Int): Int = ((f * fa + b * ba * (1 - fa)) / outA).roundToInt()
        return argb(
            (outA * 255).roundToInt(),
            channel(red(foreground), red(background)),
            channel(green(foreground), green(background)),
            channel(blue(foreground), blue(background)),
        )
    }

    /** Converts an ARGB color to HSV. Grays get hue 0 and black gets saturation 0. */
    public fun toHsv(argb: Int): Hsv {
        val r = red(argb) / 255.0
        val g = green(argb) / 255.0
        val b = blue(argb) / 255.0
        val max = maxOf(r, g, b)
        val min = minOf(r, g, b)
        val delta = max - min
        val saturation = if (max == 0.0) 0.0 else delta / max
        return Hsv(hueOf(r, g, b, max, delta).toFloat(), saturation.toFloat(), max.toFloat(), alphaFraction(argb))
    }

    /** Converts HSV (hue in degrees, any value; saturation, value and alpha 0..1) to ARGB. */
    public fun hsvToArgb(hue: Float, saturation: Float, value: Float, alpha: Float = 1f): Int {
        val s = saturation.coerceIn(0f, 1f).toDouble()
        val v = value.coerceIn(0f, 1f).toDouble()
        val c = v * s
        return fromChroma(normalizeHue(hue).toDouble(), c, v - c, alpha)
    }

    /** Converts an ARGB color to HSL. Grays get hue 0 and saturation 0. */
    public fun toHsl(argb: Int): Hsl {
        val r = red(argb) / 255.0
        val g = green(argb) / 255.0
        val b = blue(argb) / 255.0
        val max = maxOf(r, g, b)
        val min = minOf(r, g, b)
        val delta = max - min
        val lightness = (max + min) / 2
        val saturation = if (delta == 0.0) 0.0 else delta / (1 - abs(2 * lightness - 1))
        return Hsl(
            hueOf(r, g, b, max, delta).toFloat(),
            saturation.coerceIn(0.0, 1.0).toFloat(),
            lightness.toFloat(),
            alphaFraction(argb),
        )
    }

    /** Converts HSL (hue in degrees, any value; saturation, lightness and alpha 0..1) to ARGB. */
    public fun hslToArgb(hue: Float, saturation: Float, lightness: Float, alpha: Float = 1f): Int {
        val s = saturation.coerceIn(0f, 1f).toDouble()
        val l = lightness.coerceIn(0f, 1f).toDouble()
        val c = (1 - abs(2 * l - 1)) * s
        return fromChroma(normalizeHue(hue).toDouble(), c, l - c / 2, alpha)
    }

    /** Wraps any angle into 0 until 360 degrees. */
    public fun normalizeHue(hue: Float): Float {
        if (hue.isNaN() || hue.isInfinite()) return 0f
        val wrapped = hue % 360f
        val positive = if (wrapped < 0f) wrapped + 360f else wrapped
        return if (positive >= 360f) 0f else positive
    }

    private fun hueOf(r: Double, g: Double, b: Double, max: Double, delta: Double): Double {
        if (delta == 0.0) return 0.0
        val hue = when (max) {
            r -> 60 * (((g - b) / delta) % 6)
            g -> 60 * (((b - r) / delta) + 2)
            else -> 60 * (((r - g) / delta) + 4)
        }
        val positive = if (hue < 0) hue + 360 else hue
        return if (positive >= 360) 0.0 else positive
    }

    private fun fromChroma(hue: Double, c: Double, m: Double, alpha: Float): Int {
        val h = hue / 60
        val x = c * (1 - abs(h % 2 - 1))
        val (r, g, b) = when {
            h < 1 -> Triple(c, x, 0.0)
            h < 2 -> Triple(x, c, 0.0)
            h < 3 -> Triple(0.0, c, x)
            h < 4 -> Triple(0.0, x, c)
            h < 5 -> Triple(x, 0.0, c)
            else -> Triple(c, 0.0, x)
        }
        return argb(
            alphaToInt(alpha),
            ((r + m) * 255).roundToInt(),
            ((g + m) * 255).roundToInt(),
            ((b + m) * 255).roundToInt(),
        )
    }
}

/** A color in HSV: [hue] in degrees 0 until 360, [saturation], [value] and [alpha] 0..1. */
public data class Hsv(
    public val hue: Float,
    public val saturation: Float,
    public val value: Float,
    public val alpha: Float = 1f,
) {
    /** Packs this color as ARGB. */
    public fun toArgb(): Int = ColorMath.hsvToArgb(hue, saturation, value, alpha)

    public companion object {
        public fun fromArgb(argb: Int): Hsv = ColorMath.toHsv(argb)
    }
}

/** A color in HSL: [hue] in degrees 0 until 360, [saturation], [lightness] and [alpha] 0..1. */
public data class Hsl(
    public val hue: Float,
    public val saturation: Float,
    public val lightness: Float,
    public val alpha: Float = 1f,
) {
    /** Packs this color as ARGB. */
    public fun toArgb(): Int = ColorMath.hslToArgb(hue, saturation, lightness, alpha)

    public companion object {
        public fun fromArgb(argb: Int): Hsl = ColorMath.toHsl(argb)
    }
}

/** A color as 8 bit channels: [red], [green], [blue] and [alpha] 0..255. */
public data class Rgb(
    public val red: Int,
    public val green: Int,
    public val blue: Int,
    public val alpha: Int = 255,
) {
    /** Packs this color as ARGB (channels are clamped to 0..255). */
    public fun toArgb(): Int = ColorMath.argb(alpha, red, green, blue)

    public companion object {
        public fun fromArgb(argb: Int): Rgb =
            Rgb(ColorMath.red(argb), ColorMath.green(argb), ColorMath.blue(argb), ColorMath.alpha(argb))
    }
}
