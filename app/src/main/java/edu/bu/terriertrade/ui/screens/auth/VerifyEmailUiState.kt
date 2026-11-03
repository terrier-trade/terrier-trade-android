package edu.bu.terriertrade.ui.screens.auth

data class VerifyEmailUiState(
    val code: String = "",
    val errorMessage: String? = null,
    val infoMessage: String? = null,
)