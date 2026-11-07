package edu.bu.terriertrade.data.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import edu.bu.terriertrade.data.model.Chat
import edu.bu.terriertrade.data.model.Message
import edu.bu.terriertrade.data.model.MessageType
import edu.bu.terriertrade.data.repository.ChatRepository
import edu.bu.terriertrade.data.deal.RequestOutcome
import edu.bu.terriertrade.data.deal.RequestPlan
import edu.bu.terriertrade.data.deal.RequestResult
import edu.bu.terriertrade.data.deal.RequestRules
import edu.bu.terriertrade.data.model.Listing
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
 * TODO(deals): deals inside a chat are saved with the chat screens, later.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class FirestoreChatRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
) : ChatRepository {

    private val chats get() = db.collection(COLLECTION)

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

    companion object {
        const val COLLECTION = "chats"
        private const val MESSAGES = "messages"
        private const val FIELD_PARTICIPANTS = "participantIds"
    }
}
