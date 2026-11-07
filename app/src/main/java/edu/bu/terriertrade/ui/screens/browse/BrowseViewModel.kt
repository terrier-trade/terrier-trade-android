package edu.bu.terriertrade.ui.screens.browse

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import edu.bu.terriertrade.data.model.Listing
import edu.bu.terriertrade.data.model.ListingType as DataListingType
import edu.bu.terriertrade.data.model.User
import edu.bu.terriertrade.data.repository.ChatRepository
import edu.bu.terriertrade.data.repository.ListingRepository
import edu.bu.terriertrade.data.repository.SavedListingsRepository
import edu.bu.terriertrade.data.repository.UserRepository
import edu.bu.terriertrade.di.appContainer
import edu.bu.terriertrade.ui.model.ListingCategory
import edu.bu.terriertrade.ui.model.ListingType
import edu.bu.terriertrade.ui.model.ListingUi
import edu.bu.terriertrade.ui.model.WantedPostUi
import edu.bu.terriertrade.ui.model.toListingUi
import edu.bu.terriertrade.ui.model.toWantedPostUi
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
 * The Browse feed. Listings, their sellers and the student's hearts come live from the repositories,
 * so a new post shows up straight away. Search, category and type chips filter on top.
 */
class BrowseViewModel(
    private val listingRepository: ListingRepository,
    private val userRepository: UserRepository,
    private val savedRepository: SavedListingsRepository,
    private val chatRepository: ChatRepository,
    private val now: () -> Long = System::currentTimeMillis,
) : ViewModel() {

    var uiState by mutableStateOf(BrowseUiState(isLoading = true))
        private set

    private var allListings: List<ListingUi> = emptyList()
    private var allWanted: List<WantedPostUi> = emptyList()
    // Real listings by id (including Looking for posts). The feed shows ListingUi and WantedPostUi,
    // but a request needs the real Listing: owner, price, type and status.
    private var listingsById: Map<String, Listing> = emptyMap()
    private var loadJob: Job? = null

    init {
        load()
    }

    /** Starts (or restarts, after an error) listening to the repositories. */
    fun load() {
        loadJob?.cancel()
        uiState = uiState.copy(isLoading = allListings.isEmpty() && allWanted.isEmpty(), errorMessage = null)
        loadJob = viewModelScope.launch {
            combine(observeListingsWithOwners(), savedRepository.observeSavedIds()) { feed, saved -> feed to saved }
                .catch { error ->
                    uiState = uiState.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Couldn't load listings.",
                    )
                }
                .collect { (feed, saved) ->
                    val (listings, owners) = feed
                    listingsById = listings.associateBy { it.id }
                    val time = now()
                    allListings = listings.mapNotNull { it.toListingUi(owners[it.ownerId], time) }
                    allWanted = listings
                        .filter { it.type == DataListingType.WANTED }
                        .map { it.toWantedPostUi(owners[it.ownerId], time) }
                    uiState = uiState.copy(isLoading = false, errorMessage = null, favoriteIds = saved)
                    applyFilters()
                }
        }
    }

    fun onQueryChange(query: String) = update { copy(query = query) }

    /** Tapping the selected category again clears it. */
    fun onCategorySelect(category: ListingCategory) =
        update { copy(category = if (this.category == category) null else category) }

    /** "All" also clears the category, so it really shows everything. */
    fun onFilterSelect(filter: BrowseFilter) =
        update { copy(filter = filter, category = if (filter == BrowseFilter.ALL) null else category) }

    fun clearFilters() = update {
        copy(query = "", category = null, filter = BrowseFilter.ALL, filters = BrowseFilters(), draftFilters = BrowseFilters())
    }

    // Filters sheet

    fun onOpenFilters() {
        uiState = uiState.copy(showFilterSheet = true, draftFilters = uiState.filters)
        updateDraftCount()
    }

    fun onDraftFiltersChange(filters: BrowseFilters) {
        uiState = uiState.copy(draftFilters = filters)
        updateDraftCount()
    }

    fun onResetDraftFilters() = onDraftFiltersChange(BrowseFilters())

    fun onApplyFilters() = update { copy(filters = draftFilters, showFilterSheet = false) }

    fun onDismissFilters() {
        uiState = uiState.copy(showFilterSheet = false)
    }

    private fun updateDraftCount() {
        val preview = uiState.copy(filters = uiState.draftFilters)
        uiState = uiState.copy(draftResultCount = filterListings(allListings, preview).size)
    }
    /** Saved in SavedListingsRepository, so the heart is the same on every screen. */
    fun onToggleFavorite(listingId: String) {
        viewModelScope.launch { savedRepository.toggleSaved(listingId) }
    }

    /**
     * Request on a listing card, or "I can help" on a Looking for note (those are listings too).
     * Opens or reuses the chat with the owner and starts a deal, then signals the screen to open it.
     */
    fun onRequest(listingId: String) {
        val listing = listingsById[listingId] ?: return
        if (uiState.requestingId != null) return // one request at a time, ignores double taps

        uiState = uiState.copy(requestingId = listingId, requestError = null)
        viewModelScope.launch {
            chatRepository.requestListing(listing)
                .onSuccess { result ->
                    uiState = uiState.copy(requestingId = null, openChatId = result.chatId)
                }
                .onFailure { error ->
                    uiState = uiState.copy(
                        requestingId = null,
                        requestError = error.message ?: "Couldn't send your request. Try again.",
                    )
                }
        }
    }

    /** Called by the screen once it has opened the chat, so the signal fires only once. */
    fun onChatOpened() {
        uiState = uiState.copy(openChatId = null)
    }

    /** Called by the screen once it has shown the error, so it isn't shown again. */
    fun onRequestErrorShown() {
        uiState = uiState.copy(requestError = null)
    }

    private fun update(change: BrowseUiState.() -> BrowseUiState) {
        uiState = uiState.change()
        applyFilters()
    }

    private fun applyFilters() {
        uiState = uiState.copy(
            listings = filterListings(allListings, uiState),
            wanted = filterWanted(allWanted, uiState.query),
        )
    }

    /** Active listings plus each owner's profile, updating when either changes. */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeListingsWithOwners(): Flow<Pair<List<Listing>, Map<String, User?>>> =
        listingRepository.observeActiveListings().flatMapLatest { listings ->
            val ownerIds = listings.map { it.ownerId }.distinct()
            if (ownerIds.isEmpty()) {
                flowOf(listings to emptyMap())
            } else {
                combine(ownerIds.map { id -> userRepository.observeUser(id).map { id to it } }) { owners ->
                    listings to owners.toMap()
                }
            }
        }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                BrowseViewModel(
                    listingRepository = appContainer.listingRepository,
                    userRepository = appContainer.userRepository,
                    savedRepository = appContainer.savedListingsRepository,
                    chatRepository = appContainer.chatRepository,
                )
            }
        }

        fun filterListings(listings: List<ListingUi>, state: BrowseUiState): List<ListingUi> {
            if (state.filter == BrowseFilter.LOOKING_FOR) return emptyList()
            val text = state.query.trim()
            val matching = listings.filter { listing ->
                matchesType(listing.type, state.filter) &&
                        (state.category == null || listing.category == state.category) &&
                        (text.isEmpty() || listing.title.contains(text, ignoreCase = true)) &&
                        state.filters.matches(listing)
            }
            return state.filters.sort(matching)
        }

        fun filterWanted(wanted: List<WantedPostUi>, query: String): List<WantedPostUi> {
            val text = query.trim()
            return if (text.isEmpty()) wanted else wanted.filter { it.title.contains(text, ignoreCase = true) }
        }

        private fun matchesType(type: ListingType, filter: BrowseFilter): Boolean = when (filter) {
            BrowseFilter.ALL -> true
            BrowseFilter.ITEMS -> type == ListingType.ITEM
            BrowseFilter.SERVICES -> type == ListingType.SERVICE
            BrowseFilter.TICKETS -> type == ListingType.TICKET
            BrowseFilter.LOOKING_FOR -> false
        }
    }
}
