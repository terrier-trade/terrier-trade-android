package edu.bu.terriertrade.ui.screens.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

/**
 * Holds the 6-digit code. No backend yet: any 6 digits count as correct.
 * The backend PR replaces verify() and resend() with Firebase calls.
 */
class VerifyEmailViewModel : ViewModel() {

    var uiState by mutableStateOf(VerifyEmailUiState())
        private set

    fun onCodeChange(code: String) {
        uiState = uiState.copy(code = code, errorMessage = null)
    }

    fun verify(): Boolean {
        if (uiState.code.length < CODE_LENGTH) {
            uiState = uiState.copy(errorMessage = "Enter all $CODE_LENGTH digits")
            return false
        }
        return true
    }

    fun resend() {
        uiState = uiState.copy(code = "", errorMessage = null, infoMessage = "New code sent. Check your inbox.")
    }

    companion object {
        const val CODE_LENGTH = 6
    }
}