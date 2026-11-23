package edu.bu.terriertrade.data.repository

import edu.bu.terriertrade.data.model.Review
import kotlinx.coroutines.flow.Flow

interface ReviewRepository {
    /** Reviews written about [userId], newest first. */
    fun observeReviewsFor(userId: String): Flow<List<Review>>

    /** The finished deals in [chatId] I've already reviewed, as their "Deal done" message ids. */
    fun observeMyReviewedPrompts(chatId: String): Flow<Set<String>>

    /**
     * Saves my review of a finished deal. Fills in the id, the reviewer and the time.
     * Fails with ReviewRules' reason, or if I've already reviewed this deal.
     */
    suspend fun addReview(review: Review): Result<Unit>
}