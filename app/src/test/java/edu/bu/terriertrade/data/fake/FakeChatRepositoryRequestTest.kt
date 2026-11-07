package edu.bu.terriertrade.data.fake

import edu.bu.terriertrade.data.deal.RequestOutcome
import edu.bu.terriertrade.data.model.DealItem
import edu.bu.terriertrade.data.model.MessageType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Checks FakeChatRepository.requestListing() behaves like the real Firestore version:
 * a new request creates the chat, deal and REQUEST message, repeats change nothing,
 * and own listings fail before any chat is created. ViewModel tests rely on this fake.
 */
class FakeChatRepositoryRequestTest {

    private val auth = FakeAuthRepository()
    private val repo = FakeChatRepository(auth)

    @Test
    fun newPerson_startsChatWithDealAndRequestMessage() = runTest {
        val chair = SampleData.listing("l2")!! // Leo's desk chair, no chat with Leo yet

        val result = repo.requestListing(chair).getOrThrow()

        assertEquals(RequestOutcome.STARTED_DEAL, result.outcome)
        val chat = repo.observeChat(result.chatId).first()!!
        assertEquals(listOf(DealItem("l2", 40)), chat.deal!!.items)
        assertEquals("New request: Desk chair", chat.lastMessage)

        val request = repo.observeMessages(result.chatId).first().last()
        assertEquals(MessageType.REQUEST, request.type)
        assertEquals("requested Desk chair", request.text)
        assertEquals(SampleData.ME, request.senderId)
    }

    @Test
    fun itemAlreadyInDeal_reusesChatWithoutChanges() = runTest {
        val calculator = SampleData.listing("l3")!! // already in the deal with Sam

        val result = repo.requestListing(calculator).getOrThrow()

        assertEquals("c_sam", result.chatId)
        assertEquals(RequestOutcome.ALREADY_IN_DEAL, result.outcome)
    }

    @Test
    fun otherItemFromSameSeller_isDealInProgress() = runTest {
        val notes = SampleData.listing("l7")!! // Sam's notes, Sam's deal already has the calculator

        val result = repo.requestListing(notes).getOrThrow()

        assertEquals(RequestOutcome.DEAL_IN_PROGRESS, result.outcome)
        assertEquals(listOf(DealItem("l3", 40)), repo.observeChat("c_sam").first()!!.deal!!.items)
    }

    @Test
    fun ownListing_failsWithoutCreatingAChat() = runTest {
        val myLamp = SampleData.listing("m1")!!
        val chatsBefore = repo.observeChats().first().size

        val result = repo.requestListing(myLamp)

        assertTrue(result.isFailure)
        assertEquals(chatsBefore, repo.observeChats().first().size)
    }
}