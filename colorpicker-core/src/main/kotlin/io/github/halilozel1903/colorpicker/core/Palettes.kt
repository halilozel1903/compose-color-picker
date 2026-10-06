package io.github.halilozel1903.colorpicker.core

/** Generates related colors: tints, shades and color harmonies. All functions keep the base color's alpha. */
public object Palettes {

    /** [count] tints of [argb], mixed toward white in even steps, from the closest to the base to the lightest. */
    public fun tints(argb: Int, count: Int = 5): List<Int> = steps(argb, ColorMath.withAlpha(ColorContrast.WHITE, ColorMath.alphaFraction(argb)), count)

    /** [count] shades of [argb], mixed toward black in even steps, from the closest to the base to the darkest. */
    public fun shades(argb: Int, count: Int = 5): List<Int> = steps(argb, ColorMath.withAlpha(ColorContrast.BLACK, ColorMath.alphaFraction(argb)), count)

    /** A scale from the lightest tint through [argb] to the darkest shade: `count` tints, the base, `count` shades. */
    public fun tintsAndShades(argb: Int, count: Int = 4): List<Int> =
        tints(argb, count).reversed() + argb + shades(argb, count)

    /** The base and the color opposite it on the wheel. */
    public fun complementary(argb: Int): List<Int> = listOf(argb, rotate(argb, 180f))

    /** The base flanked by its neighbours [angle] degrees away: `[hue - angle, hue, hue + angle]`. */
    public fun analogous(argb: Int, angle: Float = 30f): List<Int> =
        listOf(rotate(argb, -angle), argb, rotate(argb, angle))

    /** Three colors 120 degrees apart, starting with the base. */
    public fun triadic(argb: Int): List<Int> = listOf(argb, rotate(argb, 120f), rotate(argb, 240f))

    /** The base and the two neighbours of its complement, [angle] degrees either side of it. */
    public fun splitComplementary(argb: Int, angle: Float = 30f): List<Int> =
        listOf(argb, rotate(argb, 180f - angle), rotate(argb, 180f + angle))

    /** Four colors 90 degrees apart, starting with the base. */
    public fun tetradic(argb: Int): List<Int> =
        listOf(argb, rotate(argb, 90f), rotate(argb, 180f), rotate(argb, 270f))

    /** Turns the hue of [argb] by [degrees], keeping saturation, value and alpha. */
    public fun rotate(argb: Int, degrees: Float): Int {
        val hsv = ColorMath.toHsv(argb)
        return ColorMath.withAlpha(ColorMath.hsvToArgb(hsv.hue + degrees, hsv.saturation, hsv.value), hsv.alpha)
    }

    private fun steps(from: Int, to: Int, count: Int): List<Int> {
        require(count >= 0) { "count must not be negative, was $count" }
        return List(count) { index -> ColorMath.mix(from, to, (index + 1f) / (count + 1f)) }
    }
}

/** A color with a display name, such as a Material palette swatch. */
public data class NamedColor(public val name: String, public val argb: Int)

/** The Material Design 2014 palette: the 500 tone of each hue, plus neutrals. */
public object MaterialPalette {

    /** The nineteen Material hues at tone 500, from red to blue grey. */
    public val primaries: List<NamedColor> = listOf(
        NamedColor("Red", 0xFFF44336.toInt()),
        NamedColor("Pink", 0xFFE91E63.toInt()),
        NamedColor("Purple", 0xFF9C27B0.toInt()),
        NamedColor("Deep Purple", 0xFF673AB7.toInt()),
        NamedColor("Indigo", 0xFF3F51B5.toInt()),
        NamedColor("Blue", 0xFF2196F3.toInt()),
        NamedColor("Light Blue", 0xFF03A9F4.toInt()),
        NamedColor("Cyan", 0xFF00BCD4.toInt()),
        NamedColor("Teal", 0xFF009688.toInt()),
        NamedColor("Green", 0xFF4CAF50.toInt()),
        NamedColor("Light Green", 0xFF8BC34A.toInt()),
        NamedColor("Lime", 0xFFCDDC39.toInt()),
        NamedColor("Yellow", 0xFFFFEB3B.toInt()),
        NamedColor("Amber", 0xFFFFC107.toInt()),
        NamedColor("Orange", 0xFFFF9800.toInt()),
        NamedColor("Deep Orange", 0xFFFF5722.toInt()),
        NamedColor("Brown", 0xFF795548.toInt()),
        NamedColor("Grey", 0xFF9E9E9E.toInt()),
        NamedColor("Blue Grey", 0xFF607D8B.toInt()),
    )

    /** White, three greys and black. */
    public val neutrals: List<NamedColor> = listOf(
        NamedColor("White", 0xFFFFFFFF.toInt()),
        NamedColor("Grey 300", 0xFFE0E0E0.toInt()),
        NamedColor("Grey 500", 0xFF9E9E9E.toInt()),
        NamedColor("Grey 800", 0xFF424242.toInt()),
        NamedColor("Black", 0xFF000000.toInt()),
    )

    /** [primaries] followed by [neutrals] without the duplicate grey. */
    public val all: List<NamedColor> = primaries + neutrals.filter { n -> primaries.none { it.argb == n.argb } }
}
