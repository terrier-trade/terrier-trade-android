package edu.bu.terriertrade.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Display and headline styles: Barlow Condensed Bold, for screen titles and big prices.
 * The prototype shows titles in capitals: call .uppercase() on the text where you want that.
 * Title, body, and label styles: Barlow.
 */
private val Default = Typography()

private fun TextStyle.condensed() = copy(fontFamily = BarlowCondensed, fontWeight = FontWeight.Bold)
private fun TextStyle.barlow(weight: FontWeight) = copy(fontFamily = Barlow, fontWeight = weight)

val Typography = Typography(
    displayLarge = Default.displayLarge.condensed(),
    displayMedium = Default.displayMedium.condensed(),
    displaySmall = Default.displaySmall.condensed(),
    headlineLarge = Default.headlineLarge.condensed().copy(letterSpacing = 0.5.sp),
    headlineMedium = Default.headlineMedium.condensed().copy(letterSpacing = 0.5.sp),
    headlineSmall = Default.headlineSmall.condensed().copy(letterSpacing = 0.5.sp),

    titleLarge = Default.titleLarge.barlow(FontWeight.Bold),
    titleMedium = Default.titleMedium.barlow(FontWeight.Bold),
    titleSmall = Default.titleSmall.barlow(FontWeight.SemiBold),

    bodyLarge = Default.bodyLarge.barlow(FontWeight.Normal),
    bodyMedium = Default.bodyMedium.barlow(FontWeight.Normal),
    bodySmall = Default.bodySmall.barlow(FontWeight.Normal),

    labelLarge = Default.labelLarge.barlow(FontWeight.Bold),
    labelMedium = Default.labelMedium.barlow(FontWeight.SemiBold),
    labelSmall = Default.labelSmall.barlow(FontWeight.SemiBold).copy(letterSpacing = 0.8.sp),
)