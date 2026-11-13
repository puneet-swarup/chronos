package com.chronos.app.ui.theme

import androidx.compose.ui.graphics.Color

// Design tokens: single source of truth for color. UI uses these via
// MaterialTheme instead of hard-coded colors, so the app can be re-themed
// without touching screens.
object ChronosColors {
 val Primary = Color(0xFF4F6BFF)
 val OnPrimary = Color(0xFFFFFFFF)
 val Secondary = Color(0xFF6C5CE7)
 val OnSecondary = Color(0xFFFFFFFF)
 val Tertiary = Color(0xFF00B894)

 val Background = Color(0xFF0F1115)
 val OnBackground = Color(0xFFECEDEE)
 val Surface = Color(0xFF171A21)
 val OnSurface = Color(0xFFECEDEE)
 val SurfaceVariant = Color(0xFF232732)
 val OnSurfaceVariant = Color(0xFFB4B8C5)

 val Error = Color(0xFFE5484D)
 val OnError = Color(0xFFFFFFFF)
 val Outline = Color(0xFF3A3F4B)
}
