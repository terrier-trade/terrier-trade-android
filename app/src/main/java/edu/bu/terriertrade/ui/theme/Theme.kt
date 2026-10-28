package edu.bu.terriertrade.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Scarlet,
    onPrimary = Color.White,
    primaryContainer = ScarletLight,
    onPrimaryContainer = ScarletDark,
    secondary = ScarletDark,
    onSecondary = Color.White,
    background = Paper,
    onBackground = Ink,
    surface = Card,
    onSurface = Ink,
    surfaceVariant = PaperDim,
    onSurfaceVariant = InkMuted,
    outline = Line,
    outlineVariant = Line,
)

private val DarkColors = darkColorScheme(
    primary = ScarletOnDark,
    onPrimary = Color(0xFF690000),
    primaryContainer = ScarletDark,
    onPrimaryContainer = ScarletLight,
    secondary = ScarletOnDark,
    onSecondary = Color(0xFF690000),
    background = NightBg,
    onBackground = NightText,
    surface = NightSurface,
    onSurface = NightText,
    surfaceVariant = NightSurfaceHigh,
    onSurfaceVariant = NightMuted,
    outline = NightLine,
    outlineVariant = NightLine,
)

/** Dynamic color stays off so the app always looks BU red. */
@Composable
fun TerrierTradeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalTTExtendedColors provides if (darkTheme) DarkExtendedColors else LightExtendedColors,
    ) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColors else LightColors,
            typography = Typography,
            shapes = Shapes,
            content = content
        )
    }
}