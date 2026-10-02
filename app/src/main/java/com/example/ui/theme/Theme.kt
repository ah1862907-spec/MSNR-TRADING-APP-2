package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val TerminalDarkColorScheme = darkColorScheme(
    primary = BullGreen,
    onPrimary = TerminalBg,
    primaryContainer = BullGreenContainer,
    onPrimaryContainer = BullGreenLight,
    secondary = CyanAccent,
    onSecondary = TerminalBg,
    secondaryContainer = CyanContainer,
    onSecondaryContainer = CyanAccent,
    tertiary = GoldAccent,
    onTertiary = TerminalBg,
    tertiaryContainer = GoldContainer,
    onTertiaryContainer = GoldAccent,
    error = BearRed,
    onError = TerminalBg,
    errorContainer = BearRedContainer,
    onErrorContainer = BearRedLight,
    background = TerminalBg,
    onBackground = TextPrimary,
    surface = TerminalSurface,
    onSurface = TextPrimary,
    surfaceVariant = TerminalSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = TerminalBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force Dark Trading Terminal by default
    content: @Composable () -> Unit
) {
    val colorScheme = TerminalDarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = TerminalBg.toArgb()
                window.navigationBarColor = TerminalBg.toArgb()
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = false
                controller.isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
