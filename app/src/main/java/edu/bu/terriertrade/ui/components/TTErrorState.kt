package edu.bu.terriertrade.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.PreviewLightDark

@Composable
fun TTErrorState(
    modifier: Modifier = Modifier,
    title: String = "Something went wrong",
    message: String? = "Check your connection and try again.",
    icon: ImageVector? = null,
    retryText: String = "Try again",
    onRetry: (() -> Unit)? = null,
) {
    TTMessageState(
        title = title,
        modifier = modifier,
        message = message,
        icon = icon,
        iconTint = MaterialTheme.colorScheme.error,
        actionText = retryText,
        onAction = onRetry,
    )
}

@PreviewLightDark
@Composable
private fun TTErrorStatePreview() {
    TTPreviewColumn {
        TTErrorState(onRetry = {})
    }
}