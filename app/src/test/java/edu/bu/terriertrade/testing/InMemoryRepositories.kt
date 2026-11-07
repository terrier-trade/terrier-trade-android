package edu.bu.terriertrade.testing

import edu.bu.terriertrade.data.model.Listing
import edu.bu.terriertrade.data.model.ListingStatus
import edu.bu.terriertrade.data.model.Review
import edu.bu.terriertrade.data.model.User
import edu.bu.terriertrade.data.repository.ListingRepository
import edu.bu.terriertrade.data.repository.ReviewRepository
import edu.bu.terriertrade.data.repository.SavedListingsRepository
import edu.bu.terriertrade.data.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/*
 * Small in-memory repositories for ViewModel tests.
 * Each one keeps its data in a MutableStateFlow, so changing `listings`, `users`, `saved` or `reviews`
 * in a test updates the screen the same way a Firestore snapshot would.
 */

class InMemoryListingRepository(initial: List<Listing> = emptyList()) : ListingRepository {
    val listings = MutableStateFlow(initial)

    override fun observeActiveListings(): Flow<List<Listing>> =
        listings.map { all -> all.filter { it.status == ListingStatus.ACTIVE }.sortedByDescending { it.createdAt } }

    override fun observeListing(listingId: String): Flow<Listing?> =
        listings.map { all -> all.find { it.id == listingId } }

    override fun observeListingsByOwner(ownerId: String): Flow<List<Listing>> =
        listings.map { all -> all.filter { it.ownerId == ownerId } }

    override suspend fun createListing(listing: Listing): Result<String> {
        listings.update { it + listing }
        return Result.success(listing.id)
    }

    override suspend fun updateListing(listing: Listing): Result<Unit> {
        listings.update { all -> all.map { if (it.id == listing.id) listing else it } }
        return Result.success(Unit)
    }

    override suspend fun setStatus(listingId: String, status: ListingStatus): Result<Unit> {
        listings.update { all -> all.map { if (it.id == listingId) it.copy(status = status) else it } }
        return Result.success(Unit)
    }
}

class InMemoryUserRepository(initial: List<User> = emptyList()) : UserRepository {
    val users = MutableStateFlow(initial.associateBy { it.id })

    override fun observeUser(userId: String): Flow<User?> = users.map { it[userId] }

    override suspend fun updateProfile(user: User): Result<Unit> {
        users.update { it + (user.id to user) }
        return Result.success(Unit)
    }
}

class InMemorySavedRepository(initial: Set<String> = emptySet()) : SavedListingsRepository {
    val saved = MutableStateFlow(initial)

    override fun observeSavedIds(): Flow<Set<String>> = saved

    override suspend fun toggleSaved(listingId: String) {
        saved.update { if (listingId in it) it - listingId else it + listingId }
    }
}

/**
 * Reviews kept in memory. observeReviewsFor returns only the reviews about that student, newest first,
 * like the Firestore query.
 */
class InMemoryReviewRepository(initial: List<Review> = emptyList()) : ReviewRepository {
    val reviews = MutableStateFlow(initial)

    override fun observeReviewsFor(userId: String): Flow<List<Review>> =
        reviews.map { all -> all.filter { it.toUserId == userId }.sortedByDescending { it.createdAt } }

    override suspend fun addReview(review: Review): Result<Unit> {
        reviews.update { it + review }
        return Result.success(Unit)
    }
}
