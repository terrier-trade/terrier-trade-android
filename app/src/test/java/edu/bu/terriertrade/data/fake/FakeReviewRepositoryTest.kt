package edu.bu.terriertrade.data.fake

import edu.bu.terriertrade.data.model.Review
import edu.bu.terriertrade.data.model.ReviewTag
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * FakeReviewRepository follows the same rules as Firestore: it fills in who and when,
 * remembers which deals I've reviewed, and allows one review per person per finished deal.
 */
class FakeReviewRepositoryTest {

    private val repo = FakeReviewRepository(FakeAuthRepository())

    // My review of Sam for the deal that ended with message m9. Who and when are filled in on save.
    private val review = Review(
        id = "", fromUserId = "", toUserId = "sam", chatId = "c_sam",
        stars = 4, tags = listOf(ReviewTag.FAIR_PRICE), text = "Smooth trade", promptId = "m9",
    )

    private suspend fun savedReview() = repo.observeReviewsFor("sam").first().first { it.id == "m9_alex" }

    @Test
    fun addReview_fillsInWhoAndWhen_andShowsOnTheirProfile() = runTest {
        repo.addReview(review).getOrThrow()

        val saved = savedReview()
        assertEquals(SampleData.ME, saved.fromUserId)
        assertEquals(4, saved.stars)
        assertTrue(saved.createdAt > 0)
    }

    @Test
    fun reviewedPrompts_listTheDealsIveReviewed_inThatChatOnly() = runTest {
        assertTrue(repo.observeMyReviewedPrompts("c_sam").first().isEmpty())

        repo.addReview(review).getOrThrow()

        assertEquals(setOf("m9"), repo.observeMyReviewedPrompts("c_sam").first())
        assertTrue(repo.observeMyReviewedPrompts("c_nina").first().isEmpty())
    }

    @Test
    fun reviewingTheSameDealTwice_fails_andKeepsTheFirstReview() = runTest {
        repo.addReview(review).getOrThrow()

        val second = repo.addReview(review.copy(stars = 1))

        assertEquals("You've already reviewed this deal.", second.exceptionOrNull()?.message)
        assertEquals(4, savedReview().stars)
    }

    @Test
    fun badReview_failsWithTheRulesReason() = runTest {
        val result = repo.addReview(review.copy(stars = 0))

        assertEquals("Tap a star to rate them.", result.exceptionOrNull()?.message)
    }
}