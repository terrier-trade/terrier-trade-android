package edu.bu.terriertrade.data.local

import edu.bu.terriertrade.data.model.Category
import edu.bu.terriertrade.data.model.Condition
import edu.bu.terriertrade.data.model.Listing
import edu.bu.terriertrade.data.model.ListingStatus
import edu.bu.terriertrade.data.model.ListingType
import edu.bu.terriertrade.data.model.PriceUnit
import edu.bu.terriertrade.data.model.ServiceMode

private const val PHOTO_SEPARATOR = "\n"

internal fun Listing.toEntity(): ListingEntity = ListingEntity(
    id = id,
    ownerId = ownerId,
    type = type.name,
    title = title,
    price = price,
    description = description,
    priceUnit = priceUnit.name,
    category = category.name,
    condition = condition?.name,
    serviceMode = serviceMode?.name,
    meetSpot = meetSpot,
    photoUrls = photoUrls.joinToString(PHOTO_SEPARATOR),
    isbn = isbn,
    eventDateMillis = eventDateMillis,
    quantity = quantity,
    status = status.name,
    createdAt = createdAt,
)

/** Null if the row can't be read (for example an enum renamed in a newer app version), so it's skipped. */
internal fun ListingEntity.toListing(): Listing? {
    val type = enumOrNull<ListingType>(type) ?: return null
    val status = enumOrNull<ListingStatus>(status) ?: return null
    return Listing(
        id = id,
        ownerId = ownerId,
        type = type,
        title = title,
        price = price,
        description = description,
        priceUnit = enumOrNull<PriceUnit>(priceUnit) ?: PriceUnit.NONE,
        category = enumOrNull<Category>(category) ?: Category.OTHER,
        condition = enumOrNull<Condition>(condition),
        serviceMode = enumOrNull<ServiceMode>(serviceMode),
        meetSpot = meetSpot,
        photoUrls = if (photoUrls.isEmpty()) emptyList() else photoUrls.split(PHOTO_SEPARATOR),
        isbn = isbn,
        eventDateMillis = eventDateMillis,
        quantity = quantity,
        status = status,
        createdAt = createdAt,
    )
}

private inline fun <reified T : Enum<T>> enumOrNull(name: String?): T? =
    name?.let { wanted -> enumValues<T>().firstOrNull { it.name == wanted } }