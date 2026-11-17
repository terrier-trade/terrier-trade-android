package edu.bu.terriertrade.ui.screens.chat

import androidx.lifecycle.SavedStateHandle
import edu.bu.terriertrade.data.fake.FakeAuthRepository
import edu.bu.terriertrade.data.fake.FakeChatRepository
import edu.bu.terriertrade.data.fake.FakeListingRepository
import edu.bu.terriertrade.data.fake.FakeUserRepository
import edu.bu.terriertrade.data.model.DealItem
import edu.bu.terriertrade.navigation.Routes
import edu.bu.terriertrade.testing.MainDispatcherRule
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
 * The deal sheet in ChatViewModel: which mode it opens in, editing the draft, sending, accepting,
 * showing why a change was refused, and dropping unsent edits when the sheet closes.
 * In the sample data Sam has suggested changes in c_sam, and nothing is waiting in c_nina.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModelDealTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val auth = FakeAuthRepository()
    private val chats = FakeChatRepository(auth)

    private fun TestScope.openSheet(chatId: String): ChatViewModel {
        val viewModel = ChatViewModel(
            savedStateHandle = SavedStateHandle(mapOf(Routes.ARG_CHAT_ID to chatId)),
            authRepository = auth,
            chatRepository = chats,
            userRepository = FakeUserRepository(),
            listingRepository = FakeListingRepository(auth),
        )
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.dealSheet.collect {} }
        viewModel.openDeal()
        return viewModel
    }

    private fun ChatViewModel.window(): DealWindowUi =
        dealSheet.value.window ?: error("The deal sheet isn't showing: ${dealSheet.value}")

    @Test
    fun samsChat_opensInReview() = runTest {
        val viewModel = openSheet("c_sam")

        assertEquals(DealMode.REVIEW, viewModel.window().mode)
        assertEquals("Sam suggested changes", viewModel.window().title)
    }

    @Test
    fun accept_updatesTheDeal_andClosesTheSheet() = runTest {
        val viewModel = openSheet("c_sam")

        viewModel.acceptChanges()

        assertEquals(listOf(DealItem("l3", 40), DealItem("l7", 10)), chats.observeChat("c_sam").first()!!.deal!!.items)
        assertFalse(viewModel.dealSheet.value.isOpen)
    }

    @Test
    fun addingMyLamp_thenSending_suggestsTheChange() = runTest {
        val viewModel = openSheet("c_nina")
        assertEquals(DealMode.EDIT, viewModel.window().mode)
        assertFalse(viewModel.window().canSend)

        viewModel.addToDeal(viewModel.window().addable.first { it.listingId == "m1" })
        assertTrue(viewModel.window().canSend)
        assertEquals(2, viewModel.window().rows.size)

        viewModel.sendChanges()

        val pending = chats.observeChat("c_nina").first()!!.deal!!.pending!!
        assertEquals(listOf(DealItem("l4", 20), DealItem("m1", 12)), pending.items)
        assertFalse(viewModel.dealSheet.value.isOpen)
    }

    @Test
    fun priceBox_keepsOnlyDigits() = runTest {
        val viewModel = openSheet("c_nina")

        viewModel.onPriceChange("l4", "1a5")

        val row = viewModel.window().rows.single()
        assertEquals(15, row.price)
        assertEquals(RowChange.PRICE_CHANGED, row.change)
    }

    @Test
    fun sendingNoRealChange_showsTheReason_andKeepsTheSheetOpen() = runTest {
        val viewModel = openSheet("c_nina")
        viewModel.addToDeal(viewModel.window().addable.first { it.listingId == "m1" })
        viewModel.removeFromDeal("m1") // back to the deal as it is

        viewModel.sendChanges()

        assertEquals("Nothing has changed yet.", viewModel.dealSheet.value.error)
        assertTrue(viewModel.dealSheet.value.isOpen)
    }

    @Test
    fun closing_dropsUnsentEdits() = runTest {
        val viewModel = openSheet("c_nina")
        viewModel.onPriceChange("l4", "15")

        viewModel.closeDeal()
        viewModel.openDeal()

        assertEquals(20, viewModel.window().rows.single().price)
    }
}