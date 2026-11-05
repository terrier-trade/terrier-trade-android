package edu.bu.terriertrade.ui.model

/** The top of a profile: who they are and how they trade. Display-ready text. */
data class ProfileHeaderUi(
    val name: String,
    val initials: String,
    /** "Computer Science · Junior", or blank. */
    val subtitle: String,
    val isVerified: Boolean,
    /** "4.8", or "New" before the first review. */
    val ratingLabel: String,
    val tradeCount: Int,
    /** "'27", or null when the grad term isn't set. */
    val classOf: String?,
    /** "Fall 2024 to May 2027", or null. */
    val buDates: String?,
    val bio: String,
    val meetSpot: String,
)

/** One review someone left after a trade. */
data class ReviewUi(
    val id: String,
    val fromName: String,
    val fromInitials: String,
    val stars: Int,
    val tags: List<String>,
    val text: String,
    val postedAgo: String,
)

/** One of the student's own listings, with whether it has sold. */
data class MyListingUi(
    val listing: ListingUi,
    val isSold: Boolean,
)
