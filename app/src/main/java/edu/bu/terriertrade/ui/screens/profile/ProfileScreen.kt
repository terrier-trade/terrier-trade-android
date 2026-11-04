package edu.bu.terriertrade.ui.screens.profile

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import edu.bu.terriertrade.ui.screens.common.TabDetail
import edu.bu.terriertrade.ui.screens.common.TabDetailHost

/**
 * The Profile tab. Navigation already calls this, so the real profile shows without touching navigation.
 * Listings and other students' profiles open on top of it (see TabDetailHost).
 * TODO(nav): navigation can call MyProfileRoute directly, then this file can be deleted.
 */
@Composable
fun ProfileScreen(modifier: Modifier = Modifier) {
    TabDetailHost { open ->
        MyProfileRoute(onListingClick = { open(TabDetail.Listing(it)) }, modifier = modifier)
    }
}
