package edu.bu.terriertrade.ui.screens.chat

import edu.bu.terriertrade.data.model.Deal
import edu.bu.terriertrade.data.model.DealItem
import edu.bu.terriertrade.data.model.DealProposal
import edu.bu.terriertrade.data.model.Listing
import edu.bu.terriertrade.data.model.ListingStatus
import edu.bu.terriertrade.data.model.ListingType
import edu.bu.terriertrade.data.model.Message
import edu.bu.terriertrade.data.model.MessageType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The deal window in each mode: what rows it shows, how a suggestion is compared with the current deal,
 * which way each item goes, the totals, what can be added, and when Send is allowed.
 */
class DealWindowUiStateTest {

    private val me = "alex"
    private val sam = "sam"

    private val calculator = listing("l3", owner = sam, title = "TI-84 calculator", price = 45)
    private val notes = listing("l7", owner = sam, title = "PY 211 notes", price = 10)
    private val lamp = listing("m1", owner = me, title = "Desk lamp", price = 12)
    private val all = listOf(calculator, notes, lamp).associateBy { it.id }

    // The calculator is in the deal at an agreed $40
    private val deal = Deal(items = listOf(DealItem("l3", 40)), version = 1)

    private fun listing(id: String, owner: String, title: String, price: Int, status: ListingStatus = ListingStatus.ACTIVE) =
        Listing(id = id, ownerId = owner, type = ListingType.ITEM, title = title, price = price, status = status)

    private fun window(
        deal: Deal = this.deal,
        draft: List<DealItem>? = null,
        listings: Map<String, Listing> = all,
    ) = buildDealWindow(
        myId = me,
        partnerFirstName = "Sam",
        deal = deal,
        draft = draft,
        listings = listings,
        myListings = listOf(lamp),
        partnerListings = listOf(calculator, notes),
    )

    @Test
    fun edit_withoutChanges_showsTheDealAsItIs_andCantSend() {
        val ui = window()

        assertEquals(DealMode.EDIT, ui.mode)
        val row = ui.rows.single()
        assertEquals("You get", row.directionLabel)
        assertEquals(40, row.price)
        assertEquals(RowChange.SAME, row.change)
        assertEquals("You pay \$40", ui.totalLabel)
        assertNull(ui.currentTotalLabel)
        // Their notes first, then my lamp. The calculator is already in the deal.
        assertEquals(listOf("l7" to false, "m1" to true), ui.addable.map { it.listingId to it.isMine })
        assertFalse(ui.canSend)
    }

    @Test
    fun edit_addingMyLamp_marksItNew_updatesTotals_andCanSend() {
        val ui = window(draft = listOf(DealItem("l3", 40), DealItem("m1", 12)))

        val lampRow = ui.rows.single { it.listingId == "m1" }
        assertEquals(RowChange.ADDED, lampRow.change)
        assertEquals("You give", lampRow.directionLabel)
        // I get the $40 calculator and give a $12 lamp
        assertEquals("You pay \$28", ui.totalLabel)
        assertEquals("You pay \$40", ui.currentTotalLabel)
        assertFalse(ui.addable.any { it.listingId == "m1" })
        assertTrue(ui.canSend)
    }

    @Test
    fun edit_changingAPrice_showsTheOldOne() {
        val row = window(draft = listOf(DealItem("l3", 35))).rows.single()

        assertEquals(RowChange.PRICE_CHANGED, row.change)
        assertEquals(35, row.price)
        assertEquals(40, row.oldPrice)
    }

    @Test
    fun review_showsTheirSuggestion_includingWhatItRemoves() {
        val theirs = deal.copy(pending = DealProposal("p1", sam, listOf(DealItem("l7", 10)), baseVersion = 1))

        val ui = window(deal = theirs)

        assertEquals(DealMode.REVIEW, ui.mode)
        assertEquals("Sam suggested changes", ui.title)
        assertEquals(RowChange.ADDED, ui.rows.single { it.listingId == "l7" }.change)
        assertEquals(RowChange.REMOVED, ui.rows.single { it.listingId == "l3" }.change)
        assertEquals("You pay \$10", ui.totalLabel)
        assertEquals("You pay \$40", ui.currentTotalLabel)
        assertTrue(ui.addable.isEmpty())
        assertFalse(ui.canSend)
    }

    @Test
    fun waiting_whenISuggestedChanges() {
        val mine = deal.copy(pending = DealProposal("p1", me, listOf(DealItem("l3", 35)), baseVersion = 1))

        val ui = window(deal = mine)

        assertEquals(DealMode.WAITING, ui.mode)
        assertEquals("Waiting for Sam", ui.title)
    }

    @Test
    fun soldOrMissingListing_isShownAsUnavailable() {
        val sold = all + ("l3" to calculator.copy(status = ListingStatus.SOLD))
        assertFalse(window(listings = sold).rows.single().isAvailable)

        val missing = window(listings = all - "l3").rows.single()
        assertFalse(missing.isAvailable)
        assertEquals("Listing no longer available", missing.title)
    }

    @Test
    fun suggestionCard_isActiveOnlyWhileItsProposalIsPending() {
        val card = Message("a6", "c_sam", sam, MessageType.SUGGESTION, proposalId = "p1")

        assertTrue(card.toUi(me, "Sam", 0L, pendingProposalId = "p1").isActive)
        assertFalse(card.toUi(me, "Sam", 0L, pendingProposalId = "p2").isActive)
        assertFalse(card.toUi(me, "Sam", 0L, pendingProposalId = null).isActive)
    }

    // Mark as done ----------------------------------------------------------------------------------

    @Test
    fun openDeal_canBeMarkedDone() {
        val ui = window()

        assertTrue(ui.canMarkDone)
        assertNull(ui.markDoneBlockedReason)
        assertFalse(ui.doneByMe)
        assertFalse(ui.doneByPartner)
    }

    @Test
    fun afterSamMarksDone_myTapWouldFinishIt() {
        val ui = window(deal = deal.copy(doneBy = listOf(sam)))

        assertTrue(ui.doneByPartner)
        assertFalse(ui.doneByMe)
        assertTrue(ui.canMarkDone)
    }

    @Test
    fun afterIMarkDone_itsOff_withoutAReason() {
        val ui = window(deal = deal.copy(doneBy = listOf(me)))

        assertTrue(ui.doneByMe)
        assertFalse(ui.canMarkDone)
        // The sheet shows "Waiting for Sam" instead of an error
        assertNull(ui.markDoneBlockedReason)
    }

    @Test
    fun removedItem_blocksMarkDone_withTheReason() {
        val gone = all + ("l3" to calculator.copy(status = ListingStatus.REMOVED))

        val ui = window(listings = gone)

        assertFalse(ui.canMarkDone)
        assertEquals("Take out items that are no longer available first.", ui.markDoneBlockedReason)
    }

    @Test
    fun waitingChanges_dontOfferMarkDone() {
        val samsChanges = DealProposal("p1", sam, listOf(DealItem("l3", 35)), baseVersion = 1)

        val ui = window(deal = deal.copy(pending = samsChanges))

        assertEquals(DealMode.REVIEW, ui.mode)
        assertFalse(ui.canMarkDone)
        assertNull(ui.markDoneBlockedReason)
    }
}