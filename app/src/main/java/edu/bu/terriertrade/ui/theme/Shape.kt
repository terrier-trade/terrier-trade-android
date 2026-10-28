package edu.bu.terriertrade.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * small: tags and badges. medium: inputs and list rows. large: cards.
 * extraLarge: bottom sheets and the rounded bottom of red headers.
 * Buttons and chips are pills: use CircleShape.
 */
val Shapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp),
)