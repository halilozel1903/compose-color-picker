package io.github.halilozel1903.colorpicker

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.halilozel1903.colorpicker.core.ColorMath

/**
 * A slider with a color gradient track, the building block of [HueSlider], [AlphaSlider] and [ColorSliders].
 *
 * It behaves like a Material slider for accessibility: screen readers announce [contentDescription] and
 * [stateDescription] and can set the value directly (`setProgress`), and with a keyboard or D-pad the arrow keys
 * move it by [keyboardStep].
 *
 * @param trackBrush paints the track, usually a horizontal gradient from the color at the start of [valueRange]
 * to the color at its end.
 * @param thumbColor the color shown inside the thumb.
 * @param checkerboard draws a checkerboard under the track, for translucent gradients.
 */
@Composable
public fun ColorChannelSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    trackBrush: Brush,
    thumbColor: Color,
    contentDescription: String,
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    stateDescription: String = defaultStateDescription(value, valueRange),
    checkerboard: Boolean = false,
    keyboardStep: Float = (valueRange.endInclusive - valueRange.start) / 100f,
    height: Dp = 28.dp,
) {
    val range by rememberUpdatedState(valueRange)
    val onChange by rememberUpdatedState(onValueChange)
    val currentValue by rememberUpdatedState(value)
    val step by rememberUpdatedState(keyboardStep)
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()
    val focusColor = MaterialTheme.colorScheme.primary
    val span = valueRange.endInclusive - valueRange.start
    val fraction = if (span > 0f) ((value - valueRange.start) / span).coerceIn(0f, 1f) else 0f

    fun set(target: Float) {
        val clamped = target.coerceIn(range.start, range.endInclusive)
        if (clamped != currentValue) onChange(clamped)
    }

    Canvas(
        modifier
            .fillMaxWidth()
            .height(height)
            .semantics {
                this.contentDescription = contentDescription
                this.stateDescription = stateDescription
                progressBarRangeInfo = ProgressBarRangeInfo(value.coerceIn(valueRange.start, valueRange.endInclusive), valueRange)
                setProgress { target ->
                    val clamped = target.coerceIn(range.start, range.endInclusive)
                    if (clamped == currentValue) {
                        false
                    } else {
                        onChange(clamped)
                        true
                    }
                }
            }
            .onKeyEvent { event ->
                if (event.type != KeyEventType.KeyDown) return@onKeyEvent false
                when (event.key) {
                    Key.DirectionRight, Key.DirectionUp -> {
                        set(currentValue + step)
                        true
                    }
                    Key.DirectionLeft, Key.DirectionDown -> {
                        set(currentValue - step)
                        true
                    }
                    Key.MoveHome -> {
                        set(range.start)
                        true
                    }
                    Key.MoveEnd -> {
                        set(range.endInclusive)
                        true
                    }
                    else -> false
                }
            }
            .focusable(interactionSource = interactionSource)
            .pressAndDrag(Unit) { position, size ->
                val radius = size.height / 2f
                val usable = size.width - 2 * radius
                val f = if (usable > 0f) ((position.x - radius) / usable).coerceIn(0f, 1f) else 0f
                set(range.start + f * (range.endInclusive - range.start))
            },
    ) {
        val radius = size.height / 2f
        val trackHeight = size.height - 6.dp.toPx()
        val top = (size.height - trackHeight) / 2f
        val track = Path().apply {
            addRoundRect(RoundRect(0f, top, size.width, top + trackHeight, CornerRadius(trackHeight / 2f)))
        }
        clipPath(track) {
            if (checkerboard) drawCheckerboard(trackHeight / 3f)
            drawRect(trackBrush)
        }
        drawPath(track, Color.Black.copy(alpha = 0.12f), style = Stroke(1.dp.toPx()))
        val thumbCenter = Offset(radius + fraction * (size.width - 2 * radius), size.height / 2f)
        if (focused) {
            drawCircle(focusColor, radius = radius + 3.dp.toPx(), center = thumbCenter, style = Stroke(2.dp.toPx()))
        }
        drawThumb(thumbCenter, radius, thumbColor)
    }
}

/** Describes [value] as a percentage of [valueRange]. */
internal fun defaultStateDescription(value: Float, valueRange: ClosedFloatingPointRange<Float>): String {
    val span = valueRange.endInclusive - valueRange.start
    return (if (span > 0f) (value - valueRange.start) / span else 0f).percent()
}

/** A hue slider over the full spectrum, 0 to 360 degrees. */
@Composable
public fun HueSlider(
    hue: Float,
    onHueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String = "Hue",
    height: Dp = 28.dp,
) {
    ColorChannelSlider(
        value = hue,
        onValueChange = onHueChange,
        trackBrush = Brush.horizontalGradient(HueSpectrum),
        thumbColor = Color(ColorMath.hsvToArgb(hue, 1f, 1f)),
        contentDescription = contentDescription,
        modifier = modifier,
        valueRange = 0f..360f,
        stateDescription = hue.degrees(),
        keyboardStep = 1f,
        height = height,
    )
}

/** A [HueSlider] bound to a [ColorPickerState]. */
@Composable
public fun HueSlider(
    state: ColorPickerState,
    modifier: Modifier = Modifier,
    contentDescription: String = "Hue",
    height: Dp = 28.dp,
) {
    HueSlider(state.hue, state::updateHue, modifier, contentDescription, height)
}

/**
 * An alpha (opacity) slider for [color] over a checkerboard, from fully transparent to opaque.
 * The alpha of [color] itself is ignored; [alpha] is shown.
 */
@Composable
public fun AlphaSlider(
    alpha: Float,
    onAlphaChange: (Float) -> Unit,
    color: Color,
    modifier: Modifier = Modifier,
    contentDescription: String = "Opacity",
    height: Dp = 28.dp,
) {
    val opaque = color.copy(alpha = 1f)
    ColorChannelSlider(
        value = alpha,
        onValueChange = onAlphaChange,
        trackBrush = Brush.horizontalGradient(listOf(opaque.copy(alpha = 0f), opaque)),
        thumbColor = opaque.copy(alpha = alpha.coerceIn(0f, 1f)),
        contentDescription = contentDescription,
        modifier = modifier,
        valueRange = 0f..1f,
        stateDescription = alpha.percent(),
        checkerboard = true,
        height = height,
    )
}

/** An [AlphaSlider] bound to a [ColorPickerState]. */
@Composable
public fun AlphaSlider(
    state: ColorPickerState,
    modifier: Modifier = Modifier,
    contentDescription: String = "Opacity",
    height: Dp = 28.dp,
) {
    AlphaSlider(state.alpha, state::updateAlpha, state.opaqueColor, modifier, contentDescription, height)
}
