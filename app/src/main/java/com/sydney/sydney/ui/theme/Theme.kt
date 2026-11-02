package com.sydney.sydney.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/*
 * How the three colour sets map onto Material roles:
 *   background, surface            60  canvas
 *   primary (buttons), onSurface,
 *   surfaceVariant (cards),
 *   outline*                       30  ink and surfaces
 *   tertiary*                      10  accent
 * Reach for tertiary only when something should stand out. If everything is accent, nothing is.
 */
private val LightColors = lightColorScheme(
    primary = Ink,
    onPrimary = Bone,
    primaryContainer = Wash,
    onPrimaryContainer = Ink,
    secondary = InkMuted,
    onSecondary = Paper,
    secondaryContainer = Wash,
    onSecondaryContainer = Ink,
    tertiary = Emerald,
    onTertiary = Paper,
    tertiaryContainer = EmeraldTint,
    onTertiaryContainer = Emerald,
    background = Bone,
    onBackground = Ink,
    surface = Bone,
    onSurface = Ink,
    surfaceVariant = Paper,
    onSurfaceVariant = InkMuted,
    surfaceTint = Color.Transparent,
    outline = InputLine,
    outlineVariant = Hairline,
    error = ErrorLight,
)

private val DarkColors = darkColorScheme(
    primary = StoneText,
    onPrimary = Night,
    primaryContainer = StoneWash,
    onPrimaryContainer = StoneText,
    secondary = StoneMuted,
    onSecondary = Night,
    secondaryContainer = StoneWash,
    onSecondaryContainer = StoneText,
    tertiary = Mint,
    onTertiary = Night,
    tertiaryContainer = MintTint,
    onTertiaryContainer = Mint,
    background = Night,
    onBackground = StoneText,
    surface = Night,
    onSurface = StoneText,
    surfaceVariant = Stone,
    onSurfaceVariant = StoneMuted,
    surfaceTint = Color.Transparent,
    outline = StoneInput,
    outlineVariant = StoneHairline,
    error = ErrorDark,
)

/** Crisp corners: 8 for controls, 12 for cards, 16 for sheets and dialogs. Nothing is a pill. */
private val ColFindShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(16.dp),
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
        shapes = ColFindShapes,
        content = content,
    )
}
