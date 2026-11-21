package edu.bu.terriertrade.testing

import edu.bu.terriertrade.data.model.ListingStatus
import edu.bu.terriertrade.data.repository.ListingSearch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val WORD = Regex("[\\p{L}\\p{N}]+")

/**
 * The same rule as Room's full-text search, for tests: every word of [query] must be the start
 * of some word in [text], ignoring case. "calc text" matches "Calculus textbook".
 */
fun matchesEveryWord(text: String, query: String): Boolean {
    val words = WORD.findAll(text.lowercase()).map { it.value }.toList()
    val wanted = WORD.findAll(query.lowercase()).map { it.value }.toList()
    return wanted.isNotEmpty() && wanted.all { term -> words.any { it.startsWith(term) } }
}

/**
 * ListingSearch over an [InMemoryListingRepository], matching title and description like the Room version.
 * Live: changing `listings.listings` re-emits. Set [error] to make search fail.
 */
class InMemoryListingSearch(private val listings: InMemoryListingRepository) : ListingSearch {
    var error: Throwable? = null

    override fun observeMatchingIds(query: String): Flow<Set<String>> =
        listings.listings.map { all ->
            error?.let { throw it }
            all.filter { it.status == ListingStatus.ACTIVE && matchesEveryWord("${it.title} ${it.description}", query) }
                .map { it.id }
                .toSet()
        }
}