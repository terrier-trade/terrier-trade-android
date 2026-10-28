package edu.bu.terriertrade.data.model

/** A conversation between two students. Each chat holds at most one open deal. */
data class Chat(
    val id: String,
    val participantIds: List<String>,
    val deal: Deal? = null,
    val lastMessage: String = "",
    val updatedAt: Long = 0L,
) {
    /** The other person in the chat. */
    fun partnerId(myId: String): String = participantIds.firstOrNull { it != myId } ?: myId
}

enum class MessageType {
    /** Normal message typed by a student. */
    TEXT,

    /** Grey system line such as "You requested TI-84 calculator". */
    EVENT,

    /** A deal change waiting for accept or decline. Links to [Message.proposalId]. */
    SUGGESTION,

    /** Prompt to rate the other person after a finished deal. */
    REVIEW_PROMPT,
}

data class Message(
    val id: String,
    val chatId: String,
    val senderId: String,
    val type: MessageType,
    val text: String = "",
    val proposalId: String? = null,
    val createdAt: Long = 0L,
)
