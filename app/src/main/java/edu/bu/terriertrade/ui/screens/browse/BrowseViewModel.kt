package edu.bu.terriertrade.ui.screens.browse

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import edu.bu.terriertrade.ui.model.ListingCategory
import edu.bu.terriertrade.ui.model.ListingType
import edu.bu.terriertrade.ui.model.ListingUi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import edu.bu.terriertrade.ui.model.SampleData
import edu.bu.terriertrade.ui.model.WantedPostUi

/**
 * Holds the Browse feed: search, category, type chip, favorites, and requests.
 * Uses SampleData for now. TODO(data): take listings and wanted posts from the repository.
 */
class BrowseViewModel(
    private val allListings: List<ListingUi> = SampleData.listings,
    private val allWanted: List<WantedPostUi> = SampleData.wanted,
) : ViewModel() {

    var uiState by mutableStateOf(BrowseUiState(listings = allListings, wanted = allWanted))
        private set

    fun onQueryChange(query: String) = update { copy(query = query) }

    /** Tapping the selected category again clears it. */
    fun onCategorySelect(category: ListingCategory) =
        update { copy(category = if (this.category == category) null else category) }

    fun onFilterSelect(filter: BrowseFilter) = update { copy(filter = filter) }

    fun onToggleFavorite(listingId: String) {
        val favorites = uiState.favoriteIds
        uiState = uiState.copy(
            favoriteIds = if (listingId in favorites) favorites - listingId else favorites + listingId,
        )
    }

    /** TODO(data): send the request through the repository. */
    fun onRequest(listingId: String) {
        uiState = uiState.copy(requestedIds = uiState.requestedIds + listingId)
    }

    private fun update(change: BrowseUiState.() -> BrowseUiState) {
        val next = uiState.change()
        uiState = next.copy(
            listings = filterListings(allListings, next),
            wanted = filterWanted(allWanted, next.query),
        )
    }

    companion object {
        fun filterListings(listings: List<ListingUi>, state: BrowseUiState): List<ListingUi> {
            if (state.filter == BrowseFilter.LOOKING_FOR) return emptyList()
            val text = state.query.trim()
            return listings.filter { listing ->
                matchesType(listing.type, state.filter) &&
                        (state.category == null || listing.category == state.category) &&
                        (text.isEmpty() || listing.title.contains(text, ignoreCase = true))
            }
        }

        fun filterWanted(wanted: List<WantedPostUi>, query: String): List<WantedPostUi> {
            val text = query.trim()
            return if (text.isEmpty()) wanted else wanted.filter { it.title.contains(text, ignoreCase = true) }
        }

        private fun matchesType(type: ListingType, filter: BrowseFilter): Boolean = when (filter) {
            BrowseFilter.ALL -> true
            BrowseFilter.ITEMS -> type == ListingType.ITEM
            BrowseFilter.SERVICES -> type == ListingType.SERVICE
            BrowseFilter.TICKETS -> type == ListingType.TICKET
            BrowseFilter.LOOKING_FOR -> false
        }
    }
}