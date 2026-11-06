package edu.bu.terriertrade.ui.screens.browse

import edu.bu.terriertrade.ui.model.ListingUi

/** How the listings are ordered. */
enum class SortOrder(val label: String, val headerText: String) {
    NEWEST("Newest", "Newest first"),
    LOWEST_PRICE("Lowest price", "Lowest price first"),
    TOP_RATED("Top rated", "Top rated sellers first"),
}

/** Price limits on the Filters sheet. null means any price. */
enum class PriceLimit(val label: String, val max: Int?) {
    ANY("Any", null),
    UNDER_20("Under $20", 19),
    UNDER_50("Under $50", 49),
}

/** Condition on the Filters sheet. Each keeps that condition and better (New is best). */
enum class ConditionFilter(val label: String, val worstRank: Int?) {
    ANY("Any", null),
    LIKE_NEW("Like new", 1),
    GOOD("Good", 2),
}

/** Seller rating on the Filters sheet. */
enum class RatingFilter(val label: String, val min: Double?) {
    ANY("Any", null),
    FOUR_HALF("4.5 stars and up", 4.5),
}

/** Everything on the Filters sheet from the proposal. The defaults mean "no filter". */
data class BrowseFilters(
    val sort: SortOrder = SortOrder.NEWEST,
    val price: PriceLimit = PriceLimit.ANY,
    val condition: ConditionFilter = ConditionFilter.ANY,
    val rating: RatingFilter = RatingFilter.ANY,
) {
    val isDefault: Boolean get() = this == BrowseFilters()

    /** How many filters are switched on, for the badge. Sort doesn't count, it only reorders. */
    val activeCount: Int
        get() = listOf(price != PriceLimit.ANY, condition != ConditionFilter.ANY, rating != RatingFilter.ANY).count { it }

    fun matches(listing: ListingUi): Boolean {
        val maxPrice = price.max
        val worstRank = condition.worstRank
        val minRating = rating.min
        return (maxPrice == null || listing.price <= maxPrice) &&
            // Condition only applies to items; services and tickets have no condition, so they're left out.
            (worstRank == null || (listing.conditionRank != null && listing.conditionRank <= worstRank)) &&
            (minRating == null || listing.sellerRating >= minRating)
    }

    fun sort(listings: List<ListingUi>): List<ListingUi> = when (sort) {
        SortOrder.NEWEST -> listings.sortedByDescending { it.createdAt }
        SortOrder.LOWEST_PRICE -> listings.sortedWith(compareBy<ListingUi> { it.price }.thenByDescending { it.createdAt })
        SortOrder.TOP_RATED -> listings.sortedWith(compareByDescending<ListingUi> { it.sellerRating }.thenByDescending { it.createdAt })
    }
}
