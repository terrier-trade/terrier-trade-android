package edu.bu.terriertrade.ui.model

import edu.bu.terriertrade.data.model.Category
import edu.bu.terriertrade.data.model.ListingType

/** The person selling a listing, ready to display. */
data class SellerUi(
    val name: String,
    val initials: String,
    val rating: String,
    val tradeCount: Int,
    val isVerified: Boolean = true,
    /** Their user id, so tapping them can open their profile. Blank in previews. */
    val id: String = "",
)

/**
 * Everything the UI needs to show one listing, as display-ready text.
 * The data layer converts its own Listing model into this, so screens never depend on Firebase.
 */
data class ListingUi(
    val id: String,
    val title: String,
    val priceLabel: String,
    /** Never WANTED: wanted posts are shown as notes (WantedPostUi), not cards. */
    val type: ListingType,
    val category: Category,
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
