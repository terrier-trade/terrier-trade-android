package edu.bu.terriertrade.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

/** Reads and writes the saved listings. The Flows re-emit whenever the table changes. */
@Dao
abstract class ListingDao {

    @Query("SELECT * FROM listings WHERE status = 'ACTIVE' ORDER BY createdAt DESC")
    abstract fun observeActive(): Flow<List<ListingEntity>>

    @Query("SELECT * FROM listings WHERE id = :id")
    abstract fun observeById(id: String): Flow<ListingEntity?>

    @Query("SELECT * FROM listings WHERE ownerId = :ownerId AND status != 'REMOVED' ORDER BY createdAt DESC")
    abstract fun observeByOwner(ownerId: String): Flow<List<ListingEntity>>

    /**
     * Ids of ACTIVE listings matching an FTS query from toFtsQuery, such as "calc* text*".
     * Room watches both tables, so the result updates when the cache does.
     */
    @Query(
        """
        SELECT listings.id FROM listings
        JOIN listings_fts ON listings.rowid = listings_fts.rowid
        WHERE listings_fts MATCH :ftsQuery AND listings.status = 'ACTIVE'
        """,
    )
    abstract fun observeActiveIdsMatching(ftsQuery: String): Flow<List<String>>

    @Upsert
    abstract suspend fun upsert(listing: ListingEntity)

    @Upsert
    abstract suspend fun upsertAll(listings: List<ListingEntity>)

    @Query("DELETE FROM listings WHERE id = :id")
    abstract suspend fun delete(id: String)

    @Query("DELETE FROM listings WHERE status = 'ACTIVE'")
    abstract suspend fun deleteActive()

    @Query("DELETE FROM listings WHERE ownerId = :ownerId")
    abstract suspend fun deleteByOwner(ownerId: String)

    @Query("UPDATE listings SET status = :status WHERE id = :id")
    abstract suspend fun updateStatus(id: String, status: String)

    /** Swaps the saved Browse feed for a fresh one, so listings that sold or were removed drop out. */
    @Transaction
    open suspend fun replaceActive(listings: List<ListingEntity>) {
        deleteActive()
        upsertAll(listings)
    }

    /** Swaps one student's saved listings for a fresh copy. */
    @Transaction
    open suspend fun replaceOwner(ownerId: String, listings: List<ListingEntity>) {
        deleteByOwner(ownerId)
        upsertAll(listings)
    }
}