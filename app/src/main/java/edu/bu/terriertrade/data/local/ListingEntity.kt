package edu.bu.terriertrade.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * One listing saved on the phone, as a row in the "listings" table.
 * Enums are stored by name and photo URLs as one newline-separated string,
 * so Room needs no type converters. ListingEntityMappers turns it back into a Listing.
 */
@Entity(tableName = "listings")
data class ListingEntity(
    @PrimaryKey val id: String,
    val ownerId: String,
    val type: String,
    val title: String,
    val price: Int,
    val description: String,
    val priceUnit: String,
    val category: String,
    val condition: String?,
    val serviceMode: String?,
    val meetSpot: String,
    val photoUrls: String,
    val isbn: String?,
    val eventDateMillis: Long?,
    val quantity: Int?,
    val status: String,
    val createdAt: Long,
)