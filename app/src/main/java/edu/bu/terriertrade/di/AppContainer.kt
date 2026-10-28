package edu.bu.terriertrade.di

import android.content.Context
import edu.bu.terriertrade.data.fake.FakeAuthRepository
import edu.bu.terriertrade.data.fake.FakeChatRepository
import edu.bu.terriertrade.data.fake.FakeListingRepository
import edu.bu.terriertrade.data.fake.FakeReviewRepository
import edu.bu.terriertrade.data.fake.FakeSavedListingsRepository
import edu.bu.terriertrade.data.fake.FakeUserRepository
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

    override val authRepository: AuthRepository = FakeAuthRepository()

    override val userRepository: UserRepository = FakeUserRepository()

    override val listingRepository: ListingRepository = FakeListingRepository(authRepository)

    override val savedListingsRepository: SavedListingsRepository = FakeSavedListingsRepository()

    override val chatRepository: ChatRepository = FakeChatRepository(authRepository)

    override val reviewRepository: ReviewRepository = FakeReviewRepository()

    override val networkMonitor: NetworkMonitor = NetworkMonitor(context.applicationContext)
}
