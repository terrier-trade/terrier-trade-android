package edu.bu.terriertrade.data.local

import edu.bu.terriertrade.data.repository.ListingSearch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

/** ListingSearch backed by Room's full-text index (ListingFtsEntity), so it works offline too. */
class RoomListingSearch(private val dao: ListingDao) : ListingSearch {

    override fun observeMatchingIds(query: String): Flow<Set<String>> {
        val ftsQuery = toFtsQuery(query) ?: return flowOf(emptySet())
        return dao.observeActiveIdsMatching(ftsQuery).map { it.toSet() }
    }
}