package edu.bu.terriertrade.ui.screens.newlisting

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import edu.bu.terriertrade.data.model.Category
import edu.bu.terriertrade.data.model.Condition
import edu.bu.terriertrade.data.model.Listing
import edu.bu.terriertrade.data.model.ListingType
import edu.bu.terriertrade.data.model.MeetSpots
import edu.bu.terriertrade.data.model.PriceUnit
import edu.bu.terriertrade.data.model.ServiceMode
import edu.bu.terriertrade.data.repository.BookLookup
import edu.bu.terriertrade.data.repository.ListingRepository
import edu.bu.terriertrade.di.appContainer
import edu.bu.terriertrade.util.Isbn
import kotlinx.coroutines.launch

/**
 * Holds the New listing form for all four post types, checks it, and saves it through ListingRepository.
 * Scanning a textbook barcode looks the book up with BookLookup and fills in the form.
 * TODO(photos): upload photos to storage and save their URLs. For now the picked Uris are saved as they are.
 */
class NewListingViewModel(
    private val listingRepository: ListingRepository,
    private val bookLookup: BookLookup,
    private val now: () -> Long = System::currentTimeMillis,
) : ViewModel() {

    var uiState by mutableStateOf(NewListingUiState())
        private set

    /** Switching type keeps what still makes sense (title, description) and resets the rest. */
    fun onTypeSelect(type: ListingType) {
        if (type == uiState.type) return
        val keepCategory = uiState.category?.takeIf { it in categoriesFor(type) }
        uiState = NewListingUiState(
            type = type,
            title = uiState.title,
            description = uiState.description,
            price = uiState.price,
            category = if (type == ListingType.TICKET) Category.TICKETS else keepCategory,
            priceUnit = if (type == ListingType.SERVICE) PriceUnit.PER_HOUR else PriceUnit.NONE,
            photos = uiState.photos.takeIf { type.allowsPhotos }.orEmpty(),
        )
    }

    // Shared fields

    fun onTitleChange(value: String) {
        uiState = uiState.copy(
            title = value.take(MAX_TITLE_LENGTH), titleError = null, submitError = null, postedMessage = null,
        )
    }

    fun onPriceChange(value: String) {
        uiState = uiState.copy(price = value, priceError = null, submitError = null)
    }

    fun onPriceUnitSelect(unit: PriceUnit) {
        uiState = uiState.copy(priceUnit = unit)
    }

    fun onDescriptionChange(value: String) {
        uiState = uiState.copy(description = value.take(MAX_DESCRIPTION_LENGTH))
    }

    fun onCategorySelect(category: Category) {
        uiState = uiState.copy(category = category, categoryError = null)
    }

    fun onMeetSpotSelect(spot: String) {
        uiState = uiState.copy(meetSpot = spot, meetSpotError = null)
    }

    /** Adds picked photos, keeping at most MAX_PHOTOS. */
    fun onPhotosPicked(uris: List<String>) {
        uiState = uiState.copy(photos = (uiState.photos + uris).distinct().take(MAX_PHOTOS))
    }

    fun onRemovePhoto(index: Int) {
        uiState = uiState.copy(photos = uiState.photos.filterIndexed { i, _ -> i != index })
    }

    // Items

    fun onConditionSelect(condition: Condition) {
        uiState = uiState.copy(condition = condition, conditionError = null)
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

    // Services

    fun onServiceModeSelect(mode: ServiceMode) {
        uiState = uiState.copy(
            serviceMode = mode,
            serviceModeError = null,
            meetSpot = uiState.meetSpot.takeIf { mode != ServiceMode.ONLINE },
            meetSpotError = null,
        )
    }

    // Tickets

    fun onEventDateChange(millis: Long) {
        uiState = uiState.copy(eventDateMillis = millis, eventDateError = null)
    }

    fun onSectionChange(value: String) {
        uiState = uiState.copy(section = value.take(40))
    }

    fun onQuantityChange(value: Int) {
        uiState = uiState.copy(quantity = value.coerceIn(1, MAX_TICKETS))
    }

    fun onPlatformSelect(platform: TicketPlatform) {
        uiState = uiState.copy(platform = platform, platformError = null)
    }

    // Posting

    /** Saves the listing when the form is OK, then clears the form and reports the new id. */
    fun post(onPosted: (listingId: String) -> Unit) {
        if (uiState.isPosting || !validate()) return
        val form = uiState
        uiState = form.copy(isPosting = true, submitError = null)
        val listing = form.toListing()

        viewModelScope.launch {
            listingRepository.createListing(listing)
                .onSuccess { id ->
                    uiState = NewListingUiState(
                        type = form.type,
                        priceUnit = if (form.type == ListingType.SERVICE) PriceUnit.PER_HOUR else PriceUnit.NONE,
                        category = if (form.type == ListingType.TICKET) Category.TICKETS else null,
                        postedMessage = "Posted! \"${listing.title}\" is live.",
                    )
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

    /** Shows an error under every field that's wrong for this post type. Returns true when the form is OK. */
    fun validate(): Boolean {
        val s = uiState
        val type = s.type
        val titleError = when {
            s.title.isBlank() -> when (type) {
                ListingType.TICKET -> "Enter the event"
                ListingType.WANTED -> "Say what you're looking for"
                else -> "Give your listing a title"
            }
            s.title.trim().length < MIN_TITLE_LENGTH -> "Use at least $MIN_TITLE_LENGTH characters"
            else -> null
        }
        val priceError = when {
            s.price.isNotBlank() -> null
            type == ListingType.WANTED -> "Enter your budget"
            type == ListingType.TICKET -> "Enter the price for each ticket"
            else -> "Enter a price, or 0 for free"
        }
        val categoryError = if (type != ListingType.TICKET && s.category == null) "Pick a category" else null
        val conditionError = if (type == ListingType.ITEM && s.condition == null) "Pick the condition" else null
        val serviceModeError = if (type == ListingType.SERVICE && s.serviceMode == null) "Pick how you'll meet" else null
        val meetSpotError = if (s.needsMeetSpot && s.meetSpot == null) "Pick where you'll meet" else null
        val eventDateError = when {
            type != ListingType.TICKET -> null
            s.eventDateMillis == null -> "Pick the date and time"
            s.eventDateMillis <= now() -> "That event has already happened"
            else -> null
        }
        val platformError = if (type == ListingType.TICKET && s.platform == null) "Pick where the ticket lives" else null

        val errors = listOf(
            titleError, priceError, categoryError, conditionError,
            serviceModeError, meetSpotError, eventDateError, platformError,
        )
        val isValid = errors.all { it == null }
        uiState = s.copy(
            titleError = titleError,
            priceError = priceError,
            categoryError = categoryError,
            conditionError = conditionError,
            serviceModeError = serviceModeError,
            meetSpotError = meetSpotError,
            eventDateError = eventDateError,
            platformError = platformError,
            // The errors can be far up the page, so say so next to the button too.
            submitError = if (isValid) null else FIX_FIELDS_MESSAGE,
        )
        return isValid
    }

    /** Turns the form into a Listing. Details without their own Listing field go at the start of the description. */
    private fun NewListingUiState.toListing(): Listing {
        // TODO(model): Listing has no author, section or platform fields yet, so they start the description.
        val details = when (type) {
            ListingType.ITEM -> author.trim().takeIf { isBook && it.isNotEmpty() }?.let { "By $it" }
            ListingType.TICKET -> listOfNotNull(
                section.trim().takeIf { it.isNotEmpty() }?.let { "Section $it" },
                platform?.let { "Transfer on ${it.label}" },
            ).joinToString(" · ").ifEmpty { null }
            else -> null
        }
        val fullDescription = listOfNotNull(details, description.trim().takeIf { it.isNotEmpty() })
            .joinToString("\n\n")

        return Listing(
            id = "",
            ownerId = "",
            type = type,
            title = title.trim(),
            price = price.toInt(),
            priceUnit = priceUnit,
            description = fullDescription,
            category = category ?: Category.OTHER,
            condition = condition.takeIf { type == ListingType.ITEM },
            serviceMode = serviceMode.takeIf { type == ListingType.SERVICE },
            meetSpot = when {
                type == ListingType.TICKET -> "Online"
                type == ListingType.SERVICE && serviceMode == ServiceMode.ONLINE -> "Online"
                else -> meetSpot.orEmpty()
            },
            photoUrls = photos.takeIf { type.allowsPhotos }.orEmpty(),
            isbn = isbn.takeIf { isBook },
            eventDateMillis = eventDateMillis.takeIf { type == ListingType.TICKET },
            quantity = quantity.takeIf { type == ListingType.TICKET },
        )
    }

    companion object {
        const val MAX_PHOTOS = 5
        const val MAX_TICKETS = 10
        const val MIN_TITLE_LENGTH = 3
        const val MAX_TITLE_LENGTH = 80
        const val MAX_DESCRIPTION_LENGTH = 500
        const val FIX_FIELDS_MESSAGE = "Some details are missing. Check the fields marked in red above."
        const val SCAN_FILLED_MESSAGE = "Title and author filled in from Open Library. You can still edit them."

        /** The categories offered for each post type. Tickets are always TICKETS, so they don't pick one. */
        fun categoriesFor(type: ListingType): List<Category> = when (type) {
            ListingType.ITEM -> listOf(Category.BOOKS, Category.FURNITURE, Category.TECH, Category.NOTES, Category.OTHER)
            ListingType.SERVICE -> listOf(Category.TUTORING, Category.OTHER)
            ListingType.TICKET -> listOf(Category.TICKETS)
            ListingType.WANTED -> listOf(
                Category.BOOKS, Category.TUTORING, Category.FURNITURE, Category.TECH, Category.NOTES,
                Category.TICKETS, Category.OTHER,
            )
        }

        /** Items are handed over in person, so "Online" is left out for them. */
        val inPersonMeetSpots: List<String> = MeetSpots.all.filterNot { it == "Online" }

        val Factory = viewModelFactory {
            initializer { NewListingViewModel(appContainer.listingRepository, appContainer.bookLookup) }
        }
    }
}

/** Items and services show photos. Tickets and wanted notes don't. */
val ListingType.allowsPhotos: Boolean
    get() = this == ListingType.ITEM || this == ListingType.SERVICE
