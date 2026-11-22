package edu.bu.terriertrade.data.fake

import edu.bu.terriertrade.data.deal.DealPlan
import edu.bu.terriertrade.data.deal.DealRules
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
import edu.bu.terriertrade.data.deal.RequestOutcome
import edu.bu.terriertrade.data.deal.RequestPlan
import edu.bu.terriertrade.data.deal.RequestResult
import edu.bu.terriertrade.data.deal.RequestRules
import edu.bu.terriertrade.data.model.Deal
import edu.bu.terriertrade.data.model.DealItem
import edu.bu.terriertrade.data.repository.DataError
import edu.bu.terriertrade.data.review.ReviewRules
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
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

class FakeChatRepository(
    private val auth: AuthRepository,
    // Finishing a deal reads its listings and marks them sold, like the Firestore version does
    private val listingRepository: ListingRepository = FakeListingRepository(auth),
) : ChatRepository {
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

    override suspend fun requestListing(listing: Listing): Result<RequestResult> {
        val me = auth.currentUserId.value ?: return Result.failure(notSignedIn())

        // Check own or sold listings before creating a chat
        val check = RequestRules.plan(listing, me, currentDeal = null)
        if (check is RequestPlan.NotAllowed) return Result.failure(IllegalStateException(check.reason))

        val chatId = openChatWith(listing.ownerId).getOrElse { return Result.failure(it) }
        val chat = chats.value.first { it.id == chatId }

        val outcome = when (val plan = RequestRules.plan(listing, me, chat.deal)) {
            is RequestPlan.NotAllowed -> return Result.failure(IllegalStateException(plan.reason))
            RequestPlan.AlreadyInDeal -> RequestOutcome.ALREADY_IN_DEAL
            RequestPlan.DealInProgress -> RequestOutcome.DEAL_IN_PROGRESS
            is RequestPlan.StartDeal -> {
                val now = System.currentTimeMillis()
                messages.update {
                    it + Message("m${nextId++}", chatId, me, MessageType.REQUEST, plan.actionText, createdAt = now)
                }
                chats.update { all ->
                    all.map {
                        if (it.id == chatId) {
                            it.copy(deal = plan.deal, lastMessage = RequestRules.previewText(listing), updatedAt = now)
                        } else {
                            it
                        }
                    }
                }
                RequestOutcome.STARTED_DEAL
            }
        }
        return Result.success(RequestResult(chatId, outcome))
    }

    override suspend fun proposeDeal(chatId: String, items: List<DealItem>): Result<Unit> =
        changeDeal(chatId) { deal, me, messageId, now, _ ->
            DealRules.propose(deal, me, items, proposalId = messageId, now = now)
        }

    override suspend fun acceptProposal(chatId: String): Result<Unit> =
        changeDeal(chatId) { deal, me, _, _, _ -> DealRules.accept(deal, me) }

    override suspend fun declineProposal(chatId: String): Result<Unit> =
        changeDeal(chatId) { deal, me, _, _, _ -> DealRules.decline(deal, me) }

    override suspend fun markDealDone(chatId: String): Result<Unit> =
        changeDeal(chatId, readListings = true) { deal, me, _, _, listings -> DealRules.markDone(deal, me, listings) }

    override suspend fun cancelDeal(chatId: String): Result<Unit> =
        changeDeal(chatId) { deal, _, _, _, _ -> DealRules.cancel(deal) }

    /** Same steps as the Firestore version, without the transaction: read, decide, then save. */
    private suspend fun changeDeal(
        chatId: String,
        readListings: Boolean = false,
        decide: (deal: Deal?, me: String, messageId: String, now: Long, listings: Map<String, Listing>) -> DealPlan,
    ): Result<Unit> {
        val me = auth.currentUserId.value ?: return Result.failure(notSignedIn())
        val chat = chats.value.find { it.id == chatId } ?: return Result.failure(DataError("This chat isn't available."))
        val dealListings = if (readListings) listingsInDeal(chat.deal) else emptyMap()
        val messageId = "m${nextId++}"
        val now = System.currentTimeMillis()

        return when (val plan = decide(chat.deal, me, messageId, now, dealListings)) {
            is DealPlan.NotAllowed -> Result.failure(DataError(plan.reason))
            is DealPlan.Change -> {
                messages.update {
                    it + Message(messageId, chatId, me, plan.messageType, plan.messageText, plan.proposalId, now)
                }
                updateChat(chatId) { it.copy(deal = plan.deal, lastMessage = plan.preview, updatedAt = now) }
                Result.success(Unit)
            }
            is DealPlan.End -> {
                messages.update {
                    it + Message(messageId, chatId, me, plan.messageType, plan.messageText, createdAt = now)
                }
                updateChat(chatId) { it.copy(deal = null, lastMessage = plan.preview, updatedAt = now) }
                plan.soldListingIds.forEach { listingRepository.setStatus(it, ListingStatus.SOLD) }
                Result.success(Unit)
            }
        }
    }

    private fun updateChat(chatId: String, change: (Chat) -> Chat) {
        chats.update { all -> all.map { if (it.id == chatId) change(it) else it } }
    }

    /** The deal's listings by id, removed ones included, like reading each document in Firestore. */
    private suspend fun listingsInDeal(deal: Deal?): Map<String, Listing> =
        deal?.items.orEmpty()
            .mapNotNull { listingRepository.observeListing(it.listingId).first() }
            .associateBy { it.id }
}

/**
 * Reviews in memory, following the same rules as Firestore: one review per person per finished deal.
 * It doesn't check that the deal really finished, because chats live in FakeChatRepository.
 */
class FakeReviewRepository(
    private val auth: AuthRepository = FakeAuthRepository(),
) : ReviewRepository {
    private val reviews = MutableStateFlow(SampleData.reviews)

    override fun observeReviewsFor(userId: String): Flow<List<Review>> =
        reviews.map { all -> all.filter { it.toUserId == userId }.sortedByDescending { it.createdAt } }

    override fun observeMyReviewedPrompts(chatId: String): Flow<Set<String>> =
        combine(auth.currentUserId, reviews) { me, all ->
            all.filter { it.chatId == chatId && it.fromUserId == me && it.promptId.isNotBlank() }
                .map { it.promptId }
                .toSet()
        }

    override suspend fun addReview(review: Review): Result<Unit> {
        val me = auth.currentUserId.value ?: return Result.failure(notSignedIn())
        ReviewRules.problem(review, me)?.let { return Result.failure(DataError(it)) }

        val id = ReviewRules.reviewId(review.promptId, me)
        if (reviews.value.any { it.id == id }) return Result.failure(DataError(ReviewRules.ALREADY_REVIEWED))

        reviews.update { it + review.copy(id = id, fromUserId = me, createdAt = System.currentTimeMillis()) }
        return Result.success(Unit)
    }
}
