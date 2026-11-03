package edu.bu.terriertrade.ui.screens.auth

/** Everything the Sign in screen draws. Errors are null when the field is fine. */
data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val submitError: String? = null,
    val isSubmitting: Boolean = false,
)