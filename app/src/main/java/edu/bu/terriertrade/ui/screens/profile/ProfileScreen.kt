package edu.bu.terriertrade.ui.screens.profile

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import edu.bu.terriertrade.ui.components.TTEmptyState

@Composable
fun ProfileScreen(modifier: Modifier = Modifier) {
    TTEmptyState(
        title = "Your profile",
        message = "Your listings and saved items will go here.",
        modifier = modifier,
    )
}