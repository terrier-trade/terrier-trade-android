package edu.bu.terriertrade.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Blush box with a label, standing in for a listing photo.
 * TODO(photos): show the real image here once listings have photo URLs.
 */
@Composable
fun TTPhotoPlaceholder(
    label: String,
    modifier: Modifier = Modifier,
    height: Dp = 96.dp,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}

@PreviewLightDark
@Composable
private fun TTPhotoPlaceholderPreview() {
    TTPreviewColumn {
        TTPhotoPlaceholder(label = "Textbooks")
    }
}