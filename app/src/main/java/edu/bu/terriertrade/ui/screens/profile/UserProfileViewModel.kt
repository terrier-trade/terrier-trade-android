package edu.bu.terriertrade.ui.screens.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import edu.bu.terriertrade.data.model.ListingStatus
import edu.bu.terriertrade.data.repository.AuthRepository
import edu.bu.terriertrade.data.repository.ChatRepository
import edu.bu.terriertrade.data.repository.ListingRepository
import edu.bu.terriertrade.data.repository.ReviewRepository
import edu.bu.terriertrade.data.repository.SavedListingsRepository
import edu.bu.terriertrade.data.repository.UserRepository
import edu.bu.terriertrade.di.appContainer
import edu.bu.terriertrade.ui.model.observeUsers
import edu.bu.terriertrade.ui.model.toListingUi
import edu.bu.terriertrade.ui.model.toProfileHeaderUi
import edu.bu.terriertrade.ui.model.toReviewUi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Another student's public profile: who they are, what they're selling, and their reviews.
 * "Open chat" starts (or reopens) a chat with them through ChatRepository.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class UserProfileViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val listingRepository: ListingRepository,
    private val savedRepository: SavedListingsRepository,
    private val reviewRepository: ReviewRepository,
    private val chatRepository: ChatRepository,
    private val now: () -> Long = System::currentTimeMillis,
) : ViewModel() {

    var uiState by mutableStateOf(UserProfileUiState())
        private set

    private var loadedId: String? = null
    private var loadJob: Job? = null

    fun load(userId: String) {
        if (userId == loadedId) return
        loadedId = userId
        loadJob?.cancel()
        uiState = UserProfileUiState(isMe = authRepository.currentUserId.value == userId)

        val reviews = reviewRepository.observeReviewsFor(userId).flatMapLatest { list ->
            userRepository.observeUsers(list.map { it.fromUserId }).map { authors ->
                list.map { it.toReviewUi(authors[it.fromUserId], now()) }
            }
        }

        loadJob = viewModelScope.launch {
            combine(
                userRepository.observeUser(userId),
                listingRepository.observeListingsByOwner(userId),
                reviews,
                savedRepository.observeSavedIds(),
            ) { user, listings, reviewList, saved ->
                val time = now()
                uiState.copy(
                    isLoading = false,
                    notFound = user == null,
                    header = user?.toProfileHeaderUi(),
                    listings = listings
                        .filter { it.status == ListingStatus.ACTIVE }
                        .mapNotNull { it.toListingUi(user, time) },
                    reviews = reviewList,
                    favoriteIds = saved,
                )
            }
                .catch { uiState = uiState.copy(isLoading = false, notFound = true) }
                .collect { uiState = it }
        }
    }

    fun onToggleFavorite(listingId: String) {
        viewModelScope.launch { savedRepository.toggleSaved(listingId) }
    }

    /**
     * "Open chat" on someone's profile: finds or creates the chat with them, then signals the
     * screen to open it. No deal is started here; deals start from Request on a listing.
     */
    fun onOpenChat() {
        val userId = loadedId ?: return
        if (uiState.isOpeningChat || uiState.isMe) return // ignore double taps, and no chat with yourself

        uiState = uiState.copy(isOpeningChat = true, chatError = null)
        viewModelScope.launch {
            chatRepository.openChatWith(userId)
                .onSuccess { chatId ->
                    uiState = uiState.copy(isOpeningChat = false, openChatId = chatId)
                }
                .onFailure { error ->
                    uiState = uiState.copy(
                        isOpeningChat = false,
                        chatError = error.message ?: "Couldn't open a chat. Try again.",
                    )
                }
        }
    }

    /** Called by the screen once it has opened the chat, so the signal fires only once. */
    fun onChatOpened() {
        uiState = uiState.copy(openChatId = null)
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                UserProfileViewModel(
                    authRepository = appContainer.authRepository,
                    userRepository = appContainer.userRepository,
                    listingRepository = appContainer.listingRepository,
                    savedRepository = appContainer.savedListingsRepository,
                    reviewRepository = appContainer.reviewRepository,
                    chatRepository = appContainer.chatRepository,
                )
            }
        }
    }
}
