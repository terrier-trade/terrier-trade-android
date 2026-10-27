package edu.bu.terriertrade.ui.screens.post

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import edu.bu.terriertrade.ui.components.TTEmptyState

@Composable
fun PostListingScreen(modifier: Modifier = Modifier) {
    TTEmptyState(
        title = "Post a listing",
        message = "Photos, title, price and description will go here.",
        modifier = modifier,
    )
}