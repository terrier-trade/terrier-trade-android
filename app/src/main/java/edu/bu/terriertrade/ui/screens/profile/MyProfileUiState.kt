package edu.bu.terriertrade.ui.screens.profile

import edu.bu.terriertrade.ui.model.ListingUi
import edu.bu.terriertrade.ui.model.MyListingUi
import edu.bu.terriertrade.ui.model.ProfileHeaderUi
import edu.bu.terriertrade.ui.model.ReviewUi

/** The three lists on your profile. */
enum class ProfileTab(val label: String) {
    LISTINGS("My listings"),
    SAVED("Saved"),
    REVIEWS("Reviews"),
}

/** Everything the Profile tab draws. */
data class MyProfileUiState(
    val isLoading: Boolean = true,
    val isSignedOut: Boolean = false,
    val header: ProfileHeaderUi? = null,
    val myListings: List<MyListingUi> = emptyList(),
    val saved: List<ListingUi> = emptyList(),
    val reviews: List<ReviewUi> = emptyList(),
    val tab: ProfileTab = ProfileTab.LISTINGS,
    /** Set while the "Remove listing?" dialog is open. */
    val confirmRemove: MyListingUi? = null,
    /** A short note after an action, like "Marked as sold". */
    val message: String? = null,
    val errorMessage: String? = null,
) {
    val liveCount: Int get() = myListings.count { !it.isSold }

    fun countFor(tab: ProfileTab): Int = when (tab) {
        ProfileTab.LISTINGS -> myListings.size
        ProfileTab.SAVED -> saved.size
        ProfileTab.REVIEWS -> reviews.size
    }
}
