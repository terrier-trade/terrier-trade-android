package edu.bu.terriertrade.ui.screens.listingdetail

import edu.bu.terriertrade.ui.model.ListingUi

data class ListingDetailUiState(
    val listing: ListingUi? = null,
    val isFavorite: Boolean = false,
    val notFound: Boolean = false,
    /** True while the request is being saved: the button shows a spinner and ignores taps. */
    val isRequesting: Boolean = false,
    /** Why the request failed, shown above the button. Null when there's nothing to show. */
    val requestError: String? = null,
    /**
     * One-time signal to open this chat. The screen opens it, then calls onChatOpened()
     * to clear it, so it doesn't open a second time after rotation.
     */
    val openChatId: String? = null,
)