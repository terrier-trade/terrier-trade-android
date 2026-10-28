package edu.bu.terriertrade.util

import edu.bu.terriertrade.data.model.Listing
import edu.bu.terriertrade.data.model.ListingType
import edu.bu.terriertrade.data.model.PriceUnit
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object PriceFormat {
    /** 45 -> "$45", 20 per hour -> "$20/hr", wanted posts -> "Up to $30", 0 -> "Free". */
    fun format(price: Int, unit: PriceUnit = PriceUnit.NONE, upTo: Boolean = false): String {
        val amount = if (price == 0 && unit == PriceUnit.NONE && !upTo) "Free" else "\$$price"
        val prefix = if (upTo) "Up to " else ""
        return prefix + amount + unit.suffix
    }
}

/** The price line shown on cards and the detail screen. */
fun Listing.priceLabel(): String =
    PriceFormat.format(price, priceUnit, upTo = type == ListingType.WANTED)

object TimeFormat {
    private val dateFormatter = DateTimeFormatter.ofPattern("MMM d", Locale.US)

    /** "now", "5m", "3h", "2d", then a date like "Oct 12" after a week. */
    fun ago(
        timeMillis: Long,
        nowMillis: Long = System.currentTimeMillis(),
        zone: ZoneId = ZoneId.systemDefault(),
    ): String {
        val minutes = (nowMillis - timeMillis).coerceAtLeast(0L) / 60_000L
        return when {
            minutes < 1 -> "now"
            minutes < 60 -> "${minutes}m"
            minutes < 60 * 24 -> "${minutes / 60}h"
            minutes < 60 * 24 * 7 -> "${minutes / (60 * 24)}d"
            else -> Instant.ofEpochMilli(timeMillis).atZone(zone).format(dateFormatter)
        }
    }
}
