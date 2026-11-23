package edu.bu.terriertrade.ui.screens.chat

import androidx.lifecycle.SavedStateHandle
import edu.bu.terriertrade.data.fake.FakeAuthRepository
import edu.bu.terriertrade.data.fake.FakeChatRepository
import edu.bu.terriertrade.data.fake.FakeListingRepository
import edu.bu.terriertrade.data.fake.FakeReviewRepository
import edu.bu.terriertrade.data.fake.FakeUserRepository
import edu.bu.terriertrade.data.model.Review
import edu.bu.terriertrade.data.model.ReviewTag
import edu.bu.terriertrade.navigation.Routes
import edu.bu.terriertrade.testing.MainDispatcherRule
import edu.bu.terriertrade.ui.common.UiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * The review sheet in ChatViewModel: the "Deal done" prompt shows Rate until I've reviewed,
 * Send only turns on once a star is picked, and saving closes the sheet and marks the prompt rated.
 * In the sample data Maya finished a deal with me in c_maya (message c3), and I haven't rated her yet.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModelReviewTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val auth = FakeAuthRepository()
    private val reviews = FakeReviewRepository(auth)

    private fun TestScope.openChat(chatId: String = "c_maya"): ChatViewModel {
        val viewModel = ChatViewModel(
            savedStateHandle = SavedStateHandle(mapOf(Routes.ARG_CHAT_ID to chatId)),
            authRepository = auth,
            chatRepository = FakeChatRepository(auth),
            userRepository = FakeUserRepository(),
            listingRepository = FakeListingRepository(auth),
            reviewRepository = reviews,
        )
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.reviewSheet.collect {} }
        return viewModel
    }

    private fun ChatViewModel.prompt(): MessageUi =
        (uiState.value as UiState.Success).data.messages.first { it.id == "c3" }

    @Test
    fun dealDonePrompt_offersRate_untilIReview() = runTest {
        val prompt = openChat().prompt()

        assertEquals(MessageKind.REVIEW_PROMPT, prompt.kind)
        assertEquals("Deal done with Maya", prompt.text)
        assertFalse(prompt.isRated)
    }

    @Test
    fun send_staysOff_untilAStarIsPicked() = runTest {
        val viewModel = openChat()

        viewModel.openReview("c3")
        assertTrue(viewModel.reviewSheet.value.isOpen)
        assertEquals("Maya", viewModel.reviewSheet.value.partnerFirstName)
        assertFalse(viewModel.reviewSheet.value.canSend)

        viewModel.onStarsChange(4)
        assertTrue(viewModel.reviewSheet.value.canSend)
    }

    @Test
    fun sending_savesTheReview_closesTheSheet_andMarksThePromptRated() = runTest {
        val viewModel = openChat()
        viewModel.openReview("c3")
        viewModel.onStarsChange(5)
        viewModel.onTagToggle(ReviewTag.FRIENDLY)
        viewModel.onTagToggle(ReviewTag.ON_TIME)
        viewModel.onTagToggle(ReviewTag.ON_TIME) // tapped twice, so it's off again
        viewModel.onReviewTextChange("Easy trade")

        viewModel.sendReview()

        val saved = reviews.observeReviewsFor("maya").first().single()
        assertEquals("c3_alex", saved.id)
        assertEquals(5, saved.stars)
        assertEquals(listOf(ReviewTag.FRIENDLY), saved.tags)
        assertEquals("c3", saved.promptId)
        assertFalse(viewModel.reviewSheet.value.isOpen)
        assertTrue(viewModel.prompt().isRated)
    }

    @Test
    fun rateOnAnAlreadyRatedDeal_doesNothing() = runTest {
        val viewModel = openChat()
        viewModel.openReview("c3")
        viewModel.onStarsChange(5)
        viewModel.sendReview()

        viewModel.openReview("c3")

        assertFalse(viewModel.reviewSheet.value.isOpen)
    }

    @Test
    fun reviewedOnAnotherPhone_showsTheReason_andKeepsTheSheetOpen() = runTest {
        val viewModel = openChat()
        viewModel.openReview("c3")
        viewModel.onStarsChange(3)
        // The same account reviewed this deal somewhere else while this sheet was open
        reviews.addReview(Review("", "", "maya", "c_maya", stars = 5, promptId = "c3")).getOrThrow()

        viewModel.sendReview()

        assertEquals("You've already reviewed this deal.", viewModel.reviewSheet.value.error)
        assertTrue(viewModel.reviewSheet.value.isOpen)
    }

    @Test
    fun text_stopsAtTheLimit() = runTest {
        val viewModel = openChat()
        viewModel.openReview("c3")

        viewModel.onReviewTextChange("a".repeat(400))

        assertEquals(300, viewModel.reviewSheet.value.text.length)
    }
}