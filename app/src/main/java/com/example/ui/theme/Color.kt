package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val SkyBlue80 = Color(0xFF90CAF9)
val AmberSun80 = Color(0xFFFFD54F)
val Slate80 = Color(0xFFB0BEC5)

val WeatherPrimary = Color(0xFF0288D1)
val WeatherSecondary = Color(0xFF00ACC1)
val WeatherTertiary = Color(0xFFFFB300)

val DarkSkySurface = Color(0xFF0F172A)
val DarkSkySurfaceVariant = Color(0xFF1E293B)
val DarkSkyCard = Color(0x33334155)
val DarkSkyBorder = Color(0x3394A3B8)

// Weather Sky Gradients (Soft, rich sky-blue & high-contrast atmospheric palettes)
val ClearDayGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF0B4FA8), // Deep gentle azure zenith
        Color(0xFF1976D2), // Soft vibrant sky-blue mid
        Color(0xFF2E8AE6)  // Clean sky-blue horizon (high contrast for white text)
    )
)

val ClearNightGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF0A1024), // Midnight azure
        Color(0xFF162447), // Deep indigo-blue
        Color(0xFF1F3A68)  // Evening twilight blue
    )
)

val PartlyCloudyDayGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF0E4585),
        Color(0xFF1E63A6),
        Color(0xFF2F78BD)
    )
)

val PartlyCloudyNightGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF0B1325),
        Color(0xFF182A4A),
        Color(0xFF26406B)
    )
)

val OvercastGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF1D3554),
        Color(0xFF28476E),
        Color(0xFF335882)
    )
)

val RainGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF13263E),
        Color(0xFF1E3A5C),
        Color(0xFF2B4C73)
    )
)

val HeavyRainGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF0D1B2E),
        Color(0xFF182D49),
        Color(0xFF223D61)
    )
)

val SnowGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF1B4572),
        Color(0xFF295F96),
        Color(0xFF3A75AD)
    )
)

val ThunderstormGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF0B1020),
        Color(0xFF18203B),
        Color(0xFF2A265C)
    )
)

val FogGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF224264),
        Color(0xFF2E547C),
        Color(0xFF3A6794)
    )
)
