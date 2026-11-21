package edu.bu.terriertrade.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Room full-text search against a real (in-memory) SQLite database on the emulator.
 * Unit tests use a fake DAO, so this is what proves the FTS table, its sync triggers and the
 * MATCH query actually work.
 */
@RunWith(AndroidJUnit4::class)
class ListingSearchTest {

    private lateinit var database: TerrierTradeDatabase
    private lateinit var dao: ListingDao
    private lateinit var search: RoomListingSearch

    @Before
    fun openDatabase() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            TerrierTradeDatabase::class.java,
        ).build()
        dao = database.listingDao()
        search = RoomListingSearch(dao)
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    private fun row(
        id: String,
        title: String,
        description: String = "",
        status: String = "ACTIVE",
    ) = ListingEntity(
        id = id, ownerId = "maya", type = "ITEM", title = title, price = 10, description = description,
        priceUnit = "NONE", category = "OTHER", condition = "GOOD", serviceMode = null, meetSpot = "GSU",
        photoUrls = "", isbn = null, eventDateMillis = null, quantity = null, status = status, createdAt = 1L,
    )

    private fun found(query: String): Set<String> = runBlocking { search.observeMatchingIds(query).first() }

    private fun save(vararg rows: ListingEntity) = runBlocking { dao.upsertAll(rows.toList()) }

    @Test
    fun startOfAWord_matches() {
        save(row("book", "Calculus textbook"), row("chair", "Desk chair"))

        assertEquals(setOf("book"), found("calc"))
        assertEquals(setOf("book"), found("CALCULUS"))
    }

    @Test
    fun description_isSearchedToo() {
        save(row("calc", "TI-84", description = "Graphing calculator, barely used"))

        assertEquals(setOf("calc"), found("graphing"))
    }

    @Test
    fun everyWordMustMatch() {
        save(row("book", "Calculus textbook"), row("chair", "Desk chair"))

        assertEquals(setOf("book"), found("calculus text"))
        assertEquals(emptySet<String>(), found("calculus chair"))
    }

    @Test
    fun soldAndRemovedListings_areNotFound() {
        save(
            row("live", "Desk lamp"),
            row("sold", "Desk chair", status = "SOLD"),
            row("gone", "Desk fan", status = "REMOVED"),
        )

        assertEquals(setOf("live"), found("desk"))
    }

    /** The index follows edits: the old title stops matching and the new one starts. */
    @Test
    fun editedTitle_isReindexed() {
        save(row("item", "Mini fridge"))
        save(row("item", "Microwave"))

        assertEquals(emptySet<String>(), found("fridge"))
        assertEquals(setOf("item"), found("micro"))
    }

    /** A fresh Browse feed from Firestore drops listings that are gone, and so does search. */
    @Test
    fun replacedFeed_dropsOldListingsFromSearch() {
        save(row("old", "Desk chair"), row("new", "Desk lamp"))

        runBlocking { dao.replaceActive(listOf(row("new", "Desk lamp"))) }

        assertEquals(setOf("new"), found("desk"))
    }

    @Test
    fun nothingTyped_findsNothing() {
        save(row("book", "Calculus textbook"))

        assertEquals(emptySet<String>(), found("   "))
    }
}