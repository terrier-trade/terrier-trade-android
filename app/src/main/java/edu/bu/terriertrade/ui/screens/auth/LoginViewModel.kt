package edu.bu.terriertrade.ui.screens.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import edu.bu.terriertrade.data.firebase.FirebasePasswordReset
import edu.bu.terriertrade.data.firebase.PasswordReset
import edu.bu.terriertrade.data.repository.AuthRepository
import edu.bu.terriertrade.di.appContainer
import edu.bu.terriertrade.util.EmailCheck
import edu.bu.terriertrade.util.EmailValidator
import kotlinx.coroutines.launch

/**
 * Signs in with a BU email and password.
 * Tells the screen whether the email is verified, so navigation can send
 * unverified students to the Verify screen instead of the feed.
 * "Forgot password?" emails a reset link to the BU email typed in.
 */
class LoginViewModel(
    private val authRepository: AuthRepository,
    private val passwordReset: PasswordReset = PasswordReset { Result.failure(Exception(AuthMessages.GENERIC_ERROR)) },
) : ViewModel() {

    var uiState by mutableStateOf(LoginUiState())
        private set

    fun onEmailChange(value: String) {
        uiState = uiState.copy(email = value, emailError = null, submitError = null, infoMessage = null)
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

    /** Emails a reset link to the BU email in the field. Asks for the email first if it's missing. */
    fun onForgotPassword() {
        if (uiState.isSendingReset) return
        val email = uiState.email.trim()
        val emailError = emailProblem(email)
        if (emailError != null) {
            uiState = uiState.copy(
                emailError = if (email.isEmpty()) AuthMessages.ENTER_EMAIL_FOR_RESET else emailError,
                passwordError = null,
                infoMessage = null,
            )
            return
        }
        uiState = uiState.copy(isSendingReset = true, emailError = null, submitError = null, infoMessage = null)

        viewModelScope.launch {
            passwordReset.sendResetEmail(email)
                .onSuccess { uiState = uiState.copy(isSendingReset = false, infoMessage = AuthMessages.resetSent(email)) }
                .onFailure { error -> uiState = uiState.copy(isSendingReset = false, submitError = AuthMessages.from(error)) }
        }
    }

    /** Shows an error under every field that's wrong. Returns true when the form is OK. */
    fun validate(): Boolean {
        val state = uiState
        val emailError = emailProblem(state.email)
        val passwordError = if (state.password.isEmpty()) AuthMessages.ENTER_PASSWORD else null

        uiState = state.copy(emailError = emailError, passwordError = passwordError)
        return emailError == null && passwordError == null
    }

    private fun emailProblem(email: String): String? = when (EmailValidator.check(email)) {
        EmailCheck.Valid -> null
        EmailCheck.Empty -> "Enter your BU email"
        EmailCheck.Malformed -> "That doesn't look like an email address"
        EmailCheck.NotBuEmail -> "Use your @bu.edu email"
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { LoginViewModel(appContainer.authRepository, FirebasePasswordReset()) }
        }
    }
}
