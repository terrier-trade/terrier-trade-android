package edu.bu.terriertrade.ui.screens.auth

import edu.bu.terriertrade.data.firebase.PasswordReset

/** Records reset emails instead of sending them. Set [result] to make sending fail. */
class TestPasswordReset : PasswordReset {
    val sentTo = mutableListOf<String>()
    var result: Result<Unit> = Result.success(Unit)

    override suspend fun sendResetEmail(email: String): Result<Unit> {
        sentTo += email
        return result
    }
}
