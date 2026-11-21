package edu.bu.terriertrade.data.local

private val WORD = Regex("[\\p{L}\\p{N}]+")

/**
 * Turns what a student typed into an FTS MATCH query, or null when there's nothing to search for.
 *
 * Every word must match, and each can be the start of a word: "calc text" becomes "calc* text*",
 * which finds "Calculus textbook". Only letters and digits are kept, so quotes, dashes and words like
 * OR or NOT (lowercased here) can't break the query or change its meaning.
 */
internal fun toFtsQuery(typed: String): String? {
    val words = WORD.findAll(typed.lowercase()).map { it.value }.toList()
    if (words.isEmpty()) return null
    return words.joinToString(" ") { "$it*" }
}