package edu.bu.terriertrade.ui.model

/**
 * Placeholder content so screens work before the data layer is connected.
 * TODO(data): replace with the repository's listings, mapped to ListingUi.
 */
object SampleData {

    private val maya = SellerUi(name = "Maya", initials = "MR", rating = "4.7", tradeCount = 6)
    private val nina = SellerUi(name = "Nina", initials = "NP", rating = "5.0", tradeCount = 9)
    private val leo = SellerUi(name = "Leo", initials = "LT", rating = "4.6", tradeCount = 3)

    val listings: List<ListingUi> = listOf(
        ListingUi(
            id = "calc-stewart",
            title = "Calculus: Early Transcendentals",
            priceLabel = "$35",
            type = ListingType.ITEM,
            category = ListingCategory.BOOKS,
            meetSpot = "Mugar Library",
            postedAgo = "2h",
            condition = "Like new",
            description = "Used for MA 123 last spring. Light highlighting in chapters 2 and 3. Free weekdays after 2pm.",
            seller = maya,
            details = listOf("Author" to "James Stewart", "Course" to "MA 123"),
        ),
        ListingUi(
            id = "cs330-tutoring",
            title = "CS 330 tutoring",
            priceLabel = "$20/hr",
            type = ListingType.SERVICE,
            category = ListingCategory.TUTORING,
            meetSpot = "CDS building",
            postedAgo = "3h",
            condition = "In person or online",
            description = "Got an A last fall. Happy to go over problem sets and exam prep, weekday evenings.",
            seller = nina,
        ),
        ListingUi(
            id = "hockey-bc",
            title = "BU Hockey vs BC",
            priceLabel = "$25 each",
            type = ListingType.TICKET,
            category = ListingCategory.TICKETS,
            meetSpot = "Agganis Arena",
            postedAgo = "1d",
            condition = "2 left",
            description = "Student section. I'll transfer in Ticketmaster once we agree.",
            seller = nina,
            details = listOf("Event date" to "Fri Nov 14, 7pm", "Quantity" to "2"),
        ),
        ListingUi(
            id = "desk-chair",
            title = "Desk chair",
            priceLabel = "$40",
            type = ListingType.ITEM,
            category = ListingCategory.FURNITURE,
            meetSpot = "West Campus",
            postedAgo = "5h",
            condition = "Good",
            description = "Adjustable height, wheels work fine. You'll need to carry it out.",
            seller = leo,
        ),
        ListingUi(
            id = "cs111-notes",
            title = "CS 111 study notes",
            priceLabel = "$10",
            type = ListingType.ITEM,
            category = ListingCategory.NOTES,
            meetSpot = "Mugar Library",
            postedAgo = "1d",
            condition = "Typed, 40 pages",
            description = "Full semester of typed notes with worked examples. Shared as a printed copy.",
            seller = maya,
        ),
        ListingUi(
            id = "ti-84",
            title = "TI-84 Plus calculator",
            priceLabel = "$45",
            type = ListingType.ITEM,
            category = ListingCategory.TECH,
            meetSpot = "CDS building",
            postedAgo = "2d",
            condition = "Good",
            description = "Works great, new batteries. Some scratches on the back.",
            seller = leo,
        ),
    )

    val wanted: List<WantedPostUi> = listOf(
        WantedPostUi(id = "w1", title = "Math tutor for MA 124", budgetLabel = "Up to $25/hr", postedBy = "Maya R.", postedAgo = "8h ago"),
        WantedPostUi(id = "w2", title = "CS 501 textbook", budgetLabel = "Up to $30", postedBy = "Sam K.", postedAgo = "1d ago"),
    )
}