package edu.bu.terriertrade.data.firebase

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import edu.bu.terriertrade.data.repository.AuthRepository
import edu.bu.terriertrade.util.EmailCheck
import edu.bu.terriertrade.util.EmailValidator
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

/** A failure with a message that's safe to show on screen as it is. */
class AuthError(message: String) : Exception(message)

/**
 * The real AuthRepository, backed by Firebase Auth.
 *
 * currentUserId is only set once the email is verified, so the rest of the app treats an
 * unverified account as signed out. This matches firestore.rules, which only let verified
 * @bu.edu accounts read or write.
 */
class FirebaseAuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val profiles: FirestoreUserRepository = FirestoreUserRepository(),
) : AuthRepository {

    private val _currentUserId = MutableStateFlow(auth.currentUser?.verifiedUid())
    override val currentUserId: StateFlow<String?> = _currentUserId.asStateFlow()

    /** For small jobs that shouldn't block sign-in, like creating the profile. */
    private val background = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        auth.addAuthStateListener { firebaseAuth ->
            _currentUserId.value = firebaseAuth.currentUser?.verifiedUid()
        }
        // Already signed in from last time: make sure the profile exists too.
        auth.currentUser?.takeIf { it.isEmailVerified }?.let { user -> background.launch { ensureProfile(user) } }
    }

    override suspend fun signIn(email: String, password: String): Result<Unit> = runAuth {
        auth.signInWithEmailAndPassword(email.trim(), password).await()
        auth.currentUser?.takeIf { it.isEmailVerified }?.let { ensureProfile(it) }
    }

    override suspend fun signUp(name: String, email: String, password: String): Result<Unit> = runAuth {
        // The screen checks this too; this stops any other caller from skipping it.
        if (EmailValidator.check(email) != EmailCheck.Valid) throw AuthError("Use your @bu.edu email")

        val user = auth.createUserWithEmailAndPassword(email.trim(), password).await().user
            ?: throw AuthError(GENERIC_ERROR)
        val profile = UserProfileChangeRequest.Builder().setDisplayName(name.trim()).build()
        user.updateProfile(profile).await()
    }

    override suspend fun sendVerificationEmail(): Result<Unit> = runAuth {
        val user = auth.currentUser ?: throw AuthError("Sign in again to get a new email.")
        user.sendEmailVerification().await()
    }

    /**
     * Asks Firebase for the latest verified flag (it's cached on the phone until reload()).
     * When verified, it also refreshes the sign-in token so Firestore sees email_verified = true.
     */
    override suspend fun isEmailVerified(): Boolean {
        val user = auth.currentUser ?: return false
        return try {
            user.reload().await()
            val fresh = auth.currentUser ?: return false
            if (fresh.isEmailVerified) {
                fresh.getIdToken(true).await()
                ensureProfile(fresh)
                _currentUserId.value = fresh.uid
            }
            fresh.isEmailVerified
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            false
        }
    }

    override fun signOut() {
        auth.signOut()
    }

    private fun FirebaseUser.verifiedUid(): String? = uid.takeIf { isEmailVerified }

    /**
     * Creates users/{uid} the first time a verified student gets in, so their name shows on their
     * listings and Profile. It can't be made at sign-up: firestore.rules only allow verified accounts.
     * A failure here never blocks signing in; the next sign-in tries again.
     */
    private suspend fun ensureProfile(user: FirebaseUser) {
        try {
            profiles.ensureProfile(user.uid, user.displayName.orEmpty())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            android.util.Log.w("FirebaseAuth", "Couldn't create the profile yet", e)
        }
    }

    /** Runs a Firebase call and turns any failure into an AuthError with a friendly message. */
    private suspend fun runAuth(block: suspend () -> Unit): Result<Unit> =
        try {
            block()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e.toAuthError())
        }

    private fun Exception.toAuthError(): AuthError = when (this) {
        is AuthError -> this
        is FirebaseAuthUserCollisionException -> AuthError("There's already an account with this email. Sign in instead.")
        // Weak password is a kind of invalid credentials, so it has to come first.
        is FirebaseAuthWeakPasswordException -> AuthError("Pick a stronger password.")
        is FirebaseAuthInvalidUserException,
        is FirebaseAuthInvalidCredentialsException -> AuthError("Wrong email or password.")
        is FirebaseTooManyRequestsException -> AuthError("Too many tries. Wait a few minutes and try again.")
        is FirebaseNetworkException -> AuthError("No internet connection. Check it and try again.")
        else -> AuthError(GENERIC_ERROR)
    }

    private companion object {
        const val GENERIC_ERROR = "Something went wrong. Try again."
    }
}