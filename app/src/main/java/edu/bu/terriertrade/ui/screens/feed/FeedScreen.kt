package edu.bu.terriertrade.ui.screens.feed

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import edu.bu.terriertrade.ui.screens.browse.BrowseRoute
import edu.bu.terriertrade.ui.screens.common.TabDetail
import edu.bu.terriertrade.ui.screens.common.TabDetailHost

/**
 * The Browse tab. Navigation already calls this, so the real feed shows without touching navigation.
 * Listings and sellers' profiles open on top of it (see TabDetailHost).
 * TODO(nav): navigation can call BrowseRoute directly, then this file can be deleted.
 */
@Composable
fun FeedScreen(modifier: Modifier = Modifier) {
    TabDetailHost { open ->
        // The app frame already draws the red header on this tab, so Browse leaves it out.
        BrowseRoute(
            onListingClick = { open(TabDetail.Listing(it)) },
            showAppHeader = false,
            modifier = modifier,
        )
    }
}
