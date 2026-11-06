package edu.bu.terriertrade.ui.screens.chats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import edu.bu.terriertrade.data.repository.AuthRepository
import edu.bu.terriertrade.data.repository.ChatRepository
import edu.bu.terriertrade.data.repository.UserRepository
import edu.bu.terriertrade.di.appContainer
import edu.bu.terriertrade.ui.common.UiState
import edu.bu.terriertrade.ui.common.asUiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/** Everything the chats list screen needs once data has loaded. */
data class ChatsContent(
    val sections: List<ChatSection>,
    val totalCount: Int,
    val needsReplyCount: Int,
)

class ChatsViewModel(
    authRepository: AuthRepository,
    chatRepository: ChatRepository,
    userRepository: UserRepository,
    private val clock: () -> Long = { System.currentTimeMillis() }
) : ViewModel() {

    // What the user typed in "Search people"
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    // Every chat as a row, with the other person's profile filled in
    @OptIn(ExperimentalCoroutinesApi::class)
    private val rows: Flow<List<ChatRowUi>> =
        combine(authRepository.currentUserId, chatRepository.observeChats()) { me, chats -> me to chats }
            .flatMapLatest { (me, chats) ->
                if (me == null || chats.isEmpty()) {
                    flowOf(emptyList())
                } else {
                    val rowFlows = chats.map { chat ->
                        userRepository.observeUser(chat.partnerId(me))
                            .map { partner -> chat.toRow(me, partner, clock()) }
                    }
                    combine(rowFlows) { it.toList() }
                }
            }

    // The single state the screen draws
    val uiState: StateFlow<UiState<ChatsContent>> =
        combine(rows, _query) { allRows, query ->
            val text = query.trim()
            val visible = if (text.isEmpty()) allRows
            else allRows.filter { it.partnerName.contains(text, ignoreCase = true) }

            ChatsContent(
                sections = visible.toSections(),
                totalCount = allRows.size,
                needsReplyCount = allRows.count { it.group == ChatGroup.NEEDS_REPLY },
            )
        }
            .asUiState()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = UiState.Loading,
            )

    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val container = appContainer
                ChatsViewModel(
                    authRepository = container.authRepository,
                    chatRepository = container.chatRepository,
                    userRepository = container.userRepository,
                )
            }
        }
    }
}