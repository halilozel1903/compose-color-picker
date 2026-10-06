package io.github.halilozel1903.colorpicker.sample

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

/** The three colors the user is building a theme from. */
enum class ColorRole(val label: String, val hint: String) {
    Brand("Brand", "Top bar, logo and headings"),
    Accent("Accent", "Buttons and highlights"),
    Background("Background", "Screens and cards"),
}

/** A theme in progress: one color per [ColorRole]. */
data class ThemeColors(val brand: Color, val accent: Color, val background: Color) {
    operator fun get(role: ColorRole): Color = when (role) {
        ColorRole.Brand -> brand
        ColorRole.Accent -> accent
        ColorRole.Background -> background
    }

    fun with(role: ColorRole, color: Color): ThemeColors = when (role) {
        ColorRole.Brand -> copy(brand = color)
        ColorRole.Accent -> copy(accent = color)
        ColorRole.Background -> copy(background = color)
    }

    companion object {
        /** Juniper Bakery's starting theme: forest green, amber and cream. */
        val Juniper = ThemeColors(
            brand = Color(0xFF2E5E4E),
            accent = Color(0xFFF2A541),
            background = Color(0xFFFFF8EE),
        )

        val Saver: Saver<ThemeColors, Any> = listSaver<ThemeColors, Int>(
            save = { listOf(it.brand.toArgb(), it.accent.toArgb(), it.background.toArgb()) },
            restore = { ThemeColors(Color(it[0]), Color(it[1]), Color(it[2])) },
        )
    }
}

/** Colors "picked before", so the recent row has something to show from the first launch. */
val StartingRecentColors: List<Color> = listOf(
    Color(0xFFE76F51),
    Color(0xFF2A9D8F),
    Color(0xFF264653),
    Color(0xFFE9C46A),
    Color(0xFF8E7DBE),
    Color(0xFFF4F1DE),
)
