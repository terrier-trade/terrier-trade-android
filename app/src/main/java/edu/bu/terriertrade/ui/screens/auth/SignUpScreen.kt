package edu.bu.terriertrade.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.lifecycle.viewmodel.compose.viewModel
import edu.bu.terriertrade.ui.components.TTBottomActions
import edu.bu.terriertrade.ui.components.TTCheckboxRow
import edu.bu.terriertrade.ui.components.TTChoiceChips
import edu.bu.terriertrade.ui.components.TTEmailField
import edu.bu.terriertrade.ui.components.TTInlineLink
import edu.bu.terriertrade.ui.components.TTPasswordField
import edu.bu.terriertrade.ui.components.TTPrimaryButton
import edu.bu.terriertrade.ui.components.TTScreenHeader
import edu.bu.terriertrade.ui.components.TTTextField
import edu.bu.terriertrade.ui.theme.Spacing
import edu.bu.terriertrade.ui.theme.TerrierTradeTheme

/** Stateful entry point used by navigation. Owns the ViewModel. */
@Composable
fun SignUpRoute(
    onBack: () -> Unit,
    onCodeSent: (email: String) -> Unit,
    onSignIn: () -> Unit,
    viewModel: SignUpViewModel = viewModel(),
) {
    SignUpScreen(
        uiState = viewModel.uiState,
        onNameChange = viewModel::onNameChange,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onYearSelect = viewModel::onYearSelect,
        onAgreeChange = viewModel::onAgreeChange,
        onSubmit = {
            if (viewModel.validate()) onCodeSent(viewModel.uiState.email.trim())
        },
        onBack = onBack,
        onSignIn = onSignIn,
    )
}

/** Stateless UI built from shared components. */
@Composable
fun SignUpScreen(
    uiState: SignUpUiState,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onYearSelect: (ClassYear) -> Unit,
    onAgreeChange: (Boolean) -> Unit,
    onSubmit: () -> Unit,
    onBack: () -> Unit,
    onSignIn: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TTScreenHeader(
                title = "Create your account",
                subtitle = "Step 1 of 2 · Only @bu.edu emails can join",
                onBack = onBack,
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = Spacing.xl, vertical = Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(Spacing.md),
            ) {
                TTTextField(
                    value = uiState.name,
                    onValueChange = onNameChange,
                    label = "Full name",
                    errorMessage = uiState.nameError,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
                TTEmailField(
                    value = uiState.email,
                    onValueChange = onEmailChange,
                    errorMessage = uiState.emailError,
                    modifier = Modifier.fillMaxWidth(),
                )
                TTPasswordField(
                    value = uiState.password,
                    onValueChange = onPasswordChange,
                    errorMessage = uiState.passwordError,
                    helperText = "At least ${SignUpViewModel.MIN_PASSWORD_LENGTH} characters",
                    modifier = Modifier.fillMaxWidth(),
                )
                TTChoiceChips(
                    label = "Year",
                    options = ClassYear.entries,
                    selected = uiState.year,
                    onSelect = onYearSelect,
                    optionLabel = { it.label },
                    errorMessage = uiState.yearError,
                )
                TTCheckboxRow(
                    checked = uiState.agreedToRules,
                    onCheckedChange = onAgreeChange,
                    text = "I'll meet in public places on campus and follow the community rules.",
                    errorMessage = uiState.rulesError,
                )
            }

            TTBottomActions {
                TTPrimaryButton(
                    text = "Send verification email",
                    onClick = onSubmit,
                    loading = uiState.isSubmitting,
                    modifier = Modifier.fillMaxWidth(),
                )
                TTInlineLink(prompt = "Already have an account?", actionText = "Sign in", onClick = onSignIn)
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun SignUpScreenPreview() {
    TerrierTradeTheme {
        SignUpScreen(
            uiState = SignUpUiState(name = "Priya Shah", email = "priya@bu.edu", year = ClassYear.JUNIOR),
            onNameChange = {}, onEmailChange = {}, onPasswordChange = {}, onYearSelect = {},
            onAgreeChange = {}, onSubmit = {}, onBack = {}, onSignIn = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun SignUpScreenErrorsPreview() {
    TerrierTradeTheme {
        SignUpScreen(
            uiState = SignUpUiState(
                email = "priya@gmail.com",
                nameError = "Enter your name",
                emailError = "Use your @bu.edu email",
                passwordError = "Use at least 8 characters",
                yearError = "Pick your year",
                rulesError = "Agree to the community rules to continue",
            ),
            onNameChange = {}, onEmailChange = {}, onPasswordChange = {}, onYearSelect = {},
            onAgreeChange = {}, onSubmit = {}, onBack = {}, onSignIn = {},
        )
    }
}