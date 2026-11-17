package edu.bu.terriertrade.data.deal

import edu.bu.terriertrade.data.model.Deal
import edu.bu.terriertrade.data.model.DealItem
import edu.bu.terriertrade.data.model.DealProposal
import edu.bu.terriertrade.data.model.MessageType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Every branch of DealRules: when a proposal is allowed, what accepting and declining do to the deal,
 * and the version check that expires a proposal made from an older deal.
 */
class DealRulesTest {

    private val me = "alex"
    private val sam = "sam"

    private val calculator = DealItem("l3", 40)
    private val lamp = DealItem("m1", 12)

    private val deal = Deal(items = listOf(calculator), version = 1)

    private fun proposal(from: String, items: List<DealItem>, baseVersion: Int = 1) =
        DealProposal(id = "p1", fromUserId = from, items = items, baseVersion = baseVersion)

    private fun DealPlan.change(): DealPlan.Change {
        assertTrue("Expected a Change but was $this", this is DealPlan.Change)
        return this as DealPlan.Change
    }

    private fun DealPlan.reason(): String {
        assertTrue("Expected NotAllowed but was $this", this is DealPlan.NotAllowed)
        return (this as DealPlan.NotAllowed).reason
    }

    // Propose ---------------------------------------------------------------------------------------

    @Test
    fun propose_savesPendingProposal_fromTheCurrentVersion() {
        val change = DealRules.propose(deal, me, listOf(calculator, lamp), proposalId = "p9", now = 5L).change()

        val pending = change.deal.pending!!
        assertEquals(me, pending.fromUserId)
        assertEquals(listOf(calculator, lamp), pending.items)
        assertEquals(1, pending.baseVersion)
        // The deal itself doesn't change until the other person accepts
        assertEquals(listOf(calculator), change.deal.items)
        assertEquals(MessageType.SUGGESTION, change.messageType)
        assertEquals("p9", change.proposalId)
    }

    @Test
    fun propose_replacesMyOwnWaitingProposal() {
        val waiting = deal.copy(pending = proposal(from = me, items = listOf(calculator, lamp)))

        val change = DealRules.propose(waiting, me, listOf(DealItem("l3", 35)), "p2", 0L).change()

        assertEquals("p2", change.deal.pending!!.id)
    }

    @Test
    fun propose_isBlocked_whileTheirProposalWaits() {
        val theirs = deal.copy(pending = proposal(from = sam, items = listOf(lamp)))

        val reason = DealRules.propose(theirs, me, listOf(calculator, lamp), "p2", 0L).reason()

        assertEquals("Accept or decline their changes before suggesting new ones.", reason)
    }

    @Test
    fun propose_rejectsBadItemLists() {
        assertEquals("Request an item first to start a deal.", DealRules.propose(null, me, listOf(lamp), "p", 0L).reason())
        assertEquals("A deal needs at least one item.", DealRules.propose(deal, me, emptyList(), "p", 0L).reason())
        assertEquals(
            "Each item can only be in the deal once.",
            DealRules.propose(deal, me, listOf(lamp, DealItem("m1", 5)), "p", 0L).reason(),
        )
        assertEquals("Prices can't be negative.", DealRules.propose(deal, me, listOf(DealItem("l3", -1)), "p", 0L).reason())
    }

    @Test
    fun propose_withNoChanges_isNotAllowed() {
        assertEquals("Nothing has changed yet.", DealRules.propose(deal, me, listOf(calculator), "p", 0L).reason())
    }

    @Test
    fun propose_priceChangeOnly_counts() {
        val change = DealRules.propose(deal, me, listOf(DealItem("l3", 35)), "p", 0L).change()

        assertEquals(35, change.deal.pending!!.items.single().agreedPrice)
    }

    // Accept ----------------------------------------------------------------------------------------

    @Test
    fun accept_replacesItems_bumpsVersion_andResetsDone() {
        val waiting = deal.copy(pending = proposal(from = sam, items = listOf(calculator, lamp)), doneBy = listOf(me))

        val change = DealRules.accept(waiting, me).change()

        assertEquals(listOf(calculator, lamp), change.deal.items)
        assertEquals(2, change.deal.version)
        assertNull(change.deal.pending)
        assertTrue(change.deal.doneBy.isEmpty())
        assertEquals(MessageType.ACTION, change.messageType)
        assertEquals("accepted the changes", change.messageText)
    }

    @Test
    fun accept_myOwnProposal_isNotAllowed() {
        val mine = deal.copy(pending = proposal(from = me, items = listOf(lamp)))

        assertEquals("Wait for the other person to accept your changes.", DealRules.accept(mine, me).reason())
    }

    @Test
    fun accept_withNothingWaiting_isNotAllowed() {
        assertEquals("There are no changes to accept.", DealRules.accept(deal, me).reason())
    }

    @Test
    fun accept_proposalFromAnOlderVersion_expiresInstead() {
        // Sent when the deal was at version 1, but the deal has moved on to version 2
        val stale = deal.copy(version = 2, pending = proposal(from = sam, items = listOf(lamp), baseVersion = 1))

        val change = DealRules.accept(stale, me).change()

        assertEquals(listOf(calculator), change.deal.items)
        assertEquals(2, change.deal.version)
        assertNull(change.deal.pending)
        assertEquals(MessageType.EVENT, change.messageType)
    }

    // Decline and withdraw --------------------------------------------------------------------------

    @Test
    fun decline_theirProposal_keepsTheDeal() {
        val waiting = deal.copy(pending = proposal(from = sam, items = listOf(lamp)))

        val change = DealRules.decline(waiting, me).change()

        assertEquals(listOf(calculator), change.deal.items)
        assertEquals(1, change.deal.version)
        assertNull(change.deal.pending)
        assertEquals("declined the changes", change.messageText)
    }

    @Test
    fun decline_myOwnProposal_isAWithdraw() {
        val mine = deal.copy(pending = proposal(from = me, items = listOf(lamp)))

        val change = DealRules.decline(mine, me).change()

        assertEquals("withdrew the suggested changes", change.messageText)
        assertEquals("Changes withdrawn", change.preview)
    }

    @Test
    fun decline_withNothingWaiting_isNotAllowed() {
        assertEquals("There are no changes to decline.", DealRules.decline(deal, me).reason())
    }
}