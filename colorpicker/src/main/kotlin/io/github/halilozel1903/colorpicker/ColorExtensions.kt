package io.github.halilozel1903.colorpicker

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import io.github.halilozel1903.colorpicker.core.ColorContrast
import io.github.halilozel1903.colorpicker.core.ColorMath
import io.github.halilozel1903.colorpicker.core.HexColor
import io.github.halilozel1903.colorpicker.core.Hsl
import io.github.halilozel1903.colorpicker.core.Hsv

/** Formats this color as `#RRGGBB`, or `#AARRGGBB` when [includeAlpha] (by default: when it is not opaque). */
public fun Color.toHex(includeAlpha: Boolean = alpha < 1f): String = HexColor.format(toArgb(), includeAlpha)

/** Parses `#RGB`, `#RRGGBB` or `#AARRGGBB` (the `#` is optional) to a [Color], or returns null. */
public fun parseHexColor(text: String): Color? = HexColor.parse(text)?.let { Color(it) }

/** This color in HSV. */
public fun Color.toHsv(): Hsv = ColorMath.toHsv(toArgb())

/** This color in HSL. */
public fun Color.toHsl(): Hsl = ColorMath.toHsl(toArgb())

/** The HSV color as a Compose [Color]. */
public fun Hsv.toColor(): Color = Color(toArgb())

/** The HSL color as a Compose [Color]. */
public fun Hsl.toColor(): Color = Color(toArgb())

/** The WCAG contrast ratio (1 to 21) between this color as text and [background]. */
public fun Color.contrastRatioOn(background: Color): Double = ColorContrast.contrastRatio(toArgb(), background.toArgb())

/** White or black, whichever reads better on this color. */
public fun Color.bestOnColor(light: Color = Color.White, dark: Color = Color.Black): Color =
    Color(ColorContrast.bestOnColor(toArgb(), light.toArgb(), dark.toArgb()))
