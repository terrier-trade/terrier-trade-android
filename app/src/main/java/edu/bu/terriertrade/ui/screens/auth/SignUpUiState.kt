package edu.bu.terriertrade.ui.screens.auth

enum class ClassYear(val label: String) {
    FIRST_YEAR("First year"),
    SOPHOMORE("Sophomore"),
    JUNIOR("Junior"),
    SENIOR("Senior"),
    GRAD("Grad"),
}

/**
 * Everything the Sign up screen draws. Errors are null when the field is fine.
 * submitError is for problems from Firebase, like an email that already has an account.
 */
data class SignUpUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val year: ClassYear? = null,
    val agreedToRules: Boolean = false,
    val nameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val yearError: String? = null,
    val rulesError: String? = null,
    val submitError: String? = null,
    val isSubmitting: Boolean = false,
)