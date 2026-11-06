package edu.bu.terriertrade.ui.model

import edu.bu.terriertrade.data.model.Review
import edu.bu.terriertrade.data.model.User
import edu.bu.terriertrade.util.TimeFormat
import java.time.ZoneId
import java.util.Locale

/*
 * User and Review → display models for the profile screens. Plain Kotlin, so it's unit tested.
 */

fun User.toProfileHeaderUi(): ProfileHeaderUi = ProfileHeaderUi(
    name = name,
    initials = initials,
    subtitle = listOf(major, year).filter { it.isNotBlank() }.joinToString(" · "),
    isVerified = verified,
    ratingLabel = if (reviewCount == 0) "New" else String.format(Locale.US, "%.1f", rating),
    tradeCount = tradeCount,
    classOf = Regex("\\d{4}").find(gradTerm)?.value?.let { "'" + it.takeLast(2) },
    buDates = if (startTerm.isNotBlank() && gradTerm.isNotBlank()) "$startTerm to $gradTerm" else null,
    bio = bio,
    meetSpot = meetSpot,
)

fun Review.toReviewUi(
    from: User?,
    nowMillis: Long = System.currentTimeMillis(),
    zone: ZoneId = ZoneId.systemDefault(),
): ReviewUi = ReviewUi(
    id = id,
    fromName = from?.name?.takeIf { it.isNotBlank() } ?: "A BU student",
    fromInitials = from?.initials ?: "BU",
    stars = stars,
    tags = tags.map { it.label },
    text = text,
    postedAgo = TimeFormat.ago(createdAt, nowMillis, zone).let { if (it == "now") "just now" else it },
)
