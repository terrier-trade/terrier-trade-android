package edu.bu.terriertrade.data.firebase

import edu.bu.terriertrade.data.model.Deal
import edu.bu.terriertrade.data.model.DealItem
import edu.bu.terriertrade.data.model.DealProposal
import edu.bu.terriertrade.data.model.ProposalStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DealMappersTest {

    private val deal = Deal(
        items = listOf(DealItem("l3", 40), DealItem("l7", 10)),
        pending = DealProposal(
            id = "p1",
            fromUserId = "sam",
            items = listOf(DealItem("l3", 40)),
            baseVersion = 2,
            status = ProposalStatus.PENDING,
            createdAt = 1_000L,
        ),
        doneBy = listOf("alex"),
        version = 2,
    )

    @Test
    fun deal_roundTrips() {
        assertEquals(deal, dealFromMap(deal.toMap()))
    }

    @Test
    fun numbersStoredAsLong_areReadBack() {
        // Firestore always returns whole numbers as Long
        val fromFirestore = mapOf(
            "items" to listOf(mapOf("listingId" to "l3", "agreedPrice" to 40L)),
            "version" to 1L,
        )

        val read = dealFromMap(fromFirestore)!!

        assertEquals(listOf(DealItem("l3", 40)), read.items)
        assertEquals(1, read.version)
    }

    @Test
    fun brokenItems_areSkipped() {
        val data = mapOf(
            "items" to listOf(
                mapOf("listingId" to "l3", "agreedPrice" to 40L),
                mapOf("agreedPrice" to 10L),   // no listingId
                "not a map",
            ),
        )

        assertEquals(listOf(DealItem("l3", 40)), dealFromMap(data)!!.items)
    }

    @Test
    fun missingOrEmptyDeal_isNull() {
        assertNull(dealFromMap(null))
        assertNull(dealFromMap(mapOf("items" to emptyList<Any>())))
    }

    @Test
    fun chatFromMap_readsTheDeal() {
        val chat = chatFromMap(
            "c1",
            mapOf("participantIds" to listOf("alex", "sam"), "deal" to deal.toMap()),
        )!!

        assertEquals(deal, chat.deal)
    }
}