package edu.bu.terriertrade.ui.screens.profile

import edu.bu.terriertrade.data.model.Category
import edu.bu.terriertrade.data.model.Condition
import edu.bu.terriertrade.data.model.Listing
import edu.bu.terriertrade.data.model.ListingStatus
import edu.bu.terriertrade.data.model.ListingType
import edu.bu.terriertrade.data.model.Review
import edu.bu.terriertrade.data.model.ReviewTag
import edu.bu.terriertrade.data.model.User
import edu.bu.terriertrade.ui.screens.auth.TestAuthRepository
import edu.bu.terriertrade.ui.screens.browse.InMemoryListingRepository
import edu.bu.terriertrade.ui.screens.browse.InMemorySavedRepository
import edu.bu.terriertrade.ui.screens.browse.InMemoryUserRepository
import edu.bu.terriertrade.util.MainDispatcherRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class MyProfileViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val now = 1_800_000_000_000L
    private fun hoursAgo(h: Int) = now - h * 3_600_000L

    private val alex = User(
        id = "alex", name = "Alex Morgan", major = "Computer Science", year = "Junior",
        startTerm = "Fall 2024", gradTerm = "May 2027", meetSpot = "GSU", verified = true,
        ratingSum = 58, reviewCount = 12, tradeCount = 12,
    )
    private val maya = User(id = "maya", name = "Maya R.", verified = true)

    private val lamp = Listing(
        id = "m1", ownerId = "alex", type = ListingType.ITEM, title = "Desk lamp", price = 12,
        category = Category.FURNITURE, condition = Condition.LIKE_NEW, createdAt = hoursAgo(3),
    )
    private val lock = Listing(
        id = "m2", ownerId = "alex", type = ListingType.ITEM, title = "Bike lock", price = 15,
        status = ListingStatus.SOLD, createdAt = hoursAgo(1),
    )
    private val mayaBook = Listing(
        id = "l1", ownerId = "maya", type = ListingType.ITEM, title = "Calculus textbook", price = 35,
        category = Category.BOOKS, createdAt = hoursAgo(2),
    )

    private val auth = TestAuthRepository().apply { currentUserId.value = "alex" }
    private val users = InMemoryUserRepository(listOf(alex, maya))
    private val listings = InMemoryListingRepository(listOf(lamp, lock, mayaBook))
    private val saved = InMemorySavedRepository(setOf("l1"))
    private val reviews = InMemoryReviewRepository(
        listOf(
            Review("r1", fromUserId = "maya", toUserId = "alex", chatId = "c1", stars = 5,
                tags = listOf(ReviewTag.ON_TIME), text = "Super friendly.", createdAt = hoursAgo(5)),
            Review("r2", fromUserId = "sam", toUserId = "maya", chatId = "c2", stars = 4, createdAt = hoursAgo(6)),
        ),
    )

    private fun viewModel() = MyProfileViewModel(auth, users, listings, saved, reviews, now = { now })

    @Test
    fun header_showsWhoYouAre() {
        val header = viewModel().uiState.header!!
        assertEquals("Alex Morgan", header.name)
        assertEquals("AM", header.initials)
        assertEquals("Computer Science · Junior", header.subtitle)
        assertEquals("4.8", header.ratingLabel)
        assertEquals(12, header.tradeCount)
        assertEquals("'27", header.classOf)
        assertEquals("Fall 2024 to May 2027", header.buDates)
        assertTrue(header.isVerified)
    }

    @Test
    fun myListings_liveFirst_thenSold() {
        val state = viewModel().uiState
        assertEquals(listOf("Desk lamp", "Bike lock"), state.myListings.map { it.listing.title })
        assertEquals(listOf(false, true), state.myListings.map { it.isSold })
        assertEquals(1, state.liveCount)
    }

    @Test
    fun saved_showsHeartedActiveListingsWithSeller() {
        val state = viewModel().uiState
        assertEquals(listOf("Calculus textbook"), state.saved.map { it.title })
        assertEquals("Maya", state.saved.first().seller.name)
    }

    @Test
    fun reviews_onlyAboutYou_withAuthorNames() {
        val state = viewModel().uiState
        assertEquals(1, state.reviews.size)
        assertEquals("Maya R.", state.reviews.first().fromName)
        assertEquals(listOf("On time"), state.reviews.first().tags)
    }

    @Test
    fun markSold_movesListingToSold() {
        val vm = viewModel()
        vm.onMarkSold("m1")
        assertTrue(vm.uiState.myListings.first { it.listing.id == "m1" }.isSold)
        assertEquals(0, vm.uiState.liveCount)
        assertEquals("Marked as sold. It's off Browse now.", vm.uiState.message)
    }

    @Test
    fun remove_asksFirst_thenHidesListing() {
        val vm = viewModel()
        val item = vm.uiState.myListings.first { it.listing.id == "m1" }

        vm.onRemoveClick(item)
        assertEquals(item, vm.uiState.confirmRemove)

        vm.onConfirmRemove()
        assertNull(vm.uiState.confirmRemove)
        assertFalse(vm.uiState.myListings.any { it.listing.id == "m1" })
    }

    @Test
    fun remove_dismiss_keepsListing() {
        val vm = viewModel()
        vm.onRemoveClick(vm.uiState.myListings.first())
        vm.onDismissRemove()
        assertNull(vm.uiState.confirmRemove)
        assertEquals(2, vm.uiState.myListings.size)
    }

    @Test
    fun unsaving_removesFromSavedTab() {
        val vm = viewModel()
        vm.onToggleSaved("l1")
        assertTrue(vm.uiState.saved.isEmpty())
    }

    @Test
    fun tabs_switchAndCount() {
        val vm = viewModel()
        vm.onTabSelect(ProfileTab.REVIEWS)
        assertEquals(ProfileTab.REVIEWS, vm.uiState.tab)
        assertEquals(2, vm.uiState.countFor(ProfileTab.LISTINGS))
        assertEquals(1, vm.uiState.countFor(ProfileTab.SAVED))
        assertEquals(1, vm.uiState.countFor(ProfileTab.REVIEWS))
    }

    @Test
    fun signedOut_showsSignedOutState() {
        auth.currentUserId.value = null
        val state = viewModel().uiState
        assertTrue(state.isSignedOut)
        assertNull(state.header)
    }

    @Test
    fun signOut_signsOutOfTheAccount() {
        val vm = viewModel()
        vm.onSignOut()
        assertEquals(1, auth.signOutCalls)
    }
}
