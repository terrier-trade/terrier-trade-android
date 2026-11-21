package edu.bu.terriertrade.ui.screens.profile

import edu.bu.terriertrade.data.fake.FakeAuthRepository
import edu.bu.terriertrade.data.fake.FakeChatRepository
import edu.bu.terriertrade.data.fake.FakeListingRepository
import edu.bu.terriertrade.data.fake.FakeReviewRepository
import edu.bu.terriertrade.data.fake.FakeSavedListingsRepository
import edu.bu.terriertrade.data.fake.FakeUserRepository
import edu.bu.terriertrade.data.fake.SampleData
import edu.bu.terriertrade.data.repository.DataError
import edu.bu.terriertrade.data.repository.ListingRepository
import edu.bu.terriertrade.testing.InMemoryListingRepository
import edu.bu.terriertrade.testing.MainDispatcherRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * "Open chat" on another student's profile. Uses the fake repositories with the sample data:
 * signed in as "alex", who already has a chat with Sam ("c_sam") and none with Leo.
 */
class UserProfileViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val auth = FakeAuthRepository()
    private val chatRepo = FakeChatRepository(auth)

    private fun viewModel(
        listingRepository: ListingRepository = FakeListingRepository(auth),
    ) = UserProfileViewModel(
        authRepository = auth,
        userRepository = FakeUserRepository(),
        listingRepository = listingRepository,
        savedRepository = FakeSavedListingsRepository(),
        reviewRepository = FakeReviewRepository(),
        chatRepository = chatRepo,
    )

    /** A failed load shows an error with retry, not "Profile not found". */
    @Test
    fun loadFails_showsErrorNotNotFound() {
        val listings = InMemoryListingRepository().apply { observeError = DataError("You're offline.") }
        val vm = viewModel(listingRepository = listings).apply { load("sam") }

        assertFalse(vm.uiState.isLoading)
        assertEquals("You're offline.", vm.uiState.errorMessage)
        assertFalse(vm.uiState.notFound)
    }

    @Test
    fun retry_afterError_loadsProfile() {
        val listings = InMemoryListingRepository().apply { observeError = DataError("You're offline.") }
        val vm = viewModel(listingRepository = listings).apply { load("sam") }

        listings.observeError = null
        vm.retry()

        assertNull(vm.uiState.errorMessage)
        assertNotNull(vm.uiState.header)
    }

    /** An existing chat is reused, not duplicated. */
    @Test
    fun openChat_existingChat_signalsIt() {
        val vm = viewModel().apply { load("sam") }

        vm.onOpenChat()

        assertEquals("c_sam", vm.uiState.openChatId)
    }

    /** A first chat with someone is created, and it starts without a deal. */
    @Test
    fun openChat_newPerson_createsChatWithoutDeal() = runTest {
        val vm = viewModel().apply { load("leo") }

        vm.onOpenChat()

        val chatId = vm.uiState.openChatId!!
        val chat = chatRepo.observeChat(chatId).first()!!
        assertTrue("leo" in chat.participantIds)
        assertNull(chat.deal)
    }

    /** On your own profile there's no Open chat, so nothing happens. */
    @Test
    fun openChat_onOwnProfile_doesNothing() {
        val vm = viewModel().apply { load(SampleData.ME) }

        vm.onOpenChat()

        assertTrue(vm.uiState.isMe)
        assertNull(vm.uiState.openChatId)
    }

    /** Once the screen has opened the chat, the signal is cleared. */
    @Test
    fun chatOpened_clearsTheSignal() {
        val vm = viewModel().apply { load("sam") }
        vm.onOpenChat()

        vm.onChatOpened()

        assertNull(vm.uiState.openChatId)
    }
}