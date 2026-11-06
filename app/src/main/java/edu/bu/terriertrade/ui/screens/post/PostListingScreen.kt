package edu.bu.terriertrade.ui.screens.post

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * The Post tab. Navigation already calls this, so the new form shows up without touching navigation.
 * TODO(nav): navigation can call NewListingRoute directly and open the listing after posting,
 * then this file can be deleted.
 */
@Composable
fun PostListingScreen(modifier: Modifier = Modifier) {
    NewListingRoute(onPosted = {}, modifier = modifier)
}
