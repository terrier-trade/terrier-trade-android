package edu.bu.terriertrade.data.deal

import edu.bu.terriertrade.data.model.Deal
import edu.bu.terriertrade.data.model.DealItem
import edu.bu.terriertrade.data.model.Listing
import edu.bu.terriertrade.data.model.ListingStatus
import edu.bu.terriertrade.data.model.ListingType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Covers every branch of RequestRules.plan(): starting a deal, own or sold listings,
 * an item already in the deal, and a deal already in progress with other items.
 * These rules decide what a Request does, so each case has its own test.
 */
class RequestRulesTest {

    private val me = "alex"

    private fun listing(
        id: String = "l3",
        ownerId: String = "sam",
        type: ListingType = ListingType.ITEM,
        status: ListingStatus = ListingStatus.ACTIVE,
    ) = Listing(id = id, ownerId = ownerId, type = type, title = "TI-84 calculator", price = 45, status = status)

    @Test
    fun noDeal_startsDealWithListedPrice() {
        val plan = RequestRules.plan(listing(), me, currentDeal = null)

        assertTrue(plan is RequestPlan.StartDeal)
        val start = plan as RequestPlan.StartDeal
        assertEquals(listOf(DealItem("l3", 45)), start.deal.items)
        assertEquals(1, start.deal.version)
        assertEquals("requested TI-84 calculator", start.actionText)
    }

    @Test
    fun ownListing_isNotAllowed() {
        val plan = RequestRules.plan(listing(ownerId = me), me, currentDeal = null)

        assertEquals(RequestPlan.NotAllowed("This is your own listing."), plan)
    }

    @Test
    fun soldListing_isNotAllowed() {
        val plan = RequestRules.plan(listing(status = ListingStatus.SOLD), me, currentDeal = null)

        assertTrue(plan is RequestPlan.NotAllowed)
    }

    @Test
    fun itemAlreadyInDeal_justOpensChat() {
        val deal = Deal(items = listOf(DealItem("l3", 40)), version = 2)

        val plan = RequestRules.plan(listing(), me, deal)

        assertEquals(RequestPlan.AlreadyInDeal, plan)
    }

    @Test
    fun differentItemInDeal_isDealInProgress() {
        val deal = Deal(items = listOf(DealItem("l7", 10)), version = 1)

        val plan = RequestRules.plan(listing(), me, deal)

        assertEquals(RequestPlan.DealInProgress, plan)
    }

    @Test
    fun lookingForPost_saysOfferedToHelp() {
        val wanted = listing(type = ListingType.WANTED).copy(title = "Math tutor for MA 124")

        val plan = RequestRules.plan(wanted, me, currentDeal = null) as RequestPlan.StartDeal

        assertEquals("offered to help with Math tutor for MA 124", plan.actionText)
    }
}