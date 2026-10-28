package edu.bu.terriertrade.data.fake

import edu.bu.terriertrade.data.model.Category
import edu.bu.terriertrade.data.model.Chat
import edu.bu.terriertrade.data.model.Condition
import edu.bu.terriertrade.data.model.Deal
import edu.bu.terriertrade.data.model.DealItem
import edu.bu.terriertrade.data.model.DealProposal
import edu.bu.terriertrade.data.model.Listing
import edu.bu.terriertrade.data.model.ListingType
import edu.bu.terriertrade.data.model.Message
import edu.bu.terriertrade.data.model.MessageType
import edu.bu.terriertrade.data.model.PriceUnit
import edu.bu.terriertrade.data.model.Review
import edu.bu.terriertrade.data.model.ReviewTag
import edu.bu.terriertrade.data.model.ServiceMode
import edu.bu.terriertrade.data.model.User

/**
 * Demo data matching the prototype. Used by the fake repositories and by @Preview functions.
 * "alex" is the signed-in student.
 */
object SampleData {
    const val ME = "alex"

    private val now = System.currentTimeMillis()
    private fun minutesAgo(m: Int) = now - m * 60_000L
    private fun hoursAgo(h: Int) = now - h * 3_600_000L

    val users: List<User> = listOf(
        User(
            id = "alex", name = "Alex Morgan", email = "alex@bu.edu", major = "Computer Science", year = "Junior",
            startTerm = "Fall 2024", gradTerm = "May 2027", meetSpot = "GSU", verified = true,
            bio = "CS junior selling stuff from my dorm. Happy to help with Python too.",
            ratingSum = 58, reviewCount = 12, tradeCount = 12,
        ),
        User(
            id = "sam", name = "Sam K.", email = "sam@bu.edu", major = "Physics", year = "Senior",
            startTerm = "Fall 2022", gradTerm = "May 2026", meetSpot = "GSU", verified = true,
            bio = "Physics senior clearing out my desk before graduation. Usually free after 3pm.",
            ratingSum = 69, reviewCount = 14, tradeCount = 14,
        ),
        User(
            id = "maya", name = "Maya R.", email = "maya@bu.edu", major = "Mathematics", year = "Sophomore",
            startTerm = "Fall 2025", gradTerm = "May 2029", meetSpot = "Mugar Library", verified = true,
            bio = "Math major. I keep my textbooks in good shape and only list what I actually used.",
            ratingSum = 38, reviewCount = 8, tradeCount = 8,
        ),
        User(
            id = "nina", name = "Nina P.", email = "nina@bu.edu", major = "Data Science", year = "Grad",
            startTerm = "Fall 2025", gradTerm = "May 2027", meetSpot = "CDS building", verified = true,
            bio = "Grad student in CDS. I tutor CS 330 and CS 112 and sometimes sell dorm stuff.",
            ratingSum = 105, reviewCount = 21, tradeCount = 21,
        ),
        User(
            id = "leo", name = "Leo T.", email = "leo@bu.edu", major = "Business", year = "Junior",
            startTerm = "Fall 2023", gradTerm = "May 2027", meetSpot = "West Campus", verified = true,
            bio = "Mostly event tickets and dorm furniture. Message me anytime.",
            ratingSum = 23, reviewCount = 5, tradeCount = 5,
        ),
    )

    val listings: List<Listing> = listOf(
        Listing(
            id = "l1", ownerId = "maya", type = ListingType.ITEM, title = "Calculus: Early Transcendentals",
            price = 35, category = Category.BOOKS, condition = Condition.LIKE_NEW, meetSpot = "Mugar Library",
            description = "8th edition. A few pencil notes in chapter 3, otherwise clean.", createdAt = hoursAgo(2),
        ),
        Listing(
            id = "l2", ownerId = "leo", type = ListingType.ITEM, title = "Desk chair",
            price = 40, category = Category.FURNITURE, condition = Condition.GOOD, meetSpot = "West Campus",
            description = "Adjustable height, black mesh. Pick up near West Campus.", createdAt = hoursAgo(5),
        ),
        Listing(
            id = "l3", ownerId = "sam", type = ListingType.ITEM, title = "TI-84 calculator",
            price = 45, category = Category.TECH, condition = Condition.GOOD, meetSpot = "GSU",
            description = "Works perfectly. New batteries included.", createdAt = hoursAgo(24),
        ),
        Listing(
            id = "l4", ownerId = "nina", type = ListingType.SERVICE, title = "CS 330 tutoring",
            price = 20, priceUnit = PriceUnit.PER_HOUR, category = Category.TUTORING, serviceMode = ServiceMode.EITHER,
            meetSpot = "CDS building", description = "Got an A in CS 330. Happy to go over problem sets and exam prep.",
            createdAt = hoursAgo(3),
        ),
        Listing(
            id = "l5", ownerId = "leo", type = ListingType.TICKET, title = "BU Hockey vs. BC",
            price = 25, category = Category.TICKETS, quantity = 2, eventDateMillis = now + 17 * 24 * 3_600_000L,
            meetSpot = "Agganis Arena", description = "Student section. Transfers through the official ticket app.",
            createdAt = hoursAgo(6),
        ),
        Listing(
            id = "l7", ownerId = "sam", type = ListingType.ITEM, title = "PY 211 notes",
            price = 10, category = Category.NOTES, condition = Condition.GOOD, meetSpot = "GSU",
            description = "Full semester of handwritten notes, plus a scanned copy.", createdAt = hoursAgo(48),
        ),
        Listing(
            id = "l8", ownerId = "leo", type = ListingType.SERVICE, title = "Resume review",
            price = 15, priceUnit = PriceUnit.PER_SESSION, category = Category.TUTORING, serviceMode = ServiceMode.ONLINE,
            meetSpot = "Online", description = "I review resumes for internship applications over a 30 minute call.",
            createdAt = hoursAgo(4),
        ),
        Listing(
            id = "l9", ownerId = "nina", type = ListingType.ITEM, title = "Mini fridge",
            price = 60, category = Category.FURNITURE, condition = Condition.GOOD, meetSpot = "Warren Towers",
            description = "3.1 cu ft, fits under a dorm desk.", createdAt = hoursAgo(26),
        ),
        Listing(
            id = "l10", ownerId = "maya", type = ListingType.WANTED, title = "Math tutor for MA 124",
            price = 25, priceUnit = PriceUnit.PER_HOUR, category = Category.TUTORING, meetSpot = "Mugar Library",
            description = "Need help twice a week before midterms.", createdAt = hoursAgo(8),
        ),
        Listing(
            id = "l6", ownerId = "sam", type = ListingType.WANTED, title = "CS 501 textbook",
            price = 30, category = Category.BOOKS, meetSpot = "GSU",
            description = "Any recent edition is fine.", createdAt = hoursAgo(24),
        ),
        Listing(
            id = "m1", ownerId = ME, type = ListingType.ITEM, title = "Desk lamp",
            price = 12, category = Category.FURNITURE, condition = Condition.LIKE_NEW, meetSpot = "GSU",
            description = "Warm LED lamp with a flexible neck.", createdAt = hoursAgo(72),
        ),
        Listing(
            id = "m2", ownerId = ME, type = ListingType.ITEM, title = "Bike lock",
            price = 15, category = Category.OTHER, condition = Condition.GOOD, meetSpot = "GSU",
            description = "U-lock with two keys.", createdAt = hoursAgo(120),
        ),
        Listing(
            id = "m3", ownerId = ME, type = ListingType.SERVICE, title = "Python help",
            price = 18, priceUnit = PriceUnit.PER_HOUR, category = Category.TUTORING, serviceMode = ServiceMode.ONLINE,
            meetSpot = "Online", description = "Debugging and intro Python concepts.", createdAt = hoursAgo(168),
        ),
    )

    val chats: List<Chat> = listOf(
        Chat(
            id = "c_sam", participantIds = listOf(ME, "sam"),
            deal = Deal(
                items = listOf(DealItem("l3", 40)),
                pending = DealProposal(
                    id = "p1", fromUserId = "sam", baseVersion = 1,
                    items = listOf(DealItem("l3", 40), DealItem("l7", 10)), createdAt = minutesAgo(2),
                ),
                version = 1,
            ),
            lastMessage = "Sam suggested changes to the deal", updatedAt = minutesAgo(2),
        ),
        Chat(
            id = "c_nina", participantIds = listOf(ME, "nina"),
            deal = Deal(items = listOf(DealItem("l4", 20))),
            lastMessage = "Thursday works, see you then!", updatedAt = hoursAgo(1),
        ),
        Chat(
            id = "c_maya", participantIds = listOf(ME, "maya"),
            lastMessage = "Deal done", updatedAt = hoursAgo(24 * 16),
        ),
    )

    val messages: List<Message> = listOf(
        Message("a1", "c_sam", ME, MessageType.EVENT, "You requested TI-84 calculator", createdAt = minutesAgo(40)),
        Message("a2", "c_sam", "sam", MessageType.TEXT, "Hey! It is still available.", createdAt = minutesAgo(35)),
        Message("a3", "c_sam", ME, MessageType.TEXT, "Great. Can we meet at GSU tomorrow? Would you do $40?", createdAt = minutesAgo(30)),
        Message("a4", "c_sam", "sam", MessageType.EVENT, "Sam changed TI-84 calculator from $45 to $40", createdAt = minutesAgo(25)),
        Message("a5", "c_sam", "sam", MessageType.TEXT, "Sure. I also have PY 211 notes if you want them.", createdAt = minutesAgo(5)),
        Message("a6", "c_sam", "sam", MessageType.SUGGESTION, proposalId = "p1", createdAt = minutesAgo(2)),
        Message("b1", "c_nina", ME, MessageType.EVENT, "You requested CS 330 tutoring", createdAt = hoursAgo(3)),
        Message("b2", "c_nina", "nina", MessageType.TEXT, "I am free Thursdays 4 to 6pm.", createdAt = hoursAgo(2)),
        Message("b3", "c_nina", ME, MessageType.TEXT, "Thursday works, see you then!", createdAt = hoursAgo(1)),
        Message("c1", "c_maya", ME, MessageType.EVENT, "You requested Linear Algebra textbook", createdAt = hoursAgo(24 * 17)),
        Message("c2", "c_maya", "maya", MessageType.TEXT, "Thanks for the quick trade!", createdAt = hoursAgo(24 * 16)),
        Message("c3", "c_maya", ME, MessageType.EVENT, "Deal done", createdAt = hoursAgo(24 * 16)),
    )

    val reviews: List<Review> = listOf(
        Review("r1", "maya", ME, "c_maya", 5, listOf(ReviewTag.ON_TIME, ReviewTag.FRIENDLY), "Easy to coordinate and super friendly.", hoursAgo(24 * 16)),
        Review("r2", "nina", ME, "c_old1", 4, listOf(ReviewTag.AS_DESCRIBED), "Good trade, the lamp works fine.", hoursAgo(24 * 30)),
        Review("r3", "priya", "sam", "c_old2", 5, listOf(ReviewTag.FRIENDLY), "Super quick and friendly. The notes were really well organized.", hoursAgo(24 * 14)),
        Review("r4", "jordan", "sam", "c_old3", 5, listOf(ReviewTag.ON_TIME, ReviewTag.AS_DESCRIBED), "Showed up at GSU on time, everything as described.", hoursAgo(24 * 30)),
    )

    /** Hearted listings for the demo account. */
    val savedIds: Set<String> = setOf("l4", "l9")

    fun user(id: String): User? = users.find { it.id == id }

    fun listing(id: String): Listing? = listings.find { it.id == id }
}
