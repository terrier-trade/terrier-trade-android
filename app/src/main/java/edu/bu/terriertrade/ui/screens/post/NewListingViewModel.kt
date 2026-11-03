package edu.bu.terriertrade.ui.screens.post

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import edu.bu.terriertrade.data.books.BookLookup
import edu.bu.terriertrade.data.books.OpenLibraryBookLookup
import edu.bu.terriertrade.data.model.Category
import edu.bu.terriertrade.data.model.Condition
import edu.bu.terriertrade.data.model.Listing
import edu.bu.terriertrade.data.model.ListingType
import edu.bu.terriertrade.data.model.MeetSpots
import edu.bu.terriertrade.data.repository.ListingRepository
import edu.bu.terriertrade.di.appContainer
import edu.bu.terriertrade.util.Isbn
import kotlinx.coroutines.launch

/**
 * Holds the New listing form, checks it, and saves it through ListingRepository.
 * Scanning a textbook barcode looks the book up with BookLookup and fills in the form.
 * TODO(photos): upload photos to storage and save their URLs. For now the picked Uris are saved as they are.
 */
class NewListingViewModel(
    private val listingRepository: ListingRepository,
    private val bookLookup: BookLookup,
) : ViewModel() {

    var uiState by mutableStateOf(NewListingUiState())
        private set

    fun onTypeSelect(type: ListingType) {
        uiState = uiState.copy(type = type, submitError = null)
    }

    /** Adds picked photos, keeping at most MAX_PHOTOS. */
    fun onPhotosPicked(uris: List<String>) {
        uiState = uiState.copy(photos = (uiState.photos + uris).distinct().take(MAX_PHOTOS))
    }

    fun onRemovePhoto(index: Int) {
        uiState = uiState.copy(photos = uiState.photos.filterIndexed { i, _ -> i != index })
    }

    fun onTitleChange(value: String) {
        uiState = uiState.copy(
            title = value.take(MAX_TITLE_LENGTH), titleError = null, submitError = null, postedMessage = null,
        )
    }

    fun onAuthorChange(value: String) {
        uiState = uiState.copy(author = value.take(MAX_TITLE_LENGTH))
    }

    fun onScanClick() {
        uiState = uiState.copy(showScanner = true, scanMessage = null, scanFailed = false)
    }

    fun onScannerDismiss() {
        uiState = uiState.copy(showScanner = false)
    }

    /**
     * Called with whatever the camera read, or what the student typed.
     * Sets the listing up as a book, then fills in title, author and cover from Open Library.
     */
    fun onIsbnScanned(raw: String) {
        val isbn = Isbn.normalize(raw)
        if (isbn == null) {
            uiState = uiState.copy(scanMessage = "That isn't a valid ISBN. Check the number on the back cover.", scanFailed = true)
            return
        }
        uiState = uiState.copy(
            showScanner = false,
            type = ListingType.ITEM,
            category = Category.BOOKS,
            categoryError = null,
            isbn = isbn,
            isLookingUpBook = true,
            scanMessage = null,
            scanFailed = false,
            postedMessage = null,
        )

        viewModelScope.launch {
            bookLookup.findByIsbn(isbn)
                .onSuccess { book ->
                    val photos = listOfNotNull(book.coverUrl) + uiState.photos
                    uiState = uiState.copy(
                        isLookingUpBook = false,
                        title = book.title.take(MAX_TITLE_LENGTH),
                        titleError = null,
                        author = book.authorLine,
                        photos = photos.distinct().take(MAX_PHOTOS),
                        scanMessage = SCAN_FILLED_MESSAGE,
                        scanFailed = false,
                    )
                }
                .onFailure { error ->
                    uiState = uiState.copy(
                        isLookingUpBook = false,
                        scanMessage = error.message ?: "Couldn't look that book up. Fill in the details yourself.",
                        scanFailed = true,
                    )
                }
        }
    }

    fun onPriceChange(value: String) {
        uiState = uiState.copy(price = value, priceError = null, submitError = null)
    }

    fun onDescriptionChange(value: String) {
        uiState = uiState.copy(description = value.take(MAX_DESCRIPTION_LENGTH))
    }

    fun onCategorySelect(category: Category) {
        uiState = uiState.copy(category = category, categoryError = null)
    }

    fun onConditionSelect(condition: Condition) {
        uiState = uiState.copy(condition = condition, conditionError = null)
    }

    fun onMeetSpotSelect(spot: String) {
        uiState = uiState.copy(meetSpot = spot, meetSpotError = null)
    }

    /** Saves the listing when the form is OK, then clears the form and reports the new id. */
    fun post(onPosted: (listingId: String) -> Unit) {
        if (uiState.isPosting || !uiState.isFormReady || !validate()) return
        val form = uiState
        uiState = form.copy(isPosting = true, submitError = null)

        // TODO(author): Listing has no author field yet, so books start their description with it.
        val description = listOf(
            form.author.trim().takeIf { form.isBook && it.isNotEmpty() }?.let { "By $it" },
            form.description.trim().takeIf { it.isNotEmpty() },
        ).filterNotNull().joinToString("\n\n")

        val listing = Listing(
            id = "",
            ownerId = "",
            type = ListingType.ITEM,
            title = form.title.trim(),
            price = form.price.toInt(),
            description = description,
            isbn = form.isbn.takeIf { form.isBook },
            category = form.category ?: Category.OTHER,
            condition = form.condition,
            meetSpot = form.meetSpot.orEmpty(),
            photoUrls = form.photos,
        )

        viewModelScope.launch {
            listingRepository.createListing(listing)
                .onSuccess { id ->
                    uiState = NewListingUiState(postedMessage = "Posted! \"${listing.title}\" is live.")
                    onPosted(id)
                }
                .onFailure { error ->
                    uiState = uiState.copy(
                        isPosting = false,
                        submitError = error.message ?: "Couldn't post your listing. Try again.",
                    )
                }
        }
    }

    /** Shows an error under every field that's wrong. Returns true when the form is OK. */
    fun validate(): Boolean {
        val state = uiState
        val titleError = when {
            state.title.isBlank() -> "Give your listing a title"
            state.title.trim().length < MIN_TITLE_LENGTH -> "Use at least $MIN_TITLE_LENGTH characters"
            else -> null
        }
        val priceError = if (state.price.isBlank()) "Enter a price, or 0 for free" else null
        val categoryError = if (state.category == null) "Pick a category" else null
        val conditionError = if (state.condition == null) "Pick the condition" else null
        val meetSpotError = if (state.meetSpot == null) "Pick where you'll meet" else null

        uiState = state.copy(
            titleError = titleError,
            priceError = priceError,
            categoryError = categoryError,
            conditionError = conditionError,
            meetSpotError = meetSpotError,
        )
        return listOf(titleError, priceError, categoryError, conditionError, meetSpotError).all { it == null }
    }

    companion object {
        const val MAX_PHOTOS = 5
        const val MIN_TITLE_LENGTH = 3
        const val MAX_TITLE_LENGTH = 80
        const val MAX_DESCRIPTION_LENGTH = 500
        const val SCAN_FILLED_MESSAGE = "Title and author filled in from Open Library. You can still edit them."

        /** Categories that make sense for a physical item. Tutoring and tickets have their own post types. */
        val itemCategories: List<Category> = listOf(
            Category.BOOKS, Category.FURNITURE, Category.TECH, Category.NOTES, Category.OTHER,
        )

        /** Items are handed over in person, so "Online" isn't offered. */
        val itemMeetSpots: List<String> = MeetSpots.all.filterNot { it == "Online" }

        val Factory = viewModelFactory {
            // TODO(di): BookLookup can move into AppContainer later; it's created here so di/ stays untouched.
            initializer { NewListingViewModel(appContainer.listingRepository, OpenLibraryBookLookup()) }
        }
    }
}
