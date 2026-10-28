package edu.bu.terriertrade.data.model

/** One listing inside a deal and the price both people agreed on for it. */
data class DealItem(
    val listingId: String,
    val agreedPrice: Int,
)

enum class ProposalStatus {
    PENDING,
    ACCEPTED,
    DECLINED,

    /** The deal changed after this was sent, so it can no longer be accepted. */
    EXPIRED,
}

/**
 * A suggested change to a deal: the full item list the sender wants.
 * Only one can be PENDING per deal. [baseVersion] is the deal version it was made from.
 */
data class DealProposal(
    val id: String,
    val fromUserId: String,
    val items: List<DealItem>,
    val baseVersion: Int,
    val status: ProposalStatus = ProposalStatus.PENDING,
    val createdAt: Long = 0L,
)

/**
 * The trade inside a chat. The money total is not stored: it is worked out from [items].
 * [version] goes up on every change so stale proposals can be detected.
 * [doneBy] holds the ids of the people who tapped Mark as done.
 */
data class Deal(
    val items: List<DealItem> = emptyList(),
    val pending: DealProposal? = null,
    val doneBy: List<String> = emptyList(),
    val version: Int = 0,
)
