package io.github.halilozel1903.colorpicker.sample

import io.github.halilozel1903.colorpicker.ColorPickerTab

/** Screenshot scenes, picked with the `scene` intent extra. */
enum class Scene(val key: String, val role: ColorRole?, val tab: ColorPickerTab) {
    Wheel("wheel", ColorRole.Brand, ColorPickerTab.Wheel),
    Sliders("sliders", ColorRole.Accent, ColorPickerTab.Sliders),
    Swatches("swatches", ColorRole.Background, ColorPickerTab.Swatches),
    Tablet("tablet", null, ColorPickerTab.Wheel),
    ;

    companion object {
        fun from(key: String?): Scene? = entries.firstOrNull { it.key == key }
    }
}
