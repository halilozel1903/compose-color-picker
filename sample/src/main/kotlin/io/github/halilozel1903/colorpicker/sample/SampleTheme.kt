package io.github.halilozel1903.colorpicker.sample

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** A warm violet theme for the Theme Builder app itself (the theme being built is shown in the preview). */
@Composable
fun SampleTheme(dark: Boolean, content: @Composable () -> Unit) {
    val colors = if (dark) {
        darkColorScheme(
            primary = Color(0xFFCFBDFE),
            onPrimary = Color(0xFF36265D),
            primaryContainer = Color(0xFF4D3D75),
            onPrimaryContainer = Color(0xFFE9DDFF),
            secondary = Color(0xFFCBC3DC),
            secondaryContainer = Color(0xFF4A4458),
            onSecondaryContainer = Color(0xFFE8DEF8),
            tertiary = Color(0xFFF0B8C8),
            background = Color(0xFF141218),
            onBackground = Color(0xFFE6E0E9),
            surface = Color(0xFF141218),
            onSurface = Color(0xFFE6E0E9),
            surfaceVariant = Color(0xFF49454E),
            onSurfaceVariant = Color(0xFFCAC4CF),
            surfaceContainerLowest = Color(0xFF0F0D13),
            surfaceContainerLow = Color(0xFF1D1B20),
            surfaceContainer = Color(0xFF211F26),
            surfaceContainerHigh = Color(0xFF2B2930),
            surfaceContainerHighest = Color(0xFF36343B),
            outline = Color(0xFF948F99),
            outlineVariant = Color(0xFF49454E),
        )
    } else {
        lightColorScheme(
            primary = Color(0xFF65558F),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFE9DDFF),
            onPrimaryContainer = Color(0xFF201047),
            secondary = Color(0xFF625B71),
            secondaryContainer = Color(0xFFE8DEF8),
            onSecondaryContainer = Color(0xFF1E192B),
            tertiary = Color(0xFF7E5260),
            background = Color(0xFFFDF7FF),
            onBackground = Color(0xFF1D1B20),
            surface = Color(0xFFFDF7FF),
            onSurface = Color(0xFF1D1B20),
            surfaceVariant = Color(0xFFE7E0EB),
            onSurfaceVariant = Color(0xFF49454E),
            surfaceContainerLowest = Color(0xFFFFFFFF),
            surfaceContainerLow = Color(0xFFF7F2FA),
            surfaceContainer = Color(0xFFF2ECF4),
            surfaceContainerHigh = Color(0xFFECE6EE),
            surfaceContainerHighest = Color(0xFFE6E0E9),
            outline = Color(0xFF7A757F),
            outlineVariant = Color(0xFFCAC4CF),
        )
    }
    MaterialTheme(colorScheme = colors, content = content)
}
