package edu.bu.terriertrade.ui.screens.chats

import edu.bu.terriertrade.data.fake.FakeAuthRepository
import edu.bu.terriertrade.data.fake.FakeChatRepository
import edu.bu.terriertrade.data.fake.FakeUserRepository
import edu.bu.terriertrade.testing.MainDispatcherRule
import edu.bu.terriertrade.ui.common.UiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * The chat list: sorting chats into Needs your reply, Active deals and Earlier, searching by name, and
 * showing nothing when signed out.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ChatsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val auth = FakeAuthRepository()

    private fun createViewModel() = ChatsViewModel(
        authRepository = auth,
        chatRepository = FakeChatRepository(auth),
        userRepository = FakeUserRepository(),
    )

    /** Starts listening to uiState, like a screen would, so the ViewModel starts working. */
    private fun TestScope.startCollecting(viewModel: ChatsViewModel) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }
    }

    /** Reads the loaded content, failing the test if the state isn't Success. */
    private fun ChatsViewModel.content(): ChatsContent {
        val state = uiState.value
        assertTrue("Expected Success but was $state", state is UiState.Success)
        return (state as UiState.Success<ChatsContent>).data
    }

    @Test
    fun chats_areSortedIntoTheRightGroups() = runTest {
        val viewModel = createViewModel()
        startCollecting(viewModel)

        val content = viewModel.content()

        assertEquals(
            listOf(ChatGroup.NEEDS_REPLY, ChatGroup.ACTIVE, ChatGroup.EARLIER),
            content.sections.map { it.group },
        )
        val samRow = content.sections[0].rows.single()
        assertEquals("c_sam", samRow.chatId)
        assertEquals(DealTag.REVIEW_CHANGES, samRow.dealTag)
        assertEquals(1, content.needsReplyCount)
        assertEquals(3, content.totalCount)
    }

    @Test
    fun search_showsOnlyMatchingNames() = runTest {
        val viewModel = createViewModel()
        startCollecting(viewModel)

        viewModel.onQueryChange("nina")

        val content = viewModel.content()
        val names = content.sections.flatMap { it.rows }.map { it.partnerName }
        assertEquals(listOf("Nina P."), names)
        assertEquals(3, content.totalCount) // total still counts every chat
    }

    @Test
    fun signedOut_showsNoChats() = runTest {
        val viewModel = createViewModel()
        startCollecting(viewModel)

        auth.signOut()

        assertTrue(viewModel.content().sections.isEmpty())
    }
}