package edu.bu.terriertrade.data.repository

import edu.bu.terriertrade.data.model.Listing
import edu.bu.terriertrade.data.model.ListingStatus
import kotlinx.coroutines.flow.Flow

interface ListingRepository {
    /** Every ACTIVE listing, newest first. Search and filters are applied on top of this. */
    fun observeActiveListings(): Flow<List<Listing>>

    fun observeListing(listingId: String): Flow<Listing?>

    /** A student's own listings, including sold ones, but not removed ones. */
    fun observeListingsByOwner(ownerId: String): Flow<List<Listing>>

    /** Saves a new listing for the signed-in student and returns its id. */
    suspend fun createListing(listing: Listing): Result<String>

    suspend fun updateListing(listing: Listing): Result<Unit>

    /** Used for remove (REMOVED) and for finished deals (SOLD). Listings are never hard-deleted. */
    suspend fun setStatus(listingId: String, status: ListingStatus): Result<Unit>
}
