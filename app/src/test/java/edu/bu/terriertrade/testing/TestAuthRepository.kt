package edu.bu.terriertrade.testing

import edu.bu.terriertrade.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow

/** An AuthRepository for tests: set what each call returns, then check what was called. */
class TestAuthRepository(
    var signUpResult: Result<Unit> = Result.success(Unit),
    var signInResult: Result<Unit> = Result.success(Unit),
    var sendEmailResult: Result<Unit> = Result.success(Unit),
    var verified: Boolean = false,
) : AuthRepository {

    override val currentUserId = MutableStateFlow<String?>(null)

    var signUpCalls = 0
    var signInCalls = 0
    var sendEmailCalls = 0
    var signOutCalls = 0
    var lastSignUpName: String? = null

    override suspend fun signIn(email: String, password: String): Result<Unit> {
        signInCalls++
        return signInResult
    }

    override suspend fun signUp(name: String, email: String, password: String): Result<Unit> {
        signUpCalls++
        lastSignUpName = name
        return signUpResult
    }

    override suspend fun sendVerificationEmail(): Result<Unit> {
        sendEmailCalls++
        return sendEmailResult
    }

    override suspend fun isEmailVerified(): Boolean = verified

    override fun signOut() {
        signOutCalls++
    }
}