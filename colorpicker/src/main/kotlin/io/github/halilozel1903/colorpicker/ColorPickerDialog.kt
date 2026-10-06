package io.github.halilozel1903.colorpicker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/**
 * A Material 3 dialog around [ColorPicker]: a title, the old and new color, Wheel / Sliders / Swatches tabs, a hex
 * field, an opacity slider and Cancel / Select buttons. Select adds the color to the state's recent colors and
 * calls [onColorSelected].
 *
 * The dialog does not hide itself: stop showing it in [onColorSelected] and [onDismissRequest].
 *
 * ```
 * var showPicker by remember { mutableStateOf(false) }
 * if (showPicker) {
 *     ColorPickerDialog(
 *         initialColor = brand,
 *         onDismissRequest = { showPicker = false },
 *         onColorSelected = { brand = it; showPicker = false },
 *     )
 * }
 * ```
 *
 * Pass your own [state] (from [rememberColorPickerState]) to keep recent colors between openings.
 */
@Composable
public fun ColorPickerDialog(
    initialColor: Color,
    onDismissRequest: () -> Unit,
    onColorSelected: (Color) -> Unit,
    modifier: Modifier = Modifier,
    state: ColorPickerState = rememberColorPickerState(initialColor),
    title: String = ColorPickerDefaults.TITLE,
    initialTab: ColorPickerTab = ColorPickerTab.Wheel,
    tabs: List<ColorPickerTab> = ColorPickerDefaults.tabs,
    showAlpha: Boolean = true,
    swatches: List<Color> = ColorPickerDefaults.swatches,
    confirmText: String = "Select",
    dismissText: String = "Cancel",
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = modifier
                .widthIn(max = 520.dp)
                .padding(horizontal = 16.dp, vertical = 24.dp)
                .semantics { paneTitle = title },
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 6.dp,
        ) {
            Column(Modifier.padding(top = 24.dp, bottom = 12.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 16.dp),
                )
                ColorPicker(
                    state = state,
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp),
                    initialTab = initialTab,
                    tabs = tabs,
                    showAlpha = showAlpha,
                    swatches = swatches,
                )
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp, end = 12.dp, top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                ) {
                    TextButton(onClick = onDismissRequest) { Text(dismissText) }
                    TextButton(
                        onClick = {
                            state.addToRecent()
                            onColorSelected(state.color)
                        },
                    ) { Text(confirmText) }
                }
            }
        }
    }
}
