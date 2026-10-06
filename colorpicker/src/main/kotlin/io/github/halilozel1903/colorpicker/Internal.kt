package io.github.halilozel1903.colorpicker

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import io.github.halilozel1903.colorpicker.core.HexColor
import kotlin.math.ceil
import kotlin.math.roundToInt

internal val CheckerLight = Color(0xFFFFFFFF)
internal val CheckerDark = Color(0xFFD0D0D0)

/** Fills the draw area with a light/dark checkerboard so translucent colors show their alpha. */
internal fun DrawScope.drawCheckerboard(cellSize: Float) {
    drawRect(CheckerLight)
    if (cellSize <= 0f) return
    val columns = ceil(size.width / cellSize).toInt()
    val rows = ceil(size.height / cellSize).toInt()
    val cell = Size(cellSize, cellSize)
    for (row in 0 until rows) {
        for (column in 0 until columns) {
            if ((row + column) % 2 == 1) {
                drawRect(CheckerDark, topLeft = Offset(column * cellSize, row * cellSize), size = cell)
            }
        }
    }
}

/** Draws a checkerboard behind the content. Clip first (for example with `Modifier.clip`) to shape it. */
internal fun Modifier.checkerboard(enabled: Boolean = true): Modifier =
    if (!enabled) this else drawBehind { drawCheckerboard(6.dp.toPx()) }

/** A picker thumb: the picked color in a white ring with a soft dark outline, readable on any background. */
internal fun DrawScope.drawThumb(center: Offset, radius: Float, fill: Color) {
    val ring = 3.dp.toPx()
    drawCircle(Color.Black.copy(alpha = 0.28f), radius = radius + 1.dp.toPx(), center = center)
    drawCircle(Color.White, radius = radius, center = center)
    if (fill.alpha < 1f) {
        drawCircle(CheckerDark, radius = radius - ring, center = center)
    }
    drawCircle(fill, radius = radius - ring, center = center)
    drawCircle(Color.Black.copy(alpha = 0.18f), radius = radius - ring, center = center, style = Stroke(1.dp.toPx()))
}

/**
 * Calls [onPosition] with the pointer position and the component size for the press and every move of a drag,
 * consuming the events so a scrolling parent does not steal the drag.
 */
internal fun Modifier.pressAndDrag(
    key: Any?,
    onPosition: Density.(position: Offset, size: IntSize) -> Unit,
): Modifier =
    pointerInput(key) {
        val density: Density = this
        awaitEachGesture {
            val down = awaitFirstDown()
            down.consume()
            onPosition(density, down.position, size)
            drag(down.id) { change ->
                change.consume()
                onPosition(density, change.position, size)
            }
        }
    }

internal fun Float.percent(): String = "${(this * 100).roundToInt()}%"

internal fun Float.degrees(): String = "${roundToInt() % 360}°"

/** A short spoken description of a color: its hex code and alpha. */
internal fun describe(color: Color): String {
    val hex = color.toHex(includeAlpha = false)
    return if (color.alpha < 1f) "$hex, ${color.alpha.percent()} opaque" else hex
}

internal fun hexDigits(color: Color, includeAlpha: Boolean): String =
    HexColor.format(color.toArgb(), includeAlpha = includeAlpha, withHash = false)
