package edu.bu.terriertrade.ui.screens.chat

import edu.bu.terriertrade.data.model.Chat
import edu.bu.terriertrade.data.model.Deal
import edu.bu.terriertrade.data.model.DealItem
import edu.bu.terriertrade.data.model.DealProposal
import org.junit.Assert.assertEquals
import org.junit.Test

/** Which pill the deal bar shows, so each person can tell whose turn it is at a glance. */
class DealBarStatusTest {

    private val me = "alex"
    private val sam = "sam"
    private val deal = Deal(items = listOf(DealItem("l3", 40)), version = 1)

    private fun statusFor(deal: Deal): DealStatus =
        buildChatContent(
            myId = me,
            chat = Chat("c1", listOf(me, sam), deal),
            partner = null,
            messages = emptyList(),
            dealListings = emptyMap(),
            nowMillis = 0L,
        ).dealBar!!.status

    @Test
    fun nothingWaiting_isOpen() {
        assertEquals(DealStatus.OPEN, statusFor(deal))
    }

    @Test
    fun samMarkedItDone_asksMeToConfirm() {
        assertEquals(DealStatus.CONFIRM_DONE, statusFor(deal.copy(doneBy = listOf(sam))))
    }

    @Test
    fun iMarkedItDone_waitsForSam() {
        assertEquals(DealStatus.WAITING, statusFor(deal.copy(doneBy = listOf(me))))
    }

    @Test
    fun waitingChanges_comeBeforeDoneTaps() {
        // I marked it done, then Sam suggested changes: answering them comes first
        val samsChanges = DealProposal("p1", sam, listOf(DealItem("l3", 35)), baseVersion = 1)

        assertEquals(DealStatus.REVIEW_CHANGES, statusFor(deal.copy(pending = samsChanges, doneBy = listOf(me))))
    }
}