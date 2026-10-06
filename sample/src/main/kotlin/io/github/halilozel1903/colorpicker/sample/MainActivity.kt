package io.github.halilozel1903.colorpicker.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier

/**
 * Theme Builder, a fictional app for picking a brand, accent and background color with compose-color-picker and
 * seeing them on a live preview. `scripts/screenshots.sh` starts it with `--es scene <scene>` to open a fixed
 * screen for README screenshots:
 *
 * - `wheel`: the color picker dialog for the brand color, on the Wheel tab
 * - `sliders`: the dialog for the accent color, on the Sliders tab
 * - `swatches`: the dialog for the background color, on the Swatches tab, with recent colors
 * - `tablet`: the two pane builder (picker left, preview right), which wide windows always get
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val scene = Scene.from(intent.getStringExtra(EXTRA_SCENE))
        setContent {
            SampleTheme(dark = isSystemInDarkTheme()) {
                // The Surface makes text default to onBackground, so it stays readable in dark mode.
                Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
                    ThemeBuilderApp(scene = scene)
                }
            }
        }
    }

    companion object {
        const val EXTRA_SCENE = "scene"
    }
}
