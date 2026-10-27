package com.decibeltracker.app.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val InstrumentColorScheme = darkColorScheme(
    primary = BrandTurquoise,
    onPrimary = CharcoalBg,
    primaryContainer = SlateSurfaceElevated,
    onPrimaryContainer = SoftWhiteText,
    secondary = BrandTurquoise,
    onSecondary = CharcoalBg,
    surface = SlateSurface,
    onSurface = SoftWhiteText,
    background = CharcoalBg,
    onBackground = SoftWhiteText,
    error = LevelRed,
    onError = SoftWhiteText,
    outline = MutedSlateBorder
)

@Composable
fun DecibelTrackerTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            window?.let {
                WindowCompat.setDecorFitsSystemWindows(it, false)
                WindowCompat.getInsetsController(it, view).isAppearanceLightStatusBars = false
                WindowCompat.getInsetsController(it, view).isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = InstrumentColorScheme,
        typography = Typography,
        content = content
    )
}
