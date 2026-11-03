package edu.bu.terriertrade.ui.screens.post

import edu.bu.terriertrade.data.model.Category
import edu.bu.terriertrade.data.model.Condition
import edu.bu.terriertrade.data.model.ListingType
import edu.bu.terriertrade.util.MainDispatcherRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class NewListingViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repo = TestListingRepository()
    private val books = TestBookLookup()

    private fun filledIn() = NewListingViewModel(repo, books).apply {
        onTitleChange("  Desk chair ")
        onPriceChange("40")
        onDescriptionChange("Adjustable height")
        onCategorySelect(Category.FURNITURE)
        onConditionSelect(Condition.GOOD)
        onMeetSpotSelect("West Campus")
    }

    @Test
    fun emptyForm_showsEveryError() {
        val vm = NewListingViewModel(repo, books)
        assertFalse(vm.validate())
        with(vm.uiState) {
            assertEquals("Give your listing a title", titleError)
            assertEquals("Enter a price, or 0 for free", priceError)
            assertEquals("Pick a category", categoryError)
            assertEquals("Pick the condition", conditionError)
            assertEquals("Pick where you'll meet", meetSpotError)
        }
    }

    @Test
    fun shortTitle_showsError() {
        val vm = filledIn().apply { onTitleChange("ab") }
        assertFalse(vm.validate())
        assertEquals("Use at least 3 characters", vm.uiState.titleError)
    }

    @Test
    fun zeroPrice_isAllowed() {
        val vm = filledIn().apply { onPriceChange("0") }
        assertTrue(vm.validate())
    }

    @Test
    fun editingAField_clearsItsError() {
        val vm = NewListingViewModel(repo, books)
        vm.validate()
        vm.onTitleChange("Desk chair")
        assertNull(vm.uiState.titleError)
    }

    @Test
    fun photos_capAtFive_andRemoveWorks() {
        val vm = NewListingViewModel(repo, books)
        vm.onPhotosPicked(listOf("a", "b", "c"))
        vm.onPhotosPicked(listOf("d", "e", "f", "g"))
        assertEquals(listOf("a", "b", "c", "d", "e"), vm.uiState.photos)
        assertFalse(vm.uiState.canAddPhoto)

        vm.onRemovePhoto(0)
        assertEquals(listOf("b", "c", "d", "e"), vm.uiState.photos)
        assertTrue(vm.uiState.canAddPhoto)
    }

    @Test
    fun post_validForm_savesListingAndClearsForm() {
        var postedId: String? = null
        val vm = filledIn().apply { onPhotosPicked(listOf("content://photo/1")) }

        vm.post { postedId = it }

        assertEquals("new-1", postedId)
        val saved = repo.lastCreated!!
        assertEquals(ListingType.ITEM, saved.type)
        assertEquals("Desk chair", saved.title)
        assertEquals(40, saved.price)
        assertEquals(Category.FURNITURE, saved.category)
        assertEquals(Condition.GOOD, saved.condition)
        assertEquals("West Campus", saved.meetSpot)
        assertEquals(listOf("content://photo/1"), saved.photoUrls)
        assertEquals(NewListingUiState(postedMessage = "Posted! \"Desk chair\" is live."), vm.uiState)
    }

    @Test
    fun startingANewListing_hidesPostedMessage() {
        val vm = filledIn()
        vm.post { }
        vm.onTitleChange("Lamp")
        assertNull(vm.uiState.postedMessage)
    }

    @Test
    fun post_invalidForm_doesNotSave() {
        NewListingViewModel(repo, books).post { }
        assertEquals(0, repo.createCalls)
    }

    @Test
    fun post_failure_keepsFormAndShowsMessage() {
        repo.createResult = Result.failure(Exception("Not signed in"))
        var postedId: String? = null
        val vm = filledIn()

        vm.post { postedId = it }

        assertNull(postedId)
        assertEquals("Not signed in", vm.uiState.submitError)
        assertEquals("Desk chair", vm.uiState.title.trim())
        assertFalse(vm.uiState.isPosting)
    }

    @Test
    fun otherTypes_cannotPostYet() {
        val vm = filledIn().apply { onTypeSelect(ListingType.SERVICE) }
        vm.post { }
        assertFalse(vm.uiState.isFormReady)
        assertEquals(0, repo.createCalls)
    }

    @Test
    fun scan_fillsInTheBook() {
        val vm = NewListingViewModel(repo, books)
        vm.onScanClick()
        assertTrue(vm.uiState.showScanner)

        vm.onIsbnScanned("978-0-262-04630-5")

        with(vm.uiState) {
            assertFalse(showScanner)
            assertFalse(isLookingUpBook)
            assertEquals("9780262046305", books.lastIsbn)
            assertEquals("9780262046305", isbn)
            assertEquals(Category.BOOKS, category)
            assertEquals("Introduction to Algorithms", title)
            assertEquals("Thomas H. Cormen, Charles E. Leiserson · 2022", author)
            assertEquals(listOf("https://covers.openlibrary.org/b/id/1-M.jpg"), photos)
            assertEquals(NewListingViewModel.SCAN_FILLED_MESSAGE, scanMessage)
            assertFalse(scanFailed)
        }
    }

    @Test
    fun scan_invalidIsbn_staysOpenWithError() {
        val vm = NewListingViewModel(repo, books)
        vm.onScanClick()

        vm.onIsbnScanned("12345")

        assertTrue(vm.uiState.showScanner)
        assertTrue(vm.uiState.scanFailed)
        assertNull(books.lastIsbn)
    }

    @Test
    fun scan_bookNotFound_keepsFormAndWarns() {
        books.result = Result.failure(Exception("We couldn't find ISBN 9780262046305. Fill in the details yourself."))
        val vm = NewListingViewModel(repo, books).apply { onTitleChange("My book") }

        vm.onIsbnScanned("9780262046305")

        with(vm.uiState) {
            assertEquals("My book", title)
            assertEquals(Category.BOOKS, category)
            assertTrue(scanFailed)
            assertEquals("We couldn't find ISBN 9780262046305. Fill in the details yourself.", scanMessage)
        }
    }

    @Test
    fun post_scannedBook_savesIsbnAndAuthor() {
        val vm = NewListingViewModel(repo, books).apply {
            onIsbnScanned("9780262046305")
            onPriceChange("35")
            onDescriptionChange("Highlighting in ch. 2")
            onConditionSelect(Condition.GOOD)
            onMeetSpotSelect("Mugar Library")
        }

        vm.post { }

        val saved = repo.lastCreated!!
        assertEquals("9780262046305", saved.isbn)
        assertEquals(Category.BOOKS, saved.category)
        assertEquals("By Thomas H. Cormen, Charles E. Leiserson · 2022\n\nHighlighting in ch. 2", saved.description)
    }

    @Test
    fun post_notABook_doesNotSaveAuthorOrIsbn() {
        val vm = filledIn()

        vm.post { }

        assertNull(repo.lastCreated!!.isbn)
        assertEquals("Adjustable height", repo.lastCreated!!.description)
    }
}
