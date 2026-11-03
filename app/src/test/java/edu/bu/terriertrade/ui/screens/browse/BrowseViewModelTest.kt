package edu.bu.terriertrade.ui.screens.browse

import edu.bu.terriertrade.ui.model.ListingCategory
import edu.bu.terriertrade.ui.model.ListingType
import edu.bu.terriertrade.ui.model.ListingUi
import edu.bu.terriertrade.ui.model.WantedPostUi
import edu.bu.terriertrade.ui.model.SellerUi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BrowseViewModelTest {

    private val seller = SellerUi(name = "Maya", initials = "MR", rating = "4.7", tradeCount = 6)

    private fun listing(id: String, title: String, type: ListingType, category: ListingCategory) = ListingUi(
        id = id, title = title, priceLabel = "$10", type = type, category = category, meetSpot = "Mugar",
        postedAgo = "1h", condition = "Good", description = "", seller = seller,
    )

    private val book = listing("1", "Calculus textbook", ListingType.ITEM, ListingCategory.BOOKS)
    private val tutoring = listing("2", "CS 330 tutoring", ListingType.SERVICE, ListingCategory.TUTORING)
    private val tickets = listing("3", "Hockey vs BC", ListingType.TICKET, ListingCategory.TICKETS)
    private val chair = listing("4", "Desk chair", ListingType.ITEM, ListingCategory.FURNITURE)
    private val wanted = listOf(
        WantedPostUi("w1", "Math tutor for MA 124", "Up to $25/hr", "Maya R.", "8h ago"),
        WantedPostUi("w2", "CS 501 textbook", "Up to $30", "Sam K.", "1d ago"),
    )

    private fun viewModel() = BrowseViewModel(allListings = listOf(book, tutoring, tickets, chair), allWanted = wanted)

    @Test
    fun startsWithEverything() {
        val state = viewModel().uiState
        assertEquals(4, state.listings.size)
        assertTrue(state.showWantedStrip)
    }

    @Test
    fun itemsChip_showsOnlyItems() {
        val vm = viewModel().apply { onFilterSelect(BrowseFilter.ITEMS) }
        assertEquals(listOf(book, chair), vm.uiState.listings)
    }

    @Test
    fun servicesChip_showsOnlyServices() {
        val vm = viewModel().apply { onFilterSelect(BrowseFilter.SERVICES) }
        assertEquals(listOf(tutoring), vm.uiState.listings)
    }

    @Test
    fun lookingForChip_showsWantedListInsteadOfListings() {
        val vm = viewModel().apply { onFilterSelect(BrowseFilter.LOOKING_FOR) }
        assertTrue(vm.uiState.listings.isEmpty())
        assertTrue(vm.uiState.showWantedList)
        assertEquals(2, vm.uiState.wanted.size)
    }

    @Test
    fun category_filtersAndTappingAgainClears() {
        val vm = viewModel()
        vm.onCategorySelect(ListingCategory.BOOKS)
        assertEquals(listOf(book), vm.uiState.listings)
        vm.onCategorySelect(ListingCategory.BOOKS)
        assertNull(vm.uiState.category)
        assertEquals(4, vm.uiState.listings.size)
    }

    @Test
    fun search_ignoresCaseAndSpaces_andHidesStrip() {
        val vm = viewModel().apply { onQueryChange("  CALCULUS ") }
        assertEquals(listOf(book), vm.uiState.listings)
        assertFalse(vm.uiState.showWantedStrip)
    }

    @Test
    fun favorite_togglesOnAndOff() {
        val vm = viewModel()
        vm.onToggleFavorite("2")
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