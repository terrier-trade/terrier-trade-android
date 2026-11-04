package edu.bu.terriertrade.ui.screens.profile

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import edu.bu.terriertrade.ui.screens.listing.ListingDetailRoute

/**
 * The Profile tab. Navigation already calls this, so the real profile shows without touching navigation.
 * Tapping a listing opens its detail right here for now.
 * TODO(nav): navigation can call MyProfileRoute directly and open Routes.listingDetail(id),
 * then this file can be deleted.
 */
@Composable
fun ProfileScreen(modifier: Modifier = Modifier) {
    var openListingId by rememberSaveable { mutableStateOf<String?>(null) }
    val openId = openListingId

    if (openId == null) {
        MyProfileRoute(onListingClick = { openListingId = it }, modifier = modifier)
    } else {
        BackHandler { openListingId = null }
        ListingDetailRoute(listingId = openId, onBack = { openListingId = null })
    }
}
