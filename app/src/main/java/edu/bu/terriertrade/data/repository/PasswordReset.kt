package edu.bu.terriertrade.data.repository

/** Sends a "reset your password" email. Kept apart from AuthRepository because only Sign in uses it. */
fun interface PasswordReset {
    suspend fun sendResetEmail(email: String): Result<Unit>
}
