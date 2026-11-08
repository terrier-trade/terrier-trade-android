package edu.bu.terriertrade.data.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import edu.bu.terriertrade.data.model.Review
import edu.bu.terriertrade.data.repository.DataError
import edu.bu.terriertrade.data.repository.ReviewRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

/** The real ReviewRepository: reviews live in Firestore at reviews/{id}. */
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

    override suspend fun addReview(review: Review): Result<Unit> = firestoreResult("Couldn't save your review. Try again.") {
        val me = signedInUidOrThrow(auth)
        if (review.toUserId == me) throw DataError("You can't review yourself.")
        val doc = reviews.document()
        doc.set(review.copy(id = doc.id, fromUserId = me, createdAt = System.currentTimeMillis()).toMap()).await()
        Unit
    }

    companion object {
        const val COLLECTION = "reviews"
    }
}
