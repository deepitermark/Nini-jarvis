package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val NovaDarkColorScheme = darkColorScheme(
    primary = AuraCyan,
    onPrimary = NovaDarkBackground,
    primaryContainer = NovaSurfaceCard,
    onPrimaryContainer = TextPrimary,
    secondary = AuraPurple,
    onSecondary = TextPrimary,
    secondaryContainer = NovaSurfaceCard,
    onSecondaryContainer = AuraPurple,
    tertiary = AuraMagenta,
    onTertiary = TextPrimary,
    background = NovaDarkBackground,
    onBackground = TextPrimary,
    surface = NovaSurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = NovaSurfaceCard,
    onSurfaceVariant = TextSecondary,
    outline = NovaSurfaceCardStroke
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = NovaDarkBackground.toArgb()
                window.navigationBarColor = NovaDarkBackground.toArgb()
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = false
                controller.isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = NovaDarkColorScheme,
        typography = Typography,
        content = content
    )
}
