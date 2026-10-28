package edu.bu.terriertrade.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import edu.bu.terriertrade.ui.components.TTEmptyState

/** Stand-in for a screen that hasn't been built yet. Replace it in the feature branch. */
@Composable
fun PlaceholderScreen(
    title: String,
    modifier: Modifier = Modifier,
    message: String = "This screen hasn't been built yet.",
    onBack: (() -> Unit)? = null,
) {
    TTEmptyState(
        title = title,
        message = message,
        modifier = modifier,
        actionText = if (onBack != null) "Go back" else null,
        onAction = onBack,
    )
}
