package com.chronos.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// The app-wide theme wrapper. Screens read MaterialTheme only, so swapping this
// composable or the token objects re-themes everything at once.

private val DarkScheme = darkColorScheme(
    primary = ChronosColors.Primary,
    onPrimary = ChronosColors.OnPrimary,
    primaryContainer = ChronosColors.PrimaryContainer,
    onPrimaryContainer = ChronosColors.OnPrimaryContainer,
    secondary = ChronosColors.Secondary,
    onSecondary = ChronosColors.OnSecondary,
    tertiary = ChronosColors.Tertiary,
    onTertiary = ChronosColors.OnTertiary,
    background = ChronosColors.Background,
    onBackground = ChronosColors.OnBackground,
    surface = ChronosColors.Surface,
    onSurface = ChronosColors.OnSurface,
    surfaceVariant = ChronosColors.SurfaceVariant,
    onSurfaceVariant = ChronosColors.OnSurfaceVariant,
    error = ChronosColors.Error,
    onError = ChronosColors.OnError,
    errorContainer = ChronosColors.ErrorContainer,
    onErrorContainer = ChronosColors.OnErrorContainer,
    outline = ChronosColors.Outline,
)

private val LightScheme = lightColorScheme(
    primary = ChronosLightColors.Primary,
    onPrimary = ChronosLightColors.OnPrimary,
    primaryContainer = ChronosLightColors.PrimaryContainer,
    onPrimaryContainer = ChronosLightColors.OnPrimaryContainer,
    secondary = ChronosLightColors.Secondary,
    onSecondary = ChronosLightColors.OnSecondary,
    tertiary = ChronosLightColors.Tertiary,
    onTertiary = ChronosLightColors.OnTertiary,
    background = ChronosLightColors.Background,
    onBackground = ChronosLightColors.OnBackground,
    surface = ChronosLightColors.Surface,
    onSurface = ChronosLightColors.OnSurface,
    surfaceVariant = ChronosLightColors.SurfaceVariant,
    onSurfaceVariant = ChronosLightColors.OnSurfaceVariant,
    error = ChronosLightColors.Error,
    onError = ChronosLightColors.OnError,
    errorContainer = ChronosLightColors.ErrorContainer,
    onErrorContainer = ChronosLightColors.OnErrorContainer,
    outline = ChronosLightColors.Outline,
)

@Composable
fun ChronosTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkScheme else LightScheme,
        typography = ChronosType.typography,
        content = content,
    )
}
