package edu.bu.terriertrade.data.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import edu.bu.terriertrade.data.model.User
import edu.bu.terriertrade.data.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

/**
 * The real UserRepository: profiles live in Firestore at users/{uid}.
 * Rating and trade count come from the reviews about the student, so a review never has to
 * edit someone else's profile (firestore.rules only lets you edit your own).
 */
class FirestoreUserRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
) : UserRepository {

    private val users get() = db.collection(COLLECTION)

    override fun observeUser(userId: String): Flow<User?> {
        if (userId.isBlank()) return flowOf(null)
        val profile = users.document(userId).snapshotFlow().map { userFromMap(it.id, it.data) }
        val reviews = db.collection(FirestoreReviewRepository.COLLECTION)
            .whereEqualTo("toUserId", userId)
            .snapshotFlow()
            .map { snap -> snap.documents.mapNotNull { reviewFromMap(it.id, it.data) } }
            .catch { emit(emptyList()) }
        return combine(profile, reviews) { user, list -> user?.withReviews(list) }
            .catch { emitAll(flowOf(null)) }
    }

    override suspend fun updateProfile(user: User): Result<Unit> = firestoreResult("Couldn't save your profile. Try again.") {
        users.document(user.id).set(user.toProfileMap(), SetOptions.merge()).await()
        Unit
    }

    /**
     * Makes sure a verified student has a profile, so their name shows on listings and Profile.
     * Only creates it the first time; never overwrites what they've edited.
     */
    suspend fun ensureProfile(uid: String, name: String) {
        val doc = users.document(uid)
        if (!doc.get().await().exists()) {
            doc.set(User(id = uid, name = name.ifBlank { "BU student" }, verified = true).toProfileMap()).await()
        }
    }

    companion object {
        const val COLLECTION = "users"
    }
}
