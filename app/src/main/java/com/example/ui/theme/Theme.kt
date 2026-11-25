package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val ObsidianColorScheme = darkColorScheme(
    primary = CrimsonPrimary,
    onPrimary = BoneWhite,
    primaryContainer = CrimsonMuted,
    onPrimaryContainer = BoneWhite,
    secondary = MutedParchment,
    onSecondary = ObsidianBackground,
    secondaryContainer = ElevatedSlate,
    onSecondaryContainer = BoneWhite,
    tertiary = EmeraldPass,
    onTertiary = ObsidianBackground,
    background = ObsidianBackground,
    onBackground = BoneWhite,
    surface = CharcoalSurface,
    onSurface = BoneWhite,
    surfaceVariant = ElevatedSlate,
    onSurfaceVariant = MutedParchment,
    outline = HairlineBorder,
    outlineVariant = HairlineBorder,
    error = CrimsonPrimary,
    onError = BoneWhite
)

private val MissingPageShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

@Composable
fun MissingPageTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ObsidianColorScheme,
        typography = MissingPageTypography,
        shapes = MissingPageShapes,
        content = content
    )
}
