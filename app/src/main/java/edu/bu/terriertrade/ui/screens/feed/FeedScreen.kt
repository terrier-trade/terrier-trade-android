package edu.bu.terriertrade.ui.screens.feed

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import edu.bu.terriertrade.ui.components.TTEmptyState

@Composable
fun FeedScreen(modifier: Modifier = Modifier) {
    TTEmptyState(
        title = "Listings coming soon",
        message = "Items posted by BU students will show up here.",
        modifier = modifier,
    )
}