package edu.bu.terriertrade.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** A row of 5 stars with [stars] filled in. Read out as "4 out of 5 stars". */
@Composable
fun TTStarRating(
    stars: Int,
    modifier: Modifier = Modifier,
    size: Dp = 16.dp,
) {
    val filled = stars.coerceIn(0, 5)
    Row(modifier = modifier.semantics { contentDescription = "$filled out of 5 stars" }) {
        repeat(5) { index ->
            Icon(
                imageVector = if (index < filled) Icons.Filled.Star else Icons.Outlined.StarOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(size),
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun TTStarRatingPreview() {
    TTPreviewColumn {
        TTStarRating(stars = 4)
    }
}
