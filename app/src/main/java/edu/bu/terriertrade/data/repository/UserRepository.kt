package edu.bu.terriertrade.data.repository

import edu.bu.terriertrade.data.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    /** Emits the profile and again whenever it changes. Emits null if the user doesn't exist. */
    fun observeUser(userId: String): Flow<User?>

    suspend fun updateProfile(user: User): Result<Unit>
}
