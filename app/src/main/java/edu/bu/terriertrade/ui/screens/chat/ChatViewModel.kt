package edu.bu.terriertrade.ui.screens.chat

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import edu.bu.terriertrade.data.model.Chat
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
import kotlinx.coroutines.launch

class ChatViewModel(
    private val savedStateHandle: SavedStateHandle,
    authRepository: AuthRepository,
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

    /** Watches every listing in the deal, so the total updates if a listing changes. */
    private fun observeDealListings(chat: Chat): Flow<Map<String, Listing>> {
        val ids = chat.deal?.items?.map { it.listingId }.orEmpty()
        if (ids.isEmpty()) return flowOf(emptyMap())
        return combine(ids.map { listingRepository.observeListing(it) }) { listings ->
            listings.filterNotNull().associateBy { it.id }
        }
    }

    companion object {
        private const val KEY_DRAFT = "draft"

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