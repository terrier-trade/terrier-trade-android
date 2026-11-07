package edu.bu.terriertrade.data.deal

import edu.bu.terriertrade.data.model.Deal
import edu.bu.terriertrade.data.model.DealItem
import edu.bu.terriertrade.data.model.Listing
import edu.bu.terriertrade.data.model.ListingType

/** What happened after tapping Request, so the screen can say the right thing. */
enum class RequestOutcome {
    /** A new deal was started with this item. */
    STARTED_DEAL,

    /** The item was already in the deal. The chat just opens. */
    ALREADY_IN_DEAL,

    /** There's already a deal with this person. Add the item from the deal window. */
    DEAL_IN_PROGRESS,
}

/** Which chat to open, and what happened. */
data class RequestResult(
    val chatId: String,
    val outcome: RequestOutcome,
)

/** What a request should do, decided before anything is saved. */
sealed interface RequestPlan {
    data class StartDeal(val deal: Deal, val actionText: String) : RequestPlan
    data object AlreadyInDeal : RequestPlan
    data object DealInProgress : RequestPlan
    data class NotAllowed(val reason: String) : RequestPlan
}

/**
 * The rules for tapping Request (or "I can help" on a Looking for post).
 * Plain Kotlin with no Firebase, so every case is covered by fast unit tests.
 */
object RequestRules {

    fun plan(listing: Listing, myId: String, currentDeal: Deal?): RequestPlan = when {
        listing.ownerId == myId ->
            RequestPlan.NotAllowed("This is your own listing.")

        !listing.isActive ->
            RequestPlan.NotAllowed("This listing is no longer available.")

        currentDeal == null ->
            RequestPlan.StartDeal(
                deal = Deal(items = listOf(DealItem(listing.id, listing.price)), version = 1),
                actionText = actionText(listing),
            )

        currentDeal.items.any { it.listingId == listing.id } ->
            RequestPlan.AlreadyInDeal

        else ->
            RequestPlan.DealInProgress
    }

    /** The chat line without a name, e.g. "requested TI-84 calculator". The screen adds "You" or the person's name. */
    fun actionText(listing: Listing): String =
        if (listing.type == ListingType.WANTED) "offered to help with ${listing.title}"
        else "requested ${listing.title}"

    /** What the Chats list shows as the last message after a request. */
    fun previewText(listing: Listing): String = "New request: ${listing.title}"
}