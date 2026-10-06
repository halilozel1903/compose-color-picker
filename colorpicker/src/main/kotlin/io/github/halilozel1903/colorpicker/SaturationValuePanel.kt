package io.github.halilozel1903.colorpicker

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.halilozel1903.colorpicker.core.ColorMath

/**
 * A saturation/value square for one [hue]: saturation grows to the right and value (brightness) upward, so the
 * top right corner is the pure hue, the top left white and the bottom black. Pair it with a [HueSlider].
 *
 * Give it a size (for example `Modifier.fillMaxWidth().height(180.dp)`). The thumb can extend [thumbRadius]
 * past the edges, so leave that much room around it.
 */
@Composable
public fun SaturationValuePanel(
    hue: Float,
    saturation: Float,
    value: Float,
    onSaturationValueChange: (saturation: Float, value: Float) -> Unit,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 12.dp,
    thumbRadius: Dp = 12.dp,
    contentDescription: String = "Saturation and brightness",
) {
    val currentSaturation by rememberUpdatedState(saturation)
    val currentValue by rememberUpdatedState(value)
    val onChange by rememberUpdatedState(onSaturationValueChange)
    Canvas(
        modifier
            .semantics {
                this.contentDescription = contentDescription
                stateDescription = "Saturation ${saturation.percent()}, brightness ${value.percent()}"
                customActions = listOf(
                    CustomAccessibilityAction("More saturated") {
                        onChange((currentSaturation + 0.1f).coerceAtMost(1f), currentValue)
                        true
                    },
                    CustomAccessibilityAction("Less saturated") {
                        onChange((currentSaturation - 0.1f).coerceAtLeast(0f), currentValue)
                        true
                    },
                    CustomAccessibilityAction("Brighter") {
                        onChange(currentSaturation, (currentValue + 0.1f).coerceAtMost(1f))
                        true
                    },
                    CustomAccessibilityAction("Darker") {
                        onChange(currentSaturation, (currentValue - 0.1f).coerceAtLeast(0f))
                        true
                    },
                )
            }
            .pressAndDrag(Unit) { position, size ->
                val s = if (size.width > 0) position.x / size.width else 0f
                val v = if (size.height > 0) 1f - position.y / size.height else 0f
                onChange(s.coerceIn(0f, 1f), v.coerceIn(0f, 1f))
            },
    ) {
        val corner = CornerRadius(cornerRadius.toPx())
        val pureHue = Color(ColorMath.hsvToArgb(hue, 1f, 1f))
        drawRoundRect(Brush.horizontalGradient(listOf(Color.White, pureHue)), cornerRadius = corner)
        drawRoundRect(Brush.verticalGradient(listOf(Color.Transparent, Color.Black)), cornerRadius = corner)
        val thumbCenter = Offset(
            saturation.coerceIn(0f, 1f) * size.width,
            (1f - value.coerceIn(0f, 1f)) * size.height,
        )
        drawThumb(thumbCenter, thumbRadius.toPx(), Color(ColorMath.hsvToArgb(hue, saturation, value)))
    }
}

/** A [SaturationValuePanel] bound to a [ColorPickerState]. */
@Composable
public fun SaturationValuePanel(
    state: ColorPickerState,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 12.dp,
    thumbRadius: Dp = 12.dp,
    contentDescription: String = "Saturation and brightness",
) {
    SaturationValuePanel(
        hue = state.hue,
        saturation = state.saturation,
        value = state.value,
        onSaturationValueChange = state::updateSaturationValue,
        modifier = modifier,
        cornerRadius = cornerRadius,
        thumbRadius = thumbRadius,
        contentDescription = contentDescription,
    )
}
