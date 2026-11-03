package edu.bu.terriertrade.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.lifecycle.viewmodel.compose.viewModel
import edu.bu.terriertrade.ui.components.TTBottomActions
import edu.bu.terriertrade.ui.components.TTEmailField
import edu.bu.terriertrade.ui.components.TTErrorText
import edu.bu.terriertrade.ui.components.TTInlineLink
import edu.bu.terriertrade.ui.components.TTPasswordField
import edu.bu.terriertrade.ui.components.TTPrimaryButton
import edu.bu.terriertrade.ui.components.TTScreenHeader
import edu.bu.terriertrade.ui.theme.Spacing
import edu.bu.terriertrade.ui.theme.TerrierTradeTheme

/** Stateful entry point used by navigation. Owns the ViewModel. */
@Composable
fun LoginRoute(
    onBack: () -> Unit,
    onSignedIn: (isVerified: Boolean) -> Unit,
    onCreateAccount: () -> Unit,
    viewModel: LoginViewModel = viewModel(factory = LoginViewModel.Factory),
) {
    LoginScreen(
        uiState = viewModel.uiState,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onSubmit = { viewModel.submit(onSignedIn) },
        onBack = onBack,
        onCreateAccount = onCreateAccount,
    )
}

/** Stateless UI built from shared components. */
@Composable
fun LoginScreen(
    uiState: LoginUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onBack: () -> Unit,
    onCreateAccount: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TTScreenHeader(
                title = "Welcome back",
                subtitle = "Sign in with your BU email",
                onBack = onBack,
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = Spacing.xl, vertical = Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(Spacing.md),
            ) {
                TTEmailField(
                    value = uiState.email,
                    onValueChange = onEmailChange,
                    errorMessage = uiState.emailError,
                    imeAction = ImeAction.Next,
                    modifier = Modifier.fillMaxWidth(),
                )
                TTPasswordField(
                    value = uiState.password,
                    onValueChange = onPasswordChange,
                    errorMessage = uiState.passwordError,
                    imeAction = ImeAction.Done,
                    keyboardActions = KeyboardActions(onDone = { onSubmit() }),
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            TTBottomActions {
                TTErrorText(message = uiState.submitError)
                TTPrimaryButton(
                    text = "Sign in",
                    onClick = onSubmit,
                    loading = uiState.isSubmitting,
                    modifier = Modifier.fillMaxWidth(),
                )
                TTInlineLink(prompt = "New to Terrier Trade?", actionText = "Create an account", onClick = onCreateAccount)
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun LoginScreenPreview() {
    TerrierTradeTheme {
        LoginScreen(
            uiState = LoginUiState(email = "priya02@bu.edu", password = "terriers2026"),
            onEmailChange = {}, onPasswordChange = {}, onSubmit = {}, onBack = {}, onCreateAccount = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun LoginScreenErrorPreview() {
    TerrierTradeTheme {
        LoginScreen(
            uiState = LoginUiState(email = "priya02@bu.edu", password = "wrong", submitError = "Wrong email or password."),
            onEmailChange = {}, onPasswordChange = {}, onSubmit = {}, onBack = {}, onCreateAccount = {},
        )
    }
}
