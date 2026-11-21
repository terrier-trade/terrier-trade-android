package edu.bu.terriertrade.data.local

import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.FtsOptions

/**
 * Full-text search index over each saved listing's title and description.
 *
 * It's an "external content" table: the text lives in [ListingEntity], and Room adds triggers that
 * keep this index in step whenever a listing row is inserted, updated or deleted. Nothing writes here
 * directly. unicode61 makes matching ignore case, including letters outside A–Z.
 */
@Fts4(contentEntity = ListingEntity::class, tokenizer = FtsOptions.TOKENIZER_UNICODE61)
@Entity(tableName = "listings_fts")
data class ListingFtsEntity(
    val title: String,
    val description: String,
)