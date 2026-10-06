package io.github.halilozel1903.colorpicker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.width
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.halilozel1903.colorpicker.core.ColorMath
import kotlin.math.roundToInt

/** The channels [ColorSliders] shows. */
public enum class ColorSliderMode {
    /** Red, green and blue, 0 to 255. */
    Rgb,

    /** Hue in degrees, saturation and value in percent. */
    Hsv,
}

/**
 * One slider per channel, in RGB or HSV, each with a gradient track that previews where it leads, plus an
 * optional opacity slider. With [showModeSelector] the user can switch between RGB and HSV.
 */
@Composable
public fun ColorSliders(
    state: ColorPickerState,
    modifier: Modifier = Modifier,
    initialMode: ColorSliderMode = ColorSliderMode.Rgb,
    showModeSelector: Boolean = true,
    showAlpha: Boolean = true,
) {
    var mode by rememberSaveable { mutableStateOf(initialMode) }
    Column(modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (showModeSelector) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ColorSliderMode.entries.forEach { option ->
                    FilterChip(
                        selected = mode == option,
                        onClick = { mode = option },
                        label = { Text(if (option == ColorSliderMode.Rgb) "RGB" else "HSV") },
                    )
                }
            }
        }
        when (mode) {
            ColorSliderMode.Rgb -> RgbSliders(state)
            ColorSliderMode.Hsv -> HsvSliders(state)
        }
        if (showAlpha) {
            ChannelRow(label = "A", valueText = state.alpha.percent()) {
                AlphaSlider(state, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun RgbSliders(state: ColorPickerState) {
    val argb = state.opaqueColor.toArgb()
    val channels = listOf(
        Triple("R", "Red", ColorMath.red(argb)),
        Triple("G", "Green", ColorMath.green(argb)),
        Triple("B", "Blue", ColorMath.blue(argb)),
    )
    channels.forEachIndexed { index, (label, name, channel) ->
        fun withChannel(newValue: Int): Int = when (index) {
            0 -> ColorMath.rgb(newValue, ColorMath.green(argb), ColorMath.blue(argb))
            1 -> ColorMath.rgb(ColorMath.red(argb), newValue, ColorMath.blue(argb))
            else -> ColorMath.rgb(ColorMath.red(argb), ColorMath.green(argb), newValue)
        }
        ChannelRow(label = label, valueText = channel.toString()) {
            ColorChannelSlider(
                value = channel.toFloat(),
                onValueChange = { state.setColor(Color(withChannel(it.roundToInt())), keepAlpha = true) },
                trackBrush = Brush.horizontalGradient(listOf(Color(withChannel(0)), Color(withChannel(255)))),
                thumbColor = state.opaqueColor,
                contentDescription = name,
                modifier = Modifier.weight(1f),
                valueRange = 0f..255f,
                stateDescription = channel.toString(),
                keyboardStep = 1f,
            )
        }
    }
}

@Composable
private fun HsvSliders(state: ColorPickerState) {
    ChannelRow(label = "H", valueText = state.hue.degrees()) {
        HueSlider(state, Modifier.weight(1f))
    }
    ChannelRow(label = "S", valueText = state.saturation.percent()) {
        ColorChannelSlider(
            value = state.saturation,
            onValueChange = state::updateSaturation,
            trackBrush = Brush.horizontalGradient(
                listOf(
                    Color(ColorMath.hsvToArgb(state.hue, 0f, state.value)),
                    Color(ColorMath.hsvToArgb(state.hue, 1f, state.value)),
                ),
            ),
            thumbColor = state.opaqueColor,
            contentDescription = "Saturation",
            modifier = Modifier.weight(1f),
        )
    }
    ChannelRow(label = "V", valueText = state.value.percent()) {
        ColorChannelSlider(
            value = state.value,
            onValueChange = state::updateValue,
            trackBrush = Brush.horizontalGradient(
                listOf(Color.Black, Color(ColorMath.hsvToArgb(state.hue, state.saturation, 1f))),
            ),
            thumbColor = state.opaqueColor,
            contentDescription = "Brightness",
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun ChannelRow(
    label: String,
    valueText: String,
    slider: @Composable RowScope.() -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        // The slider announces its own name and value, so the visible labels are hidden from screen readers.
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(16.dp).clearAndSetSemantics {},
        )
        slider()
        Text(
            text = valueText,
            style = MaterialTheme.typography.labelLarge,
            fontFamily = FontFamily.Monospace,
            textAlign = TextAlign.End,
            modifier = Modifier.width(44.dp).clearAndSetSemantics {},
        )
    }
}
