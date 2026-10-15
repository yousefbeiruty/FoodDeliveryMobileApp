package com.example.fooddelivery.ui.theme

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

private val LightColorScheme = lightColorScheme(
    primary = FreshGreenPrimary,
    onPrimary = FreshGreenOnPrimary,
    primaryContainer = FreshGreenPrimaryContainer,
    onPrimaryContainer = FreshGreenOnPrimaryContainer,
    secondary = MintSecondary,
    onSecondary = MintOnSecondary,
    secondaryContainer = MintSecondaryContainer,
    onSecondaryContainer = MintOnSecondaryContainer,
    tertiary = EcoTealTertiary,
    onTertiary = EcoTealOnTertiary,
    tertiaryContainer = EcoTealTertiaryContainer,
    onTertiaryContainer = EcoTealOnTertiaryContainer,
    background = LightBackground,
    onBackground = LightOnSurface,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant
)

private val DarkColorScheme = darkColorScheme(
    primary = FreshGreenPrimaryDark,
    onPrimary = FreshGreenOnPrimaryDark,
    primaryContainer = FreshGreenPrimaryContainerDark,
    onPrimaryContainer = FreshGreenOnPrimaryContainerDark,
    secondary = MintSecondaryDark,
    onSecondary = MintOnSecondaryDark,
    secondaryContainer = DarkSurfaceVariant,
    onSecondaryContainer = DarkOnSurface,
    tertiary = EcoTealTertiary,
    onTertiary = EcoTealOnTertiary,
    background = DarkBackground,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant
)

@Composable
fun FreshBiteTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = FreshBiteTypography,
        content = content
    )
}
