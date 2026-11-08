package edu.bu.terriertrade.data.firebase

import edu.bu.terriertrade.data.model.Category
import edu.bu.terriertrade.data.model.Condition
import edu.bu.terriertrade.data.model.Listing
import edu.bu.terriertrade.data.model.ListingStatus
import edu.bu.terriertrade.data.model.ListingType
import edu.bu.terriertrade.data.model.Message
import edu.bu.terriertrade.data.model.MessageType
import edu.bu.terriertrade.data.model.Review
import edu.bu.terriertrade.data.model.ReviewTag
import edu.bu.terriertrade.data.model.User
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Model to Firestore fields and back. Firestore returns numbers as Long and old documents can have odd
 * values, so reading must never crash.
 */
class FirestoreMappersTest {

    @Test
    fun listing_roundTrips_andDropsPhoneOnlyPhotos() {
        val lamp = Listing(
            id = "l1", ownerId = "alex", type = ListingType.ITEM, title = "Desk lamp", price = 12,
            category = Category.FURNITURE, condition = Condition.GOOD, meetSpot = "GSU",
            photoUrls = listOf("content://media/1", "https://covers.openlibrary.org/b/id/1-M.jpg"),
            createdAt = 1_000L,
        )
        val map = lamp.toMap()

        // A content:// link only works on the phone that picked it, so it isn't saved.
        assertEquals(listOf("https://covers.openlibrary.org/b/id/1-M.jpg"), map["photoUrls"])
        assertEquals("ITEM", map["type"])

        // Firestore hands numbers back as Long, which is what the map has after a real read.
        val back = listingFromMap("l1", map + ("price" to 12L))!!
        assertEquals(lamp.copy(photoUrls = listOf("https://covers.openlibrary.org/b/id/1-M.jpg")), back)
    }

    @Test
    fun listing_withoutRequiredFields_isSkipped() {
        assertNull(listingFromMap("x", mapOf("type" to "ITEM", "title" to "No owner")))
        assertNull(listingFromMap("x", mapOf("ownerId" to "alex", "type" to "BOAT", "title" to "Unknown type")))
        assertNull(listingFromMap("x", null))
    }

    @Test
    fun listing_withOddFields_getsSafeDefaults() {
        val listing = listingFromMap(
            "x",
            mapOf("ownerId" to "alex", "type" to "SERVICE", "title" to "Tutoring", "category" to "SPACESHIPS"),
        )!!

        assertEquals(Category.OTHER, listing.category)
        assertEquals(ListingStatus.ACTIVE, listing.status)
        assertEquals(0, listing.price)
    }

    @Test
    fun profile_roundTrips_withoutEmailOrRatings() {
        val alex = User(
            id = "alex", name = " Alex Morgan ", email = "alex@bu.edu", major = "CS", year = "Junior",
            startTerm = "Fall 2024", gradTerm = "May 2027", bio = "Hi", meetSpot = "GSU",
            ratingSum = 40, reviewCount = 9, tradeCount = 9, verified = true,
        )
        val map = alex.toProfileMap()

        assertFalse("email" in map)
        assertFalse("ratingSum" in map)

        val back = userFromMap("alex", map)!!
        assertEquals("Alex Morgan", back.name)
        assertEquals("May 2027", back.gradTerm)
        assertEquals(0, back.reviewCount)
        assertTrue(back.verified)
    }

    @Test
    fun missingProfile_isNull_andBlankName_isFriendly() {
        assertNull(userFromMap("x", null))
        assertEquals("BU student", userFromMap("x", mapOf("name" to ""))!!.name)
    }

    @Test
    fun ratingAndTrades_comeFromReviews() {
        val reviews = listOf(
            Review("r1", "maya", "alex", "c1", stars = 5),
            Review("r2", "sam", "alex", "c2", stars = 4),
        )
        val alex = User(id = "alex", name = "Alex").withReviews(reviews)
        assertEquals(9, alex.ratingSum)
        assertEquals(2, alex.reviewCount)
        assertEquals(2, alex.tradeCount)
        assertEquals(4.5, alex.rating, 0.001)
    }

    @Test
    fun review_roundTrips_andTagsAreNames() {
        val review = Review("r1", "maya", "alex", "c1", stars = 5, tags = listOf(ReviewTag.ON_TIME), text = " Great ", createdAt = 7L)
        val map = review.toMap()
        assertEquals(listOf("ON_TIME"), map["tags"])

        val back = reviewFromMap("r1", map)!!
        assertEquals(listOf(ReviewTag.ON_TIME), back.tags)
        assertEquals("Great", back.text)
        assertEquals(7L, back.createdAt)
    }

    @Test
    fun review_withOddValues_staysSafe() {
        val back = reviewFromMap("r", mapOf("fromUserId" to "a", "toUserId" to "b", "stars" to 9L, "tags" to listOf("NOPE")))!!
        assertEquals(5, back.stars)
        assertTrue(back.tags.isEmpty())
        assertNull(reviewFromMap("r", mapOf("toUserId" to "b")))
    }

    @Test
    fun chat_peopleAreSorted_andEmptyChatIsSkipped() {
        val map = chatToMap(listOf("sam", "alex", "sam"), lastMessage = "", updatedAt = 3L)
        assertEquals(listOf("alex", "sam"), map["participantIds"])
        assertEquals(listOf("alex", "sam"), chatFromMap("c1", map)!!.participantIds)
        assertNull(chatFromMap("c2", mapOf("participantIds" to emptyList<String>())))
    }

    @Test
    fun message_roundTrips() {
        val msg = Message("m1", "c1", "alex", MessageType.TEXT, "Still available?", createdAt = 5L)
        val back = messageFromMap("m1", "c1", msg.toMap())!!
        assertEquals("Still available?", back.text)
        assertEquals(MessageType.TEXT, back.type)
        assertEquals(5L, back.createdAt)
    }
}
