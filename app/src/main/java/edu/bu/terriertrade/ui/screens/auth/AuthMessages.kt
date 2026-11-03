package edu.bu.terriertrade.ui.screens.auth

/** Messages shared by the auth screens, kept in one place so tests can check them. */
object AuthMessages {
    const val GENERIC_ERROR = "Something went wrong. Try again."
    const val NOT_VERIFIED_YET = "Not verified yet. Tap the link in the email, then try again."
    const val EMAIL_RESENT = "Sent again. Check your inbox and your Junk folder."
    const val ENTER_PASSWORD = "Enter your password"

    /** Turns a failed Result into text for the screen. */
    fun from(error: Throwable): String = error.message ?: GENERIC_ERROR
}