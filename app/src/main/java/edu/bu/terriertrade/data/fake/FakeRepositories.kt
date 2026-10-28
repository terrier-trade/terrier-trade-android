package edu.bu.terriertrade.data.fake

import edu.bu.terriertrade.data.model.Chat
import edu.bu.terriertrade.data.model.Listing
import edu.bu.terriertrade.data.model.ListingStatus
import edu.bu.terriertrade.data.model.Message
import edu.bu.terriertrade.data.model.MessageType
import edu.bu.terriertrade.data.model.Review
import edu.bu.terriertrade.data.model.User
import edu.bu.terriertrade.data.repository.AuthRepository
import edu.bu.terriertrade.data.repository.ChatRepository
import edu.bu.terriertrade.data.repository.ListingRepository
import edu.bu.terriertrade.data.repository.ReviewRepository
import edu.bu.terriertrade.data.repository.SavedListingsRepository
import edu.bu.terriertrade.data.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/*
 * In-memory versions of every repository, filled with SampleData.
 * They let both of us build screens before Firebase and Room are ready,
 * and they double as test fakes. Data resets when the app restarts.
 */

private fun notSignedIn() = IllegalStateException("Not signed in")

class FakeAuthRepository : AuthRepository {
    private val _currentUserId = MutableStateFlow<String?>(SampleData.ME)
    override val currentUserId: StateFlow<String?> = _currentUserId.asStateFlow()

    override suspend fun signIn(email: String, password: String): Result<Unit> {
        _currentUserId.value = SampleData.ME
        return Result.success(Unit)
    }

    override suspend fun signUp(name: String, email: String, password: String): Result<Unit> =
        signIn(email, password)

    override suspend fun sendVerificationEmail(): Result<Unit> = Result.success(Unit)

    override suspend fun isEmailVerified(): Boolean = true

    override fun signOut() {
        _currentUserId.value = null
    }
}

class FakeUserRepository : UserRepository {
    private val users = MutableStateFlow(SampleData.users.associateBy { it.id })

    override fun observeUser(userId: String): Flow<User?> = users.map { it[userId] }

    override suspend fun updateProfile(user: User): Result<Unit> {
        users.update { it + (user.id to user) }
        return Result.success(Unit)
    }
}

class FakeListingRepository(private val auth: AuthRepository) : ListingRepository {
    private val listings = MutableStateFlow(SampleData.listings)
    private var nextId = 1000

    override fun observeActiveListings(): Flow<List<Listing>> =
        listings.map { all -> all.filter { it.isActive }.sortedByDescending { it.createdAt } }

    override fun observeListing(listingId: String): Flow<Listing?> =
        listings.map { all -> all.find { it.id == listingId } }

    override fun observeListingsByOwner(ownerId: String): Flow<List<Listing>> =
        listings.map { all ->
            all.filter { it.ownerId == ownerId && it.status != ListingStatus.REMOVED }
                .sortedByDescending { it.createdAt }
        }

    override suspend fun createListing(listing: Listing): Result<String> {
        val ownerId = auth.currentUserId.value ?: return Result.failure(notSignedIn())
        val id = "l${nextId++}"
        listings.update { it + listing.copy(id = id, ownerId = ownerId, createdAt = System.currentTimeMillis()) }
        return Result.success(id)
    }

    override suspend fun updateListing(listing: Listing): Result<Unit> {
        listings.update { all -> all.map { if (it.id == listing.id) listing else it } }
        return Result.success(Unit)
    }

    override suspend fun setStatus(listingId: String, status: ListingStatus): Result<Unit> {
        listings.update { all -> all.map { if (it.id == listingId) it.copy(status = status) else it } }
        return Result.success(Unit)
    }
}

class FakeSavedListingsRepository : SavedListingsRepository {
    private val saved = MutableStateFlow(SampleData.savedIds)

    override fun observeSavedIds(): Flow<Set<String>> = saved

    override suspend fun toggleSaved(listingId: String) {
        saved.update { if (listingId in it) it - listingId else it + listingId }
    }
}

class FakeChatRepository(private val auth: AuthRepository) : ChatRepository {
    private val chats = MutableStateFlow(SampleData.chats)
    private val messages = MutableStateFlow(SampleData.messages)
    private var nextId = 1000

    override fun observeChats(): Flow<List<Chat>> =
        combine(auth.currentUserId, chats) { me, all ->
            if (me == null) emptyList<Chat>()
            else all.filter { me in it.participantIds }.sortedByDescending { it.updatedAt }
        }

    override fun observeChat(chatId: String): Flow<Chat?> =
        chats.map { all -> all.find { it.id == chatId } }

    override fun observeMessages(chatId: String): Flow<List<Message>> =
        messages.map { all -> all.filter { it.chatId == chatId }.sortedBy { it.createdAt } }

    override suspend fun sendMessage(chatId: String, text: String): Result<Unit> {
        val me = auth.currentUserId.value ?: return Result.failure(notSignedIn())
        val now = System.currentTimeMillis()
        messages.update { it + Message("m${nextId++}", chatId, me, MessageType.TEXT, text, createdAt = now) }
        chats.update { all -> all.map { if (it.id == chatId) it.copy(lastMessage = text, updatedAt = now) else it } }
        return Result.success(Unit)
    }

    override suspend fun openChatWith(partnerId: String): Result<String> {
        val me = auth.currentUserId.value ?: return Result.failure(notSignedIn())
        chats.value.find { me in it.participantIds && partnerId in it.participantIds }?.let {
            return Result.success(it.id)
        }
        val chat = Chat(
            id = "c${nextId++}",
            participantIds = listOf(me, partnerId),
            updatedAt = System.currentTimeMillis(),
        )
        chats.update { it + chat }
        return Result.success(chat.id)
    }
}

class FakeReviewRepository : ReviewRepository {
    private val reviews = MutableStateFlow(SampleData.reviews)

    override fun observeReviewsFor(userId: String): Flow<List<Review>> =
        reviews.map { all -> all.filter { it.toUserId == userId }.sortedByDescending { it.createdAt } }

    override suspend fun addReview(review: Review): Result<Unit> {
        reviews.update { it + review }
        return Result.success(Unit)
    }
}
