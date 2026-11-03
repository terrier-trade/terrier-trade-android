package edu.bu.terriertrade.ui.screens.listing

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import edu.bu.terriertrade.ui.model.ListingUi
import edu.bu.terriertrade.ui.model.SampleData

/**
 * Loads one listing by id and handles Favorite and Request.
 * TODO(data): load from the repository and send the request through it.
 */
class ListingDetailViewModel(
    private val listings: List<ListingUi> = SampleData.listings,
) : ViewModel() {

    var uiState by mutableStateOf(ListingDetailUiState())
        private set

    fun load(listingId: String) {
        val found = listings.firstOrNull { it.id == listingId }
        uiState = uiState.copy(listing = found, notFound = found == null)
    }

    fun onToggleFavorite() {
        uiState = uiState.copy(isFavorite = !uiState.isFavorite)
    }

    fun onRequest() {
        uiState = uiState.copy(requestSent = true)
    }
}