package io.github.halilozel1903.colorpicker.sample

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.halilozel1903.colorpicker.AlphaSlider
import io.github.halilozel1903.colorpicker.ColorPickerDialog
import io.github.halilozel1903.colorpicker.ColorPickerTab
import io.github.halilozel1903.colorpicker.ColorPreview
import io.github.halilozel1903.colorpicker.ColorSwatches
import io.github.halilozel1903.colorpicker.HexColorField
import io.github.halilozel1903.colorpicker.HueSlider
import io.github.halilozel1903.colorpicker.RecentColorsRow
import io.github.halilozel1903.colorpicker.SaturationValuePanel
import io.github.halilozel1903.colorpicker.SwatchGrid
import io.github.halilozel1903.colorpicker.rememberColorPickerState

/**
 * Theme Builder: pick Juniper Bakery's brand, accent and background colors and see them on its home screen.
 * Phones edit a color in [ColorPickerDialog]; wide windows show the picker and the preview side by side.
 */
@Composable
fun ThemeBuilderApp(scene: Scene?) {
    var colors by rememberSaveable(stateSaver = ThemeColors.Saver) { mutableStateOf(ThemeColors.Juniper) }
    var recent by remember { mutableStateOf(StartingRecentColors) }
    BoxWithConstraints(Modifier.fillMaxSize()) {
        if (maxWidth >= 840.dp || scene == Scene.Tablet) {
            TabletBuilder(
                colors = colors,
                onColorsChange = { colors = it },
                recent = recent,
                onRecentChange = { recent = it },
            )
        } else {
            PhoneBuilder(
                colors = colors,
                onColorsChange = { colors = it },
                recent = recent,
                onRecentChange = { recent = it },
                scene = scene,
            )
        }
    }
}

@Composable
private fun PhoneBuilder(
    colors: ThemeColors,
    onColorsChange: (ThemeColors) -> Unit,
    recent: List<Color>,
    onRecentChange: (List<Color>) -> Unit,
    scene: Scene?,
) {
    var editing by rememberSaveable { mutableStateOf(scene?.role) }
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Header()
        ColorRole.entries.forEach { role ->
            RoleRow(role = role, color = colors[role], selected = false, onClick = { editing = role })
        }
        SectionTitle("Live preview", Modifier.padding(top = 8.dp))
        ThemePreviewCard(colors, Modifier.fillMaxWidth())
        ContrastChecks(colors, Modifier.padding(top = 4.dp))
    }

    val role = editing
    if (role != null) {
        key(role) {
            val state = rememberColorPickerState(initialColor = colors[role], recentColors = recent)
            ColorPickerDialog(
                initialColor = colors[role],
                state = state,
                title = "${role.label} color",
                initialTab = if (scene != null && scene.role == role) scene.tab else ColorPickerTab.Wheel,
                onDismissRequest = { editing = null },
                onColorSelected = { color ->
                    onColorsChange(colors.with(role, color))
                    onRecentChange(state.recentColors)
                    editing = null
                },
            )
        }
    }
}

@Composable
private fun TabletBuilder(
    colors: ThemeColors,
    onColorsChange: (ThemeColors) -> Unit,
    recent: List<Color>,
    onRecentChange: (List<Color>) -> Unit,
) {
    var role by rememberSaveable { mutableStateOf(ColorRole.Brand) }
    val picker = key(role) { rememberColorPickerState(initialColor = colors[role], recentColors = recent) }
    // The color being edited is applied live, so the preview follows every drag.
    val live = colors.with(role, picker.color)
    val select = { next: ColorRole ->
        if (next != role) {
            picker.addToRecent()
            onRecentChange(picker.recentColors)
            onColorsChange(live)
            role = next
        }
    }

    Row(
        Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(24.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Surface(
            modifier = Modifier
                .weight(0.42f)
                .fillMaxHeight(),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
        ) {
            Column(
                Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text(
                    "${role.label} color",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.semantics { heading() },
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ColorRole.entries.forEach { option ->
                        FilterChip(
                            selected = option == role,
                            onClick = { select(option) },
                            label = { Text(option.label) },
                        )
                    }
                }
                SaturationValuePanel(
                    picker,
                    Modifier
                        .fillMaxWidth()
                        .height(176.dp),
                )
                HueSlider(picker)
                AlphaSlider(picker)
                HexColorField(
                    color = picker.color,
                    onColorChange = { picker.setColor(it) },
                    modifier = Modifier.fillMaxWidth(),
                )
                SectionTitle("Palette")
                SwatchGrid(
                    colors = ColorSwatches.material,
                    onColorSelected = { picker.setColor(it, keepAlpha = true) },
                    selectedColor = picker.opaqueColor,
                    swatchSize = 32.dp,
                )
                SectionTitle("Recent")
                RecentColorsRow(
                    colors = picker.recentColors,
                    onColorSelected = { picker.setColor(it) },
                    selectedColor = picker.color,
                    swatchSize = 32.dp,
                )
            }
        }
        Column(
            Modifier
                .weight(0.58f)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Header()
            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SectionTitle("Live preview")
                    ThemePreviewCard(live, Modifier.fillMaxWidth())
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SectionTitle("Colors")
                    ColorRole.entries.forEach { option ->
                        RoleRow(
                            role = option,
                            color = live[option],
                            selected = option == role,
                            onClick = { select(option) },
                            showEdit = false,
                        )
                    }
                    ColorPreview(oldColor = picker.originalColor, newColor = picker.color, onOldColorClick = picker::reset)
                    ContrastChecks(live, Modifier.padding(top = 4.dp))
                }
            }
        }
    }
}

@Composable
private fun Header() {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            "Theme Builder",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.semantics { heading() },
        )
        Text(
            "Pick Juniper Bakery's colors and check them on a live preview.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier.semantics { heading() },
    )
}
