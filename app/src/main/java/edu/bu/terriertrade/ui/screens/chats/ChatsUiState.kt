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
    WAITING,        // you suggested a change, waiting for them
    OPEN,           // deal in progress, nothing waiting
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

    val group = when {
        pending != null && pending.fromUserId != myId -> ChatGroup.NEEDS_REPLY
        deal != null -> ChatGroup.ACTIVE
        else -> ChatGroup.EARLIER
    }

    val tag = when {
        group == ChatGroup.NEEDS_REPLY -> DealTag.REVIEW_CHANGES
        pending != null -> DealTag.WAITING
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