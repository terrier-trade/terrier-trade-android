package edu.bu.terriertrade.data.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import edu.bu.terriertrade.data.model.Category
import edu.bu.terriertrade.data.model.Condition
import edu.bu.terriertrade.data.model.Listing
import edu.bu.terriertrade.data.model.ListingStatus
import edu.bu.terriertrade.data.model.ListingType
import edu.bu.terriertrade.data.model.PriceUnit
import edu.bu.terriertrade.data.model.ServiceMode
import edu.bu.terriertrade.data.repository.ListingRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

/**
 * The real ListingRepository: listings live in the Firestore "listings" collection,
 * so every student sees them. firestore.rules only lets verified @bu.edu accounts read,
 * and only the owner create or edit.
 *
 * Enums are saved as their names ("ITEM", "GOOD") so the data is easy to read in the console.
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

    override fun observeListing(listingId: String): Flow<Listing?> = callbackFlow {
        val registration = listings.document(listingId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            trySend(snapshot?.toListing())
        }
        awaitClose { registration.remove() }
    }

    override fun observeListingsByOwner(ownerId: String): Flow<List<Listing>> =
        listings.whereEqualTo(FIELD_OWNER_ID, ownerId)
            .observeListings()
            .map { all -> all.filter { it.status != ListingStatus.REMOVED }.sortedByDescending { it.createdAt } }

    override suspend fun createListing(listing: Listing): Result<String> = firestoreCall {
        val uid = auth.currentUser?.uid ?: throw ListingError("Sign in to post a listing.")
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

    override suspend fun updateListing(listing: Listing): Result<Unit> = firestoreCall {
        listings.document(listing.id).set(listing.toMap()).await()
        Unit
    }

    override suspend fun setStatus(listingId: String, status: ListingStatus): Result<Unit> = firestoreCall {
        listings.document(listingId).update(FIELD_STATUS, status.name).await()
        Unit
    }

    /** Live list for a query. Documents that can't be read are skipped instead of crashing the screen. */
    private fun Query.observeListings(): Flow<List<Listing>> = callbackFlow {
        val registration = addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            trySend(snapshot?.documents?.mapNotNull { it.toListing() }.orEmpty())
        }
        awaitClose { registration.remove() }
    }

    /** Runs a Firestore call and turns failures into messages that can go straight on screen. */
    private suspend fun <T> firestoreCall(block: suspend () -> T): Result<T> =
        try {
            Result.success(block())
        } catch (e: CancellationException) {
            throw e
        } catch (e: ListingError) {
            Result.failure(e)
        } catch (e: FirebaseFirestoreException) {
            val message = when (e.code) {
                FirebaseFirestoreException.Code.PERMISSION_DENIED -> "Only verified BU accounts can post. Sign in again."
                FirebaseFirestoreException.Code.UNAVAILABLE -> "No connection. Check it and try again."
                else -> "Couldn't save your listing. Try again."
            }
            Result.failure(ListingError(message))
        } catch (e: Exception) {
            Result.failure(ListingError("Couldn't save your listing. Try again."))
        }

    companion object {
        const val COLLECTION = "listings"
        private const val FIELD_STATUS = "status"
        private const val FIELD_OWNER_ID = "ownerId"
    }
}

/** A failure with a message that's safe to show on screen as it is. */
class ListingError(message: String) : Exception(message)

/** Listing → Firestore fields. Only web photo URLs are kept, since phone-only photo links mean nothing to other phones. */
internal fun Listing.toMap(): Map<String, Any?> = mapOf(
    "ownerId" to ownerId,
    "type" to type.name,
    "title" to title,
    "price" to price,
    "description" to description,
    "priceUnit" to priceUnit.name,
    "category" to category.name,
    "condition" to condition?.name,
    "serviceMode" to serviceMode?.name,
    "meetSpot" to meetSpot,
    "photoUrls" to photoUrls.filter { it.startsWith("http") },
    "isbn" to isbn,
    "eventDateMillis" to eventDateMillis,
    "quantity" to quantity,
    "status" to status.name,
    "createdAt" to createdAt,
)

/** Firestore document → Listing, or null when a required field is missing or unreadable. */
internal fun DocumentSnapshot.toListing(): Listing? {
    if (!exists()) return null
    val ownerId = getString("ownerId") ?: return null
    val type = enumOrNull<ListingType>(getString("type")) ?: return null
    val title = getString("title") ?: return null
    return Listing(
        id = id,
        ownerId = ownerId,
        type = type,
        title = title,
        price = getLong("price")?.toInt() ?: 0,
        description = getString("description").orEmpty(),
        priceUnit = enumOrNull<PriceUnit>(getString("priceUnit")) ?: PriceUnit.NONE,
        category = enumOrNull<Category>(getString("category")) ?: Category.OTHER,
        condition = enumOrNull<Condition>(getString("condition")),
        serviceMode = enumOrNull<ServiceMode>(getString("serviceMode")),
        meetSpot = getString("meetSpot").orEmpty(),
        photoUrls = (get("photoUrls") as? List<*>)?.filterIsInstance<String>().orEmpty(),
        isbn = getString("isbn"),
        eventDateMillis = getLong("eventDateMillis"),
        quantity = getLong("quantity")?.toInt(),
        status = enumOrNull<ListingStatus>(getString("status")) ?: ListingStatus.ACTIVE,
        createdAt = getLong("createdAt") ?: 0L,
    )
}

private inline fun <reified T : Enum<T>> enumOrNull(name: String?): T? =
    name?.let { value -> enumValues<T>().firstOrNull { it.name == value } }
