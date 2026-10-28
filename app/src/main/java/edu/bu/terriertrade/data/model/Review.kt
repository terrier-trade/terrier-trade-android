package edu.bu.terriertrade.data.model

enum class ReviewTag(val label: String) {
    ON_TIME("On time"),
    AS_DESCRIBED("As described"),
    FRIENDLY("Friendly"),
    FAIR_PRICE("Fair price"),
}

/** Left after a finished deal. Only people who traded in [chatId] can review each other. */
data class Review(
    val id: String,
    val fromUserId: String,
    val toUserId: String,
    val chatId: String,
    val stars: Int,
    val tags: List<ReviewTag> = emptyList(),
    val text: String = "",
    val createdAt: Long = 0L,
)
