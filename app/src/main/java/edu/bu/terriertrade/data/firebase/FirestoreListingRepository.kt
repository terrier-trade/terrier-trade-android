package edu.bu.terriertrade.data.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import edu.bu.terriertrade.data.model.Listing
import edu.bu.terriertrade.data.model.ListingStatus
import edu.bu.terriertrade.data.repository.ListingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

/**
 * The real ListingRepository: listings live in the Firestore "listings" collection,
 * so every student sees them. firestore.rules only lets verified @bu.edu accounts read,
 * and only the owner create or edit.
 *
 * The field mapping is in FirestoreMappers (Listing.toMap and listingFromMap).
 * TODO(photos): only http(s) photo URLs are saved. Photos picked on the phone need uploading
 * to Firebase Storage first, which is a separate step.
 */
class FirestoreListingRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
) : ListingRepository {

    private val listings get() = db.collection(COLLECTION)

    override fun observeActiveListings(): Flow<List<Listing>> =
        listings.whereEqualTo(FIELD_STATUS, ListingStatus.ACTIVE.name)
            .observeListings()
            .map { all -> all.sortedByDescending { it.createdAt } }

    override fun observeListing(listingId: String): Flow<Listing?> =
        listings.document(listingId).snapshotFlow().map { listingFromMap(it.id, it.data) }

    override fun observeListingsByOwner(ownerId: String): Flow<List<Listing>> =
        listings.whereEqualTo(FIELD_OWNER_ID, ownerId)
            .observeListings()
            .map { all -> all.filter { it.status != ListingStatus.REMOVED }.sortedByDescending { it.createdAt } }

    override suspend fun createListing(listing: Listing): Result<String> = firestoreResult(SAVE_FAILED) {
        val uid = signedInUidOrThrow(auth)
        val document = listings.document()
        val toSave = listing.copy(
            id = document.id,
            ownerId = uid,
            status = ListingStatus.ACTIVE,
            createdAt = System.currentTimeMillis(),
        )
        document.set(toSave.toMap()).await()
        document.id
    }

    override suspend fun updateListing(listing: Listing): Result<Unit> = firestoreResult(SAVE_FAILED) {
        listings.document(listing.id).set(listing.toMap()).await()
        Unit
    }

    override suspend fun setStatus(listingId: String, status: ListingStatus): Result<Unit> = firestoreResult(SAVE_FAILED) {
        listings.document(listingId).update(FIELD_STATUS, status.name).await()
        Unit
    }

    /** Live list for a query. Documents that can't be read are skipped instead of crashing the screen. */
    private fun Query.observeListings(): Flow<List<Listing>> =
        snapshotFlow().map { snap -> snap.documents.mapNotNull { listingFromMap(it.id, it.data) } }

    companion object {
        const val COLLECTION = "listings"
        private const val FIELD_STATUS = "status"
        private const val FIELD_OWNER_ID = "ownerId"
        private const val SAVE_FAILED = "Couldn't save your listing. Try again."
    }
}
