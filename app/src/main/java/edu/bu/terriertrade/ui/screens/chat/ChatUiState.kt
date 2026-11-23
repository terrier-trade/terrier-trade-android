package edu.bu.terriertrade.ui.screens.chat

import edu.bu.terriertrade.data.deal.dealTotal
import edu.bu.terriertrade.data.model.Chat
import edu.bu.terriertrade.data.model.Listing
import edu.bu.terriertrade.data.model.Message
import edu.bu.terriertrade.data.model.MessageType
import edu.bu.terriertrade.data.model.User
import edu.bu.terriertrade.util.TimeFormat
import java.util.Locale

/** How a message is drawn: my bubble, their bubble, a grey event line, a deal suggestion card, or a review prompt. */
enum class MessageKind { MINE, THEIRS, EVENT, SUGGESTION, REVIEW_PROMPT }

data class MessageUi(
    val id: String,
    val text: String,
    val kind: MessageKind,
    val timeLabel: String = "",
    /** Suggestions only: true while this is still the proposal waiting for an answer. */
    val isActive: Boolean = false,
    /** Review prompts only: true once I've reviewed this finished deal. */
    val isRated: Boolean = false,
)

/** What the deal bar shows on its right side. */
enum class DealStatus {
    /** They suggested changes. */
    REVIEW_CHANGES,

    /** My changes are waiting, or I've marked it done and they haven't yet. */
    WAITING,

    /** They marked it done. My tap finishes the deal. */
    CONFIRM_DONE,

    OPEN,
}

data class DealBarUi(
    val itemCountLabel: String, // "1 item"
    val totalLabel: String,     // "You pay $40"
    val status: DealStatus,
)

/** Everything the chat screen needs once loaded. */
data class ChatContent(
    val chatId: String,
    val partnerId: String,
    val partnerName: String,
    val partnerFirstName: String,
    val partnerInitials: String,
    val partnerRatingLabel: String,
    val messages: List<MessageUi>,
    val dealBar: DealBarUi?,
)

fun totalLabel(total: Int, partnerFirstName: String): String = when {
    total > 0 -> "You pay \$$total"
    total < 0 -> "$partnerFirstName pays you \$${-total}"
    else -> "No money"
}

fun Message.toUi(
    myId: String,
    partnerFirstName: String,
    nowMillis: Long,
    pendingProposalId: String? = null,
    reviewedPrompts: Set<String> = emptySet(),
): MessageUi {    val time = TimeFormat.ago(createdAt, nowMillis)
    return when (type) {
        MessageType.TEXT -> MessageUi(
            id = id,
            text = text,
            kind = if (senderId == myId) MessageKind.MINE else MessageKind.THEIRS,
            timeLabel = time,
        )
        MessageType.EVENT -> MessageUi(id = id, text = text, kind = MessageKind.EVENT)
        // Lines about something a person did: "You requested Desk lamp", "Sam accepted the changes"
        MessageType.REQUEST, MessageType.ACTION -> MessageUi(
            id = id,
            text = (if (senderId == myId) "You" else partnerFirstName) + " " + text,
            kind = MessageKind.EVENT,
        )
        // Messages can't be edited (firestore.rules), so a card is "open" while its proposal is the pending one
        MessageType.SUGGESTION -> MessageUi(
            id = id,
            text = if (senderId == myId) "You suggested changes to the deal"
            else "$partnerFirstName suggested changes to the deal",
            kind = MessageKind.SUGGESTION,
            timeLabel = time,
            isActive = proposalId != null && proposalId == pendingProposalId,
        )
        // The end of a finished deal. It offers Rate until I've reviewed this deal.
        MessageType.REVIEW_PROMPT -> MessageUi(
            id = id,
            text = "Deal done with $partnerFirstName",
            kind = MessageKind.REVIEW_PROMPT,
            timeLabel = time,
            isRated = id in reviewedPrompts,
        )
    }
}

/** Puts the chat, the other person, the messages and the deal's listings together for the screen. */
fun buildChatContent(
    myId: String,
    chat: Chat,
    partner: User?,
    messages: List<Message>,
    dealListings: Map<String, Listing>,
    nowMillis: Long,
    reviewedPrompts: Set<String> = emptySet(),
): ChatContent {
    val firstName = partner?.firstName ?: "them"

    val dealBar = chat.deal?.let { deal ->
        val count = deal.items.size
        val pending = deal.pending
        DealBarUi(
            itemCountLabel = if (count == 1) "1 item" else "$count items",
            totalLabel = totalLabel(dealTotal(deal.items, dealListings, myId), firstName),
            status = when {
                pending != null && pending.fromUserId == myId -> DealStatus.WAITING
                pending != null -> DealStatus.REVIEW_CHANGES
                // I marked it done, so I'm waiting for them to confirm
                myId in deal.doneBy -> DealStatus.WAITING
                // They marked it done, so it's my turn
                deal.doneBy.isNotEmpty()  -> DealStatus.CONFIRM_DONE
                else -> DealStatus.OPEN
            },
        )
    }

    return ChatContent(
        chatId = chat.id,
        partnerId = chat.partnerId(myId),
        partnerName = partner?.name ?: "BU student",
        partnerFirstName = firstName,
        partnerInitials = partner?.initials ?: "?",
        partnerRatingLabel = partner?.takeIf { it.reviewCount > 0 }
            ?.let { String.format(Locale.US, "%.1f", it.rating) } ?: "New",
        messages = messages.map { it.toUi(myId, firstName, nowMillis, chat.deal?.pending?.id, reviewedPrompts) },
        dealBar = dealBar,
    )
}