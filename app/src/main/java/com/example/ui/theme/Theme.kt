package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val AuraDarkColorScheme = darkColorScheme(
    primary = AuraCyan,
    onPrimary = AuraDeepSpace,
    primaryContainer = AuraIndigoDeep,
    onPrimaryContainer = AuraTextPrimary,
    secondary = AuraIndigo,
    onSecondary = AuraTextPrimary,
    secondaryContainer = AuraCardGlassHigh,
    onSecondaryContainer = AuraTextPrimary,
    tertiary = AuraViolet,
    onTertiary = AuraTextPrimary,
    background = AuraDeepSpace,
    onBackground = AuraTextPrimary,
    surface = AuraSurfaceDark,
    onSurface = AuraTextPrimary,
    surfaceVariant = AuraCardDark,
    onSurfaceVariant = AuraTextSecondary,
    outline = AuraGlassBorder,
    error = AuraRed,
    onError = AuraTextPrimary
)

private val AuraLightColorScheme = AuraDarkColorScheme // AURA is voice-first futuristic dark interface

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = AuraDarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = AuraDeepSpace.toArgb()
            window.navigationBarColor = AuraDeepSpace.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
