package edu.bu.terriertrade.data.firebase

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await

/** Sends a "reset your password" email. Kept separate from AuthRepository so nothing else has to change. */
fun interface PasswordReset {
    suspend fun sendResetEmail(email: String): Result<Unit>
}

/**
 * Firebase sends the email with a link to its own "choose a new password" page.
 * For privacy, Firebase answers the same way whether or not the account exists,
 * so the screen says "if there's an account" instead of confirming it.
 */
class FirebasePasswordReset(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
) : PasswordReset {

    override suspend fun sendResetEmail(email: String): Result<Unit> =
        try {
            auth.sendPasswordResetEmail(email.trim()).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            Result.failure(AuthError("That doesn't look like an email address"))
        } catch (e: FirebaseTooManyRequestsException) {
            Result.failure(AuthError("Too many tries. Wait a few minutes and try again."))
        } catch (e: FirebaseNetworkException) {
            Result.failure(AuthError("No internet connection. Check it and try again."))
        } catch (e: Exception) {
            Result.failure(AuthError("Couldn't send the reset email. Try again."))
        }
}
