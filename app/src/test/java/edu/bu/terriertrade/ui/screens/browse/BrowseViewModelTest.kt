package edu.bu.terriertrade.ui.screens.browse

import edu.bu.terriertrade.data.model.Category
import edu.bu.terriertrade.data.model.Condition
import edu.bu.terriertrade.data.model.Listing
import edu.bu.terriertrade.data.model.ListingStatus
import edu.bu.terriertrade.data.model.ListingType
import edu.bu.terriertrade.data.model.PriceUnit
import edu.bu.terriertrade.data.model.User
import edu.bu.terriertrade.ui.model.ListingCategory
import edu.bu.terriertrade.util.MainDispatcherRule
import kotlinx.coroutines.flow.update
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class BrowseViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val now = 1_800_000_000_000L
    private fun hoursAgo(h: Int) = now - h * 3_600_000L

    private val maya = User(id = "maya", name = "Maya Rao", ratingSum = 47, reviewCount = 10, tradeCount = 10, verified = true)

    private val book = Listing(
        id = "1", ownerId = "maya", type = ListingType.ITEM, title = "Calculus textbook", price = 35,
        category = Category.BOOKS, condition = Condition.LIKE_NEW, meetSpot = "Mugar Library", createdAt = hoursAgo(1),
    )
    private val tutoring = Listing(
        id = "2", ownerId = "maya", type = ListingType.SERVICE, title = "CS 330 tutoring", price = 20,
        priceUnit = PriceUnit.PER_HOUR, category = Category.TUTORING, createdAt = hoursAgo(2),
    )
    private val tickets = Listing(
        id = "3", ownerId = "leo", type = ListingType.TICKET, title = "Hockey vs BC", price = 25,
        category = Category.TICKETS, quantity = 2, createdAt = hoursAgo(3),
    )
    private val chair = Listing(
        id = "4", ownerId = "leo", type = ListingType.ITEM, title = "Desk chair", price = 40,
        category = Category.FURNITURE, condition = Condition.GOOD, createdAt = hoursAgo(4),
    )
    private val wantedTutor = Listing(
        id = "w1", ownerId = "maya", type = ListingType.WANTED, title = "Math tutor for MA 124", price = 25,
        priceUnit = PriceUnit.PER_HOUR, category = Category.TUTORING, createdAt = hoursAgo(5),
    )

    private val listingRepo = InMemoryListingRepository(listOf(book, tutoring, tickets, chair, wantedTutor))
    private val userRepo = InMemoryUserRepository(listOf(maya))
    private val savedRepo = InMemorySavedRepository()

    private fun viewModel() = BrowseViewModel(listingRepo, userRepo, savedRepo, now = { now })

    private fun BrowseViewModel.titles() = uiState.listings.map { it.title }

    @Test
    fun loadsListingsNewestFirst_andWantedSeparately() {
        val state = viewModel().uiState
        assertFalse(state.isLoading)
        assertEquals(listOf("Calculus textbook", "CS 330 tutoring", "Hockey vs BC", "Desk chair"), state.listings.map { it.title })
        assertEquals(listOf("Math tutor for MA 124"), state.wanted.map { it.title })
        assertTrue(state.showWantedStrip)
    }

    @Test
    fun sellerComesFromUserRepository_unknownSellerShowsAsNew() {
        val state = viewModel().uiState
        assertEquals("Maya", state.listings.first { it.id == "1" }.seller.name)
        assertEquals("4.7", state.listings.first { it.id == "1" }.seller.rating)
        assertEquals("New", state.listings.first { it.id == "3" }.seller.rating)
    }

    @Test
    fun newPost_showsUpStraightAway() {
        val vm = viewModel()
        listingRepo.listings.update {
            it + Listing(id = "5", ownerId = "maya", type = ListingType.ITEM, title = "Mini fridge", price = 55, createdAt = now)
        }
        assertEquals("Mini fridge", vm.titles().first())
    }

    @Test
    fun soldListings_areHidden() {
        val vm = viewModel()
        listingRepo.listings.update { all -> all.map { if (it.id == "4") it.copy(status = ListingStatus.SOLD) else it } }
        assertFalse("Desk chair" in vm.titles())
    }

    @Test
    fun itemsChip_showsOnlyItems() {
        val vm = viewModel().apply { onFilterSelect(BrowseFilter.ITEMS) }
        assertEquals(listOf("Calculus textbook", "Desk chair"), vm.titles())
    }

    @Test
    fun servicesChip_showsOnlyServices() {
        val vm = viewModel().apply { onFilterSelect(BrowseFilter.SERVICES) }
        assertEquals(listOf("CS 330 tutoring"), vm.titles())
    }

    @Test
    fun lookingForChip_showsWantedListInsteadOfListings() {
        val vm = viewModel().apply { onFilterSelect(BrowseFilter.LOOKING_FOR) }
        assertTrue(vm.uiState.listings.isEmpty())
        assertTrue(vm.uiState.showWantedList)
        assertEquals(1, vm.uiState.wanted.size)
    }

    @Test
    fun category_filtersAndTappingAgainClears() {
        val vm = viewModel()
        vm.onCategorySelect(ListingCategory.BOOKS)
        assertEquals(listOf("Calculus textbook"), vm.titles())
        vm.onCategorySelect(ListingCategory.BOOKS)
        assertNull(vm.uiState.category)
        assertEquals(4, vm.uiState.listings.size)
    }

    @Test
    fun allChip_clearsCategory() {
        val vm = viewModel().apply {
            onCategorySelect(ListingCategory.BOOKS)
            onFilterSelect(BrowseFilter.ALL)
        }
        assertNull(vm.uiState.category)
    }

    @Test
    fun search_ignoresCaseAndSpaces_andHidesStrip() {
        val vm = viewModel().apply { onQueryChange("  CALCULUS ") }
        assertEquals(listOf("Calculus textbook"), vm.titles())
        assertFalse(vm.uiState.showWantedStrip)
    }

    @Test
    fun clearFilters_resetsEverything() {
        val vm = viewModel().apply {
            onQueryChange("chair")
            onFilterSelect(BrowseFilter.ITEMS)
            clearFilters()
        }
        assertFalse(vm.uiState.hasActiveFilters)
        assertEquals(4, vm.uiState.listings.size)
    }

    @Test
    fun favorite_isSavedInRepository() {
        val vm = viewModel()
        vm.onToggleFavorite("2")
        assertTrue("2" in savedRepo.saved.value)
        assertTrue("2" in vm.uiState.favoriteIds)
        vm.onToggleFavorite("2")
        assertFalse("2" in vm.uiState.favoriteIds)
    }

    @Test
    fun request_marksListingRequested() {
        val vm = viewModel().apply { onRequest("1") }
        assertTrue("1" in vm.uiState.requestedIds)
    }
}
