package edu.bu.terriertrade.data.repository

import kotlinx.coroutines.flow.Flow

/** Search over the listings saved on the phone. Browse uses it for the search bar. */
interface ListingSearch {
    /**
     * Ids of ACTIVE listings whose title or description contains every word of [query],
     * where a word may be the start of a longer one ("calc" finds "Calculus").
     * Re-emits when the saved listings change. Emits an empty set when [query] has no words.
     */
    fun observeMatchingIds(query: String): Flow<Set<String>>
}