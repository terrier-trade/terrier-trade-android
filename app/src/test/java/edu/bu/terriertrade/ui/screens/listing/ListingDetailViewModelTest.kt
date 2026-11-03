package edu.bu.terriertrade.ui.screens.listing

import edu.bu.terriertrade.ui.model.ListingCategory
import edu.bu.terriertrade.ui.model.ListingType
import edu.bu.terriertrade.ui.model.ListingUi
import edu.bu.terriertrade.ui.model.SellerUi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ListingDetailViewModelTest {

    private val book = ListingUi(
        id = "book-1", title = "Calculus textbook", priceLabel = "$35", type = ListingType.ITEM,
        category = ListingCategory.BOOKS, meetSpot = "Mugar", postedAgo = "2h", condition = "Like new",
        description = "", seller = SellerUi(name = "Maya", initials = "MR", rating = "4.7", tradeCount = 6),
    )

    private fun viewModel() = ListingDetailViewModel(listings = listOf(book))

    @Test
    fun load_knownId_showsListing() {
        val vm = viewModel().apply { load("book-1") }
        assertEquals(book, vm.uiState.listing)
        assertFalse(vm.uiState.notFound)
    }

    @Test
    fun load_unknownId_showsNotFound() {
        val vm = viewModel().apply { load("nope") }
        assertNull(vm.uiState.listing)
        assertTrue(vm.uiState.notFound)
    }

    @Test
    fun favorite_flips() {
        val vm = viewModel().apply { load("book-1") }
        vm.onToggleFavorite()
        assertTrue(vm.uiState.isFavorite)
        vm.onToggleFavorite()
        assertFalse(vm.uiState.isFavorite)
    }

    @Test
    fun request_marksSent() {
        val vm = viewModel().apply { load("book-1") }
        vm.onRequest()
        assertTrue(vm.uiState.requestSent)
    }
}