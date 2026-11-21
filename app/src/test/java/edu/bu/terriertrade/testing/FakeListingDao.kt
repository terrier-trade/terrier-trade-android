package edu.bu.terriertrade.testing

import edu.bu.terriertrade.data.local.ListingDao
import edu.bu.terriertrade.data.local.ListingEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/**
 * A ListingDao kept in memory, so cache tests run without a real Room database.
 * `rows` is the whole table; changing it re-emits every observe Flow, like Room does.
 * replaceActive and replaceOwner come from ListingDao, so they run the same steps as the real DAO.
 */
class FakeListingDao : ListingDao() {
    val rows = MutableStateFlow<Map<String, ListingEntity>>(emptyMap())

    override fun observeActive(): Flow<List<ListingEntity>> =
        rows.map { table -> table.values.filter { it.status == "ACTIVE" }.sortedByDescending { it.createdAt } }

    override fun observeById(id: String): Flow<ListingEntity?> = rows.map { it[id] }

    override fun observeByOwner(ownerId: String): Flow<List<ListingEntity>> =
        rows.map { table ->
            table.values.filter { it.ownerId == ownerId && it.status != "REMOVED" }.sortedByDescending { it.createdAt }
        }

    override suspend fun upsert(listing: ListingEntity) {
        rows.update { it + (listing.id to listing) }
    }

    override suspend fun upsertAll(listings: List<ListingEntity>) {
        rows.update { table -> table + listings.associateBy { it.id } }
    }

    override suspend fun delete(id: String) {
        rows.update { it - id }
    }

    override suspend fun deleteActive() {
        rows.update { table -> table.filterValues { it.status != "ACTIVE" } }
    }

    override suspend fun deleteByOwner(ownerId: String) {
        rows.update { table -> table.filterValues { it.ownerId != ownerId } }
    }

    override suspend fun updateStatus(id: String, status: String) {
        rows.update { table ->
            val row = table[id] ?: return@update table
            table + (id to row.copy(status = status))
        }
    }
}