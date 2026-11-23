package edu.bu.terriertrade.data.review

import edu.bu.terriertrade.data.model.Review
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Which reviews can be saved, and the id that keeps it to one review per person per deal. */
class ReviewRulesTest {

    private val me = "alex"

    // My review of Sam for the deal that ended with message m9
    private val good = Review(id = "", fromUserId = me, toUserId = "sam", chatId = "c_sam", stars = 5, promptId = "m9")

    @Test
    fun goodReview_hasNoProblem() {
        assertNull(ReviewRules.problem(good, me))
    }

    @Test
    fun reviewId_isTheDealPlusTheReviewer() {
        assertEquals("m9_alex", ReviewRules.reviewId("m9", me))
    }

    @Test
    fun reviewWithoutAFinishedDeal_isRefused() {
        assertEquals("Reviews are only for finished deals.", ReviewRules.problem(good.copy(promptId = " "), me))
    }

    @Test
    fun reviewingMyself_isRefused() {
        assertEquals("You can't review yourself.", ReviewRules.problem(good.copy(toUserId = me), me))
    }

    @Test
    fun starsOutsideOneToFive_areRefused() {
        // 0 is what the sheet starts with before a star is tapped
        assertEquals("Tap a star to rate them.", ReviewRules.problem(good.copy(stars = 0), me))
        assertEquals("Tap a star to rate them.", ReviewRules.problem(good.copy(stars = 6), me))
    }

    @Test
    fun textLimit_ignoresSpacesAround() {
        val atLimit = "a".repeat(ReviewRules.MAX_TEXT_LENGTH)

        assertNull(ReviewRules.problem(good.copy(text = "  $atLimit  "), me))
        assertEquals(
            "Keep your review under 300 characters.",
            ReviewRules.problem(good.copy(text = atLimit + "a"), me),
        )
    }
}