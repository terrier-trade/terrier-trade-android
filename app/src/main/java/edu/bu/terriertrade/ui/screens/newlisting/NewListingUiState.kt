package edu.bu.terriertrade.ui.screens.newlisting

import edu.bu.terriertrade.data.model.Category
import edu.bu.terriertrade.data.model.Condition
import edu.bu.terriertrade.data.model.ListingType
import edu.bu.terriertrade.data.model.PriceUnit
import edu.bu.terriertrade.data.model.ServiceMode

/** Where a ticket can be sent from. Only apps with an official transfer are allowed. */
enum class TicketPlatform(val label: String) {
    TICKETMASTER("Ticketmaster"),
    AXS("AXS"),
    BU_ATHLETICS("BU Athletics app"),
}

/**
 * Everything the New listing screen draws, for all four post types.
 * Errors are null when the field is fine. photos holds image Uris or URLs; the first one is the cover.
 */
data class NewListingUiState(
    val type: ListingType = ListingType.ITEM,

    // Shared by every type
    val title: String = "",
    val price: String = "",
    val priceUnit: PriceUnit = PriceUnit.NONE,
    val description: String = "",
    val category: Category? = null,
    val meetSpot: String? = null,
    val photos: List<String> = emptyList(),

    // Items
    val condition: Condition? = null,
    /** Only shown for books. Filled in from Open Library after a scan. */
    val author: String = "",
    /** Set after a successful textbook scan; saved on the listing. */
    val isbn: String? = null,

    // Services
    val serviceMode: ServiceMode? = null,

    // Tickets
    val eventDateMillis: Long? = null,
    val section: String = "",
    val quantity: Int = 1,
    val platform: TicketPlatform? = null,

    // Errors
    val titleError: String? = null,
    val priceError: String? = null,
    val categoryError: String? = null,
    val conditionError: String? = null,
    val meetSpotError: String? = null,
    val serviceModeError: String? = null,
    val eventDateError: String? = null,
    val platformError: String? = null,
    val submitError: String? = null,

    /** Shown at the top after a successful post, until the student starts a new one. */
    val postedMessage: String? = null,
    val isPosting: Boolean = false,

    // Textbook scanning
    val showScanner: Boolean = false,
    val isLookingUpBook: Boolean = false,
    /** Green note after a scan filled in the book, or a warning when it couldn't. */
    val scanMessage: String? = null,
    val scanFailed: Boolean = false,
) {
    val isBook: Boolean get() = type == ListingType.ITEM && category == Category.BOOKS

    val canAddPhoto: Boolean get() = photos.size < NewListingViewModel.MAX_PHOTOS

    /** Services done online don't need a meetup spot. */
    val needsMeetSpot: Boolean
        get() = when (type) {
            ListingType.ITEM -> true
            ListingType.SERVICE -> serviceMode != null && serviceMode != ServiceMode.ONLINE
            ListingType.TICKET -> false
            ListingType.WANTED -> false
        }

    val postButtonText: String
        get() = when (type) {
            ListingType.TICKET -> if (quantity == 1) "Post 1 ticket" else "Post $quantity tickets"
            ListingType.WANTED -> "Post wanted note"
            else -> "Post listing"
        }
}
