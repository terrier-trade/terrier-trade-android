package edu.bu.terriertrade.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.PreviewLightDark

@Composable
fun TTEmptyState(
    title: String,
    modifier: Modifier = Modifier,
    message: String? = null,
    icon: ImageVector? = null,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
) {
    TTMessageState(
        title = title,
        modifier = modifier,
        message = message,
        icon = icon,
        actionText = actionText,
        onAction = onAction,
    )
}

@PreviewLightDark
@Composable
private fun TTEmptyStatePreview() {
    TTPreviewColumn {
        TTEmptyState(
            title = "No listings yet",
            message = "Be the first to post something for sale.",
            actionText = "Post a listing",
            onAction = {},
        )
    }
}