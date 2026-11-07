package edu.bu.terriertrade.ui.model

import edu.bu.terriertrade.data.model.Category
import edu.bu.terriertrade.data.model.Condition
import edu.bu.terriertrade.data.model.Listing
import edu.bu.terriertrade.data.model.PriceUnit
import edu.bu.terriertrade.data.model.ServiceMode
import edu.bu.terriertrade.data.model.User
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.ZoneOffset
import edu.bu.terriertrade.data.model.ListingType as DataListingType

class ListingMappersTest {

    private val now = 1_800_000_000_000L
    private val utc = ZoneOffset.UTC
    private val maya = User(id = "maya", name = "Maya Rao", ratingSum = 47, reviewCount = 10, tradeCount = 10, verified = true)

    @Test
    fun item_mapsPriceConditionAndSeller() {
        val ui = Listing(
            id = "1", ownerId = "maya", type = DataListingType.ITEM, title = "Desk chair", price = 40,
            category = Category.FURNITURE, condition = Condition.GOOD, meetSpot = "West Campus",
            createdAt = now - 2 * 3_600_000L, photoUrls = listOf("https://example.com/a.jpg"),
        ).toListingUi(maya, now, utc)!!

        assertEquals("$40", ui.priceLabel)
        assertEquals(ListingType.ITEM, ui.type)
        assertEquals(ListingCategory.FURNITURE, ui.category)
        assertEquals("Good", ui.condition)
        assertEquals("2h", ui.postedAgo)
        assertEquals("Maya", ui.seller.name)
        assertEquals("MR", ui.seller.initials)
        assertEquals("4.7", ui.seller.rating)
        assertEquals("https://example.com/a.jpg", ui.photoUrl)
    }

    @Test
    fun service_showsRateAndMode() {
        val ui = Listing(
            id = "2", ownerId = "maya", type = DataListingType.SERVICE, title = "Tutoring", price = 20,
            priceUnit = PriceUnit.PER_HOUR, serviceMode = ServiceMode.ONLINE, createdAt = now,
        ).toListingUi(maya, now, utc)!!

        assertEquals("$20/hr", ui.priceLabel)
        assertEquals("Online", ui.condition)
        assertEquals("Other", ui.category.label)
    }

    @Test
    fun ticket_showsEachAndQuantity() {
        val ui = Listing(
            id = "3", ownerId = "leo", type = DataListingType.TICKET, title = "Hockey", price = 25,
            category = Category.TICKETS, quantity = 2, createdAt = now,
        ).toListingUi(null, now, utc)!!

        assertEquals("$25 each", ui.priceLabel)
        assertEquals("2 left", ui.condition)
        assertEquals("BU student", ui.seller.name)
        assertEquals("New", ui.seller.rating)
    }

    @Test
    fun wanted_isNotACard_butIsANote() {
        val wanted = Listing(
            id = "w", ownerId = "maya", type = DataListingType.WANTED, title = "MA 124 tutor", price = 25,
            priceUnit = PriceUnit.PER_HOUR, createdAt = now - 8 * 3_600_000L,
        )
        assertNull(wanted.toListingUi(maya, now, utc))

        val note = wanted.toWantedPostUi(maya, now, utc)
        assertEquals("Up to $25/hr", note.budgetLabel)
        assertEquals("Maya Rao", note.postedBy)
        assertEquals("8h ago", note.postedAgo)
    }

    @Test
    fun tutoringService_showsItsHourlyPrice() {
        // Tutoring is a normal paid service, so the card shows the rate and how the session happens.
        val ui = Listing(
            id = "l4", ownerId = "nina", type = DataListingType.SERVICE, title = "CS 330 tutoring", price = 20,
            priceUnit = PriceUnit.PER_HOUR, category = Category.TUTORING, serviceMode = ServiceMode.ONLINE,
            createdAt = now,
        ).toListingUi(maya, now, utc)!!

        assertEquals("$20/hr", ui.priceLabel)
        assertEquals("Online", ui.condition)
    }

    @Test
    fun tutoringRequest_showsItsBudget() {
        val note = Listing(
            id = "l10", ownerId = "maya", type = DataListingType.WANTED, title = "Math tutor for MA 124", price = 25,
            priceUnit = PriceUnit.PER_HOUR, category = Category.TUTORING, createdAt = now,
        ).toWantedPostUi(maya, now, utc)

        assertEquals("Up to $25/hr", note.budgetLabel)
    }

    @Test
    fun nonTutoring_keepsItsPrice() {
        val note = Listing(
            id = "l6", ownerId = "sam", type = DataListingType.WANTED, title = "CS 501 textbook", price = 30,
            category = Category.BOOKS, createdAt = now,
        ).toWantedPostUi(maya, now, utc)

        assertEquals("Up to $30", note.budgetLabel)
    }
}
