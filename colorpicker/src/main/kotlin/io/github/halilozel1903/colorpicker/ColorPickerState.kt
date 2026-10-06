package io.github.halilozel1903.colorpicker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import io.github.halilozel1903.colorpicker.core.ColorMath
import io.github.halilozel1903.colorpicker.core.RecentColors

/**
 * The color being picked, kept as HSV so the hue survives when saturation or brightness reach zero, plus the
 * color the picker started from and a list of recently used colors.
 *
 * Create one with [rememberColorPickerState]; it survives configuration changes and process death.
 */
@Stable
public class ColorPickerState(
    initialColor: Color,
    recentColors: List<Color> = emptyList(),
    maxRecentColors: Int = RecentColors.DEFAULT_MAX_SIZE,
) {
    private val initialHsv = ColorMath.toHsv(initialColor.toArgb())

    /** Hue in degrees, 0 until 360. */
    public var hue: Float by mutableFloatStateOf(initialHsv.hue)
        private set

    /** Saturation 0..1. */
    public var saturation: Float by mutableFloatStateOf(initialHsv.saturation)
        private set

    /** Value (brightness) 0..1. */
    public var value: Float by mutableFloatStateOf(initialHsv.value)
        private set

    /** Alpha 0..1. */
    public var alpha: Float by mutableFloatStateOf(initialHsv.alpha)
        private set

    /** The color the picker started from (shown as "Current" in [ColorPreview]); see [resetOriginal]. */
    public var originalColor: Color by mutableStateOf(initialColor)
        private set

    private var recent by mutableStateOf(RecentColors(recentColors.map { it.toArgb() }, maxRecentColors))

    /** The color picked so far. */
    public val color: Color
        get() = Color(ColorMath.hsvToArgb(hue, saturation, value, alpha))

    /** The same color without transparency. */
    public val opaqueColor: Color
        get() = Color(ColorMath.hsvToArgb(hue, saturation, value, 1f))

    /** Recently used colors, most recent first. */
    public val recentColors: List<Color>
        get() = recent.colors.map { Color(it) }

    /** How many colors [recentColors] keeps. */
    public val maxRecentColors: Int
        get() = recent.maxSize

    /** Sets the hue (any angle, wrapped into 0 until 360). */
    public fun updateHue(hue: Float) {
        this.hue = ColorMath.normalizeHue(hue)
    }

    /** Sets the saturation, clamped to 0..1. */
    public fun updateSaturation(saturation: Float) {
        this.saturation = saturation.coerceIn(0f, 1f)
    }

    /** Sets the value (brightness), clamped to 0..1. */
    public fun updateValue(value: Float) {
        this.value = value.coerceIn(0f, 1f)
    }

    /** Sets the alpha, clamped to 0..1. */
    public fun updateAlpha(alpha: Float) {
        this.alpha = alpha.coerceIn(0f, 1f)
    }

    /** Sets hue and saturation together, as the color wheel does. */
    public fun updateHueSaturation(hue: Float, saturation: Float) {
        updateHue(hue)
        updateSaturation(saturation)
    }

    /** Sets saturation and value together, as the saturation/value panel does. */
    public fun updateSaturationValue(saturation: Float, value: Float) {
        updateSaturation(saturation)
        updateValue(value)
    }

    /**
     * Picks [color]. Grays and black have no hue of their own, so the current hue (and, for black, saturation) is
     * kept and the wheel's thumb does not jump. With [keepAlpha] the current alpha is kept instead of [color]'s.
     */
    public fun setColor(color: Color, keepAlpha: Boolean = false) {
        val hsv = ColorMath.toHsv(color.toArgb())
        if (hsv.value > 0f) {
            if (hsv.saturation > 0f) hue = hsv.hue
            saturation = hsv.saturation
        }
        value = hsv.value
        if (!keepAlpha) alpha = hsv.alpha
    }

    /** Puts [color] (by default the picked color) first in [recentColors]. */
    public fun addToRecent(color: Color = this.color) {
        recent = recent.add(color.toArgb())
    }

    /** Removes [color] from [recentColors]. */
    public fun removeFromRecent(color: Color) {
        recent = recent.remove(color.toArgb())
    }

    /** Empties [recentColors]. */
    public fun clearRecent() {
        recent = recent.clear()
    }

    /** Goes back to [originalColor]. */
    public fun reset() {
        setColor(originalColor)
    }

    /** Makes [color] (by default the picked color) the new [originalColor], for example after it was applied. */
    public fun resetOriginal(color: Color = this.color) {
        originalColor = color
    }

    public companion object {
        /** Saves hue, saturation, value, alpha, the original color and the recent colors. */
        public val Saver: Saver<ColorPickerState, Any> = listSaver<ColorPickerState, Any>(
            save = { state ->
                listOf<Any>(
                    state.hue,
                    state.saturation,
                    state.value,
                    state.alpha,
                    state.originalColor.toArgb(),
                    state.recent.maxSize,
                    state.recent.colors.toIntArray(),
                )
            },
            restore = { saved ->
                val recentArgb = (saved[6] as IntArray).toList()
                ColorPickerState(
                    initialColor = Color(saved[4] as Int),
                    recentColors = recentArgb.map { Color(it) },
                    maxRecentColors = saved[5] as Int,
                ).apply {
                    hue = saved[0] as Float
                    saturation = saved[1] as Float
                    value = saved[2] as Float
                    alpha = saved[3] as Float
                }
            },
        )
    }
}

/**
 * Remembers a [ColorPickerState] starting at [initialColor]. It is saved across configuration changes and
 * process death. Changing [initialColor] later does not reset the state; call [ColorPickerState.setColor].
 */
@Composable
public fun rememberColorPickerState(
    initialColor: Color,
    recentColors: List<Color> = emptyList(),
    maxRecentColors: Int = RecentColors.DEFAULT_MAX_SIZE,
): ColorPickerState = rememberSaveable(saver = ColorPickerState.Saver) {
    ColorPickerState(initialColor, recentColors, maxRecentColors)
}
