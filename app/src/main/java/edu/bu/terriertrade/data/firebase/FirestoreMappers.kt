package edu.bu.terriertrade.data.firebase

import edu.bu.terriertrade.data.model.Category
import edu.bu.terriertrade.data.model.Chat
import edu.bu.terriertrade.data.model.Condition
import edu.bu.terriertrade.data.model.Listing
import edu.bu.terriertrade.data.model.ListingStatus
import edu.bu.terriertrade.data.model.ListingType
import edu.bu.terriertrade.data.model.Message
import edu.bu.terriertrade.data.model.MessageType
import edu.bu.terriertrade.data.model.PriceUnit
import edu.bu.terriertrade.data.model.Review
import edu.bu.terriertrade.data.model.ReviewTag
import edu.bu.terriertrade.data.model.ServiceMode
import edu.bu.terriertrade.data.model.User

/*
 * Model ↔ Firestore fields, as plain maps so they're covered by fast unit tests.
 * Enums are saved as their names ("ON_TIME") so the data is easy to read in the Firebase console.
 * Reading never crashes: a missing or odd field falls back to a safe default, and a document
 * without its required fields is skipped (null).
 */

// Listings --------------------------------------------------------------------------------------

/** Listing → Firestore fields. Only web photo URLs are kept, since phone-only photo links mean nothing to other phones. */
internal fun Listing.toMap(): Map<String, Any?> = mapOf(
    "ownerId" to ownerId,
    "type" to type.name,
    "title" to title,
    "price" to price,
    "description" to description,
    "priceUnit" to priceUnit.name,
    "category" to category.name,
    "condition" to condition?.name,
    "serviceMode" to serviceMode?.name,
    "meetSpot" to meetSpot,
    "photoUrls" to photoUrls.filter { it.startsWith("http") },
    "isbn" to isbn,
    "eventDateMillis" to eventDateMillis,
    "quantity" to quantity,
    "status" to status.name,
    "createdAt" to createdAt,
)

/** Firestore fields → Listing, or null when the owner, type or title is missing. */
internal fun listingFromMap(id: String, data: Map<String, Any?>?): Listing? {
    if (data == null) return null
    val ownerId = data.string("ownerId").ifBlank { return null }
    val type = enumOrNull<ListingType>(data["type"] as? String) ?: return null
    val title = data.string("title").ifBlank { return null }
    return Listing(
        id = id,
        ownerId = ownerId,
        type = type,
        title = title,
        price = data.long("price")?.toInt() ?: 0,
        description = data.string("description"),
        priceUnit = enumOrNull<PriceUnit>(data["priceUnit"] as? String) ?: PriceUnit.NONE,
        category = enumOrNull<Category>(data["category"] as? String) ?: Category.OTHER,
        condition = enumOrNull<Condition>(data["condition"] as? String),
        serviceMode = enumOrNull<ServiceMode>(data["serviceMode"] as? String),
        meetSpot = data.string("meetSpot"),
        photoUrls = (data["photoUrls"] as? List<*>).orEmpty().filterIsInstance<String>(),
        isbn = data["isbn"] as? String,
        eventDateMillis = data.long("eventDateMillis"),
        quantity = data.long("quantity")?.toInt(),
        status = enumOrNull<ListingStatus>(data["status"] as? String) ?: ListingStatus.ACTIVE,
        createdAt = data.long("createdAt") ?: 0L,
    )
}

// Users -----------------------------------------------------------------------------------------

/**
 * What a student can edit on their own profile. Rating and trade counts are not stored here:
 * they're worked out from the reviews collection, so nobody has to edit someone else's profile.
 * The email is not stored either, because every student can read profiles.
 */
internal fun User.toProfileMap(): Map<String, Any?> = mapOf(
    "name" to name.trim(),
    "major" to major.trim(),
    "year" to year.trim(),
    "startTerm" to startTerm.trim(),
    "gradTerm" to gradTerm.trim(),
    "bio" to bio.trim(),
    "meetSpot" to meetSpot.trim(),
    "verified" to verified,
)

internal fun userFromMap(id: String, data: Map<String, Any?>?): User? {
    if (data == null) return null
    return User(
        id = id,
        name = data.string("name").ifBlank { "BU student" },
        major = data.string("major"),
        year = data.string("year"),
        startTerm = data.string("startTerm"),
        gradTerm = data.string("gradTerm"),
        bio = data.string("bio"),
        meetSpot = data.string("meetSpot"),
        verified = data["verified"] as? Boolean ?: false,
    )
}

/** Adds the rating and trade count worked out from the reviews about this student. */
internal fun User.withReviews(reviews: List<Review>): User = copy(
    ratingSum = reviews.sumOf { it.stars.coerceIn(1, 5) },
    reviewCount = reviews.size,
    tradeCount = reviews.size,
)

// Reviews ---------------------------------------------------------------------------------------

internal fun Review.toMap(): Map<String, Any?> = mapOf(
    "fromUserId" to fromUserId,
    "toUserId" to toUserId,
    "chatId" to chatId,
    "stars" to stars.coerceIn(1, 5),
    "tags" to tags.map { it.name },
    "text" to text.trim(),
    "createdAt" to createdAt,
)

internal fun reviewFromMap(id: String, data: Map<String, Any?>?): Review? {
    if (data == null) return null
    val from = data.string("fromUserId").ifBlank { return null }
    val to = data.string("toUserId").ifBlank { return null }
    return Review(
        id = id,
        fromUserId = from,
        toUserId = to,
        chatId = data.string("chatId"),
        stars = data.long("stars")?.toInt()?.coerceIn(1, 5) ?: 5,
        tags = (data["tags"] as? List<*>).orEmpty().mapNotNull { name -> enumOrNull<ReviewTag>(name as? String) },
        text = data.string("text"),
        createdAt = data.long("createdAt") ?: 0L,
    )
}

// Chats -----------------------------------------------------------------------------------------

/** Fields for a new chat. The deal is added later by requestListing. */
internal fun chatToMap(participantIds: List<String>, lastMessage: String, updatedAt: Long): Map<String, Any?> = mapOf(
    "participantIds" to participantIds.distinct().sorted(),
    "lastMessage" to lastMessage,
    "updatedAt" to updatedAt,
)

internal fun chatFromMap(id: String, data: Map<String, Any?>?): Chat? {
    if (data == null) return null
    val people = (data["participantIds"] as? List<*>).orEmpty().filterIsInstance<String>()
    if (people.isEmpty()) return null
    return Chat(
        id = id,
        participantIds = people,
        deal = dealFromMap(data["deal"]),
        lastMessage = data.string("lastMessage"),
        updatedAt = data.long("updatedAt") ?: 0L,
    )
}

// Messages --------------------------------------------------------------------------------------

internal fun Message.toMap(): Map<String, Any?> = mapOf(
    "senderId" to senderId,
    "type" to type.name,
    "text" to text,
    "proposalId" to proposalId,
    "createdAt" to createdAt,
)

internal fun messageFromMap(id: String, chatId: String, data: Map<String, Any?>?): Message? {
    if (data == null) return null
    val sender = data.string("senderId").ifBlank { return null }
    return Message(
        id = id,
        chatId = chatId,
        senderId = sender,
        type = enumOrNull<MessageType>(data["type"] as? String) ?: MessageType.TEXT,
        text = data.string("text"),
        proposalId = data["proposalId"] as? String,
        createdAt = data.long("createdAt") ?: 0L,
    )
}

// Helpers ---------------------------------------------------------------------------------------

private fun Map<String, Any?>.string(key: String): String = (this[key] as? String).orEmpty()

private fun Map<String, Any?>.long(key: String): Long? = (this[key] as? Number)?.toLong()
