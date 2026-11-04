package edu.bu.terriertrade.ui.screens.auth

/** Messages shared by the auth screens, kept in one place so tests can check them. */
object AuthMessages {
    const val GENERIC_ERROR = "Something went wrong. Try again."
    const val NOT_VERIFIED_YET = "Not verified yet. Tap the link in the email, then try again."
    const val EMAIL_RESENT = "Sent again. Check your inbox and your Junk folder."
    const val ENTER_PASSWORD = "Enter your password"
    const val ENTER_EMAIL_FOR_RESET = "Enter your BU email, then tap Forgot password"

    /** Firebase doesn't say whether the account exists, so neither do we. */
    fun resetSent(email: String) =
        "If there's an account for $email, we sent a link to reset your password. Check Junk too."

    /** Turns a failed Result into text for the screen. */
    fun from(error: Throwable): String = error.message ?: GENERIC_ERROR
}
