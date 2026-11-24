package edu.bu.terriertrade.ui.screens.chats

import edu.bu.terriertrade.data.model.Chat
import edu.bu.terriertrade.data.model.Deal
import edu.bu.terriertrade.data.model.DealItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Which section and tag a chat gets in the Chats list, so it matches the deal bar inside the chat. */
class ChatsUiStateTest {

    private val me = "alex"
    private val sam = "sam"
    private val deal = Deal(items = listOf(DealItem("l3", 40)), version = 1)

    private fun row(deal: Deal?) = Chat("c1", listOf(me, sam), deal).toRow(me, partner = null, nowMillis = 0L)

    @Test
    fun samMarkedItDone_needsMyReply() {
        val row = row(deal.copy(doneBy = listOf(sam)))

        assertEquals(ChatGroup.NEEDS_REPLY, row.group)
        assertEquals(DealTag.CONFIRM_DONE, row.dealTag)
    }

    @Test
    fun iMarkedItDone_waitsForSam() {
        val row = row(deal.copy(doneBy = listOf(me)))

        assertEquals(ChatGroup.ACTIVE, row.group)
        assertEquals(DealTag.WAITING, row.dealTag)
    }

    @Test
    fun openDeal_isActive() {
        val row = row(deal)

        assertEquals(ChatGroup.ACTIVE, row.group)
        assertEquals(DealTag.OPEN, row.dealTag)
    }

    @Test
    fun noDeal_isEarlier_withoutATag() {
        val row = row(null)

        assertEquals(ChatGroup.EARLIER, row.group)
        assertNull(row.dealTag)
    }
}
