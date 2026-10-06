package io.github.halilozel1903.colorpicker

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.halilozel1903.colorpicker.core.MaterialPalette

/**
 * One round color swatch. Translucent colors show a checkerboard, and when [selected] a check mark is drawn in
 * whichever of white and black reads better on the color. It is a selectable, radio button like item for
 * accessibility.
 */
@Composable
public fun ColorSwatch(
    color: Color,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    shape: Shape = CircleShape,
    contentDescription: String = describe(color),
) {
    val outline = MaterialTheme.colorScheme.outlineVariant
    val selectedRing = MaterialTheme.colorScheme.onSurface
    Box(
        modifier
            .size(size)
            .border(if (selected) 2.dp else 1.dp, if (selected) selectedRing else outline, shape)
            .padding(if (selected) 4.dp else 0.dp)
            .clip(shape)
            .selectable(selected = selected, onClick = onClick, role = Role.RadioButton)
            .semantics { this.contentDescription = contentDescription }
            .checkerboard(color.alpha < 1f)
            .background(color),
        contentAlignment = Alignment.Center,
    ) {
        if (selected) {
            val check = color.compositeOver(Color.White).bestOnColor()
            Canvas(Modifier.fillMaxSize().padding(size / 5)) {
                val w = this.size.width
                val h = this.size.height
                val path = Path().apply {
                    moveTo(0.1f * w, 0.52f * h)
                    lineTo(0.4f * w, 0.8f * h)
                    lineTo(0.9f * w, 0.22f * h)
                }
                drawPath(
                    path = path,
                    color = check,
                    style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
                )
            }
        }
    }
}

/**
 * A grid of [colors] that wraps to as many columns as fit. The swatch equal to [selectedColor] is marked.
 *
 * @param contentDescriptionFor what screen readers say for each swatch, by default its hex code.
 */
@Composable
public fun SwatchGrid(
    colors: List<Color>,
    onColorSelected: (Color) -> Unit,
    modifier: Modifier = Modifier,
    selectedColor: Color? = null,
    swatchSize: Dp = 40.dp,
    spacing: Dp = 8.dp,
    contentDescriptionFor: (Color) -> String = ::describe,
) {
    Layout(
        content = {
            colors.forEach { color ->
                ColorSwatch(
                    color = color,
                    selected = color == selectedColor,
                    onClick = { onColorSelected(color) },
                    size = swatchSize,
                    contentDescription = contentDescriptionFor(color),
                )
            }
        },
        modifier = modifier,
    ) { measurables, constraints ->
        val gap = spacing.roundToPx()
        val cell = swatchSize.roundToPx()
        val placeables = measurables.map { it.measure(Constraints.fixed(cell, cell)) }
        val available = if (constraints.hasBoundedWidth) constraints.maxWidth else (cell + gap) * placeables.size
        val columns = ((available + gap) / (cell + gap)).coerceAtLeast(1)
        val rows = if (placeables.isEmpty()) 0 else (placeables.size + columns - 1) / columns
        val usedColumns = minOf(columns, placeables.size)
        val width = (usedColumns * cell + (usedColumns - 1).coerceAtLeast(0) * gap)
            .coerceIn(constraints.minWidth, constraints.maxWidth)
        val height = (rows * cell + (rows - 1).coerceAtLeast(0) * gap)
            .coerceIn(constraints.minHeight, constraints.maxHeight)
        layout(width, height) {
            placeables.forEachIndexed { index, placeable ->
                placeable.placeRelative((index % columns) * (cell + gap), (index / columns) * (cell + gap))
            }
        }
    }
}

/**
 * Recently used colors in a horizontally scrolling row, most recent first. Shows [emptyText] when there are
 * none yet.
 */
@Composable
public fun RecentColorsRow(
    colors: List<Color>,
    onColorSelected: (Color) -> Unit,
    modifier: Modifier = Modifier,
    selectedColor: Color? = null,
    swatchSize: Dp = 36.dp,
    spacing: Dp = 8.dp,
    emptyText: String = "Colors you pick show up here",
) {
    if (colors.isEmpty()) {
        Text(
            text = emptyText,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = modifier.padding(vertical = 8.dp),
        )
        return
    }
    Row(
        modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(spacing),
    ) {
        colors.forEach { color ->
            ColorSwatch(
                color = color,
                selected = color == selectedColor,
                onClick = { onColorSelected(color) },
                size = swatchSize,
                contentDescription = "Recent color ${describe(color)}",
            )
        }
    }
}

/** Ready-made swatch lists. */
public object ColorSwatches {
    /** The Material palette: nineteen hues at tone 500 plus white, greys and black. */
    public val material: List<Color> = MaterialPalette.all.map { Color(it.argb) }
}
