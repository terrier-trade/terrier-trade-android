package edu.bu.terriertrade.ui.screens.listingdetail

import edu.bu.terriertrade.data.fake.FakeAuthRepository
import edu.bu.terriertrade.data.fake.FakeChatRepository
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
import org.junit.Assert.assertNotNull
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

    // The fake auth is signed in as "alex", so this one is the signed-in student's own listing
    private val myLamp = Listing(
        id = "lamp-1", ownerId = "alex", type = ListingType.ITEM, title = "Desk lamp", price = 12,
    )
    private val listingRepo = InMemoryListingRepository(listOf(book, myLamp))
    private val auth = FakeAuthRepository()
    private val userRepo = InMemoryUserRepository(listOf(User(id = "maya", name = "Maya Rao", verified = true)))
    private val savedRepo = InMemorySavedRepository()
    private val chatRepo = FakeChatRepository(auth)
    private fun viewModel() = ListingDetailViewModel(listingRepo, userRepo, savedRepo, chatRepo)

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

    /** Request saves the deal and tells the screen which chat to open (the sample data already has a chat with Maya). */
    @Test
    fun request_signalsTheChatToOpen() {
        val vm = viewModel().apply { load("book-1") }

        vm.onRequest()

        assertEquals("c_maya", vm.uiState.openChatId)
        assertFalse(vm.uiState.isRequesting)
        assertNull(vm.uiState.requestError)
    }

    /** After the screen opens the chat, the signal is cleared so it can't fire twice. */
    @Test
    fun chatOpened_clearsTheSignal() {
        val vm = viewModel().apply { load("book-1") }
        vm.onRequest()

        vm.onChatOpened()

        assertNull(vm.uiState.openChatId)
    }

    /** Requesting your own listing shows the reason and opens nothing. */
    @Test
    fun requestingOwnListing_showsError() {
        val vm = viewModel().apply { load("lamp-1") }

        vm.onRequest()

        assertNotNull(vm.uiState.requestError)
        assertNull(vm.uiState.openChatId)
    }
}
