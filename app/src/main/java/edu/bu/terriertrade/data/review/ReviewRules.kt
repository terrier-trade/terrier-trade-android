package edu.bu.terriertrade.data.review

import edu.bu.terriertrade.data.model.Review

/**
 * The rules for leaving a review after a finished deal.
 * Each review is tied to the chat's "Deal done" message, so each person can review each finished deal once.
 * Plain Kotlin with no Firebase, so every case is covered by fast unit tests.
 */
object ReviewRules {

    const val MAX_TEXT_LENGTH = 300

    /**
     * The review's document id: the "Deal done" message plus the reviewer.
     * A second review of the same deal lands on the same id, which is how "only once" is enforced.
     */
    fun reviewId(promptId: String, fromUserId: String): String = "${promptId}_$fromUserId"

    /** What's wrong with [review] written by [myId], or null when it can be saved. */
    fun problem(review: Review, myId: String): String? = when {
        review.promptId.isBlank() -> "Reviews are only for finished deals."
        review.toUserId == myId -> "You can't review yourself."
        review.stars !in 1..5 -> "Tap a star to rate them."
        review.text.trim().length > MAX_TEXT_LENGTH -> "Keep your review under $MAX_TEXT_LENGTH characters."
        else -> null
    }
}