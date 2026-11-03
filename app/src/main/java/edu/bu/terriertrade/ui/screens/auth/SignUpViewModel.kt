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
import kotlinx.coroutines.launch

/**
 * Holds the Sign up form, checks it, then creates the account and sends the verification email.
 * TODO(profile): save the class year to the user's profile once the Firestore user repository lands.
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
        uiState = uiState.copy(password = value, passwordError = null, submitError = null)
    }

    fun onYearSelect(year: ClassYear) {
        uiState = uiState.copy(year = year, yearError = null)
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
        val passwordError =
            if (state.password.length < MIN_PASSWORD_LENGTH) "Use at least $MIN_PASSWORD_LENGTH characters" else null
        val yearError = if (state.year == null) "Pick your year" else null
        val rulesError = if (!state.agreedToRules) "Agree to the community rules to continue" else null

        uiState = state.copy(
            nameError = nameError,
            emailError = emailError,
            passwordError = passwordError,
            yearError = yearError,
            rulesError = rulesError,
        )
        return listOf(nameError, emailError, passwordError, yearError, rulesError).all { it == null }
    }

    companion object {
        const val MIN_PASSWORD_LENGTH = 8

        val Factory = viewModelFactory {
            initializer { SignUpViewModel(appContainer.authRepository) }
        }
    }
}