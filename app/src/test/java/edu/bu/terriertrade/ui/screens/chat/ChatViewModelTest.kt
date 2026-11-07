package edu.bu.terriertrade.ui.screens.chat

import androidx.lifecycle.SavedStateHandle
import edu.bu.terriertrade.data.fake.FakeAuthRepository
import edu.bu.terriertrade.data.fake.FakeChatRepository
import edu.bu.terriertrade.data.fake.FakeListingRepository
import edu.bu.terriertrade.data.fake.FakeUserRepository
import edu.bu.terriertrade.data.model.Message
import edu.bu.terriertrade.data.model.MessageType
import edu.bu.terriertrade.navigation.Routes
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
 * One chat: loading the partner, messages and deal bar, sending messages, and how request lines read
 * for each person.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val auth = FakeAuthRepository()

    private fun createViewModel(chatId: String = "c_sam") = ChatViewModel(
        savedStateHandle = SavedStateHandle(mapOf(Routes.ARG_CHAT_ID to chatId)),
        authRepository = auth,
        chatRepository = FakeChatRepository(auth),
        userRepository = FakeUserRepository(),
        listingRepository = FakeListingRepository(auth),
    )

    private fun TestScope.startCollecting(viewModel: ChatViewModel) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }
    }

    private fun ChatViewModel.content(): ChatContent {
        val state = uiState.value
        assertTrue("Expected Success but was $state", state is UiState.Success)
        return (state as UiState.Success<ChatContent>).data
    }

    @Test
    fun loadsPartnerMessagesAndDealBar() = runTest {
        val viewModel = createViewModel()
        startCollecting(viewModel)

        val content = viewModel.content()

        assertEquals("Sam K.", content.partnerName)
        assertEquals("4.9", content.partnerRatingLabel)
        assertEquals(6, content.messages.size)
        assertEquals(MessageKind.EVENT, content.messages.first().kind)
        assertEquals(MessageKind.SUGGESTION, content.messages.last().kind)

        val dealBar = content.dealBar!!
        assertEquals("1 item", dealBar.itemCountLabel)
        assertEquals("You pay \$40", dealBar.totalLabel)
        assertEquals(DealStatus.REVIEW_CHANGES, dealBar.status)
    }

    @Test
    fun send_addsMyMessageAndClearsDraft() = runTest {
        val viewModel = createViewModel()
        startCollecting(viewModel)

        viewModel.onDraftChange("  See you at 3  ")
        viewModel.send()

        val last = viewModel.content().messages.last()
        assertEquals("See you at 3", last.text)
        assertEquals(MessageKind.MINE, last.kind)
        assertEquals("", viewModel.draft.value)
    }

    @Test
    fun send_ignoresBlankText() = runTest {
        val viewModel = createViewModel()
        startCollecting(viewModel)

        viewModel.onDraftChange("   ")
        viewModel.send()

        assertEquals(6, viewModel.content().messages.size)
    }

    @Test
    fun unknownChat_showsError() = runTest {
        val viewModel = createViewModel(chatId = "does_not_exist")
        startCollecting(viewModel)

        assertTrue(viewModel.uiState.value is UiState.Error)
    }

    @Test
    fun requestMessage_saysYouOrTheirName() {
        val request = Message(
            "r1",
            "c1",
            senderId = "sam",
            type = MessageType.REQUEST,
            text = "requested Desk lamp"
        )

        assertEquals("Sam requested Desk lamp", request.toUi(myId = "alex", partnerFirstName = "Sam", nowMillis = 0L).text)
        assertEquals("You requested Desk lamp", request.toUi(myId = "sam", partnerFirstName = "Alex", nowMillis = 0L).text)
    }
}