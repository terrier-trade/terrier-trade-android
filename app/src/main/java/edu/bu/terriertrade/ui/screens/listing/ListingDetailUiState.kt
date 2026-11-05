package edu.bu.terriertrade.ui.screens.listing

import edu.bu.terriertrade.ui.model.ListingUi

data class ListingDetailUiState(
    val listing: ListingUi? = null,
    val isFavorite: Boolean = false,
    val notFound: Boolean = false,
    val requestSent: Boolean = false,
)