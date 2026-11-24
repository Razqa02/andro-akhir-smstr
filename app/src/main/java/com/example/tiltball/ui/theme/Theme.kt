package com.example.tiltball.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

/** Skema warna gelap Tilt Ball. */
private val TiltBallColorScheme = darkColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    secondary = Secondary,
    background = Background,
    surface = Surface,
    surfaceVariant = SurfaceVariant,
    onBackground = OnBackground,
    onSurface = OnSurface,
    onSurfaceVariant = OnSurfaceVariant,
    error = Error
)

@Composable
fun TIltBallTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = TiltBallColorScheme,
        typography = TiltBallTypography,
        content = content
    )
}
