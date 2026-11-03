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
import kotlinx.coroutines.launch

/**
 * The student taps the link in their email, then comes back here.
 * We ask Firebase whether the link was opened and move on when it was.
 */
class VerifyEmailViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

    var uiState by mutableStateOf(VerifyEmailUiState())
        private set

    /** The screen checks every few seconds, so make sure we only move on once. */
    private var movedOn = false

    /**
     * Checks with Firebase. silent = true is for the automatic checks while the screen is open:
     * it moves on if verified but shows no error if not.
     */
    fun checkVerified(onVerified: () -> Unit, silent: Boolean = false) {
        if (uiState.isChecking) return
        if (!silent) uiState = uiState.copy(isChecking = true, errorMessage = null, infoMessage = null)

        viewModelScope.launch {
            val verified = authRepository.isEmailVerified()
            uiState = when {
                verified -> uiState.copy(isChecking = false, errorMessage = null)
                silent -> uiState
                else -> uiState.copy(isChecking = false, errorMessage = AuthMessages.NOT_VERIFIED_YET)
            }
            if (verified && !movedOn) {
                movedOn = true
                onVerified()
            }
        }
    }

    fun resend() {
        if (uiState.isResending) return
        uiState = uiState.copy(isResending = true, errorMessage = null, infoMessage = null)

        viewModelScope.launch {
            authRepository.sendVerificationEmail()
                .onSuccess { uiState = uiState.copy(isResending = false, infoMessage = AuthMessages.EMAIL_RESENT) }
                .onFailure { error ->
                    uiState = uiState.copy(isResending = false, errorMessage = AuthMessages.from(error))
                }
        }
    }

    /** Going back means starting over, so we sign out the unverified account. */
    fun cancel() {
        authRepository.signOut()
    }

    companion object {
        /** How often the Verify screen asks Firebase whether the link was tapped. */
        const val AUTO_CHECK_MILLIS = 4_000L

        val Factory = viewModelFactory {
            initializer { VerifyEmailViewModel(appContainer.authRepository) }
        }
    }
}
