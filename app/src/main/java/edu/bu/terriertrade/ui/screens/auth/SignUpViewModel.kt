package edu.bu.terriertrade.ui.screens.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import edu.bu.terriertrade.data.repository.AuthRepository
import edu.bu.terriertrade.di.appContainer
import edu.bu.terriertrade.util.EmailCheck
import edu.bu.terriertrade.util.EmailValidator
import edu.bu.terriertrade.util.PasswordRules
import kotlinx.coroutines.launch

/**
 * Holds the Create account form, checks it, then creates the account and sends the verification email.
 */
class SignUpViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

    var uiState by mutableStateOf(SignUpUiState())
        private set

    fun onNameChange(value: String) {
        uiState = uiState.copy(name = value, nameError = null, submitError = null)
    }

    fun onEmailChange(value: String) {
        uiState = uiState.copy(email = value, emailError = null, submitError = null)
    }

    fun onPasswordChange(value: String) {
        uiState = uiState.copy(password = value, passwordError = null, confirmPasswordError = null, submitError = null)
    }

    fun onConfirmPasswordChange(value: String) {
        uiState = uiState.copy(confirmPassword = value, confirmPasswordError = null, submitError = null)
    }

    fun onAgreeChange(agreed: Boolean) {
        uiState = uiState.copy(agreedToRules = agreed, rulesError = null)
    }

    /**
     * Creates the account when the form is OK. Calls onAccountCreated once the
     * verification email is on its way. If sending fails, the Verify screen has Resend.
     */
    fun submit(onAccountCreated: () -> Unit) {
        if (uiState.isSubmitting || !validate()) return
        val form = uiState
        uiState = form.copy(isSubmitting = true, submitError = null)

        viewModelScope.launch {
            authRepository.signUp(form.name.trim(), form.email.trim(), form.password)
                .onSuccess {
                    authRepository.sendVerificationEmail()
                    uiState = uiState.copy(isSubmitting = false)
                    onAccountCreated()
                }
                .onFailure { error ->
                    uiState = uiState.copy(isSubmitting = false, submitError = AuthMessages.from(error))
                }
        }
    }

    /** Shows an error under every field that's wrong. Returns true when the form is OK. */
    fun validate(): Boolean {
        val state = uiState
        val nameError = if (state.name.isBlank()) "Enter your name" else null
        val emailError = when (EmailValidator.check(state.email)) {
            EmailCheck.Valid -> null
            EmailCheck.Empty -> "Enter your BU email"
            EmailCheck.Malformed -> "That doesn't look like an email address"
            EmailCheck.NotBuEmail -> "Use your @bu.edu email"
        }
        val passwordError = PasswordRules.problem(state.password)
        val confirmPasswordError = when {
            state.confirmPassword.isEmpty() -> "Type your password again"
            state.confirmPassword != state.password -> "Passwords don't match"
            else -> null
        }
        val rulesError = if (!state.agreedToRules) "Agree to the community rules to continue" else null

        uiState = state.copy(
            nameError = nameError,
            emailError = emailError,
            passwordError = passwordError,
            confirmPasswordError = confirmPasswordError,
            rulesError = rulesError,
        )
        return listOf(nameError, emailError, passwordError, confirmPasswordError, rulesError).all { it == null }
    }

    companion object {
        const val MIN_PASSWORD_LENGTH = PasswordRules.MIN_LENGTH

        val Factory = viewModelFactory {
            initializer { SignUpViewModel(appContainer.authRepository) }
        }
    }
}
