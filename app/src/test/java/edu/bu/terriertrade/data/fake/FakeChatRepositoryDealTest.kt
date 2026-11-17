package edu.bu.terriertrade.data.fake

import edu.bu.terriertrade.data.model.DealItem
import edu.bu.terriertrade.data.model.MessageType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Checks FakeChatRepository's deal changes behave like the Firestore version: each one saves the deal
 * and one chat message together, and a change DealRules refuses fails with its reason and saves nothing.
 * The deal window's ViewModel tests rely on this fake.
 */
class FakeChatRepositoryDealTest {

    private val auth = FakeAuthRepository()
    private val repo = FakeChatRepository(auth)

    @Test
    fun propose_savesProposal_andSuggestionMessagePointingAtIt() = runTest {
        val items = listOf(DealItem("l4", 20), DealItem("m1", 12)) // Nina's tutoring plus my desk lamp

        repo.proposeDeal("c_nina", items).getOrThrow()

        val chat = repo.observeChat("c_nina").first()!!
        val pending = chat.deal!!.pending!!
        assertEquals(SampleData.ME, pending.fromUserId)
        assertEquals(items, pending.items)
        assertEquals("Changes suggested", chat.lastMessage)

        val message = repo.observeMessages("c_nina").first().last()
        assertEquals(MessageType.SUGGESTION, message.type)
        assertEquals(pending.id, message.proposalId)
    }

    @Test
    fun accept_appliesSamsChanges() = runTest {
        repo.acceptProposal("c_sam").getOrThrow()

        val deal = repo.observeChat("c_sam").first()!!.deal!!
        assertEquals(listOf(DealItem("l3", 40), DealItem("l7", 10)), deal.items)
        assertEquals(2, deal.version)
        assertNull(deal.pending)

        val message = repo.observeMessages("c_sam").first().last()
        assertEquals(MessageType.ACTION, message.type)
        assertEquals("accepted the changes", message.text)
    }

    @Test
    fun decline_keepsTheDealAsItWas() = runTest {
        repo.declineProposal("c_sam").getOrThrow()

        val deal = repo.observeChat("c_sam").first()!!.deal!!
        assertEquals(listOf(DealItem("l3", 40)), deal.items)
        assertEquals(1, deal.version)
        assertNull(deal.pending)
        assertEquals("Changes declined", repo.observeChat("c_sam").first()!!.lastMessage)
    }

    @Test
    fun propose_whileSamsChangesWait_failsWithTheReason_andSavesNothing() = runTest {
        val chatBefore = repo.observeChat("c_sam").first()
        val messagesBefore = repo.observeMessages("c_sam").first()

        val result = repo.proposeDeal("c_sam", listOf(DealItem("l3", 35)))

        assertEquals("Accept or decline their changes before suggesting new ones.", result.exceptionOrNull()?.message)
        assertEquals(chatBefore, repo.observeChat("c_sam").first())
        assertEquals(messagesBefore, repo.observeMessages("c_sam").first())
    }

    @Test
    fun propose_inChatWithoutADeal_fails() = runTest {
        assertTrue(repo.proposeDeal("c_maya", listOf(DealItem("m1", 12))).isFailure)
    }
}