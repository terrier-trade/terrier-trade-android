package edu.bu.terriertrade.ui.screens.listing

import edu.bu.terriertrade.data.model.Category
import edu.bu.terriertrade.data.model.Condition
import edu.bu.terriertrade.data.model.Listing
import edu.bu.terriertrade.data.model.ListingStatus
import edu.bu.terriertrade.data.model.ListingType
import edu.bu.terriertrade.data.model.User
import edu.bu.terriertrade.ui.screens.browse.InMemoryListingRepository
import edu.bu.terriertrade.ui.screens.browse.InMemorySavedRepository
import edu.bu.terriertrade.ui.screens.browse.InMemoryUserRepository
import edu.bu.terriertrade.util.MainDispatcherRule
import kotlinx.coroutines.flow.update
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ListingDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val book = Listing(
        id = "book-1", ownerId = "maya", type = ListingType.ITEM, title = "Calculus textbook", price = 35,
        category = Category.BOOKS, condition = Condition.LIKE_NEW, meetSpot = "Mugar Library",
    )
    private val listingRepo = InMemoryListingRepository(listOf(book))
    private val userRepo = InMemoryUserRepository(listOf(User(id = "maya", name = "Maya Rao", verified = true)))
    private val savedRepo = InMemorySavedRepository()

    private fun viewModel() = ListingDetailViewModel(listingRepo, userRepo, savedRepo)

    @Test
    fun load_knownId_showsListingAndSeller() {
        val vm = viewModel().apply { load("book-1") }
        assertEquals("Calculus textbook", vm.uiState.listing?.title)
        assertEquals("Maya", vm.uiState.listing?.seller?.name)
        assertFalse(vm.uiState.notFound)
    }

    @Test
    fun load_unknownId_showsNotFound() {
        val vm = viewModel().apply { load("nope") }
        assertNull(vm.uiState.listing)
        assertTrue(vm.uiState.notFound)
    }

    @Test
    fun removedListing_showsNotFound() {
        val vm = viewModel().apply { load("book-1") }
        listingRepo.listings.update { all -> all.map { it.copy(status = ListingStatus.REMOVED) } }
        assertTrue(vm.uiState.notFound)
    }

    @Test
    fun favorite_isSavedInRepository() {
        val vm = viewModel().apply { load("book-1") }
        vm.onToggleFavorite()
        assertTrue(vm.uiState.isFavorite)
        assertTrue("book-1" in savedRepo.saved.value)
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
