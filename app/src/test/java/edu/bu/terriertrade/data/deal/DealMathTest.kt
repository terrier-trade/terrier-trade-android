package edu.bu.terriertrade.data.deal

import edu.bu.terriertrade.data.fake.SampleData
import edu.bu.terriertrade.data.model.DealItem
import edu.bu.terriertrade.data.model.ListingStatus
import org.junit.Assert.assertEquals
import org.junit.Test

/** The deal total decides who pays whom, so each direction of money has a case here. */
class DealMathTest {

    @Test
    fun dealTotal_followsWhoGivesWhat() {
        val listings = SampleData.listings.associateBy { it.id }
        val me = SampleData.ME

        // Sam's calculator: I receive it, so I pay
        assertEquals(40, dealTotal(listOf(DealItem("l3", 40)), listings, me))
        // Plus my desk lamp going to Sam: 40 - 12
        assertEquals(28, dealTotal(listOf(DealItem("l3", 40), DealItem("m1", 12)), listings, me))
        // Sam is Looking for a textbook and I provide it, so Sam pays me
        assertEquals(-30, dealTotal(listOf(DealItem("l6", 30)), listings, me))
    }

    @Test
    fun dealTotal_skipsRemovedListings() {
        // Sam took the calculator down, so only my lamp going to Sam is left: I'm paid 12
        val listings = SampleData.listings.associateBy { it.id }.toMutableMap()
        listings["l3"] = listings.getValue("l3").copy(status = ListingStatus.REMOVED)

        val total = dealTotal(listOf(DealItem("l3", 40), DealItem("m1", 12)), listings, SampleData.ME)

        assertEquals(-12, total)
    }
}
