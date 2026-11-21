package edu.bu.terriertrade.ui.screens.newlisting

import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import edu.bu.terriertrade.data.model.BookInfo
import edu.bu.terriertrade.data.model.Category
import edu.bu.terriertrade.data.model.Condition
import edu.bu.terriertrade.data.model.Listing
import edu.bu.terriertrade.data.model.ListingStatus
import edu.bu.terriertrade.data.model.ListingType
import edu.bu.terriertrade.data.repository.BookLookup
import edu.bu.terriertrade.data.repository.DataError
import edu.bu.terriertrade.data.repository.ListingRepository
import edu.bu.terriertrade.ui.theme.TerrierTradeTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Compose UI test for backlog #24: a student fills in the New listing form and posts an item.
 * Runs on the emulator against the real screen and ViewModel. Firestore is swapped for
 * [RecordingListingRepository], so nothing is written to the real database.
 */
@RunWith(AndroidJUnit4::class)
class PostListingTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val repository = RecordingListingRepository()
    private lateinit var viewModel: NewListingViewModel
    private var postedId: String? = null

    @Before
    fun showTheScreen() {
        viewModel = NewListingViewModel(listingRepository = repository, bookLookup = NoBookLookup)
        composeRule.setContent {
            TerrierTradeTheme {
                NewListingRoute(onPosted = { id -> postedId = id }, viewModel = viewModel)
            }
        }
    }

    @Test
    fun postAnItem_savesItAndShowsPostedMessage() {
        field("Title").performTextInput("Desk lamp")
        field("Price").performTextInput("12")
        tap("Furniture")
        tap("Good")
        tap("GSU")

        tap("Post listing")

        composeRule.waitUntil(timeoutMillis = 5_000) { repository.created.isNotEmpty() }
        val saved = repository.created.single()
        assertEquals(ListingType.ITEM, saved.type)
        assertEquals("Desk lamp", saved.title)
        assertEquals(12, saved.price)
        assertEquals(Category.FURNITURE, saved.category)
        assertEquals(Condition.GOOD, saved.condition)
        assertEquals("GSU", saved.meetSpot)

        // The screen confirms the post, reports the new id, and clears the form for the next one
        text("Posted! \"Desk lamp\" is live.").assertExists()
        assertEquals(RecordingListingRepository.NEW_ID, postedId)
        assertEquals("", viewModel.uiState.title)
    }

    @Test
    fun postWithEmptyForm_showsWhatsMissingAndSavesNothing() {
        tap("Post listing")

        text("Give your listing a title").assertExists()
        text("Enter a price, or 0 for free").assertExists()
        text("Pick a category").assertExists()
        text("Pick the condition").assertExists()
        text("Pick where you'll meet").assertExists()
        text(NewListingViewModel.FIX_FIELDS_MESSAGE).assertExists()
        assertTrue(repository.created.isEmpty())
        assertEquals(null, postedId)
    }

    @Test
    fun failedPost_showsWhyAndKeepsTheForm() {
        repository.createResult = Result.failure(DataError("No connection. Check it and try again."))
        field("Title").performTextInput("Desk lamp")
        field("Price").performTextInput("12")
        tap("Furniture")
        tap("Good")
        tap("GSU")

        tap("Post listing")

        text("No connection. Check it and try again.").assertExists()
        assertEquals("Desk lamp", viewModel.uiState.title)
        assertEquals(null, postedId)
    }

    // Helpers --------------------------------------------------------------------------------------

    /** The text field with this label, scrolled into view. */
    private fun field(label: String): SemanticsNodeInteraction =
        composeRule.onNode(hasSetTextAction() and hasText(label)).performScrollTo()

    /** Scrolls to the chip, tile or button with this text and taps it. */
    private fun tap(label: String) {
        composeRule.onNodeWithText(label).performScrollTo().performClick()
        composeRule.waitForIdle()
    }

    /** Any piece of text on screen, including error lines under fields. */
    private fun text(value: String): SemanticsNodeInteraction =
        composeRule.onNodeWithText(value, useUnmergedTree = true)

    /** Remembers every listing the screen tries to save, instead of writing to Firestore. */
    private class RecordingListingRepository : ListingRepository {
        val created = mutableListOf<Listing>()
        var createResult: Result<String> = Result.success(NEW_ID)

        override suspend fun createListing(listing: Listing): Result<String> {
            if (createResult.isSuccess) created += listing
            return createResult
        }

        override fun observeActiveListings(): Flow<List<Listing>> = flowOf(emptyList())
        override fun observeListing(listingId: String): Flow<Listing?> = flowOf(null)
        override fun observeListingsByOwner(ownerId: String): Flow<List<Listing>> = flowOf(emptyList())
        override suspend fun updateListing(listing: Listing): Result<Unit> = Result.success(Unit)
        override suspend fun setStatus(listingId: String, status: ListingStatus): Result<Unit> = Result.success(Unit)

        companion object {
            const val NEW_ID = "new-listing-1"
        }
    }

    /** The book scan isn't part of these tests. */
    private object NoBookLookup : BookLookup {
        override suspend fun findByIsbn(isbn: String): Result<BookInfo> = Result.failure(DataError("Not used here"))
    }
}