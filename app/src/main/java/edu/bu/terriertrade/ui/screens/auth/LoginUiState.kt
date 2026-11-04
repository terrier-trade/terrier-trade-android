package edu.bu.terriertrade.ui.screens.auth

/** Everything the Sign in form draws. Errors are null when the field is fine. */
data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val submitError: String? = null,
    val isSubmitting: Boolean = false,
    /** True while the "reset your password" email is being sent. */
    val isSendingReset: Boolean = false,
    /** Green note, like "If there's an account for …, we sent a reset link." */
    val infoMessage: String? = null,
)
