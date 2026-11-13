package com.chronos.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// The app-wide theme wrapper. Screens never set colors directly; they read
// MaterialTheme. Swapping this composable (or the token objects) re-themes the
// entire app. Supports dark (default) and light schemes.
private val DarkScheme = darkColorScheme(
 primary = ChronosColors.Primary,
 onPrimary = ChronosColors.OnPrimary,
 secondary = ChronosColors.Secondary,
 onSecondary = ChronosColors.OnSecondary,
 tertiary = ChronosColors.Tertiary,
 background = ChronosColors.Background,
 onBackground = ChronosColors.OnBackground,
 surface = ChronosColors.Surface,
 onSurface = ChronosColors.OnSurface,
 surfaceVariant = ChronosColors.SurfaceVariant,
 onSurfaceVariant = ChronosColors.OnSurfaceVariant,
 error = ChronosColors.Error,
 onError = ChronosColors.OnError,
 outline = ChronosColors.Outline,
)

private val LightScheme = lightColorScheme(
 primary = ChronosColors.Primary,
 onPrimary = ChronosColors.OnPrimary,
 secondary = ChronosColors.Secondary,
 onSecondary = ChronosColors.OnSecondary,
 tertiary = ChronosColors.Tertiary,
)

@Composable
fun ChronosTheme(
 darkTheme: Boolean = isSystemInDarkTheme(),
 content: @Composable () -> Unit,
) {
 val scheme = if (darkTheme) DarkScheme else LightScheme
 MaterialTheme(
 colorScheme = scheme,
 typography = ChronosType.typography,
 content = content,
 )
}
