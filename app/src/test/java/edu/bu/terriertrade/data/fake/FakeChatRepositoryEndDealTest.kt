package edu.bu.terriertrade.data.fake

import edu.bu.terriertrade.data.model.ListingStatus
import edu.bu.terriertrade.data.model.MessageType
import edu.bu.terriertrade.testing.TestAuthRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Finishing and cancelling a deal in FakeChatRepository: both people have to mark it done,
 * finishing sells everything except services, and cancelling sells nothing.
 * Uses TestAuthRepository so each test can switch to the other person for their tap.
 */
class FakeChatRepositoryEndDealTest {

    private val auth = TestAuthRepository().apply { currentUserId.value = SampleData.ME }
    private val listings = FakeListingRepository(auth)
    private val repo = FakeChatRepository(auth, listings)

    private fun signInAs(userId: String) {
        auth.currentUserId.value = userId
    }

    private suspend fun statusOf(listingId: String) = listings.observeListing(listingId).first()!!.status

    @Test
    fun markDone_byOnePerson_onlyRemembersTheirTap() = runTest {
        repo.markDealDone("c_nina").getOrThrow()

        val deal = repo.observeChat("c_nina").first()!!.deal!!
        assertEquals(listOf(SampleData.ME), deal.doneBy)
        assertEquals("marked the deal as done", repo.observeMessages("c_nina").first().last().text)
    }

    @Test
    fun markDone_byBoth_endsTheDeal_sellsItems_andAsksForReviews() = runTest {
        // Take Sam's changes first, so the deal is his calculator and his notes
        repo.acceptProposal("c_sam").getOrThrow()

        repo.markDealDone("c_sam").getOrThrow()
        signInAs("sam")
        repo.markDealDone("c_sam").getOrThrow()

        val chat = repo.observeChat("c_sam").first()!!
        assertNull(chat.deal)
        assertEquals("Deal done", chat.lastMessage)
        assertEquals(MessageType.REVIEW_PROMPT, repo.observeMessages("c_sam").first().last().type)
        assertEquals(ListingStatus.SOLD, statusOf("l3"))
        assertEquals(ListingStatus.SOLD, statusOf("l7"))
    }

    @Test
    fun markDone_byBoth_keepsServicesListed() = runTest {
        repo.markDealDone("c_nina").getOrThrow()
        signInAs("nina")
        repo.markDealDone("c_nina").getOrThrow()

        assertNull(repo.observeChat("c_nina").first()!!.deal)
        // Nina's tutoring can be booked again
        assertEquals(ListingStatus.ACTIVE, statusOf("l4"))
    }

    @Test
    fun markDone_withARemovedItem_failsWithTheReason_andSavesNothing() = runTest {
        listings.setStatus("l4", ListingStatus.REMOVED)

        val result = repo.markDealDone("c_nina")

        assertEquals("Take out items that are no longer available first.", result.exceptionOrNull()?.message)
        assertTrue(repo.observeChat("c_nina").first()!!.deal!!.doneBy.isEmpty())
    }

    @Test
    fun cancel_endsTheDeal_andSellsNothing() = runTest {
        repo.cancelDeal("c_sam").getOrThrow()

        val chat = repo.observeChat("c_sam").first()!!
        assertNull(chat.deal)
        assertEquals("Deal cancelled", chat.lastMessage)
        assertEquals("cancelled the deal", repo.observeMessages("c_sam").first().last().text)
        assertEquals(ListingStatus.ACTIVE, statusOf("l3"))
    }
}