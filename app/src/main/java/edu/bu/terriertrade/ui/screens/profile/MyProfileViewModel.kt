package edu.bu.terriertrade.ui.screens.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import edu.bu.terriertrade.data.model.ListingStatus
import edu.bu.terriertrade.data.model.User
import edu.bu.terriertrade.data.repository.AuthRepository
import edu.bu.terriertrade.data.repository.ListingRepository
import edu.bu.terriertrade.data.repository.ReviewRepository
import edu.bu.terriertrade.data.repository.SavedListingsRepository
import edu.bu.terriertrade.data.repository.UserRepository
import edu.bu.terriertrade.di.appContainer
import edu.bu.terriertrade.ui.model.ListingUi
import edu.bu.terriertrade.ui.model.MyListingUi
import edu.bu.terriertrade.ui.model.ReviewUi
import edu.bu.terriertrade.ui.model.observeUsers
import edu.bu.terriertrade.ui.model.toListingUi
import edu.bu.terriertrade.ui.model.toProfileHeaderUi
import edu.bu.terriertrade.ui.model.toReviewUi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * The signed-in student's own profile: who they are, their listings, what they've hearted,
 * and reviews about them. Everything is live from the repositories.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MyProfileViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val listingRepository: ListingRepository,
    private val savedRepository: SavedListingsRepository,
    private val reviewRepository: ReviewRepository,
    private val now: () -> Long = System::currentTimeMillis,
) : ViewModel() {

    var uiState by mutableStateOf(MyProfileUiState())
        private set

    init {
        viewModelScope.launch {
            authRepository.currentUserId
                .flatMapLatest { uid -> if (uid == null) flowOf(null) else observeProfile(uid) }
                .catch { error ->
                    uiState = uiState.copy(isLoading = false, errorMessage = error.message ?: "Couldn't load your profile.")
                }
                .collect { data ->
                    uiState = if (data == null) {
                        MyProfileUiState(isLoading = false, isSignedOut = true)
                    } else {
                        uiState.copy(
                            isLoading = false,
                            isSignedOut = false,
                            errorMessage = null,
                            header = data.user?.toProfileHeaderUi(),
                            myListings = data.myListings,
                            saved = data.saved,
                            reviews = data.reviews,
                        )
                    }
                }
        }
    }

    fun onTabSelect(tab: ProfileTab) {
        uiState = uiState.copy(tab = tab, message = null)
    }

    fun onMarkSold(listingId: String) {
        viewModelScope.launch {
            listingRepository.setStatus(listingId, ListingStatus.SOLD)
                .onSuccess { uiState = uiState.copy(message = "Marked as sold. It's off Browse now.") }
                .onFailure { uiState = uiState.copy(message = it.message ?: "Couldn't update the listing.") }
        }
    }

    fun onRemoveClick(item: MyListingUi) {
        uiState = uiState.copy(confirmRemove = item, message = null)
    }

    fun onDismissRemove() {
        uiState = uiState.copy(confirmRemove = null)
    }

    /** Listings are never deleted, only marked REMOVED, so chats about them still make sense. */
    fun onConfirmRemove() {
        val item = uiState.confirmRemove ?: return
        uiState = uiState.copy(confirmRemove = null)
        viewModelScope.launch {
            listingRepository.setStatus(item.listing.id, ListingStatus.REMOVED)
                .onSuccess { uiState = uiState.copy(message = "\"${item.listing.title}\" was removed.") }
                .onFailure { uiState = uiState.copy(message = it.message ?: "Couldn't remove the listing.") }
        }
    }

    fun onToggleSaved(listingId: String) {
        viewModelScope.launch { savedRepository.toggleSaved(listingId) }
    }

    private data class ProfileData(
        val user: User?,
        val myListings: List<MyListingUi>,
        val saved: List<ListingUi>,
        val reviews: List<ReviewUi>,
    )

    private fun observeProfile(uid: String): Flow<ProfileData> =
        combine(
            userRepository.observeUser(uid),
            listingRepository.observeListingsByOwner(uid),
            observeSaved(),
            observeReviews(uid),
        ) { user, mine, saved, reviews ->
            val time = now()
            val myListings = mine
                .filter { it.status != ListingStatus.REMOVED }
                .mapNotNull { listing ->
                    listing.toListingUi(user, time)?.let { MyListingUi(it, isSold = listing.status == ListingStatus.SOLD) }
                }
                .sortedBy { it.isSold } // live ones first, newest first within each group
            ProfileData(user, myListings, saved, reviews)
        }

    /** Hearted listings that are still active, with their sellers. */
    private fun observeSaved(): Flow<List<ListingUi>> =
        combine(listingRepository.observeActiveListings(), savedRepository.observeSavedIds()) { active, ids ->
            active.filter { it.id in ids }
        }.flatMapLatest { listings ->
            userRepository.observeUsers(listings.map { it.ownerId }).map { owners ->
                val time = now()
                listings.mapNotNull { it.toListingUi(owners[it.ownerId], time) }
            }
        }

    private fun observeReviews(uid: String): Flow<List<ReviewUi>> =
        reviewRepository.observeReviewsFor(uid).flatMapLatest { reviews ->
            userRepository.observeUsers(reviews.map { it.fromUserId }).map { authors ->
                val time = now()
                reviews.map { it.toReviewUi(authors[it.fromUserId], time) }
            }
        }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                MyProfileViewModel(
                    authRepository = appContainer.authRepository,
                    userRepository = appContainer.userRepository,
                    listingRepository = appContainer.listingRepository,
                    savedRepository = appContainer.savedListingsRepository,
                    reviewRepository = appContainer.reviewRepository,
                )
            }
        }
    }
}
