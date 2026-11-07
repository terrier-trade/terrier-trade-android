package edu.bu.terriertrade.ui.screens.newlisting

import edu.bu.terriertrade.data.model.Category
import edu.bu.terriertrade.data.model.Condition
import edu.bu.terriertrade.data.model.ListingType
import edu.bu.terriertrade.data.model.PriceUnit
import edu.bu.terriertrade.data.model.ServiceMode
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

    /** A fixed "now" so ticket dates don't depend on when the test runs. */
    private val now = 1_800_000_000_000L
    private val tomorrow = now + 24 * 60 * 60 * 1000L

    private fun filledIn() = NewListingViewModel(repo, books).apply {
        onTitleChange("  Desk chair ")
        onPriceChange("40")
        onDescriptionChange("Adjustable height")
        onCategorySelect(Category.FURNITURE)
        onConditionSelect(Condition.GOOD)
        onMeetSpotSelect("West Campus")
    }

    // Items

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

    // Textbook scan

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

    // Switching type

    @Test
    fun switchingType_keepsTitleAndResetsTheRest() {
        val vm = filledIn()

        vm.onTypeSelect(ListingType.SERVICE)

        with(vm.uiState) {
            assertEquals(ListingType.SERVICE, type)
            assertEquals("  Desk chair ", title)
            assertEquals(PriceUnit.PER_HOUR, priceUnit)
            assertNull(category) // Furniture isn't a service category
            assertNull(condition)
        }
    }

    // Services

    private fun serviceFilledIn() = NewListingViewModel(repo, books).apply {
        onTypeSelect(ListingType.SERVICE)
        onTitleChange("CS 330 tutoring")
        onPriceChange("20")
        onCategorySelect(Category.TUTORING)
    }

    @Test
    fun service_needsHowYoullMeet() {
        val vm = serviceFilledIn()
        assertFalse(vm.validate())
        assertEquals("Pick how you'll meet", vm.uiState.serviceModeError)
    }

    @Test
    fun service_inPerson_needsMeetSpot() {
        val vm = serviceFilledIn().apply { onServiceModeSelect(ServiceMode.IN_PERSON) }
        assertFalse(vm.validate())
        assertEquals("Pick where you'll meet", vm.uiState.meetSpotError)
    }

    @Test
    fun service_online_savesWithoutMeetSpot() {
        val vm = serviceFilledIn().apply { onServiceModeSelect(ServiceMode.ONLINE) }

        vm.post { }

        val saved = repo.lastCreated!!
        assertEquals(ListingType.SERVICE, saved.type)
        assertEquals(PriceUnit.PER_HOUR, saved.priceUnit)
        assertEquals(ServiceMode.ONLINE, saved.serviceMode)
        assertEquals("Online", saved.meetSpot)
        assertNull(saved.condition)
    }

    // Tickets

    private fun ticketFilledIn() = NewListingViewModel(repo, books, now = { now }).apply {
        onTypeSelect(ListingType.TICKET)
        onTitleChange("BU Hockey vs Boston College")
        onEventDateChange(tomorrow)
        onSectionChange("Student, 112")
        onQuantityChange(2)
        onPriceChange("25")
        onPlatformSelect(TicketPlatform.TICKETMASTER)
    }

    @Test
    fun ticket_emptyForm_showsTicketErrors() {
        val vm = NewListingViewModel(repo, books, now = { now }).apply { onTypeSelect(ListingType.TICKET) }
        assertFalse(vm.validate())
        with(vm.uiState) {
            assertEquals("Enter the event", titleError)
            assertEquals("Enter the price for each ticket", priceError)
            assertEquals("Pick the date and time", eventDateError)
            assertEquals("Pick where the ticket lives", platformError)
            assertNull(categoryError)
            assertNull(meetSpotError)
        }
    }

    @Test
    fun ticket_pastEvent_isRejected() {
        val vm = ticketFilledIn().apply { onEventDateChange(now - 1) }
        assertFalse(vm.validate())
        assertEquals("That event has already happened", vm.uiState.eventDateError)
    }

    @Test
    fun ticket_quantity_staysBetweenOneAndTen() {
        val vm = ticketFilledIn()
        vm.onQuantityChange(0)
        assertEquals(1, vm.uiState.quantity)
        vm.onQuantityChange(25)
        assertEquals(10, vm.uiState.quantity)
    }

    @Test
    fun ticket_buttonText_countsTickets() {
        assertEquals("Post 2 tickets", ticketFilledIn().uiState.postButtonText)
    }

    @Test
    fun ticket_post_savesDateQuantityAndDetails() {
        val vm = ticketFilledIn()

        vm.post { }

        val saved = repo.lastCreated!!
        assertEquals(ListingType.TICKET, saved.type)
        assertEquals(Category.TICKETS, saved.category)
        assertEquals(tomorrow, saved.eventDateMillis)
        assertEquals(2, saved.quantity)
        assertEquals(25, saved.price)
        assertEquals("Section Student, 112 · Transfer on Ticketmaster", saved.description)
    }

    // Looking for

    @Test
    fun wanted_needsBudget_butNotMeetSpot() {
        val vm = NewListingViewModel(repo, books).apply {
            onTypeSelect(ListingType.WANTED)
            onTitleChange("Math tutor for MA 124")
            onCategorySelect(Category.TUTORING)
        }
        assertFalse(vm.validate())
        assertEquals("Enter your budget", vm.uiState.priceError)
        assertNull(vm.uiState.meetSpotError)
    }

    @Test
    fun wanted_post_savesBudgetAsPrice() {
        val vm = NewListingViewModel(repo, books).apply {
            onTypeSelect(ListingType.WANTED)
            onTitleChange("Math tutor for MA 124")
            onCategorySelect(Category.TUTORING)
            onPriceChange("25")
            onPriceUnitSelect(PriceUnit.PER_HOUR)
        }

        vm.post { }

        val saved = repo.lastCreated!!
        assertEquals(ListingType.WANTED, saved.type)
        assertEquals(25, saved.price)
        assertEquals(PriceUnit.PER_HOUR, saved.priceUnit)
        assertTrue(saved.photoUrls.isEmpty())
    }
}
