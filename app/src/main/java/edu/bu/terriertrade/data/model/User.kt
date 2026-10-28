package edu.bu.terriertrade.data.model

/**
 * A BU student's public profile. Email is stored for sign-in but is never shown to other students.
 * Rating is kept as a running sum so adding a review is one update.
 */
data class User(
    val id: String,
    val name: String,
    val email: String = "",
    val major: String = "",
    val year: String = "",
    val startTerm: String = "",
    val gradTerm: String = "",
    val bio: String = "",
    val meetSpot: String = "",
    val ratingSum: Int = 0,
    val reviewCount: Int = 0,
    val tradeCount: Int = 0,
    val verified: Boolean = false,
) {
    val firstName: String get() = name.trim().substringBefore(' ')

    val initials: String
        get() {
            val parts = name.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
            if (parts.isEmpty()) return "?"
            val first = parts.first().take(1)
            val last = if (parts.size > 1) parts.last().take(1) else ""
            return (first + last).uppercase()
        }

    /** Average stars, or 0.0 when there are no reviews yet. */
    val rating: Double get() = if (reviewCount == 0) 0.0 else ratingSum.toDouble() / reviewCount
}
