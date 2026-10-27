package edu.bu.terriertrade.ui.screens.chats

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import edu.bu.terriertrade.ui.components.TTEmptyState

@Composable
fun ChatsScreen(modifier: Modifier = Modifier) {
    TTEmptyState(
        title = "No chats yet",
        message = "Request an item to start a chat with the seller.",
        modifier = modifier,
    )
}