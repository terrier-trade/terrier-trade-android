package edu.bu.terriertrade.util

/** Result of checking an email address. */
sealed interface EmailCheck {
    data object Valid : EmailCheck
    data object Empty : EmailCheck
    data object Malformed : EmailCheck
    data object NotBuEmail : EmailCheck
}

/**
 * Checks that an email looks real and ends in @bu.edu.
 * Plain Kotlin with no Android imports, so it runs in fast unit tests.
 */
object EmailValidator {
    private const val BU_DOMAIN = "bu.edu"
    private val EMAIL_SHAPE = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun check(email: String): EmailCheck {
        val trimmed = email.trim()
        return when {
            trimmed.isEmpty() -> EmailCheck.Empty
            !EMAIL_SHAPE.matches(trimmed) -> EmailCheck.Malformed
            !trimmed.substringAfterLast('@').equals(BU_DOMAIN, ignoreCase = true) -> EmailCheck.NotBuEmail
            else -> EmailCheck.Valid
        }
    }
}