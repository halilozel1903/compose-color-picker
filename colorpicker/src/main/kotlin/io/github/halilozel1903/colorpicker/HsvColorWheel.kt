package io.github.halilozel1903.colorpicker

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
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
import io.github.halilozel1903.colorpicker.core.WheelGeometry

/** The hue spectrum from red back to red, for sweep gradients and the hue slider. */
internal val HueSpectrum: List<Color> = listOf(0f, 60f, 120f, 180f, 240f, 300f, 360f).map { hue ->
    Color(ColorMath.hsvToArgb(hue, 1f, 1f))
}

/**
 * A hue/saturation color wheel: hue goes around the circle (red at 3 o'clock, clockwise) and saturation from the
 * white center to the edge. It is drawn at [value] (brightness). Tap or drag anywhere, even outside the circle, to
 * move the thumb; positions outside snap to the edge.
 *
 * The wheel is square; give it a width or size. Screen readers announce hue and saturation and offer actions to
 * turn the hue and change the saturation.
 */
@Composable
public fun HsvColorWheel(
    hue: Float,
    saturation: Float,
    onHueSaturationChange: (hue: Float, saturation: Float) -> Unit,
    modifier: Modifier = Modifier,
    value: Float = 1f,
    thumbRadius: Dp = 14.dp,
    contentDescription: String = "Color wheel",
) {
    val currentHue by rememberUpdatedState(hue)
    val currentSaturation by rememberUpdatedState(saturation)
    val onChange by rememberUpdatedState(onHueSaturationChange)
    Canvas(
        modifier
            .aspectRatio(1f)
            .semantics {
                this.contentDescription = contentDescription
                stateDescription = "Hue ${hue.degrees()}, saturation ${saturation.percent()}"
                customActions = listOf(
                    CustomAccessibilityAction("Turn hue clockwise") {
                        onChange(ColorMath.normalizeHue(currentHue + 15f), currentSaturation)
                        true
                    },
                    CustomAccessibilityAction("Turn hue counterclockwise") {
                        onChange(ColorMath.normalizeHue(currentHue - 15f), currentSaturation)
                        true
                    },
                    CustomAccessibilityAction("More saturated") {
                        onChange(currentHue, (currentSaturation + 0.1f).coerceAtMost(1f))
                        true
                    },
                    CustomAccessibilityAction("Less saturated") {
                        onChange(currentHue, (currentSaturation - 0.1f).coerceAtLeast(0f))
                        true
                    },
                )
            }
            .pressAndDrag(thumbRadius) { position, size ->
                val radius = minOf(size.width, size.height) / 2f - thumbRadius.toPx()
                val picked = WheelGeometry.hueSaturationAt(
                    x = position.x,
                    y = position.y,
                    centerX = size.width / 2f,
                    centerY = size.height / 2f,
                    radius = radius,
                )
                // At the very center the hue is undefined; keep the current one so the color does not jump to red.
                onChange(if (picked.saturation == 0f) currentHue else picked.hue, picked.saturation)
            },
    ) {
        val thumb = thumbRadius.toPx()
        val radius = (size.minDimension / 2f - thumb).coerceAtLeast(1f)
        val c = center
        drawCircle(Brush.sweepGradient(HueSpectrum, center = c), radius = radius, center = c)
        drawCircle(
            Brush.radialGradient(listOf(Color.White, Color.White.copy(alpha = 0f)), center = c, radius = radius),
            radius = radius,
            center = c,
        )
        if (value < 1f) {
            drawCircle(Color.Black.copy(alpha = (1f - value).coerceIn(0f, 1f)), radius = radius, center = c)
        }
        val point = WheelGeometry.positionOf(hue, saturation, c.x, c.y, radius)
        drawThumb(Offset(point.x, point.y), thumb, Color(ColorMath.hsvToArgb(hue, saturation, value)))
    }
}

/** A [HsvColorWheel] bound to a [ColorPickerState], drawn at the state's brightness. */
@Composable
public fun HsvColorWheel(
    state: ColorPickerState,
    modifier: Modifier = Modifier,
    thumbRadius: Dp = 14.dp,
    contentDescription: String = "Color wheel",
) {
    HsvColorWheel(
        hue = state.hue,
        saturation = state.saturation,
        onHueSaturationChange = state::updateHueSaturation,
        modifier = modifier,
        value = state.value,
        thumbRadius = thumbRadius,
        contentDescription = contentDescription,
    )
}
