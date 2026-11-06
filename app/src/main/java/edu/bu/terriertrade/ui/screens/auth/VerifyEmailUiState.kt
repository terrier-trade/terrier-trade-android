package edu.bu.terriertrade.ui.screens.auth

/** Everything the Verify email screen draws. Firebase verifies with a link, so there's no code to type. */
data class VerifyEmailUiState(
    val isChecking: Boolean = false,
    val isResending: Boolean = false,
    val errorMessage: String? = null,
    val infoMessage: String? = null,
)