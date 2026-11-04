package edu.bu.terriertrade.ui.model

/** What kind of post it is. Shown as the tag on cards and as the type chips on Browse. */
enum class ListingType(val label: String) {
    ITEM("Item"),
    SERVICE("Service"),
    TICKET("Ticket"),
}

/** Categories shown as round icons on Browse. */
enum class ListingCategory(val label: String) {
    BOOKS("Books"),
    FURNITURE("Furniture"),
    TUTORING("Tutoring"),
    TICKETS("Tickets"),
    NOTES("Notes"),
    TECH("Tech"),
    OTHER("Other"),
}

/** The person selling a listing, ready to display. */
data class SellerUi(
    val name: String,
    val initials: String,
    val rating: String,
    val tradeCount: Int,
    val isVerified: Boolean = true,
)

/**
 * Everything the UI needs to show one listing, as display-ready text.
 * The data layer converts its own Listing model into this, so screens never depend on Firebase.
 */
data class ListingUi(
    val id: String,
    val title: String,
    val priceLabel: String,
    val type: ListingType,
    val category: ListingCategory,
    val meetSpot: String,
    val postedAgo: String,
    val condition: String,
    val description: String,
    val seller: SellerUi,
    val details: List<Pair<String, String>> = emptyList(),
    /** Cover photo URL, or null to show the placeholder. */
    val photoUrl: String? = null,
    // Raw values for sorting and filtering. The labels above are what the screen shows.
    val price: Int = 0,
    val createdAt: Long = 0L,
    val sellerRating: Double = 0.0,
    /** 0 = New, 1 = Like new, 2 = Good, 3 = Fair. Null for services and tickets. */
    val conditionRank: Int? = null,
)
