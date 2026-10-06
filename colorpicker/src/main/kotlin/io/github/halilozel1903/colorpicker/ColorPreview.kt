package io.github.halilozel1903.colorpicker

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.halilozel1903.colorpicker.core.ColorContrast
import io.github.halilozel1903.colorpicker.core.WcagLevel

/**
 * The color before and after picking, side by side, each labelled with its hex code. The new color carries a
 * contrast badge: the WCAG ratio and level of [contrastColor] text on it (by default white or black, whichever
 * reads better), so you can see at a glance whether text on the color stays readable.
 *
 * @param onOldColorClick when set, tapping the old half calls it, for example to go back to the old color.
 */
@Composable
public fun ColorPreview(
    oldColor: Color,
    newColor: Color,
    modifier: Modifier = Modifier,
    height: Dp = 80.dp,
    oldLabel: String = "Current",
    newLabel: String = "New",
    contrastColor: Color? = null,
    showContrastBadge: Boolean = true,
    onOldColorClick: (() -> Unit)? = null,
) {
    val shape = RoundedCornerShape(16.dp)
    Row(
        modifier
            .fillMaxWidth()
            .height(height)
            .clip(shape)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape),
    ) {
        PreviewHalf(
            color = oldColor,
            label = oldLabel,
            badge = null,
            modifier = Modifier
                .weight(1f)
                .then(
                    if (onOldColorClick != null) {
                        Modifier.clickable(onClickLabel = "Use $oldLabel color", role = Role.Button, onClick = onOldColorClick)
                    } else {
                        Modifier
                    },
                ),
        )
        PreviewHalf(
            color = newColor,
            label = newLabel,
            badge = if (showContrastBadge) contrastBadge(newColor, contrastColor) else null,
            modifier = Modifier.weight(1f),
        )
    }
}

private class Badge(val text: String, val spoken: String)

private fun contrastBadge(background: Color, textColor: Color?): Badge {
    val opaqueBackground = background.compositeOver(Color.White)
    val foreground = textColor ?: opaqueBackground.bestOnColor()
    val ratio = ColorContrast.contrastRatio(foreground.toArgb(), opaqueBackground.toArgb())
    val level = ColorContrast.level(ratio)
    val levelText = if (level == WcagLevel.Fail) "Low" else level.name
    val formatted = ColorContrast.formatRatio(ratio)
    return Badge(
        text = "Aa $formatted $levelText",
        spoken = "Text contrast ${formatted.removeSuffix(":1")} to 1, " +
            if (level == WcagLevel.Fail) "below WCAG AA" else "WCAG ${level.name}",
    )
}

@Composable
private fun PreviewHalf(color: Color, label: String, badge: Badge?, modifier: Modifier) {
    val onColor = color.compositeOver(Color.White).bestOnColor()
    Box(
        modifier
            .fillMaxHeight()
            .checkerboard(color.alpha < 1f)
            .background(color)
            .semantics(mergeDescendants = true) {
                contentDescription = buildString {
                    append("$label color ${describe(color)}")
                    if (badge != null) append(". ${badge.spoken}")
                }
            }
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Column(
            Modifier.clearAndSetSemantics {}.align(Alignment.TopStart),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = onColor.copy(alpha = 0.8f))
            Text(
                color.toHex(),
                style = MaterialTheme.typography.labelLarge,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold,
                color = onColor,
            )
        }
        if (badge != null) {
            Text(
                text = badge.text,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = onColor,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .clearAndSetSemantics {}
                    .border(1.dp, onColor.copy(alpha = 0.6f), RoundedCornerShape(50))
                    .padding(horizontal = 8.dp, vertical = 2.dp),
            )
        }
    }
}
