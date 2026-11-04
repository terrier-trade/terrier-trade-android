package edu.bu.terriertrade.data.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import edu.bu.terriertrade.data.repository.SavedListingsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

/**
 * The real SavedListingsRepository: each student's hearts live at users/{uid}/saved/{listingId},
 * so they follow the account to any phone and only that student can see them.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class FirestoreSavedListingsRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
) : SavedListingsRepository {

    private fun savedFor(uid: String) =
        db.collection(FirestoreUserRepository.COLLECTION).document(uid).collection(SUBCOLLECTION)

    override fun observeSavedIds(): Flow<Set<String>> =
        auth.verifiedUidFlow().flatMapLatest { uid ->
            if (uid == null) {
                flowOf(emptySet())
            } else {
                savedFor(uid).snapshotFlow()
                    .map { snap -> snap.documents.map { it.id }.toSet() }
                    .catch { emit(emptySet()) }
            }
        }

    /** Hearts or un-hearts. A failure (no connection) is ignored: the heart just doesn't change. */
    override suspend fun toggleSaved(listingId: String) {
        val uid = auth.verifiedUid() ?: return
        firestoreResult("Couldn't save that.") {
            val doc = savedFor(uid).document(listingId)
            if (doc.get().await().exists()) {
                doc.delete().await()
            } else {
                doc.set(mapOf("savedAt" to System.currentTimeMillis())).await()
            }
        }
    }

    companion object {
        const val SUBCOLLECTION = "saved"
    }
}
