package edu.bu.terriertrade.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Scarlet,
    onPrimary = Color.White,
    primaryContainer = ScarletLight,
    onPrimaryContainer = ScarletDark,
    secondary = Ink,
    onSecondary = Color.White,
    background = Paper,
    onBackground = Ink,
    surface = Paper,
    onSurface = Ink,
    surfaceVariant = PaperDim,
    onSurfaceVariant = InkMuted,
    outline = Line,
)

private val DarkColors = darkColorScheme(
    primary = ScarletOnDark,
    onPrimary = Color(0xFF690000),
    primaryContainer = ScarletDark,
    onPrimaryContainer = ScarletLight,
    secondary = NightText,
    onSecondary = NightBg,
    background = NightBg,
    onBackground = NightText,
    surface = NightSurface,
    onSurface = NightText,
    surfaceVariant = NightSurfaceHigh,
    onSurfaceVariant = NightMuted,
    outline = Color(0xFF534341),
)

@Composable
fun TerrierTradeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
