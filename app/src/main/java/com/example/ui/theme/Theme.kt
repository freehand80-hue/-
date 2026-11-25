package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val OutdoorHighContrastColorScheme = darkColorScheme(
    primary = SignalYellow,
    onPrimary = OnSignalYellow,
    primaryContainer = SignalYellowContainer,
    onPrimaryContainer = SignalYellow,
    secondary = PureWhite,
    onSecondary = DeepNavyCanvas,
    secondaryContainer = DeepNavySurfaceHighest,
    onSecondaryContainer = PureWhite,
    tertiary = SignalGreen,
    onTertiary = DeepNavyCanvas,
    tertiaryContainer = SignalGreenContainer,
    onTertiaryContainer = SignalGreen,
    background = DeepNavyCanvas,
    onBackground = PureWhite,
    surface = DeepNavySurface,
    onSurface = PureWhite,
    surfaceVariant = DeepNavySurfaceHigh,
    onSurfaceVariant = IceWhite,
    surfaceContainerLowest = DeepNavyCanvas,
    surfaceContainerLow = DeepNavySurface,
    surfaceContainer = DeepNavySurface,
    surfaceContainerHigh = DeepNavySurfaceHigh,
    surfaceContainerHighest = DeepNavySurfaceHighest,
    outline = DeepNavyOutline,
    outlineVariant = DeepNavyOutlineBright,
    error = SignalCoral,
    onError = DeepNavyCanvas,
    errorContainer = SignalCoralContainer,
    onErrorContainer = SignalCoral
)

private val AppShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    // Always enforce the outdoor sunlight-readable deep navy + signal yellow + white palette
    MaterialTheme(
        colorScheme = OutdoorHighContrastColorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
