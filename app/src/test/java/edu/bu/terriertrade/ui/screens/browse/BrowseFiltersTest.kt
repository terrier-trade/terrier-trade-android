package edu.bu.terriertrade.ui.screens.browse

import edu.bu.terriertrade.data.model.Category
import edu.bu.terriertrade.data.model.ListingType
import edu.bu.terriertrade.ui.model.ListingUi
import edu.bu.terriertrade.ui.model.SellerUi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** The Filters sheet: price, condition and rating limits, sort order, and the badge count. */
class BrowseFiltersTest {

    private fun listing(
        id: String,
        price: Int,
        createdAt: Long,
        rating: Double = 4.0,
        conditionRank: Int? = null,
        type: ListingType = ListingType.ITEM,
    ) = ListingUi(
        id = id, title = id, priceLabel = "$$price", type = type, category = Category.OTHER,
        meetSpot = "GSU", postedAgo = "1h", condition = "", description = "",
        seller = SellerUi(name = "Maya", initials = "M", rating = "$rating", tradeCount = 1),
        price = price, createdAt = createdAt, sellerRating = rating, conditionRank = conditionRank,
    )

    private val cheapOld = listing("cheap-old", price = 5, createdAt = 1, rating = 4.9, conditionRank = 3)
    private val midNew = listing("mid-new", price = 30, createdAt = 3, rating = 4.2, conditionRank = 1)
    private val pricey = listing("pricey", price = 80, createdAt = 2, rating = 4.6, conditionRank = 0)
    private val service = listing("service", price = 15, createdAt = 4, rating = 5.0, type = ListingType.SERVICE)
    private val all = listOf(cheapOld, midNew, pricey, service)

    private fun BrowseFilters.ids() = sort(all.filter { matches(it) }).map { it.id }

    @Test
    fun defaults_keepEverythingNewestFirst() {
        val filters = BrowseFilters()
        assertTrue(filters.isDefault)
        assertEquals(0, filters.activeCount)
        assertEquals(listOf("service", "mid-new", "pricey", "cheap-old"), filters.ids())
    }

    @Test
    fun priceLimit_isInclusiveOfTheCap() {
        val filters = BrowseFilters(price = PriceLimit.UNDER_20)
        assertEquals(listOf("service", "cheap-old"), filters.ids())
        // "Under $50" keeps $49 but not $50
        assertTrue(BrowseFilters(price = PriceLimit.UNDER_50).matches(listing("x", price = 49, createdAt = 0)))
        assertFalse(BrowseFilters(price = PriceLimit.UNDER_50).matches(listing("x", price = 50, createdAt = 0)))
    }

    /** "Good" keeps Good and better; services have no condition, so they're left out. */
    @Test
    fun conditionFilter_keepsThatConditionAndBetter() {
        assertEquals(listOf("mid-new", "pricey"), BrowseFilters(condition = ConditionFilter.GOOD).ids())
        assertEquals(listOf("mid-new", "pricey"), BrowseFilters(condition = ConditionFilter.LIKE_NEW).ids())
    }

    @Test
    fun ratingFilter_keepsTopRatedSellers() {
        assertEquals(listOf("service", "pricey", "cheap-old"), BrowseFilters(rating = RatingFilter.FOUR_HALF).ids())
    }

    @Test
    fun lowestPriceSort_breaksTiesByNewest() {
        val sameA = listing("same-old", price = 10, createdAt = 1)
        val sameB = listing("same-new", price = 10, createdAt = 2)
        val sorted = BrowseFilters(sort = SortOrder.LOWEST_PRICE).sort(listOf(pricey, sameA, sameB))
        assertEquals(listOf("same-new", "same-old", "pricey"), sorted.map { it.id })
    }

    @Test
    fun topRatedSort_putsBestSellersFirst() {
        assertEquals(listOf("service", "cheap-old", "pricey", "mid-new"), BrowseFilters(sort = SortOrder.TOP_RATED).ids())
    }

    /** The badge counts filters, not the sort order. */
    @Test
    fun activeCount_ignoresSort() {
        val filters = BrowseFilters(sort = SortOrder.TOP_RATED, price = PriceLimit.UNDER_50, rating = RatingFilter.FOUR_HALF)
        assertEquals(2, filters.activeCount)
        assertFalse(filters.isDefault)
    }
}