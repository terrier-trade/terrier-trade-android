package edu.bu.terriertrade.data.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import edu.bu.terriertrade.data.model.Review
import edu.bu.terriertrade.data.repository.DataError
import edu.bu.terriertrade.data.repository.ReviewRepository
import edu.bu.terriertrade.data.review.ReviewRules
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

/**
 * The real ReviewRepository: reviews live in Firestore at reviews/{promptId_reviewerId}.
 * Reviews can't be edited (firestore.rules), so that id allows one review per person per finished deal.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class FirestoreReviewRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
) : ReviewRepository {

    private val reviews get() = db.collection(COLLECTION)

    override fun observeReviewsFor(userId: String): Flow<List<Review>> =
        reviews.whereEqualTo("toUserId", userId)
            .snapshotFlow()
            .map { snap -> snap.documents.mapNotNull { reviewFromMap(it.id, it.data) }.sortedByDescending { it.createdAt } }
            .catch { emit(emptyList()) }

    override fun observeMyReviewedPrompts(chatId: String): Flow<Set<String>> =
        auth.verifiedUidFlow().flatMapLatest { uid ->
            if (uid == null) {
                flowOf(emptySet())
            } else {
                // Two equality filters, so Firestore's automatic indexes are enough
                reviews.whereEqualTo("chatId", chatId)
                    .whereEqualTo("fromUserId", uid)
                    .snapshotFlow()
                    .map { snap ->
                        snap.documents
                            .mapNotNull { reviewFromMap(it.id, it.data)?.promptId }
                            .filter { it.isNotBlank() }
                            .toSet()
                    }
                    .catch { emit(emptySet()) }
            }
        }

    override suspend fun addReview(review: Review): Result<Unit> = firestoreResult("Couldn't save your review. Try again.") {
        val me = signedInUidOrThrow(auth)
        ReviewRules.problem(review, me)?.let { throw DataError(it) }

        val doc = reviews.document(ReviewRules.reviewId(review.promptId, me))
        val toSave = review.copy(id = doc.id, fromUserId = me, createdAt = System.currentTimeMillis())

        // Check first, so a second review of the same deal gets a clear message, not a permission error
        val saved = db.runTransaction { transaction ->
            if (transaction.get(doc).exists()) {
                false
            } else {
                transaction.set(doc, toSave.toMap())
                true
            }
        }.await()

        // Thrown outside the transaction, so the message isn't swapped for the generic one
        if (!saved) throw DataError(ReviewRules.ALREADY_REVIEWED)
        Unit
    }

    companion object {
        const val COLLECTION = "reviews"
    }
}