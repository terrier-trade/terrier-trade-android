package edu.bu.terriertrade.data.repository

import kotlinx.coroutines.flow.StateFlow

/** Sign-in state for the whole app. Screens never talk to Firebase Auth directly. */
interface AuthRepository {
    /** Id of the signed-in student, or null when signed out. */
    val currentUserId: StateFlow<String?>

    suspend fun signIn(email: String, password: String): Result<Unit>

    suspend fun signUp(name: String, email: String, password: String): Result<Unit>

    suspend fun sendVerificationEmail(): Result<Unit>

    suspend fun isEmailVerified(): Boolean

    fun signOut()
}
