package edu.bu.terriertrade.data.deal

import edu.bu.terriertrade.data.model.DealItem
import edu.bu.terriertrade.data.model.Listing
import edu.bu.terriertrade.data.model.ListingType

/**
 * Money in the deal from my side. Positive means I pay, negative means they pay me.
 * I receive an item when someone else owns it, except on Looking for posts, where it flips.
 */
fun dealTotal(items: List<DealItem>, listings: Map<String, Listing>, myId: String): Int {
    var total = 0
    for (item in items) {
        val listing = listings[item.listingId] ?: continue
        val iReceive = (listing.ownerId != myId) != (listing.type == ListingType.WANTED)
        total += if (iReceive) item.agreedPrice else -item.agreedPrice
    }
    return total
}
