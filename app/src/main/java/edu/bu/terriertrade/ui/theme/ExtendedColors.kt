package edu.bu.terriertrade.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Colors Material 3 has no slot for, used by the prototype:
 * - success: pinned Deal card, "BU verified", "Live" badges, Mark as traded
 * - warning: offer waiting on you, offline banner
 * - stickyNote: "Students are looking for" wanted notes only
 */
@Immutable
data class TTExtendedColors(
    val success: Color,
    val successContainer: Color,
    val onSuccessContainer: Color,
    val warning: Color,
    val warningContainer: Color,
    val onWarningContainer: Color,
    val stickyNote: Color,
    val onStickyNote: Color,
)

internal val LightExtendedColors = TTExtendedColors(
    success = Success,
    successContainer = SuccessContainer,
    onSuccessContainer = OnSuccessContainer,
    warning = Warning,
    warningContainer = WarningContainer,
    onWarningContainer = OnWarningContainer,
    stickyNote = StickyNote,
    onStickyNote = Ink,
)

internal val DarkExtendedColors = TTExtendedColors(
    success = SuccessOnDark,
    successContainer = SuccessContainerDark,
    onSuccessContainer = SuccessOnDark,
    warning = WarningOnDark,
    warningContainer = WarningContainerDark,
    onWarningContainer = WarningOnDark,
    stickyNote = StickyNoteDark,
    onStickyNote = NightText,
)

internal val LocalTTExtendedColors = staticCompositionLocalOf { LightExtendedColors }

/** Read extra colors like MaterialTheme: `TTTheme.colors.successContainer`. */
object TTTheme {
    val colors: TTExtendedColors
        @Composable
        @ReadOnlyComposable
        get() = LocalTTExtendedColors.current
}