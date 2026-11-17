package edu.bu.terriertrade.data.deal

import edu.bu.terriertrade.data.model.Deal
import edu.bu.terriertrade.data.model.DealItem
import edu.bu.terriertrade.data.model.DealProposal
import edu.bu.terriertrade.data.model.MessageType

/** What a change to the deal should do, decided before anything is saved. */
sealed interface DealPlan {
    /**
     * Save [deal] on the chat and post one message, together in one transaction.
     * [messageText] for ACTION has no name: the screen adds "You" or the person's name.
     * [preview] is what the Chats list shows as the last message.
     * [proposalId] links a SUGGESTION message to its proposal.
     */
    data class Change(
        val deal: Deal,
        val messageType: MessageType,
        val messageText: String,
        val preview: String,
        val proposalId: String? = null,
    ) : DealPlan

    data class NotAllowed(val reason: String) : DealPlan
}

/**
 * The rules for suggesting, accepting, declining and withdrawing changes to a deal.
 * Only one proposal waits at a time. Accepting it replaces the deal's items and bumps the version,
 * so a proposal made from an older version can be spotted and expired.
 * Plain Kotlin with no Firebase, so every case is covered by fast unit tests.
 */
object DealRules {

    /** Suggests [items] as the new deal. Replaces my own waiting proposal, if there is one. */
    fun propose(deal: Deal?, myId: String, items: List<DealItem>, proposalId: String, now: Long): DealPlan {
        if (deal == null) return DealPlan.NotAllowed("Request an item first to start a deal.")
        val pending = deal.pending

        val problem = when {
            pending != null && pending.fromUserId != myId -> "Accept or decline their changes before suggesting new ones."
            items.isEmpty() -> "A deal needs at least one item."
            items.map { it.listingId }.toSet().size != items.size -> "Each item can only be in the deal once."
            items.any { it.agreedPrice < 0 } -> "Prices can't be negative."
            sameItems(items, deal.items) -> "Nothing has changed yet."
            else -> null
        }
        if (problem != null) return DealPlan.NotAllowed(problem)

        val proposal = DealProposal(
            id = proposalId,
            fromUserId = myId,
            items = items,
            baseVersion = deal.version,
            createdAt = now,
        )
        return DealPlan.Change(
            deal = deal.copy(pending = proposal),
            messageType = MessageType.SUGGESTION,
            messageText = "suggested changes to the deal",
            preview = "Changes suggested",
            proposalId = proposalId,
        )
    }

    /** Accepts the other person's waiting proposal. */
    fun accept(deal: Deal?, myId: String): DealPlan {
        if (deal == null) return DealPlan.NotAllowed(NOTHING_TO_ACCEPT)
        val pending = deal.pending ?: return DealPlan.NotAllowed(NOTHING_TO_ACCEPT)
        if (pending.fromUserId == myId) return DealPlan.NotAllowed("Wait for the other person to accept your changes.")

        // The deal changed after this was sent, so accepting it would undo someone's change
        if (pending.baseVersion != deal.version) {
            return DealPlan.Change(
                deal = deal.copy(pending = null),
                messageType = MessageType.EVENT,
                messageText = "Those changes expired because the deal changed. Suggest them again.",
                preview = "Changes expired",
            )
        }

        return DealPlan.Change(
            // New items, so anyone who already tapped Mark as done has to confirm again
            deal = deal.copy(items = pending.items, pending = null, doneBy = emptyList(), version = deal.version + 1),
            messageType = MessageType.ACTION,
            messageText = "accepted the changes",
            preview = "Changes accepted",
        )
    }

    /** Declines the other person's proposal, or withdraws my own. The deal stays as it was. */
    fun decline(deal: Deal?, myId: String): DealPlan {
        if (deal == null) return DealPlan.NotAllowed(NOTHING_TO_DECLINE)
        val pending = deal.pending ?: return DealPlan.NotAllowed(NOTHING_TO_DECLINE)
        val withdrawing = pending.fromUserId == myId

        return DealPlan.Change(
            deal = deal.copy(pending = null),
            messageType = MessageType.ACTION,
            messageText = if (withdrawing) "withdrew the suggested changes" else "declined the changes",
            preview = if (withdrawing) "Changes withdrawn" else "Changes declined",
        )
    }

    /** Same listings at the same prices, in any order. */
    private fun sameItems(a: List<DealItem>, b: List<DealItem>): Boolean =
        a.sortedBy { it.listingId } == b.sortedBy { it.listingId }

    private const val NOTHING_TO_ACCEPT = "There are no changes to accept."
    private const val NOTHING_TO_DECLINE = "There are no changes to decline."
}