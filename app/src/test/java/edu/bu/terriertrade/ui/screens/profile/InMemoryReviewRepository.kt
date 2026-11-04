package edu.bu.terriertrade.ui.screens.profile

import edu.bu.terriertrade.data.model.Review
import edu.bu.terriertrade.data.repository.ReviewRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/** Reviews kept in memory for profile tests. */
class InMemoryReviewRepository(initial: List<Review> = emptyList()) : ReviewRepository {
    val reviews = MutableStateFlow(initial)

    override fun observeReviewsFor(userId: String): Flow<List<Review>> =
        reviews.map { all -> all.filter { it.toUserId == userId }.sortedByDescending { it.createdAt } }

    override suspend fun addReview(review: Review): Result<Unit> {
        reviews.update { it + review }
        return Result.success(Unit)
    }
}
