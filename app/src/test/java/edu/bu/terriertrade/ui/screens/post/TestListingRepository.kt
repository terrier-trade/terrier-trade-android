package edu.bu.terriertrade.ui.screens.post

import edu.bu.terriertrade.data.model.Listing
import edu.bu.terriertrade.data.model.ListingStatus
import edu.bu.terriertrade.data.repository.ListingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

/** A ListingRepository for tests: remembers what was posted and can be told to fail. */
class TestListingRepository(
    var createResult: Result<String> = Result.success("new-1"),
) : ListingRepository {

    var lastCreated: Listing? = null
    var createCalls = 0

    override suspend fun createListing(listing: Listing): Result<String> {
        createCalls++
        lastCreated = listing
        return createResult
    }

    override fun observeActiveListings(): Flow<List<Listing>> = emptyFlow()
    override fun observeListing(listingId: String): Flow<Listing?> = emptyFlow()
    override fun observeListingsByOwner(ownerId: String): Flow<List<Listing>> = emptyFlow()
    override suspend fun updateListing(listing: Listing): Result<Unit> = Result.success(Unit)
    override suspend fun setStatus(listingId: String, status: ListingStatus): Result<Unit> = Result.success(Unit)
}
