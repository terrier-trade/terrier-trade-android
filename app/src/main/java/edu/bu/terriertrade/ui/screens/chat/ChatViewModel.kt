package edu.bu.terriertrade.ui.screens.chat

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import edu.bu.terriertrade.data.model.Chat
import edu.bu.terriertrade.data.model.DealItem
import edu.bu.terriertrade.data.model.Listing
import edu.bu.terriertrade.data.repository.AuthRepository
import edu.bu.terriertrade.data.repository.ChatRepository
import edu.bu.terriertrade.data.repository.ListingRepository
import edu.bu.terriertrade.data.repository.UserRepository
import edu.bu.terriertrade.di.appContainer
import edu.bu.terriertrade.navigation.Routes
import edu.bu.terriertrade.ui.common.UiState
import edu.bu.terriertrade.ui.common.asUiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class ChatViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val authRepository: AuthRepository,
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository,
    private val listingRepository: ListingRepository,
    private val clock: () -> Long = { System.currentTimeMillis() },
) : ViewModel() {

    // The chat id from the route chat/{chatId}
    private val chatId: String = checkNotNull(savedStateHandle.get<String>(Routes.ARG_CHAT_ID)) {
        "ChatViewModel needs a chat id"
    }

    // What you're typing. Kept in SavedStateHandle so it survives rotation and the app being closed in the background.
    val draft: StateFlow<String> = savedStateHandle.getStateFlow(KEY_DRAFT, "")

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<UiState<ChatContent>> =
        combine(authRepository.currentUserId, chatRepository.observeChat(chatId)) { me, chat -> me to chat }
            .flatMapLatest { (me, chat) ->
                if (me == null || chat == null) {
                    flow<ChatContent> { throw IllegalStateException("This chat isn't available") }
                } else {
                    combine(
                        userRepository.observeUser(chat.partnerId(me)),
                        chatRepository.observeMessages(chatId),
                        observeDealListings(chat),
                    ) { partner, messages, listings ->
                        buildChatContent(me, chat, partner, messages, listings, clock())
                    }
                }
            }
            .asUiState()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = UiState.Loading,
            )

    fun onDraftChange(text: String) {
        savedStateHandle[KEY_DRAFT] = text
    }

    fun send() {
        val text = draft.value.trim()
        if (text.isEmpty()) return

        savedStateHandle[KEY_DRAFT] = "" // clear right away so it feels instant
        viewModelScope.launch {
            chatRepository.sendMessage(chatId, text).onFailure {
                savedStateHandle[KEY_DRAFT] = text // put the text back so nothing is lost
            }
        }
    }

    // Deal window ---------------------------------------------------------------------------------

    private val dealOpen = MutableStateFlow(false)

    /** What I'm changing in EDIT mode, or null while I haven't changed anything. */
    private val draftItems = MutableStateFlow<List<DealItem>?>(null)

    private val dealProgress = MutableStateFlow(DealProgress())

    /**
     * The deal sheet. It only listens to Firestore while it's open, and it closes by itself
     * if the deal disappears or you sign out.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val dealSheet: StateFlow<DealSheetState> =
        dealOpen
            .flatMapLatest { open -> if (open) observeDealSheet() else flowOf(DealSheetState()) }
            // The deal can end while the sheet is open, when the other person cancels or finishes it.
            // Forget that it was open, so the sheet doesn't pop up by itself when a new deal starts.
            .onEach { if (!it.isOpen) dealOpen.value = false }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = DealSheetState(),
            )

    fun openDeal() {
        draftItems.value = null
        dealProgress.value = DealProgress()
        dealOpen.value = true
    }

    /** Closing throws the draft away: unsent changes are cancelled. */
    fun closeDeal() {
        dealOpen.value = false
        draftItems.value = null
    }

    /** Back from Change deal: drops unsent edits but keeps the sheet open. */
    fun discardChanges() {
        draftItems.value = null
        dealProgress.value = DealProgress()
    }

    /** Adds a listing at its listed price. The price can be changed afterwards. */
    fun addToDeal(item: AddableListingUi) =
        editDraft { items -> items + DealItem(item.listingId, item.price) }

    fun removeFromDeal(listingId: String) =
        editDraft { items -> items.filterNot { it.listingId == listingId } }

    /** Whole dollars only: anything that isn't a digit is ignored, and an empty box means $0. */
    fun onPriceChange(listingId: String, text: String) {
        val price = text.filter(Char::isDigit).take(MAX_PRICE_DIGITS).toIntOrNull() ?: 0
        editDraft { items -> items.map { if (it.listingId == listingId) it.copy(agreedPrice = price) else it } }
    }

    fun sendChanges() {
        val items = draftItems.value ?: return
        runDealAction { chatRepository.proposeDeal(chatId, items) }
    }

    fun acceptChanges() = runDealAction { chatRepository.acceptProposal(chatId) }

    /** Declines their changes, or withdraws mine. */
    fun declineChanges() = runDealAction { chatRepository.declineProposal(chatId) }

    /** Marks the deal as done from my side. The other person's tap finishes it. */
    fun markDone() = runDealAction { chatRepository.markDealDone(chatId) }

    /** Calls the deal off for both people. The screen asks "Are you sure?" first, so this only runs after a yes. */
    fun cancelDeal() = runDealAction { chatRepository.cancelDeal(chatId) }

    /** What the open sheet shows. Rebuilt when the chat, a listing, the draft or the saving state changes. */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeDealSheet(): Flow<DealSheetState> =
        combine(authRepository.currentUserId, chatRepository.observeChat(chatId)) { me, chat -> me to chat }
            .flatMapLatest { (me, chat) ->
                val deal = chat?.deal
                if (me == null || chat == null || deal == null) return@flatMapLatest flowOf(DealSheetState())
                val partnerId = chat.partnerId(me)

                // Both owner lists only hold listings that are still up. They fill the "add" lists.
                val ownerListings = combine(
                    listingRepository.observeListingsByOwner(me),
                    listingRepository.observeListingsByOwner(partnerId),
                ) { mine, theirs -> mine to theirs }

                combine(
                    userRepository.observeUser(partnerId),
                    ownerListings,
                    observeDealListings(chat),
                    draftItems,
                    dealProgress,
                ) { partner, (mine, theirs), inDeal, draft, progress ->
                    DealSheetState(
                        isOpen = true,
                        window = buildDealWindow(
                            myId = me,
                            partnerFirstName = partner?.firstName ?: "them",
                            deal = deal,
                            draft = draft,
                            // Deal items found by id win, so a removed listing still has its title and status
                            listings = (mine + theirs).associateBy { it.id } + inDeal,
                            myListings = mine,
                            partnerListings = theirs,
                        ),
                        isSaving = progress.isSaving,
                        error = progress.error,
                    )
                }
            }
            .catch { emit(DealSheetState()) }

    /** Changes the draft, starting from the deal as it is the first time. Does nothing outside EDIT mode. */
    private fun editDraft(change: (List<DealItem>) -> List<DealItem>) {
        val window = dealSheet.value.window ?: return
        if (window.mode != DealMode.EDIT) return
        // In EDIT mode the rows are exactly the items being edited
        val current = draftItems.value ?: window.rows.map { DealItem(it.listingId, it.price) }
        draftItems.value = change(current)
        dealProgress.value = DealProgress() // an old error goes away once they change something
    }

    /** Saves one deal change. When it works, the sheet closes and the chat shows the new message. */
    private fun runDealAction(action: suspend () -> Result<Unit>) {
        if (dealProgress.value.isSaving) return
        dealProgress.value = DealProgress(isSaving = true)
        viewModelScope.launch {
            action()
                .onSuccess {
                    dealProgress.value = DealProgress()
                    closeDeal()
                }
                .onFailure { error ->
                    dealProgress.value = DealProgress(error = error.message ?: "Couldn't update the deal. Try again.")
                }
        }
    }

    private data class DealProgress(val isSaving: Boolean = false, val error: String? = null)

    /**
     * Watches every listing in the deal and in any waiting changes, looked up by id.
     * Unlike the owner lists, this still finds removed listings, so they keep their real title.
     */
    private fun observeDealListings(chat: Chat): Flow<Map<String, Listing>> {
        val deal = chat.deal ?: return flowOf(emptyMap())
        val ids = (deal.items + deal.pending?.items.orEmpty()).map { it.listingId }.distinct()
        if (ids.isEmpty()) return flowOf(emptyMap())
        return combine(ids.map { listingRepository.observeListing(it) }) { listings ->
            listings.filterNotNull().associateBy { it.id }
        }
    }

    companion object {
        private const val KEY_DRAFT = "draft"
        private const val MAX_PRICE_DIGITS = 5

        val Factory = viewModelFactory {
            initializer {
                val container = appContainer
                ChatViewModel(
                    savedStateHandle = createSavedStateHandle(),
                    authRepository = container.authRepository,
                    chatRepository = container.chatRepository,
                    userRepository = container.userRepository,
                    listingRepository = container.listingRepository,
                )
            }
        }
    }
}