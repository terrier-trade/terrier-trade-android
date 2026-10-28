package edu.bu.terriertrade.data

import edu.bu.terriertrade.data.fake.FakeAuthRepository
import edu.bu.terriertrade.data.fake.FakeChatRepository
import edu.bu.terriertrade.data.fake.FakeListingRepository
import edu.bu.terriertrade.data.fake.SampleData
import edu.bu.terriertrade.data.model.Listing
import edu.bu.terriertrade.data.model.ListingStatus
import edu.bu.terriertrade.data.model.ListingType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FakeRepositoriesTest {

    private val auth = FakeAuthRepository()

    @Test
    fun createListing_appearsInActiveListingsForSignedInUser() = runTest {
        val repo = FakeListingRepository(auth)
        val draft = Listing(id = "", ownerId = "", type = ListingType.ITEM, title = "Desk fan", price = 12)

        val id = repo.createListing(draft).getOrThrow()

        val created = repo.observeActiveListings().first().single { it.id == id }
        assertEquals(SampleData.ME, created.ownerId)
    }

    @Test
    fun soldListing_leavesActiveListings() = runTest {
        val repo = FakeListingRepository(auth)

        repo.setStatus("l3", ListingStatus.SOLD)

        assertFalse(repo.observeActiveListings().first().any { it.id == "l3" })
    }

    @Test
    fun openChatWith_reusesExistingChat() = runTest {
        val repo = FakeChatRepository(auth)

        val first = repo.openChatWith("sam").getOrThrow()
        val second = repo.openChatWith("sam").getOrThrow()

        assertEquals(first, second)
    }

    @Test
    fun signOut_hidesChats() = runTest {
        val repo = FakeChatRepository(auth)
        assertTrue(repo.observeChats().first().isNotEmpty())

        auth.signOut()

        assertTrue(repo.observeChats().first().isEmpty())
    }
}
