package edu.bu.terriertrade.ui.screens.chat

import edu.bu.terriertrade.data.deal.DealPlan
import edu.bu.terriertrade.data.deal.DealRules
import edu.bu.terriertrade.data.deal.dealTotal
import edu.bu.terriertrade.data.deal.receivesItem
import edu.bu.terriertrade.data.model.Category
import edu.bu.terriertrade.data.model.Deal
import edu.bu.terriertrade.data.model.DealItem
import edu.bu.terriertrade.data.model.Listing
import edu.bu.terriertrade.data.model.ListingType

/** What the deal window lets you do right now. */
enum class DealMode {
    /** Nothing is waiting: change items and prices, then send them as a suggestion. */
    EDIT,

    /** The other person suggested changes: accept or decline them. */
    REVIEW,

    /** You suggested changes: wait for the other person, or withdraw them. */
    WAITING,
}

/** How a row compares with the deal as it is now. */
enum class RowChange { SAME, ADDED, REMOVED, PRICE_CHANGED }

/** One item in the deal window. */
data class DealRowUi(
    val listingId: String,
    val title: String,
    /** "You get" or "You give". Blank when the listing can't be found. */
    val directionLabel: String,
    val price: Int,
    /** The deal's current price for this item, when this row changes it. Shown struck through. */
    val oldPrice: Int? = null,
    val change: RowChange = RowChange.SAME,
    /** False when the listing was sold or removed after it went into the deal. */
    val isAvailable: Boolean = true,
    /** True for my own listings: the edit view lists them under Your listings. */
    val isMine: Boolean = false,
    /** True when I end up with this item. The sheet groups rows into You get and You give. */
    val youGet: Boolean = false,
    /** The price on the listing, shown struck through when the agreed price is different. */
    val listedPrice: Int? = null,
    val category: Category = Category.OTHER,
    /** A Looking for post: its owner receives, the other person provides. Labelled so that flip is clear. */
    val isWanted: Boolean = false,
)

/** A listing either person could add to the deal. */
data class AddableListingUi(
    val listingId: String,
    val title: String,
    val price: Int,
    val isMine: Boolean,
    val category: Category = Category.OTHER,
    /** A Looking for post: its owner receives, the other person provides. Labelled so that flip is clear. */
    val isWanted: Boolean = false,
)

/** Everything the deal window draws. */
data class DealWindowUi(
    /** For "Sam's listings" and "Send to Sam". */
    val partnerFirstName: String,
    val mode: DealMode,
    val title: String,
    val subtitle: String,
    val rows: List<DealRowUi>,
    /** The money once these rows go through, like "You pay $50". */
    val totalLabel: String,
    /** The deal's total as it is now. Only set when the rows change something. */
    val currentTotalLabel: String?,
    /** Listings that can be added. Only filled in EDIT mode. */
    val addable: List<AddableListingUi>,
    /** EDIT mode only: true when the draft is a change DealRules would accept. */
    val canSend: Boolean,
    /** True once I've tapped Mark as done. Accepting new changes clears it. */
    val doneByMe: Boolean = false,
    /** True once the other person has tapped Mark as done, so my tap would finish the deal. */
    val doneByPartner: Boolean = false,
    /** EDIT mode only: true when DealRules would let me mark the deal as done right now. */
    val canMarkDone: Boolean = false,
    /**
     * Why Mark as done is off, like "Take out items that are no longer available first."
     * Null when it's on, or when I've already tapped it (the sheet says "Waiting for Sam" instead).
     */
    val markDoneBlockedReason: String? = null,
)

/**
 * Builds the deal window from the deal and the listings it mentions.
 * [draft] is what you're editing in EDIT mode, or null when you haven't changed anything.
 * [listings] must hold every listing in the deal, its pending proposal and the draft.
 * [myListings] and [partnerListings] are what can be added.
 */
fun buildDealWindow(
    myId: String,
    partnerFirstName: String,
    deal: Deal,
    draft: List<DealItem>?,
    listings: Map<String, Listing>,
    myListings: List<Listing>,
    partnerListings: List<Listing>,
): DealWindowUi {
    val pending = deal.pending
    val mode = when {
        pending == null -> DealMode.EDIT
        pending.fromUserId == myId -> DealMode.WAITING
        else -> DealMode.REVIEW
    }

    // EDIT shows my draft (or the deal untouched). REVIEW and WAITING show the suggested items.
    val shown = pending?.items ?: draft ?: deal.items

    val current = deal.items.associateBy { it.listingId }
    val rows = shown.map { item ->
        val before = current[item.listingId]
        dealRow(
            item = item,
            listing = listings[item.listingId],
            myId = myId,
            change = when {
                before == null -> RowChange.ADDED
                before.agreedPrice != item.agreedPrice -> RowChange.PRICE_CHANGED
                else -> RowChange.SAME
            },
            oldPrice = before?.agreedPrice?.takeIf { it != item.agreedPrice },
        )
    }.toMutableList()

    // A suggestion also lists what it takes out, so nothing disappears without the other person seeing it
    if (mode != DealMode.EDIT) {
        val kept = shown.map { it.listingId }.toSet()
        deal.items.filter { it.listingId !in kept }.forEach { item ->
            rows += dealRow(item, listings[item.listingId], myId, RowChange.REMOVED, oldPrice = null)
        }
    }

    val changed = shown.sortedBy { it.listingId } != deal.items.sortedBy { it.listingId }

    val inDeal = shown.map { it.listingId }.toSet()
    val addable = if (mode != DealMode.EDIT) {
        emptyList()
    } else {
        // Their listings first: usually you're adding something you want from them
        (partnerListings.map { it to false } + myListings.map { it to true })
            .filter { (listing, _) -> listing.isActive && listing.id !in inDeal }
            .map { (listing, isMine) ->
                AddableListingUi(listing.id, listing.title, listing.price, isMine, listing.category, listing.type == ListingType.WANTED)
            }
    }

    // Reuse the real rules, so the Send button can never allow something the repository would refuse
    val canSend = mode == DealMode.EDIT && draft != null &&
            DealRules.propose(deal, myId, draft, proposalId = "", now = 0L) is DealPlan.Change

    // Same idea as canSend: ask the real rules, so the button matches what the repository allows.
    // Only offered while nothing is waiting, since waiting changes have to be answered first.
    val markDone = if (mode == DealMode.EDIT) DealRules.markDone(deal, myId, listings) else null
    val doneByMe = myId in deal.doneBy

    val (title, subtitle) = when (mode) {
        DealMode.EDIT -> "Change the deal" to
                "Add or remove items and set prices. $partnerFirstName has to accept before anything changes."
        DealMode.REVIEW -> "$partnerFirstName suggested changes" to
                "Accept to update the deal, or decline to keep it as it is."
        DealMode.WAITING -> "Waiting for $partnerFirstName" to
                "You suggested these changes. You can withdraw them until $partnerFirstName answers."
    }

    return DealWindowUi(
        partnerFirstName = partnerFirstName,
        mode = mode,
        title = title,
        subtitle = subtitle,
        rows = rows,
        totalLabel = totalLabel(dealTotal(shown, listings, myId), partnerFirstName),
        currentTotalLabel = if (changed) totalLabel(dealTotal(deal.items, listings, myId), partnerFirstName) else null,
        addable = addable,
        canSend = canSend,
        doneByMe = doneByMe,
        doneByPartner = deal.doneBy.any { it != myId },
        canMarkDone = markDone != null && markDone !is DealPlan.NotAllowed,
        // Once I've tapped it, "already marked" isn't a problem worth showing
        markDoneBlockedReason = (markDone as? DealPlan.NotAllowed)?.reason?.takeUnless { doneByMe },
    )
}

private fun dealRow(item: DealItem, listing: Listing?, myId: String, change: RowChange, oldPrice: Int?) = DealRowUi(
    listingId = item.listingId,
    title = listing?.title ?: "Listing no longer available",
    directionLabel = when {
        listing == null -> ""
        receivesItem(listing, myId) -> "You get"
        else -> "You give"
    },
    price = item.agreedPrice,
    oldPrice = oldPrice,
    change = change,
    isAvailable = listing?.isActive == true,
    isMine = listing?.ownerId == myId,
    youGet = listing != null && receivesItem(listing, myId),
    listedPrice = listing?.price,
    category = listing?.category ?: Category.OTHER,
    isWanted = listing?.type == ListingType.WANTED,
)

/** The deal sheet: closed, or open with its window while a change is saving or after it failed. */
data class DealSheetState(
    val isOpen: Boolean = false,
    /** Null for a moment while the listings load. */
    val window: DealWindowUi? = null,
    val isSaving: Boolean = false,
    val error: String? = null,
)