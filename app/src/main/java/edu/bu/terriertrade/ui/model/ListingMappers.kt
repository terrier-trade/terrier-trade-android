package edu.bu.terriertrade.ui.model

import edu.bu.terriertrade.data.model.Category
import edu.bu.terriertrade.data.model.Listing
import edu.bu.terriertrade.data.model.User
import edu.bu.terriertrade.util.TimeFormat
import edu.bu.terriertrade.util.priceLabel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import edu.bu.terriertrade.data.model.ListingType as DataListingType

/*
 * Turns the data layer's Listing and User into the display-ready models the screens use.
 * Plain Kotlin, so it's covered by fast unit tests.
 */

/** Listing → card and detail model. Wanted posts aren't cards, so they return null. */
fun Listing.toListingUi(
    owner: User?,
    nowMillis: Long = System.currentTimeMillis(),
    zone: ZoneId = ZoneId.systemDefault(),
): ListingUi? {
    val uiType = when (type) {
        DataListingType.ITEM -> ListingType.ITEM
        DataListingType.SERVICE -> ListingType.SERVICE
        DataListingType.TICKET -> ListingType.TICKET
        DataListingType.WANTED -> return null
    }
    val shortLine = when {
        type == DataListingType.SERVICE -> serviceMode?.label ?: "Service"
        type == DataListingType.TICKET -> quantity?.let { if (it == 1) "1 left" else "$it left" } ?: "Tickets"
        else -> condition?.label ?: "Used"
    }
    val details = buildList {
        isbn?.let { add("ISBN" to it) }
        eventDateMillis?.let { add("Event date" to formatEventDate(it, zone)) }
        quantity?.let { add("Quantity" to it.toString()) }
    }
    return ListingUi(
        id = id,
        title = title,
        priceLabel = if (type == DataListingType.TICKET) "${priceLabel()} each" else priceLabel(),
        type = uiType,
        category = category.toUi(),
        meetSpot = meetSpot.ifBlank { "On campus" },
        postedAgo = TimeFormat.ago(createdAt, nowMillis, zone),
        condition = shortLine,
        description = description,
        seller = owner.toSellerUi().copy(id = ownerId),
        details = details,
        photoUrl = photoUrls.firstOrNull(),
        price = price,
        createdAt = createdAt,
        sellerRating = owner?.rating ?: 0.0,
        conditionRank = condition?.ordinal,
    )
}

/** Wanted post → sticky note. */
fun Listing.toWantedPostUi(
    owner: User?,
    nowMillis: Long = System.currentTimeMillis(),
    zone: ZoneId = ZoneId.systemDefault(),
): WantedPostUi = WantedPostUi(
    id = id,
    title = title,
    budgetLabel = priceLabel(),
    postedBy = owner?.name?.takeIf { it.isNotBlank() } ?: "A BU student",
    postedAgo = TimeFormat.ago(createdAt, nowMillis, zone).let { if (it == "now") "just now" else "$it ago" },
)

/** Seller card. Someone whose profile hasn't loaded yet shows as a new BU student. */
fun User?.toSellerUi(): SellerUi =
    if (this == null) {
        SellerUi(name = "BU student", initials = "BU", rating = "New", tradeCount = 0, isVerified = true)
    } else {
        SellerUi(
            name = firstName.ifBlank { name },
            initials = initials,
            rating = if (reviewCount == 0) "New" else String.format(Locale.US, "%.1f", rating),
            tradeCount = tradeCount,
            isVerified = verified,
        )
    }

fun Category.toUi(): ListingCategory = when (this) {
    Category.BOOKS -> ListingCategory.BOOKS
    Category.FURNITURE -> ListingCategory.FURNITURE
    Category.TUTORING -> ListingCategory.TUTORING
    Category.TICKETS -> ListingCategory.TICKETS
    Category.NOTES -> ListingCategory.NOTES
    Category.TECH -> ListingCategory.TECH
    Category.OTHER -> ListingCategory.OTHER
}

private val eventDateFormat = DateTimeFormatter.ofPattern("EEE MMM d, h:mm a", Locale.US)

private fun formatEventDate(millis: Long, zone: ZoneId): String =
    Instant.ofEpochMilli(millis).atZone(zone).format(eventDateFormat)
