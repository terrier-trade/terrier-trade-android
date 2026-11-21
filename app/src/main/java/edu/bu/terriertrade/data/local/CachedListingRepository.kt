package edu.bu.terriertrade.data.local

import edu.bu.terriertrade.data.model.Listing
import edu.bu.terriertrade.data.model.ListingStatus
import edu.bu.terriertrade.data.repository.ListingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

/**
 * A ListingRepository that shows listings saved on the phone (Room) first, and keeps them
 * up to date from [remote] (Firestore) in the background.
 *
 * - Screens always read from Room, so Browse, listing detail and profiles work offline
 *   with whatever was last loaded.
 * - While a screen is open, every fresh value from Firestore is written into Room,
 *   and Room's Flow pushes it to the screen.
 * - If Firestore fails and nothing is saved yet, the error reaches the ViewModel,
 *   so the screen shows its error state with Try again. If something is saved, it's shown instead.
 * - Writes go straight to Firestore. On success Room is updated too, so the change shows right away.
 */
class CachedListingRepository(
    private val remote: ListingRepository,
    private val dao: ListingDao,
) : ListingRepository {

    override fun observeActiveListings(): Flow<List<Listing>> = cacheFirst(
        cached = dao.observeActive().map { rows -> rows.mapNotNull { it.toListing() } },
        hasData = { it.isNotEmpty() },
        sync = { remoteIsEmpty ->
            remote.observeActiveListings().collect { fresh ->
                // An empty answer while offline usually means Firestore had nothing cached,
                // not that every listing is gone, so it never wipes what's saved here.
                if (fresh.isNotEmpty()) dao.replaceActive(fresh.map { it.toEntity() }) else remoteIsEmpty()
            }
        },
    )

    override fun observeListing(listingId: String): Flow<Listing?> = cacheFirst(
        cached = dao.observeById(listingId).map { it?.toListing() },
        hasData = { it != null },
        sync = { remoteIsEmpty ->
            remote.observeListing(listingId).collect { fresh ->
                if (fresh != null) {
                    dao.upsert(fresh.toEntity())
                } else {
                    // Gone from Firestore: forget it here too, so the screen shows "This listing is gone".
                    dao.delete(listingId)
                    remoteIsEmpty()
                }
            }
        },
    )

    override fun observeListingsByOwner(ownerId: String): Flow<List<Listing>> = cacheFirst(
        cached = dao.observeByOwner(ownerId).map { rows -> rows.mapNotNull { it.toListing() } },
        hasData = { it.isNotEmpty() },
        sync = { remoteIsEmpty ->
            remote.observeListingsByOwner(ownerId).collect { fresh ->
                if (fresh.isNotEmpty()) dao.replaceOwner(ownerId, fresh.map { it.toEntity() }) else remoteIsEmpty()
            }
        },
    )

    // Firestore picks the id and createdAt, so the new listing reaches Room through the live feed.
    override suspend fun createListing(listing: Listing): Result<String> = remote.createListing(listing)

    override suspend fun updateListing(listing: Listing): Result<Unit> =
        remote.updateListing(listing).onSuccess { dao.upsert(listing.toEntity()) }

    override suspend fun setStatus(listingId: String, status: ListingStatus): Result<Unit> =
        remote.setStatus(listingId, status).onSuccess { dao.updateStatus(listingId, status.name) }

    /**
     * Emits [cached] values while [sync] copies Firestore into Room in the background.
     *
     * Until Firestore has answered, an empty cache is held back instead of emitted, so a screen
     * shows its loading spinner rather than flashing "nothing here" on first launch.
     * [sync] calls its argument when Firestore answers with nothing; when it answers with data,
     * that data lands in Room and comes through [cached] by itself.
     */
    private fun <T> cacheFirst(
        cached: Flow<T>,
        hasData: (T) -> Boolean,
        sync: suspend (remoteIsEmpty: () -> Unit) -> Unit,
    ): Flow<T> = channelFlow {
        val remoteAnswered = MutableStateFlow(false)

        launch {
            try {
                sync { remoteAnswered.value = true }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Nothing saved to fall back on: fail the Flow so the screen shows its error state.
                if (!hasData(cached.first())) throw e
                // Otherwise keep showing what's saved. The offline banner explains why it may be old.
                remoteAnswered.value = true
            }
        }

        combine(cached, remoteAnswered) { value, answered -> value to answered }
            .filter { (value, answered) -> answered || hasData(value) }
            .collect { (value, _) -> send(value) }
    }
}