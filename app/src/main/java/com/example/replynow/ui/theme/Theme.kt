package com.example.replynow.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = OnPrimaryDark,
    primaryContainer = PrimaryContainerDark,
    onPrimaryContainer = OnPrimaryContainerDark,
    secondary = SecondaryDark,
    surface = SurfaceDark,
    surfaceContainerLow = SurfaceContainerLowDark,
    onSurface = OnSurfaceDark,
    error = ErrorDark,
    errorContainer = ErrorContainerDark,
    background = BackgroundDark,
    onBackground = OnBackgroundDark
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryLight,
    onPrimary = OnPrimaryLight,
    primaryContainer = PrimaryContainerLight,
    secondary = SecondaryLight,
    surface = SurfaceLight,
    surfaceContainerLow = SurfaceContainerLowLight,
    onSurface = OnSurfaceLight,
    error = ErrorLight,
    background = Color.White,
    onBackground = OnSurfaceLight
)

@Composable
fun ReplyNowTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}