package edu.bu.terriertrade.data.model

/** The four kinds of post a student can make. */
enum class ListingType(val label: String) {
    ITEM("Item"),
    SERVICE("Service"),
    TICKET("Ticket"),
    WANTED("Looking for"),
}

enum class Category(val label: String) {
    BOOKS("Books"),
    FURNITURE("Furniture"),
    TUTORING("Tutoring"),
    TICKETS("Tickets"),
    NOTES("Notes"),
    TECH("Tech"),
    OTHER("Other"),
}

enum class Condition(val label: String) {
    NEW("New"),
    LIKE_NEW("Like new"),
    GOOD("Good"),
    FAIR("Fair"),
}

/** How a price is charged. Items and tickets use NONE, services usually PER_HOUR or PER_SESSION. */
enum class PriceUnit(val suffix: String) {
    NONE(""),
    PER_HOUR("/hr"),
    PER_SESSION("/session"),
}

enum class ServiceMode(val label: String) {
    IN_PERSON("In person"),
    ONLINE("Online"),
    EITHER("In person or online"),
}

enum class ListingStatus {
    ACTIVE,
    SOLD,
    REMOVED,
}

/**
 * One post on Terrier Trade. Prices are whole US dollars.
 * Fields that only apply to some types are nullable:
 * condition and isbn (items), serviceMode (services), eventDateMillis and quantity (tickets).
 * For a WANTED post, price is the most the student wants to pay.
 */
data class Listing(
    val id: String,
    val ownerId: String,
    val type: ListingType,
    val title: String,
    val price: Int,
    val description: String = "",
    val priceUnit: PriceUnit = PriceUnit.NONE,
    val category: Category = Category.OTHER,
    val condition: Condition? = null,
    val serviceMode: ServiceMode? = null,
    val meetSpot: String = "",
    val photoUrls: List<String> = emptyList(),
    val isbn: String? = null,
    val eventDateMillis: Long? = null,
    val quantity: Int? = null,
    val status: ListingStatus = ListingStatus.ACTIVE,
    val createdAt: Long = 0L,
) {
    val isActive: Boolean get() = status == ListingStatus.ACTIVE

    /** Services can be booked again, so they stay listed after a deal. Everything else is marked sold. */
    val staysListedAfterDeal: Boolean get() = type == ListingType.SERVICE
}
