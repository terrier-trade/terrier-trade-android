package edu.bu.terriertrade.ui.screens.chat

import edu.bu.terriertrade.data.model.Chat
import edu.bu.terriertrade.data.model.DealItem
import edu.bu.terriertrade.data.model.Listing
import edu.bu.terriertrade.data.model.ListingType
import edu.bu.terriertrade.data.model.Message
import edu.bu.terriertrade.data.model.MessageType
import edu.bu.terriertrade.data.model.User
import edu.bu.terriertrade.util.TimeFormat
import java.util.Locale

/** How a message is drawn: my bubble, their bubble, a grey event line, or a deal suggestion card. */
enum class MessageKind { MINE, THEIRS, EVENT, SUGGESTION }

data class MessageUi(
    val id: String,
    val text: String,
    val kind: MessageKind,
    val timeLabel: String = "",
)

/** What the deal bar shows on its right side. */
enum class DealStatus { REVIEW_CHANGES, WAITING, OPEN }

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

fun totalLabel(total: Int, partnerFirstName: String): String = when {
    total > 0 -> "You pay \$$total"
    total < 0 -> "$partnerFirstName pays you \$${-total}"
    else -> "No money"
}

fun Message.toUi(myId: String, partnerFirstName: String, nowMillis: Long): MessageUi {
    val time = TimeFormat.ago(createdAt, nowMillis)
    return when (type) {
        MessageType.TEXT -> MessageUi(
            id = id,
            text = text,
            kind = if (senderId == myId) MessageKind.MINE else MessageKind.THEIRS,
            timeLabel = time,
        )
        MessageType.EVENT -> MessageUi(id = id, text = text, kind = MessageKind.EVENT)
        MessageType.SUGGESTION -> MessageUi(
            id = id,
            text = if (senderId == myId) "You suggested changes to the deal"
            else "$partnerFirstName suggested changes to the deal",
            kind = MessageKind.SUGGESTION,
            timeLabel = time,
        )
        MessageType.REVIEW_PROMPT -> MessageUi(
            id = id,
            text = "Deal done. Rate $partnerFirstName",
            kind = MessageKind.EVENT,
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
): ChatContent {
    val firstName = partner?.firstName ?: "them"

    val dealBar = chat.deal?.let { deal ->
        val count = deal.items.size
        val pending = deal.pending
        DealBarUi(
            itemCountLabel = if (count == 1) "1 item" else "$count items",
            totalLabel = totalLabel(dealTotal(deal.items, dealListings, myId), firstName),
            status = when {
                pending == null -> DealStatus.OPEN
                pending.fromUserId == myId -> DealStatus.WAITING
                else -> DealStatus.REVIEW_CHANGES
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
        messages = messages.map { it.toUi(myId, firstName, nowMillis) },
        dealBar = dealBar,
    )
}