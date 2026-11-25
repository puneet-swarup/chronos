package com.chronos.app.ui.theme

import androidx.compose.ui.graphics.Color

// Design tokens: single source of truth for colour. UI reads these through
// MaterialTheme, never directly, so re-theming never touches a screen.
//
// The palette is taken from the app icon - amber on deep navy - so the launcher,
// the notification and the app itself are visibly the same product.
object ChronosColors {
    // Amber carries the brand; it is used for the active navigation item, the
    // primary action and the arrow in the icon.
    val Primary = Color(0xFFFFC24B)
    val OnPrimary = Color(0xFF241703)
    val PrimaryContainer = Color(0xFF3A2A08)
    val OnPrimaryContainer = Color(0xFFFFE0A3)

    // Steel blue for secondary actions, mint for confirmations.
    val Secondary = Color(0xFF8FB8F0)
    val OnSecondary = Color(0xFF0B1A2E)
    val Tertiary = Color(0xFF6FD3B8)
    val OnTertiary = Color(0xFF04231C)

    val Background = Color(0xFF0B1220)
    val OnBackground = Color(0xFFE9EEF7)
    val Surface = Color(0xFF131C2E)
    val OnSurface = Color(0xFFE9EEF7)
    val SurfaceVariant = Color(0xFF1D2942)
    val OnSurfaceVariant = Color(0xFFA9B6CC)

    val Error = Color(0xFFFF6B6B)
    val OnError = Color(0xFF2A0808)
    val ErrorContainer = Color(0xFF4A1D1D)
    val OnErrorContainer = Color(0xFFFFD9D9)
    val Outline = Color(0xFF2C3A55)
}

// Light scheme is a real scheme, not a half-filled one: the previous version set
// three colours and left the rest on Material's defaults, which is why light mode
// looked unrelated to dark mode.
object ChronosLightColors {
    val Primary = Color(0xFF8A5A00)
    val OnPrimary = Color(0xFFFFFFFF)
    val PrimaryContainer = Color(0xFFFFE3AE)
    val OnPrimaryContainer = Color(0xFF2B1B00)

    val Secondary = Color(0xFF2C5C8F)
    val OnSecondary = Color(0xFFFFFFFF)
    val Tertiary = Color(0xFF1D6B57)
    val OnTertiary = Color(0xFFFFFFFF)

    val Background = Color(0xFFFBF8F3)
    val OnBackground = Color(0xFF14181F)
    val Surface = Color(0xFFFFFFFF)
    val OnSurface = Color(0xFF14181F)
    val SurfaceVariant = Color(0xFFF0ECE3)
    val OnSurfaceVariant = Color(0xFF4A5464)

    val Error = Color(0xFFB3261E)
    val OnError = Color(0xFFFFFFFF)
    val ErrorContainer = Color(0xFFF9DEDC)
    val OnErrorContainer = Color(0xFF410E0B)
    val Outline = Color(0xFFD6D0C4)
}
