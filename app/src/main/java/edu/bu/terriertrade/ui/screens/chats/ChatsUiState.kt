package edu.bu.terriertrade.ui.screens.chats

import edu.bu.terriertrade.data.model.Chat
import edu.bu.terriertrade.data.model.User
import edu.bu.terriertrade.util.TimeFormat

/** The three sections of the chats list, in display order. */
enum class ChatGroup(val title: String) {
    NEEDS_REPLY("Needs your reply"),
    ACTIVE("Active deals"),
    EARLIER("Earlier"),
}

/** Small tag shown under a chat's last message. */
enum class DealTag {
    REVIEW_CHANGES, // the other person suggested a change, you need to answer
    WAITING,        // you suggested a change or marked it done, waiting for them
    OPEN,           // deal in progress, nothing waiting
    CONFIRM_DONE,   // the other person marked the deal done, your tap finishes it
}

/** One row in the chats list, ready to display. */
data class ChatRowUi(
    val chatId: String,
    val partnerName: String,
    val partnerFirstName: String,
    val partnerInitials: String,
    val lastMessage: String,
    val timeLabel: String,
    val group: ChatGroup,
    val dealTag: DealTag?,
)

/** A group heading and the chats under it. */
data class ChatSection(
    val group: ChatGroup,
    val rows: List<ChatRowUi>,
)

/** Turns a Chat and the other person's profile into a row for the list. */
fun Chat.toRow(myId: String, partner: User?, nowMillis: Long): ChatRowUi {
    val pending = deal?.pending
    val theirChangesWait = pending != null && pending.fromUserId != myId
    // Same order as the deal bar: waiting changes first, then whoever tapped Mark as done
    val theyMarkedDone = deal != null && pending == null && deal.doneBy.isNotEmpty() && myId !in deal.doneBy
    val iMarkedDone = deal != null && myId in deal.doneBy

    val group = when {
        theirChangesWait || theyMarkedDone -> ChatGroup.NEEDS_REPLY
        deal != null -> ChatGroup.ACTIVE
        else -> ChatGroup.EARLIER
    }

    val tag = when {
        theirChangesWait -> DealTag.REVIEW_CHANGES
        theyMarkedDone -> DealTag.CONFIRM_DONE
        pending != null || iMarkedDone -> DealTag.WAITING
        deal != null -> DealTag.OPEN
        else -> null
    }

    return ChatRowUi(
        chatId = id,
        partnerName = partner?.name ?: "BU student",
        partnerFirstName = partner?.firstName ?: "them",
        partnerInitials = partner?.initials ?: "?",
        lastMessage = lastMessage,
        timeLabel = TimeFormat.ago(updatedAt, nowMillis),
        group = group,
        dealTag = tag,
    )
}

/** Splits rows into sections in the order Needs your reply, Active deals, Earlier. Empty sections are skipped. */
fun List<ChatRowUi>.toSections(): List<ChatSection> =
    ChatGroup.entries.mapNotNull { group ->
        val rows = filter { it.group == group }
        if (rows.isEmpty()) null else ChatSection(group, rows)
    }