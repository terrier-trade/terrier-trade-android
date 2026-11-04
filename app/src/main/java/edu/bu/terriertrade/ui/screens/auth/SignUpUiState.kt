package edu.bu.terriertrade.ui.screens.auth

/**
 * Everything the Create account form draws. Errors are null when the field is fine.
 * submitError is for problems from Firebase, like an email that already has an account.
 */
data class SignUpUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val agreedToRules: Boolean = false,
    val nameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val rulesError: String? = null,
    val submitError: String? = null,
    val isSubmitting: Boolean = false,
)
