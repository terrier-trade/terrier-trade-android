package edu.bu.terriertrade.data.local

import edu.bu.terriertrade.data.model.Listing
import edu.bu.terriertrade.data.model.ListingStatus
import edu.bu.terriertrade.data.model.ListingType
import edu.bu.terriertrade.data.repository.DataError
import edu.bu.terriertrade.data.repository.ListingRepository
import edu.bu.terriertrade.testing.FakeListingDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The Room cache in front of Firestore: fresh data is saved, saved data is shown when Firestore fails,
 * and an error only reaches the screen when there's nothing saved to show.
 */
class CachedListingRepositoryTest {

    private val book = Listing(
        id = "book-1", ownerId = "maya", type = ListingType.ITEM, title = "Calculus textbook", price = 35, createdAt = 2L,
    )
    private val lamp = Listing(
        id = "lamp-1", ownerId = "maya", type = ListingType.ITEM, title = "Desk lamp", price = 12, createdAt = 1L,
    )

    private val remote = RemoteListings()
    private val dao = FakeListingDao()
    private val repository = CachedListingRepository(remote, dao)

    @Test
    fun freshListings_areShownAndSaved() = runTest {
        remote.listings.value = listOf(book, lamp)

        val shown = repository.observeActiveListings().first()

        assertEquals(listOf("book-1", "lamp-1"), shown.map { it.id })
        assertNotNull(dao.rows.value["book-1"])
        assertNotNull(dao.rows.value["lamp-1"])
    }

    @Test
    fun remoteFails_showsSavedListings() = runTest {
        dao.upsert(book.toEntity())
        remote.error = DataError("You're offline.")

        val shown = repository.observeActiveListings().first()

        assertEquals("Calculus textbook", shown.single().title)
    }

    @Test
    fun remoteFails_withNothingSaved_passesTheErrorOn() = runTest {
        remote.error = DataError("You're offline.")

        val error = runCatching { repository.observeActiveListings().first() }.exceptionOrNull()

        assertEquals("You're offline.", error?.message)
    }

    @Test
    fun nothingAnywhere_showsAnEmptyList() = runTest {
        assertTrue(repository.observeActiveListings().first().isEmpty())
    }

    /** An empty answer (often Firestore offline with nothing cached) doesn't wipe saved listings. */
    @Test
    fun emptyRemote_keepsSavedListings() = runTest {
        dao.upsert(book.toEntity())

        val shown = repository.observeActiveListings().first()

        assertEquals("book-1", shown.single().id)
        assertNotNull(dao.rows.value["book-1"])
    }

    /** A listing that sold drops out of the saved feed on the next fresh answer. */
    @Test
    fun soldListing_dropsOutOfSavedFeed() = runTest {
        dao.upsertAll(listOf(book.toEntity(), lamp.toEntity()))
        remote.listings.value = listOf(lamp)

        val shown = repository.observeActiveListings().first { it.size == 1 }

        assertEquals("lamp-1", shown.single().id)
        assertNull(dao.rows.value["book-1"])
    }

    @Test
    fun listingGoneFromRemote_isForgottenHere() = runTest {
        dao.upsert(book.toEntity())

        val shown = repository.observeListing("book-1").first { it == null }

        assertNull(shown)
        assertNull(dao.rows.value["book-1"])
    }

    @Test
    fun singleListing_remoteFails_showsSavedCopy() = runTest {
        dao.upsert(book.toEntity())
        remote.error = DataError("You're offline.")

        assertEquals("Calculus textbook", repository.observeListing("book-1").first()?.title)
    }

    @Test
    fun setStatus_updatesTheSavedCopy() = runTest {
        remote.listings.value = listOf(book)
        dao.upsert(book.toEntity())

        repository.setStatus("book-1", ListingStatus.SOLD)

        assertEquals("SOLD", dao.rows.value["book-1"]?.status)
    }

    /** A small Firestore stand-in for these tests. Set [error] to make every read fail. */
    private class RemoteListings : ListingRepository {
        val listings = MutableStateFlow<List<Listing>>(emptyList())
        var error: Throwable? = null

        private fun <T> read(pick: (List<Listing>) -> T): Flow<T> =
            listings.map { all -> error?.let { throw it }; pick(all) }

        override fun observeActiveListings(): Flow<List<Listing>> =
            read { all -> all.filter { it.isActive }.sortedByDescending { it.createdAt } }

        override fun observeListing(listingId: String): Flow<Listing?> = read { all -> all.find { it.id == listingId } }

        override fun observeListingsByOwner(ownerId: String): Flow<List<Listing>> =
            read { all -> all.filter { it.ownerId == ownerId && it.status != ListingStatus.REMOVED } }

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
}