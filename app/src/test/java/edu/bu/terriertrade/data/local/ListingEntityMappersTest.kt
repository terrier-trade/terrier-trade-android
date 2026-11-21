package edu.bu.terriertrade.data.local

import edu.bu.terriertrade.data.model.Category
import edu.bu.terriertrade.data.model.Condition
import edu.bu.terriertrade.data.model.Listing
import edu.bu.terriertrade.data.model.ListingType
import edu.bu.terriertrade.data.model.PriceUnit
import edu.bu.terriertrade.data.model.ServiceMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Listing ↔ ListingEntity: every field survives the round trip, and unreadable rows are skipped. */
class ListingEntityMappersTest {

    @Test
    fun fullListing_roundTrips() {
        val listing = Listing(
            id = "t-1", ownerId = "sam", type = ListingType.TICKET, title = "Red Sox tickets", price = 20,
            description = "Two seats", priceUnit = PriceUnit.NONE, category = Category.TICKETS,
            condition = Condition.NEW, serviceMode = ServiceMode.IN_PERSON, meetSpot = "GSU",
            photoUrls = listOf("https://a.example/1.jpg", "https://a.example/2.jpg"),
            isbn = "9780131103627", eventDateMillis = 1_800_000_000_000L, quantity = 2, createdAt = 42L,
        )

        assertEquals(listing, listing.toEntity().toListing())
    }

    @Test
    fun noPhotos_comesBackAsEmptyList() {
        val listing = Listing(id = "s-1", ownerId = "sam", type = ListingType.SERVICE, title = "Python help", price = 25)

        assertEquals(emptyList<String>(), listing.toEntity().toListing()?.photoUrls)
    }

    @Test
    fun unknownType_isSkipped() {
        val row = Listing(id = "x", ownerId = "sam", type = ListingType.ITEM, title = "Lamp", price = 5)
            .toEntity()
            .copy(type = "SOMETHING_NEW")

        assertNull(row.toListing())
    }
}