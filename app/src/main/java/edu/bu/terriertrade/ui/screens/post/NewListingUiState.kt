package edu.bu.terriertrade.ui.screens.post

import edu.bu.terriertrade.data.model.Category
import edu.bu.terriertrade.data.model.Condition
import edu.bu.terriertrade.data.model.ListingType

/**
 * Everything the New listing screen draws. Errors are null when the field is fine.
 * photos holds picked image Uris as strings; the first one is the cover.
 */
data class NewListingUiState(
    val type: ListingType = ListingType.ITEM,
    val photos: List<String> = emptyList(),
    val title: String = "",
    val price: String = "",
    val description: String = "",
    val category: Category? = null,
    val condition: Condition? = null,
    val meetSpot: String? = null,
    val titleError: String? = null,
    val priceError: String? = null,
    val categoryError: String? = null,
    val conditionError: String? = null,
    val meetSpotError: String? = null,
    val submitError: String? = null,
    /** Shown at the top after a successful post, until the student starts a new one. */
    val postedMessage: String? = null,
    val isPosting: Boolean = false,
) {
    val canAddPhoto: Boolean get() = photos.size < NewListingViewModel.MAX_PHOTOS

    /** This PR builds the Item form. Service, Ticket and Looking for come with #18. */
    val isFormReady: Boolean get() = type == ListingType.ITEM
}
