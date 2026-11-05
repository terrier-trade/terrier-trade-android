package edu.bu.terriertrade.ui.model

import edu.bu.terriertrade.data.model.Category
import edu.bu.terriertrade.data.model.Listing
import edu.bu.terriertrade.data.model.ListingType

/**
 * Tutoring on Terrier Trade is a skill swap: you teach something, they teach you something back,
 * and no money changes hands. So tutoring posts show what's wanted in return instead of a price.
 */
object SkillSwaps {

    /**
     * TODO(data): dummy "in return" text for the sample posts. Once listings can store
     * what someone wants in return, read it from the listing instead of this map.
     */
    private val demoReturns = mapOf(
        "l10" to "CS 511 tutoring",
        "l4" to "CS 511 tutoring",
        "l8" to "CS 511 tutoring",
        "m3" to "CS 511 tutoring",
    )

    const val PRICE_LABEL = "Skill swap"
    private const val FALLBACK = "tutoring in return"

    /** Tutoring services and tutoring requests are swaps. */
    fun isSwap(listing: Listing): Boolean =
        listing.category == Category.TUTORING &&
            (listing.type == ListingType.SERVICE || listing.type == ListingType.WANTED)

    /** What the poster wants (for a service) or offers (for a request) in return. */
    fun inReturnFor(listing: Listing): String = demoReturns[listing.id] ?: FALLBACK
}
