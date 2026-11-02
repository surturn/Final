package com.sydney.sydney.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary = Green80,
    onPrimary = Color(0xFF003822),
    primaryContainer = GreenContainerDark,
    onPrimaryContainer = GreenContainerLight,
    secondary = Slate80,
    onSecondary = Color(0xFF1E352B),
    secondaryContainer = SlateContainerDark,
    onSecondaryContainer = SlateContainerLight,
    tertiary = Amber80,
    background = SurfaceDark,
    surface = SurfaceDark,
)

private val LightColors = lightColorScheme(
    primary = Green40,
    onPrimary = Color.White,
    primaryContainer = GreenContainerLight,
    onPrimaryContainer = Color(0xFF002111),
    secondary = Slate40,
    onSecondary = Color.White,
    secondaryContainer = SlateContainerLight,
    onSecondaryContainer = Color(0xFF082016),
    tertiary = Amber40,
    background = SurfaceLight,
    surface = SurfaceLight,
)

/** A fixed brand palette. Dynamic color is off on purpose so the app looks the same on every phone. */
@Composable
fun ColFindTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography,
        content = content,
    )
}
