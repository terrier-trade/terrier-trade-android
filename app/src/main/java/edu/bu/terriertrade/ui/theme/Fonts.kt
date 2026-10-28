package edu.bu.terriertrade.ui.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import edu.bu.terriertrade.R

/**
 * Fonts are bundled in res/font so they work offline.
 * Barlow Condensed: screen titles, the logo, big prices.
 * Barlow: everything else.
 */
val BarlowCondensed = FontFamily(
    Font(R.font.barlow_condensed_semibold, FontWeight.SemiBold),
    Font(R.font.barlow_condensed_bold, FontWeight.Bold),
)

val Barlow = FontFamily(
    Font(R.font.barlow, FontWeight.Normal),
    Font(R.font.barlow_medium, FontWeight.Medium),
    Font(R.font.barlow_semibold, FontWeight.SemiBold),
    Font(R.font.barlow_bold, FontWeight.Bold),
)