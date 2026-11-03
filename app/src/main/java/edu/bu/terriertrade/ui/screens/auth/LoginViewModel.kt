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
 * Signs in with a BU email and password.
 * Tells the screen whether the email is verified, so navigation can send
 * unverified students to the Verify screen instead of the feed.
 */
class LoginViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

    var uiState by mutableStateOf(LoginUiState())
        private set

    fun onEmailChange(value: String) {
        uiState = uiState.copy(email = value, emailError = null, submitError = null)
    }

    fun onPasswordChange(value: String) {
        uiState = uiState.copy(password = value, passwordError = null, submitError = null)
    }

    fun submit(onSignedIn: (isVerified: Boolean) -> Unit) {
        if (uiState.isSubmitting || !validate()) return
        val form = uiState
        uiState = form.copy(isSubmitting = true, submitError = null)

        viewModelScope.launch {
            authRepository.signIn(form.email.trim(), form.password)
                .onSuccess {
                    val verified = authRepository.isEmailVerified()
                    uiState = uiState.copy(isSubmitting = false)
                    onSignedIn(verified)
                }
                .onFailure { error ->
                    uiState = uiState.copy(isSubmitting = false, submitError = AuthMessages.from(error))
                }
        }
    }

    /** Shows an error under every field that's wrong. Returns true when the form is OK. */
    fun validate(): Boolean {
        val state = uiState
        val emailError = when (EmailValidator.check(state.email)) {
            EmailCheck.Valid -> null
            EmailCheck.Empty -> "Enter your BU email"
            EmailCheck.Malformed -> "That doesn't look like an email address"
            EmailCheck.NotBuEmail -> "Use your @bu.edu email"
        }
        val passwordError = if (state.password.isEmpty()) AuthMessages.ENTER_PASSWORD else null

        uiState = state.copy(emailError = emailError, passwordError = passwordError)
        return emailError == null && passwordError == null
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { LoginViewModel(appContainer.authRepository) }
        }
    }
}