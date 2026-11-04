package edu.bu.terriertrade.di

import android.content.Context
import edu.bu.terriertrade.data.firebase.FirebaseAuthRepository
import edu.bu.terriertrade.data.firebase.FirestoreChatRepository
import edu.bu.terriertrade.data.firebase.FirestoreListingRepository
import edu.bu.terriertrade.data.firebase.FirestoreReviewRepository
import edu.bu.terriertrade.data.firebase.FirestoreSavedListingsRepository
import edu.bu.terriertrade.data.firebase.FirestoreUserRepository
import edu.bu.terriertrade.data.network.NetworkMonitor
import edu.bu.terriertrade.data.repository.AuthRepository
import edu.bu.terriertrade.data.repository.ChatRepository
import edu.bu.terriertrade.data.repository.ListingRepository
import edu.bu.terriertrade.data.repository.ReviewRepository
import edu.bu.terriertrade.data.repository.SavedListingsRepository
import edu.bu.terriertrade.data.repository.UserRepository

/**
 * Creates every repository once and hands them to ViewModels (manual dependency injection).
 * ViewModels only see the interfaces, so swapping a fake for the real Firebase or Room
 * version is a one-line change here.
 */
interface AppContainer {
    val authRepository: AuthRepository
    val userRepository: UserRepository
    val listingRepository: ListingRepository
    val savedListingsRepository: SavedListingsRepository
    val chatRepository: ChatRepository
    val reviewRepository: ReviewRepository
    val networkMonitor: NetworkMonitor
}

class DefaultAppContainer(context: Context) : AppContainer {

    // Each repository sits on its own line so two branches swapping
    // different repositories don't conflict.
    // The Fake versions in data/fake are still there for tests and previews.

    override val authRepository: AuthRepository = FirebaseAuthRepository()

    override val userRepository: UserRepository = FirestoreUserRepository()

    override val listingRepository: ListingRepository = FirestoreListingRepository()

    override val savedListingsRepository: SavedListingsRepository = FirestoreSavedListingsRepository()

    override val chatRepository: ChatRepository = FirestoreChatRepository()

    override val reviewRepository: ReviewRepository = FirestoreReviewRepository()

    override val networkMonitor: NetworkMonitor = NetworkMonitor(context.applicationContext)
}
