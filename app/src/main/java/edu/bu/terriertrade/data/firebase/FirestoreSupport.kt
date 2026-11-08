package edu.bu.terriertrade.data.firebase

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import edu.bu.terriertrade.data.repository.DataError
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

/*
 * Small helpers shared by the Firestore repositories.
 */

/** Live results for a query. Stops listening when nobody collects any more. */
internal fun Query.snapshotFlow(): Flow<QuerySnapshot> = callbackFlow {
    val registration = addSnapshotListener { snapshot, error ->
        if (error != null) {
            close(error)
            return@addSnapshotListener
        }
        if (snapshot != null) trySend(snapshot)
    }
    awaitClose { registration.remove() }
}

/** Live value of one document. */
internal fun DocumentReference.snapshotFlow(): Flow<DocumentSnapshot> = callbackFlow {
    val registration = addSnapshotListener { snapshot, error ->
        if (error != null) {
            close(error)
            return@addSnapshotListener
        }
        if (snapshot != null) trySend(snapshot)
    }
    awaitClose { registration.remove() }
}

/**
 * The signed-in student's id, but only once their BU email is verified,
 * matching firestore.rules. Null when signed out or not verified yet.
 */
internal fun FirebaseAuth.verifiedUidFlow(): Flow<String?> = callbackFlow {
    val listener = FirebaseAuth.AuthStateListener { auth ->
        trySend(auth.currentUser?.takeIf { it.isEmailVerified }?.uid)
    }
    addAuthStateListener(listener)
    awaitClose { removeAuthStateListener(listener) }
}.distinctUntilChanged()

internal fun FirebaseAuth.verifiedUid(): String? = currentUser?.takeIf { it.isEmailVerified }?.uid

/** Runs a Firestore call and turns failures into a message for the screen. */
internal suspend fun <T> firestoreResult(failMessage: String, block: suspend () -> T): Result<T> =
    try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: DataError) {
        Result.failure(e)
    } catch (e: FirebaseFirestoreException) {
        val message = when (e.code) {
            FirebaseFirestoreException.Code.PERMISSION_DENIED -> "Only verified BU accounts can do that. Sign in again."
            FirebaseFirestoreException.Code.UNAVAILABLE -> "No connection. Check it and try again."
            else -> failMessage
        }
        Result.failure(DataError(message))
    } catch (e: FirebaseNetworkException) {
        Result.failure(DataError("No connection. Check it and try again."))
    } catch (e: Exception) {
        Result.failure(DataError(failMessage))
    }

internal fun signedInUidOrThrow(auth: FirebaseAuth): String =
    auth.verifiedUid() ?: throw DataError("Sign in with your verified BU email first.")

/** Reads an enum saved by its name ("ITEM"). Unknown or missing names give null instead of crashing. */
internal inline fun <reified T : Enum<T>> enumOrNull(name: String?): T? =
    name?.let { value -> enumValues<T>().firstOrNull { it.name == value } }
