package io.github.halilozel1903.colorpicker.sample

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.halilozel1903.colorpicker.bestOnColor
import io.github.halilozel1903.colorpicker.core.ColorContrast
import io.github.halilozel1903.colorpicker.core.WcagLevel
import io.github.halilozel1903.colorpicker.toHex

/** Juniper Bakery's home screen drawn with the theme being built. */
@Composable
fun ThemePreviewCard(colors: ThemeColors, modifier: Modifier = Modifier) {
    val background = colors.background.compositeOver(Color.White)
    val onBackground = background.bestOnColor(light = Color.White, dark = Color(0xFF1C1B1F))
    val brand = colors.brand.compositeOver(background)
    val onBrand = brand.bestOnColor()
    val accent = colors.accent.compositeOver(background)
    val onAccent = accent.bestOnColor(light = Color.White, dark = Color(0xFF1C1B1F))
    val shape = RoundedCornerShape(24.dp)
    Column(
        modifier
            .clip(shape)
            .background(background)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .background(brand)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(accent),
                contentAlignment = Alignment.Center,
            ) {
                Text("J", color = onAccent, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
            }
            Spacer(Modifier.width(12.dp))
            Text("Juniper Bakery", color = onBrand, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Today's specials", color = onBackground, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(
                "Fresh from the oven at 7 am",
                color = onBackground.copy(alpha = 0.72f),
                style = MaterialTheme.typography.bodyMedium,
            )
            MenuItem("Sourdough loaf", "Vegan", "\$6.50", brand, onBackground)
            MenuItem("Cardamom bun", "New", "\$3.80", brand, onBackground)
            MenuItem("Rye crispbread", "Classic", "\$4.20", brand, onBackground)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .clip(RoundedCornerShape(50))
                        .background(accent)
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                ) {
                    Text("Order now", color = onAccent, fontWeight = FontWeight.SemiBold)
                }
                Box(
                    Modifier
                        .border(1.dp, brand, RoundedCornerShape(50))
                        .padding(horizontal = 16.dp, vertical = 9.dp),
                ) {
                    Text("Pickup", color = brand, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
private fun MenuItem(name: String, tag: String, price: String, brand: Color, onBackground: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(brand.copy(alpha = 0.16f)),
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(name, color = onBackground, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text(tag, color = onBackground.copy(alpha = 0.64f), style = MaterialTheme.typography.bodySmall)
        }
        Text(price, color = onBackground, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
    }
}

/** One color of the theme: swatch, name, hint and hex code. Tapping it edits the color. */
@Composable
fun RoleRow(
    role: ColorRole,
    color: Color,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showEdit: Boolean = true,
) {
    val shape = RoundedCornerShape(16.dp)
    Row(
        modifier
            .fillMaxWidth()
            .clip(shape)
            .background(if (selected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainerLow)
            .clickable(onClickLabel = "Edit ${role.label} color", role = Role.Button, onClick = onClick)
            .padding(start = 12.dp, top = 10.dp, bottom = 10.dp, end = if (showEdit) 4.dp else 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(color)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp)),
        )
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(role.label, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(role.hint, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(color.toHex(), style = MaterialTheme.typography.labelLarge, fontFamily = FontFamily.Monospace)
        if (showEdit) TextButton(onClick = onClick) { Text("Edit") }
    }
}

/** WCAG contrast of the pairs that matter for the theme. */
@Composable
fun ContrastChecks(colors: ThemeColors, modifier: Modifier = Modifier) {
    val background = colors.background.compositeOver(Color.White)
    val brand = colors.brand.compositeOver(background)
    val accent = colors.accent.compositeOver(background)
    val checks = listOf(
        "Text on background" to ratio(background.bestOnColor(dark = Color(0xFF1C1B1F)), background),
        "Text on brand" to ratio(brand.bestOnColor(), brand),
        "Text on accent" to ratio(accent.bestOnColor(dark = Color(0xFF1C1B1F)), accent),
        "Brand on background" to ratio(brand, background),
    )
    Column(modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("Contrast checks", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        checks.forEach { (label, value) ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                Text(
                    ColorContrast.formatRatio(value),
                    style = MaterialTheme.typography.labelLarge,
                    fontFamily = FontFamily.Monospace,
                )
                Spacer(Modifier.width(8.dp))
                LevelBadge(ColorContrast.level(value))
            }
        }
    }
}

private fun ratio(foreground: Color, background: Color): Double =
    ColorContrast.contrastRatio(foreground.toArgb(), background.toArgb())

@Composable
private fun LevelBadge(level: WcagLevel) {
    val (container, content) = when (level) {
        WcagLevel.AAA -> Color(0xFF1B5E20) to Color.White
        WcagLevel.AA -> Color(0xFF2E7D32) to Color.White
        WcagLevel.Fail -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
    }
    Box(
        Modifier
            .width(48.dp)
            .clip(RoundedCornerShape(50))
            .background(container)
            .padding(vertical = 2.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(if (level == WcagLevel.Fail) "Low" else level.name, color = content, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
    }
}
