package edu.bu.terriertrade.data.deal

import edu.bu.terriertrade.data.model.DealItem
import edu.bu.terriertrade.data.model.Listing
import edu.bu.terriertrade.data.model.ListingType

/**
 * True when I end up with this listing's item or service: it's someone else's listing,
 * or it's my own Looking for post that they fill.
 */
fun receivesItem(listing: Listing, myId: String): Boolean =
    (listing.ownerId != myId) != (listing.type == ListingType.WANTED)

/**
 * Money in the deal from my side. Positive means I pay, negative means they pay me.
 * I pay for what I receive and get paid for what I give.
 */
fun dealTotal(items: List<DealItem>, listings: Map<String, Listing>, myId: String): Int {
    var total = 0
    for (item in items) {
        val listing = listings[item.listingId] ?: continue
        total += if (receivesItem(listing, myId)) item.agreedPrice else -item.agreedPrice
    }
    return total
}
