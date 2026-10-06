package io.github.halilozel1903.colorpicker.core

import kotlin.math.pow

/** WCAG 2 conformance of a contrast ratio. */
public enum class WcagLevel {
    /** At least 7:1 (4.5:1 for large text). */
    AAA,

    /** At least 4.5:1 (3:1 for large text). */
    AA,

    /** Below AA. */
    Fail,
}

/** WCAG 2 relative luminance and contrast ratios. */
public object ColorContrast {

    /** Opaque white, a common light on-color. */
    public const val WHITE: Int = -0x1

    /** Opaque black, a common dark on-color. */
    public const val BLACK: Int = -0x1000000

    /** Relative luminance 0 (black) to 1 (white) of the color's RGB channels; alpha is ignored. */
    public fun relativeLuminance(argb: Int): Double {
        fun linear(channel: Int): Double {
            val c = channel / 255.0
            return if (c <= 0.04045) c / 12.92 else ((c + 0.055) / 1.055).pow(2.4)
        }
        return 0.2126 * linear(ColorMath.red(argb)) +
            0.7152 * linear(ColorMath.green(argb)) +
            0.0722 * linear(ColorMath.blue(argb))
    }

    /**
     * The contrast ratio between [foreground] and [background], 1 to 21. A translucent [foreground] is drawn over
     * the background first, and a translucent [background] over white.
     */
    public fun contrastRatio(foreground: Int, background: Int): Double {
        val bg = ColorMath.compositeOver(background, WHITE)
        val fg = ColorMath.compositeOver(foreground, bg)
        val l1 = relativeLuminance(fg)
        val l2 = relativeLuminance(bg)
        val lighter = maxOf(l1, l2)
        val darker = minOf(l1, l2)
        return (lighter + 0.05) / (darker + 0.05)
    }

    /** The WCAG level a [ratio] reaches, for normal or [largeText]. */
    public fun level(ratio: Double, largeText: Boolean = false): WcagLevel {
        val aaa = if (largeText) 4.5 else 7.0
        val aa = if (largeText) 3.0 else 4.5
        return when {
            ratio >= aaa -> WcagLevel.AAA
            ratio >= aa -> WcagLevel.AA
            else -> WcagLevel.Fail
        }
    }

    /** Whichever of [light] and [dark] has the higher contrast on [background]. Ties go to [light]. */
    public fun bestOnColor(background: Int, light: Int = WHITE, dark: Int = BLACK): Int =
        if (contrastRatio(light, background) >= contrastRatio(dark, background)) light else dark

    /** True when [background] is light enough that dark text reads better on it than white text. */
    public fun isLight(background: Int): Boolean = bestOnColor(background) == BLACK

    /** Formats a ratio like `4.5:1`. */
    public fun formatRatio(ratio: Double): String {
        val tenths = Math.round(ratio * 10)
        return "${tenths / 10}.${tenths % 10}:1"
    }
}
