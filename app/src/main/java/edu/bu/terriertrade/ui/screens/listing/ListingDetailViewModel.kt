package edu.bu.terriertrade.ui.screens.listing

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
    private val now: () -> Long = System::currentTimeMillis,
) : ViewModel() {

    var uiState by mutableStateOf(ListingDetailUiState())
        private set

    private var loadedId: String? = null
    private var loadJob: Job? = null

    fun load(listingId: String) {
        if (listingId == loadedId) return
        loadedId = listingId
        loadJob?.cancel()
        uiState = ListingDetailUiState()

        loadJob = viewModelScope.launch {
            combine(observeWithOwner(listingId), savedRepository.observeSavedIds()) { (listing, owner), saved ->
                Triple(listing, owner, saved)
            }
                .catch { uiState = uiState.copy(listing = null, notFound = true) }
                .collect { (listing, owner, saved) ->
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

    /** TODO(chat): open a chat with the seller through ChatRepository. */
    fun onRequest() {
        uiState = uiState.copy(requestSent = true)
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
                )
            }
        }
    }
}
