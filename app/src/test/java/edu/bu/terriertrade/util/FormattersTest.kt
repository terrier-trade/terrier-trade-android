package edu.bu.terriertrade.util

import edu.bu.terriertrade.data.model.Listing
import edu.bu.terriertrade.data.model.ListingType
import edu.bu.terriertrade.data.model.PriceUnit
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.ZoneOffset

class FormattersTest {

    @Test
    fun price_plainItem() {
        assertEquals("\$45", PriceFormat.format(45))
    }

    @Test
    fun price_zeroIsFree() {
        assertEquals("Free", PriceFormat.format(0))
    }

    @Test
    fun price_perHour() {
        assertEquals("\$20/hr", PriceFormat.format(20, PriceUnit.PER_HOUR))
    }

    @Test
    fun price_wantedPostSaysUpTo() {
        val wanted = Listing(id = "w", ownerId = "u", type = ListingType.WANTED, title = "Bike", price = 80)
        assertEquals("Up to \$80", wanted.priceLabel())
    }

    @Test
    fun timeAgo_buckets() {
        val now = 1_000_000_000_000L
        assertEquals("now", TimeFormat.ago(now - 30_000L, now))
        assertEquals("5m", TimeFormat.ago(now - 5 * 60_000L, now))
        assertEquals("3h", TimeFormat.ago(now - 3 * 3_600_000L, now))
        assertEquals("2d", TimeFormat.ago(now - 2 * 86_400_000L, now))
    }

    @Test
    fun timeAgo_olderThanAWeekShowsDate() {
        // 2026-10-12 12:00 UTC
        val time = 1_791_806_400_000L
        val now = time + 10 * 86_400_000L
        assertEquals("Oct 12", TimeFormat.ago(time, now, ZoneOffset.UTC))
    }
}
