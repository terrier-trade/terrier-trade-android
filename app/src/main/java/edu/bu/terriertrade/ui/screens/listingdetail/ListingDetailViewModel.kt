package edu.bu.terriertrade.ui.screens.listingdetail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import edu.bu.terriertrade.data.model.Listing
import edu.bu.terriertrade.data.model.ListingStatus
import edu.bu.terriertrade.data.model.User
import edu.bu.terriertrade.data.repository.ChatRepository
import edu.bu.terriertrade.data.repository.ListingRepository
import edu.bu.terriertrade.data.repository.SavedListingsRepository
import edu.bu.terriertrade.data.repository.UserRepository
import edu.bu.terriertrade.di.appContainer
import edu.bu.terriertrade.ui.model.toListingUi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Loads one listing live from the repository, with its seller and whether it's hearted.
 * Removed listings show as "gone".
 */
class ListingDetailViewModel(
    private val listingRepository: ListingRepository,
    private val userRepository: UserRepository,
    private val savedRepository: SavedListingsRepository,
    private val chatRepository: ChatRepository,
    private val now: () -> Long = System::currentTimeMillis,
) : ViewModel() {

    var uiState by mutableStateOf(ListingDetailUiState())
        private set

    private var loadedId: String? = null
    private var loadJob: Job? = null

    // The real Listing behind the screen. The UI only gets ListingUi (display text),
    // but a request needs the owner, price, type and status.
    private var currentListing: Listing? = null

    fun load(listingId: String) {
        if (listingId == loadedId) return
        loadedId = listingId
        loadJob?.cancel()
        uiState = ListingDetailUiState()
        currentListing = null

        loadJob = viewModelScope.launch {
            combine(observeWithOwner(listingId), savedRepository.observeSavedIds()) { (listing, owner), saved ->
                Triple(listing, owner, saved)
            }
                .catch { uiState = uiState.copy(listing = null, notFound = true) }
                .collect { (listing, owner, saved) ->
                    currentListing = listing?.takeIf { it.status != ListingStatus.REMOVED }
                    val shown = listing?.takeIf { it.status != ListingStatus.REMOVED }?.toListingUi(owner, now())
                    uiState = uiState.copy(
                        listing = shown,
                        notFound = shown == null,
                        isFavorite = listingId in saved,
                    )
                }
        }
    }

    fun onToggleFavorite() {
        val id = loadedId ?: return
        viewModelScope.launch { savedRepository.toggleSaved(id) }
    }

    /**
     * Request (or "I can help" on a Looking for post). Opens or reuses the chat with the owner
     * and starts a deal with this listing, then signals the screen to open the chat.
     * See RequestRules for what happens when a deal already exists.
     */
    fun onRequest() {
        val listing = currentListing ?: return
        if (uiState.isRequesting) return // ignore double taps while saving

        uiState = uiState.copy(isRequesting = true, requestError = null)
        viewModelScope.launch {
            chatRepository.requestListing(listing)
                .onSuccess { result ->
                    uiState = uiState.copy(isRequesting = false, openChatId = result.chatId)
                }
                .onFailure { error ->
                    uiState = uiState.copy(
                        isRequesting = false,
                        requestError = error.message ?: "Couldn't send your request. Try again.",
                    )
                }
        }
    }

    /** Called by the screen once it has opened the chat, so the signal fires only once. */
    fun onChatOpened() {
        uiState = uiState.copy(openChatId = null)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeWithOwner(listingId: String): Flow<Pair<Listing?, User?>> =
        listingRepository.observeListing(listingId).flatMapLatest { listing ->
            if (listing == null) flowOf(null to null)
            else userRepository.observeUser(listing.ownerId).map { owner -> listing to owner }
        }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                ListingDetailViewModel(
                    listingRepository = appContainer.listingRepository,
                    userRepository = appContainer.userRepository,
                    savedRepository = appContainer.savedListingsRepository,
                    chatRepository = appContainer.chatRepository,
                )
            }
        }
    }
}
