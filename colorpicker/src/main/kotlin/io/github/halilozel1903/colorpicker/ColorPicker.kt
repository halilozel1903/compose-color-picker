package io.github.halilozel1903.colorpicker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.halilozel1903.colorpicker.core.ColorMath
import io.github.halilozel1903.colorpicker.core.Palettes

/** The pages of [ColorPicker] and [ColorPickerDialog]. */
public enum class ColorPickerTab(public val label: String) {
    /** The HSV wheel with a brightness slider. */
    Wheel("Wheel"),

    /** RGB or HSV channel sliders. */
    Sliders("Sliders"),

    /** The Material palette, tints and shades, harmonies and recent colors. */
    Swatches("Swatches"),
}

/** Defaults for [ColorPicker] and [ColorPickerDialog]. */
public object ColorPickerDefaults {
    /** The dialog title. */
    public const val TITLE: String = "Pick a color"

    /** The swatches on the Swatches tab: the Material palette. */
    public val swatches: List<Color> get() = ColorSwatches.material

    /** The largest the wheel grows on the Wheel tab. */
    public val WheelMaxSize: Dp = 280.dp

    /** All tabs, in order. */
    public val tabs: List<ColorPickerTab> get() = ColorPickerTab.entries
}

/**
 * A complete color picker without a dialog around it: the old and new color with a contrast badge, tabs for the
 * wheel, sliders and swatches, a hex field and an opacity slider. Use it inline, in a bottom sheet or in a side
 * pane; [ColorPickerDialog] wraps it in a Material 3 dialog.
 *
 * It does not scroll by itself; add `Modifier.verticalScroll` when the space is short.
 *
 * @param tabs which tabs to show and in what order. With one tab the tab row is hidden.
 * @param showAlpha shows the opacity slider and accepts `#AARRGGBB` in the hex field.
 */
@Composable
public fun ColorPicker(
    state: ColorPickerState,
    modifier: Modifier = Modifier,
    initialTab: ColorPickerTab = ColorPickerTab.Wheel,
    tabs: List<ColorPickerTab> = ColorPickerDefaults.tabs,
    showAlpha: Boolean = true,
    showPreview: Boolean = true,
    showHexField: Boolean = true,
    swatches: List<Color> = ColorPickerDefaults.swatches,
) {
    require(tabs.isNotEmpty()) { "ColorPicker needs at least one tab" }
    var selectedTab by rememberSaveable { mutableStateOf(initialTab) }
    val shownTab = if (selectedTab in tabs) selectedTab else tabs.first()

    Column(modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        if (showPreview) {
            ColorPreview(
                oldColor = state.originalColor,
                newColor = state.color,
                onOldColorClick = state::reset,
            )
        }
        if (tabs.size > 1) {
            ColorPickerTabs(tabs, shownTab, onSelect = { selectedTab = it })
        }
        when (shownTab) {
            ColorPickerTab.Wheel -> WheelPage(state)
            ColorPickerTab.Sliders -> ColorSliders(state, showAlpha = false)
            ColorPickerTab.Swatches -> SwatchesPage(state, swatches)
        }
        if (showHexField) {
            HexColorField(
                color = state.color,
                onColorChange = { state.setColor(it, keepAlpha = !showAlpha) },
                includeAlpha = showAlpha,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (showAlpha) {
            LabeledRow("Opacity", state.alpha.percent()) {
                AlphaSlider(state, Modifier.weight(1f))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ColorPickerTabs(tabs: List<ColorPickerTab>, selected: ColorPickerTab, onSelect: (ColorPickerTab) -> Unit) {
    PrimaryTabRow(selectedTabIndex = tabs.indexOf(selected), containerColor = Color.Transparent) {
        tabs.forEach { tab ->
            Tab(
                selected = tab == selected,
                onClick = { onSelect(tab) },
                text = { Text(tab.label) },
            )
        }
    }
}

@Composable
private fun WheelPage(state: ColorPickerState) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        HsvColorWheel(state, Modifier.widthIn(max = ColorPickerDefaults.WheelMaxSize).fillMaxWidth())
        LabeledRow("Brightness", state.value.percent()) {
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
}

@Composable
private fun SwatchesPage(state: ColorPickerState, swatches: List<Color>) {
    // Tints, shades and harmonies follow the last palette color picked, so picking a tint does not move the scale.
    var anchor by remember { mutableStateOf(state.opaqueColor) }
    val anchorArgb = anchor.toArgb()
    val scale = remember(anchorArgb) { Palettes.tintsAndShades(anchorArgb, 3).map { Color(it) } }
    val harmonies = remember(anchorArgb) {
        (Palettes.complementary(anchorArgb).drop(1) + Palettes.analogous(anchorArgb, 30f).filter { it != anchorArgb } +
            Palettes.triadic(anchorArgb).drop(1)).map { Color(it) }
    }
    val pick = { color: Color -> state.setColor(color, keepAlpha = true) }
    val selected = state.opaqueColor
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionTitle("Palette")
        SwatchGrid(
            colors = swatches,
            onColorSelected = { color ->
                anchor = color.copy(alpha = 1f)
                pick(color)
            },
            selectedColor = selected,
            swatchSize = 36.dp,
        )
        SectionTitle("Tints and shades")
        SwatchGrid(scale, onColorSelected = pick, selectedColor = selected, swatchSize = 32.dp, spacing = 6.dp)
        SectionTitle("Harmonies")
        SwatchGrid(harmonies, onColorSelected = pick, selectedColor = selected, swatchSize = 32.dp, spacing = 6.dp)
        SectionTitle("Recent")
        RecentColorsRow(state.recentColors, onColorSelected = { state.setColor(it) }, selectedColor = state.color)
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.semantics { heading() },
    )
}

@Composable
private fun LabeledRow(label: String, valueText: String, content: @Composable RowScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(valueText, style = MaterialTheme.typography.labelLarge)
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, content = content)
    }
}
