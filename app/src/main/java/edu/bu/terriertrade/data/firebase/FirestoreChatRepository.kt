package edu.bu.terriertrade.data.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import edu.bu.terriertrade.data.deal.DealRules
import edu.bu.terriertrade.data.model.Chat
import edu.bu.terriertrade.data.model.Message
import edu.bu.terriertrade.data.model.MessageType
import edu.bu.terriertrade.data.repository.ChatRepository
import edu.bu.terriertrade.data.deal.RequestOutcome
import edu.bu.terriertrade.data.deal.RequestPlan
import edu.bu.terriertrade.data.deal.RequestResult
import edu.bu.terriertrade.data.deal.RequestRules
import edu.bu.terriertrade.data.model.Deal
import edu.bu.terriertrade.data.deal.DealPlan
import edu.bu.terriertrade.data.model.DealItem
import edu.bu.terriertrade.data.model.Listing
import edu.bu.terriertrade.data.model.ListingStatus
import edu.bu.terriertrade.data.repository.DataError
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

/**
 * The real ChatRepository: chats live at chats/{chatId}, messages at chats/{chatId}/messages.
 * Only the two students in a chat can read it (firestore.rules).
 * A request, and every change to the deal, saves the deal on the chat document
 * and its chat message together in one transaction.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class FirestoreChatRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
) : ChatRepository {

    private val chats get() = db.collection(COLLECTION)

    private val listingDocs get() = db.collection(FirestoreListingRepository.COLLECTION)

    override fun observeChats(): Flow<List<Chat>> =
        auth.verifiedUidFlow().flatMapLatest { uid ->
            if (uid == null) {
                flowOf(emptyList())
            } else {
                chats.whereArrayContains(FIELD_PARTICIPANTS, uid)
                    .snapshotFlow()
                    .map { snap -> snap.documents.mapNotNull { chatFromMap(it.id, it.data) }.sortedByDescending { it.updatedAt } }
                    .catch { emit(emptyList()) }
            }
        }

    override fun observeChat(chatId: String): Flow<Chat?> =
        chats.document(chatId).snapshotFlow()
            .map { chatFromMap(it.id, it.data) }
            .catch { emit(null) }

    override fun observeMessages(chatId: String): Flow<List<Message>> =
        chats.document(chatId).collection(MESSAGES)
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .snapshotFlow()
            .map { snap -> snap.documents.mapNotNull { messageFromMap(it.id, chatId, it.data) } }
            .catch { emit(emptyList()) }

    override suspend fun sendMessage(chatId: String, text: String): Result<Unit> =
        firestoreResult("Couldn't send your message. Try again.") {
            val me = signedInUidOrThrow(auth)
            val clean = text.trim()
            if (clean.isEmpty()) throw DataError("Type a message first.")
            val now = System.currentTimeMillis()
            val chat = chats.document(chatId)
            val message = chat.collection(MESSAGES).document()
            message.set(Message(message.id, chatId, me, MessageType.TEXT, clean, createdAt = now).toMap()).await()
            chat.update(mapOf("lastMessage" to clean, "updatedAt" to now)).await()
            Unit
        }

    /** Reuses the chat with [partnerId] if there is one, otherwise starts it. */
    override suspend fun openChatWith(partnerId: String): Result<String> =
        firestoreResult("Couldn't open a chat. Try again.") {
            val me = signedInUidOrThrow(auth)
            if (partnerId == me) throw DataError("That's you.")
            val existing = chats.whereArrayContains(FIELD_PARTICIPANTS, me).get().await()
                .documents.firstOrNull { doc ->
                    (doc.get(FIELD_PARTICIPANTS) as? List<*>)?.contains(partnerId) == true
                }
            if (existing != null) return@firestoreResult existing.id

            val doc = chats.document()
            doc.set(chatToMap(listOf(me, partnerId), lastMessage = "", updatedAt = System.currentTimeMillis())).await()
            doc.id
        }

    override suspend fun requestListing(listing: Listing): Result<RequestResult> =
        firestoreResult("Couldn't send your request. Try again.") {
            val me = signedInUidOrThrow(auth)

            // Check own or sold listings before creating a chat
            val check = RequestRules.plan(listing, me, currentDeal = null)
            if (check is RequestPlan.NotAllowed) throw DataError(check.reason)

            val chatId = openChatWith(listing.ownerId).getOrThrow()
            val chatRef = chats.document(chatId)
            val messageRef = chatRef.collection(MESSAGES).document()

            // All or nothing: read the latest deal, then save the deal and the message together
            val outcome = db.runTransaction { transaction ->
                val snapshot = transaction.get(chatRef)
                val currentDeal = dealFromMap(snapshot.get("deal"))

                when (val plan = RequestRules.plan(listing, me, currentDeal)) {
                    is RequestPlan.NotAllowed -> error(plan.reason)
                    RequestPlan.AlreadyInDeal -> RequestOutcome.ALREADY_IN_DEAL
                    RequestPlan.DealInProgress -> RequestOutcome.DEAL_IN_PROGRESS
                    is RequestPlan.StartDeal -> {
                        val now = System.currentTimeMillis()
                        val message = Message(messageRef.id, chatId, me, MessageType.REQUEST, plan.actionText, createdAt = now)
                        transaction.set(messageRef, message.toMap())
                        transaction.update(
                            chatRef,
                            mapOf(
                                "deal" to plan.deal.toMap(),
                                "lastMessage" to RequestRules.previewText(listing),
                                "updatedAt" to now,
                            ),
                        )
                        RequestOutcome.STARTED_DEAL
                    }
                }
            }.await()

            RequestResult(chatId, outcome)
        }

    override suspend fun proposeDeal(chatId: String, items: List<DealItem>): Result<Unit> =
        changeDeal(chatId, "Couldn't send your changes. Try again.") { deal, me, messageId, now, _ ->
            // The proposal reuses the message's id, so the suggestion card can find its proposal
            DealRules.propose(deal, me, items, proposalId = messageId, now = now)
        }

    override suspend fun acceptProposal(chatId: String): Result<Unit> =
        changeDeal(chatId, "Couldn't accept the changes. Try again.") { deal, me, _, _, _ ->
            DealRules.accept(deal, me)
        }

    override suspend fun declineProposal(chatId: String): Result<Unit> =
        changeDeal(chatId, "Couldn't update the deal. Try again.") { deal, me, _, _, _ ->
            DealRules.decline(deal, me)
        }

    override suspend fun markDealDone(chatId: String): Result<Unit> =
        changeDeal(chatId, "Couldn't mark the deal as done. Try again.", readListings = true) { deal, me, _, _, listings ->
            DealRules.markDone(deal, me, listings)
        }

    override suspend fun cancelDeal(chatId: String): Result<Unit> =
        changeDeal(chatId, "Couldn't cancel the deal. Try again.") { deal, _, _, _, _ ->
            DealRules.cancel(deal)
        }

    /**
     * One deal change as a transaction: read the latest deal (and its listings when [readListings]),
     * ask [decide] what to do, then save everything together, or nothing.
     * If the other person changes the deal at the same moment, Firestore runs this again
     * with their version, so a change is never made against an old deal.
     */
    private suspend fun changeDeal(
        chatId: String,
        failMessage: String,
        readListings: Boolean = false,
        decide: (deal: Deal?, me: String, messageId: String, now: Long, listings: Map<String, Listing>) -> DealPlan,
    ): Result<Unit> = firestoreResult(failMessage) {
        val me = signedInUidOrThrow(auth)
        val chatRef = chats.document(chatId)
        val messageRef = chatRef.collection(MESSAGES).document()

        val plan = db.runTransaction { transaction ->
            // Firestore needs every read in a transaction to come before any write
            val deal = dealFromMap(transaction.get(chatRef).get("deal"))
            val dealListings = if (readListings && deal != null) {
                deal.items.mapNotNull { item ->
                    val snapshot = transaction.get(listingDocs.document(item.listingId))
                    listingFromMap(snapshot.id, snapshot.data)
                }.associateBy { it.id }
            } else {
                emptyMap()
            }
            val now = System.currentTimeMillis()
            val plan = decide(deal, me, messageRef.id, now, dealListings)

            fun saveMessage(type: MessageType, text: String, proposalId: String? = null) {
                val message = Message(messageRef.id, chatId, me, type, text, proposalId, now)
                transaction.set(messageRef, message.toMap())
            }

            when (plan) {
                is DealPlan.Change -> {
                    saveMessage(plan.messageType, plan.messageText, plan.proposalId)
                    transaction.update(
                        chatRef,
                        mapOf("deal" to plan.deal.toMap(), "lastMessage" to plan.preview, "updatedAt" to now),
                    )
                }
                is DealPlan.End -> {
                    saveMessage(plan.messageType, plan.messageText)
                    // Deleting the field leaves the chat with no deal, so a new request can start one
                    transaction.update(
                        chatRef,
                        mapOf("deal" to FieldValue.delete(), "lastMessage" to plan.preview, "updatedAt" to now),
                    )
                    // soldInChatId lets firestore.rules check that both owners are in this chat
                    plan.soldListingIds.forEach { id ->
                        transaction.update(
                            listingDocs.document(id),
                            mapOf("status" to ListingStatus.SOLD.name, "soldInChatId" to chatId),
                        )
                    }
                }
                is DealPlan.NotAllowed -> Unit
            }
            plan
        }.await()

        // Thrown outside the transaction, so the student sees the real reason, not failMessage
        if (plan is DealPlan.NotAllowed) throw DataError(plan.reason)
        Unit
    }

    companion object {
        const val COLLECTION = "chats"
        private const val MESSAGES = "messages"
        private const val FIELD_PARTICIPANTS = "participantIds"
    }
}
