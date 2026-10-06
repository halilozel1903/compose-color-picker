package io.github.halilozel1903.colorpicker

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import io.github.halilozel1903.colorpicker.core.ColorMath
import io.github.halilozel1903.colorpicker.core.HexColor
import io.github.halilozel1903.colorpicker.core.HexValidation

/**
 * A text field for hex colors. It accepts `RGB`, `RRGGBB` and, with [includeAlpha], `AARRGGBB` (the `#` is shown
 * as a prefix and stripped from pasted text). Every valid entry calls [onColorChange] right away; invalid text
 * shows an error and leaves the color alone. When [color] changes elsewhere (a slider, the wheel) the text follows.
 *
 * Three and six digit entries keep the current alpha; eight digits set it.
 */
@Composable
public fun HexColorField(
    color: Color,
    onColorChange: (Color) -> Unit,
    modifier: Modifier = Modifier,
    includeAlpha: Boolean = true,
    label: String = "Hex",
    showSwatch: Boolean = true,
) {
    val argb = color.toArgb()
    val showAlpha = includeAlpha && ColorMath.alpha(argb) != 255
    var text by remember { mutableStateOf(hexDigits(color, showAlpha)) }
    val currentArgb by rememberUpdatedState(argb)
    var lastAlpha by remember { mutableIntStateOf(ColorMath.alpha(argb)) }

    // Follow outside changes, but never rewrite what the user is typing when it already means this color.
    LaunchedEffect(argb, includeAlpha) {
        val alphaChanged = includeAlpha && ColorMath.alpha(argb) != lastAlpha
        if (alphaChanged || !textMatches(text, argb)) {
            text = hexDigits(Color(argb), includeAlpha && ColorMath.alpha(argb) != 255)
        }
        lastAlpha = ColorMath.alpha(argb)
    }

    val validation = HexColor.validate(text)
    val focusManager: FocusManager = LocalFocusManager.current
    OutlinedTextField(
        value = text,
        onValueChange = { input ->
            val cleaned = HexColor.sanitize(input, maxLength = if (includeAlpha) 8 else 6)
            text = cleaned
            val parsed = HexColor.parse(cleaned) ?: return@OutlinedTextField
            val next = if (HexColor.hasAlpha(cleaned)) parsed else ColorMath.withAlpha(parsed, ColorMath.alphaFraction(currentArgb))
            if (next != currentArgb) onColorChange(Color(next))
        },
        modifier = modifier,
        label = { Text(label) },
        prefix = { Text("#") },
        trailingIcon = if (showSwatch) {
            {
                Box(
                    Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .checkerboard(color.alpha < 1f)
                        .background(color)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(6.dp)),
                )
            }
        } else {
            null
        },
        isError = validation == HexValidation.InvalidLength || validation == HexValidation.InvalidCharacter,
        supportingText = when (validation) {
            HexValidation.InvalidLength -> {
                { Text(if (includeAlpha) "Use 3, 6 or 8 hex digits" else "Use 3 or 6 hex digits") }
            }
            HexValidation.InvalidCharacter -> {
                { Text("Only 0-9 and A-F") }
            }
            else -> null
        },
        singleLine = true,
        textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace),
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Characters,
            keyboardType = KeyboardType.Ascii,
            imeAction = ImeAction.Done,
        ),
        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
    )
}

/** True when [text] already describes [argb]: all channels for 8 digits, the RGB channels otherwise. */
private fun textMatches(text: String, argb: Int): Boolean {
    val parsed = HexColor.parse(text) ?: return false
    return if (HexColor.hasAlpha(text)) parsed == argb else (parsed and 0xFFFFFF) == (argb and 0xFFFFFF)
}
