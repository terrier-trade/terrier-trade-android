package edu.bu.terriertrade.util

/**
 * Password rules for new accounts: at least 8 characters and at least one special character,
 * like ! @ # $ or %. Plain Kotlin, so it runs in fast unit tests.
 */
object PasswordRules {
    const val MIN_LENGTH = 8

    /** Shown under the password field before the student types. */
    const val HINT = "At least $MIN_LENGTH characters, including a special character like ! or @"

    /** Returns the problem to show on screen, or null when the password is OK. */
    fun problem(password: String): String? = when {
        password.length < MIN_LENGTH -> "Use at least $MIN_LENGTH characters"
        password.none { !it.isLetterOrDigit() && !it.isWhitespace() } -> "Add a special character, like ! or @"
        else -> null
    }
}
