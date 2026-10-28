package edu.bu.terriertrade.data.repository

import edu.bu.terriertrade.data.model.Review
import kotlinx.coroutines.flow.Flow

interface ReviewRepository {
    /** Reviews written about [userId], newest first. */
    fun observeReviewsFor(userId: String): Flow<List<Review>>

    suspend fun addReview(review: Review): Result<Unit>
}
