package edu.bu.terriertrade.ui.screens.auth

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MarkEmailRead
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.lifecycle.viewmodel.compose.viewModel
import edu.bu.terriertrade.ui.components.TTBannerTone
import edu.bu.terriertrade.ui.components.TTBottomActions
import edu.bu.terriertrade.ui.components.TTBrandHeader
import edu.bu.terriertrade.ui.components.TTCheckboxRow
import edu.bu.terriertrade.ui.components.TTEmailField
import edu.bu.terriertrade.ui.components.TTErrorText
import edu.bu.terriertrade.ui.components.TTFeatureRow
import edu.bu.terriertrade.ui.components.TTInfoBanner
import edu.bu.terriertrade.ui.components.TTPasswordField
import edu.bu.terriertrade.ui.components.TTPrimaryButton
import edu.bu.terriertrade.ui.components.TTSegmentedTabs
import edu.bu.terriertrade.ui.components.TTTextButton
import edu.bu.terriertrade.ui.components.TTTextField
import edu.bu.terriertrade.ui.theme.Spacing
import edu.bu.terriertrade.ui.theme.TerrierTradeTheme
import edu.bu.terriertrade.util.PasswordRules

/** The two tabs on the first screen. */
enum class AuthTab(val label: String) {
    SIGN_IN("Sign in"),
    CREATE_ACCOUNT("Create account"),
}

/** Everything the student can do on the sign-in screen, so the screen takes one parameter for it. */
data class AuthActions(
    val onTabSelect: (AuthTab) -> Unit = {},
    // Sign in
    val onLoginEmailChange: (String) -> Unit = {},
    val onLoginPasswordChange: (String) -> Unit = {},
    val onSignIn: () -> Unit = {},
    val onForgotPassword: () -> Unit = {},
    // Create account
    val onNameChange: (String) -> Unit = {},
    val onSignUpEmailChange: (String) -> Unit = {},
    val onSignUpPasswordChange: (String) -> Unit = {},
    val onConfirmPasswordChange: (String) -> Unit = {},
    val onAgreeChange: (Boolean) -> Unit = {},
    val onRulesClick: () -> Unit = {},
    val onCreateAccount: () -> Unit = {},
)

/**
 * The first screen, like "Sign in" in the proposal: one red header, then Sign in | Create account tabs.
 * After Create account, it switches to Sign in with the email filled in and says to tap the link first.
 * Signing in before the link is tapped gives isVerified = false, so navigation can show the Verify screen.
 */
@Composable
fun AuthRoute(
    onSignedIn: (isVerified: Boolean) -> Unit,
    initialTab: AuthTab = AuthTab.SIGN_IN,
    loginViewModel: LoginViewModel = viewModel(factory = LoginViewModel.Factory),
    signUpViewModel: SignUpViewModel = viewModel(factory = SignUpViewModel.Factory),
) {
    var tab by rememberSaveable { mutableStateOf(initialTab) }
    var showRules by rememberSaveable { mutableStateOf(false) }
    var notice by rememberSaveable { mutableStateOf<String?>(null) }

    if (showRules) {
        BackHandler { showRules = false }
        CommunityRulesScreen(
            onBack = { showRules = false },
            onAgree = {
                signUpViewModel.onAgreeChange(true)
                showRules = false
            },
        )
        return
    }

    AuthScreen(
        tab = tab,
        login = loginViewModel.uiState,
        signUp = signUpViewModel.uiState,
        notice = notice,
        actions = AuthActions(
            onTabSelect = { tab = it },
            onLoginEmailChange = loginViewModel::onEmailChange,
            onLoginPasswordChange = loginViewModel::onPasswordChange,
            onSignIn = { loginViewModel.submit(onSignedIn) },
            onForgotPassword = {
                notice = null
                loginViewModel.onForgotPassword()
            },
            onNameChange = signUpViewModel::onNameChange,
            onSignUpEmailChange = signUpViewModel::onEmailChange,
            onSignUpPasswordChange = signUpViewModel::onPasswordChange,
            onConfirmPasswordChange = signUpViewModel::onConfirmPasswordChange,
            onAgreeChange = signUpViewModel::onAgreeChange,
            onRulesClick = { showRules = true },
            onCreateAccount = {
                signUpViewModel.submit {
                    val email = signUpViewModel.uiState.email.trim()
                    loginViewModel.onEmailChange(email)
                    notice = "Account created! We sent a link to $email. Tap it, then sign in here."
                    tab = AuthTab.SIGN_IN
                }
            },
        ),
    )
}

/** Stateless UI: header, tabs, then the Sign in or Create account form. */
@Composable
fun AuthScreen(
    tab: AuthTab,
    login: LoginUiState,
    signUp: SignUpUiState,
    actions: AuthActions,
    modifier: Modifier = Modifier,
    notice: String? = null,
) {
    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
            ) {
                TTBrandHeader(
                    eyebrow = "Welcome to",
                    title = "Terrier Trade",
                    tagline = "Student marketplace · BU",
                    uppercaseTitle = false,
                )
                Column(
                    modifier = Modifier.padding(horizontal = Spacing.xl, vertical = Spacing.lg),
                    verticalArrangement = Arrangement.spacedBy(Spacing.md),
                ) {
                    TTSegmentedTabs(
                        options = AuthTab.entries,
                        selected = tab,
                        onSelect = actions.onTabSelect,
                        optionLabel = { it.label },
                    )
                    when (tab) {
                        AuthTab.SIGN_IN -> SignInForm(login, actions, notice)
                        AuthTab.CREATE_ACCOUNT -> CreateAccountForm(signUp, actions)
                    }
                }
            }

            TTBottomActions {
                when (tab) {
                    AuthTab.SIGN_IN -> {
                        TTErrorText(message = login.submitError)
                        TTPrimaryButton(
                            text = "Sign in",
                            onClick = actions.onSignIn,
                            loading = login.isSubmitting,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    AuthTab.CREATE_ACCOUNT -> {
                        TTErrorText(message = signUp.submitError)
                        TTPrimaryButton(
                            text = "Create account",
                            onClick = actions.onCreateAccount,
                            loading = signUp.isSubmitting,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
                Text(
                    text = "Only @bu.edu emails can sign up",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun SignInForm(login: LoginUiState, actions: AuthActions, notice: String?) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
        if (notice != null) {
            TTInfoBanner(text = notice, tone = TTBannerTone.Success, icon = Icons.Outlined.MarkEmailRead)
        } else {
            TTFeatureRow(icon = Icons.Outlined.VerifiedUser, text = "Verified BU students only")
            TTFeatureRow(icon = Icons.Outlined.PhotoCamera, text = "Snap a photo or scan a book, set a price")
            TTFeatureRow(icon = Icons.Outlined.SwapHoriz, text = "Trade skills too, not just stuff")
        }
        TTEmailField(
            value = login.email,
            onValueChange = actions.onLoginEmailChange,
            errorMessage = login.emailError,
            imeAction = ImeAction.Next,
            modifier = Modifier.fillMaxWidth(),
        )
        TTPasswordField(
            value = login.password,
            onValueChange = actions.onLoginPasswordChange,
            errorMessage = login.passwordError,
            imeAction = ImeAction.Done,
            keyboardActions = KeyboardActions(onDone = { actions.onSignIn() }),
            modifier = Modifier.fillMaxWidth(),
        )
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
            TTTextButton(
                text = if (login.isSendingReset) "Sending reset link…" else "Forgot password?",
                onClick = actions.onForgotPassword,
                enabled = !login.isSendingReset,
            )
        }
        if (login.infoMessage != null) {
            TTInfoBanner(text = login.infoMessage, tone = TTBannerTone.Success, icon = Icons.Outlined.MarkEmailRead)
        }
    }
}

@Composable
private fun CreateAccountForm(signUp: SignUpUiState, actions: AuthActions) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
        TTTextField(
            value = signUp.name,
            onValueChange = actions.onNameChange,
            label = "Full name",
            errorMessage = signUp.nameError,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next,
            ),
            modifier = Modifier.fillMaxWidth(),
        )
        TTEmailField(
            value = signUp.email,
            onValueChange = actions.onSignUpEmailChange,
            errorMessage = signUp.emailError,
            modifier = Modifier.fillMaxWidth(),
        )
        TTPasswordField(
            value = signUp.password,
            onValueChange = actions.onSignUpPasswordChange,
            errorMessage = signUp.passwordError,
            helperText = PasswordRules.HINT,
            imeAction = ImeAction.Next,
            modifier = Modifier.fillMaxWidth(),
        )
        TTPasswordField(
            value = signUp.confirmPassword,
            onValueChange = actions.onConfirmPasswordChange,
            label = "Confirm password",
            errorMessage = signUp.confirmPasswordError,
            modifier = Modifier.fillMaxWidth(),
        )
        TTCheckboxRow(
            checked = signUp.agreedToRules,
            onCheckedChange = actions.onAgreeChange,
            text = "I agree to the",
            linkText = "Terms & community rules",
            onLinkClick = actions.onRulesClick,
            errorMessage = signUp.rulesError,
        )
    }
}

@PreviewLightDark
@Composable
private fun AuthScreenSignInPreview() {
    TerrierTradeTheme {
        AuthScreen(tab = AuthTab.SIGN_IN, login = LoginUiState(), signUp = SignUpUiState(), actions = AuthActions())
    }
}

@PreviewLightDark
@Composable
private fun AuthScreenCreateAccountPreview() {
    TerrierTradeTheme {
        AuthScreen(
            tab = AuthTab.CREATE_ACCOUNT,
            login = LoginUiState(),
            signUp = SignUpUiState(name = "Priya Shah", email = "priya@bu.edu"),
            actions = AuthActions(),
        )
    }
}

/**
 * A working copy for trying the screen in Android Studio: switch tabs, type, open the Terms.
 * Click the ▶ next to this function → Run on device to open it on the emulator. Nothing is sent to Firebase.
 */
@Preview(name = "Sign in try-out", showSystemUi = true)
@Composable
private fun AuthTryOutPreview() {
    TerrierTradeTheme {
        var tab by remember { mutableStateOf(AuthTab.SIGN_IN) }
        var login by remember { mutableStateOf(LoginUiState()) }
        var signUp by remember { mutableStateOf(SignUpUiState()) }
        var showRules by remember { mutableStateOf(false) }
        var notice by remember { mutableStateOf<String?>(null) }

        if (showRules) {
            CommunityRulesScreen(
                onBack = { showRules = false },
                onAgree = {
                    signUp = signUp.copy(agreedToRules = true, rulesError = null)
                    showRules = false
                },
            )
        } else {
            AuthScreen(
                tab = tab,
                login = login,
                signUp = signUp,
                notice = notice,
                actions = AuthActions(
                    onTabSelect = { tab = it },
                    onLoginEmailChange = { login = login.copy(email = it, emailError = null, infoMessage = null) },
                    onLoginPasswordChange = { login = login.copy(password = it) },
                    onSignIn = { login = login.copy(submitError = "Try-out only: nothing was sent.") },
                    onForgotPassword = {
                        notice = null
                        login = if (login.email.isBlank()) {
                            login.copy(emailError = "Enter your BU email, then tap Forgot password")
                        } else {
                            login.copy(infoMessage = AuthMessages.resetSent(login.email))
                        }
                    },
                    onNameChange = { signUp = signUp.copy(name = it) },
                    onSignUpEmailChange = { signUp = signUp.copy(email = it) },
                    onSignUpPasswordChange = { signUp = signUp.copy(password = it, confirmPasswordError = null) },
                    onConfirmPasswordChange = { signUp = signUp.copy(confirmPassword = it, confirmPasswordError = null) },
                    onAgreeChange = { signUp = signUp.copy(agreedToRules = it, rulesError = null) },
                    onRulesClick = { showRules = true },
                    onCreateAccount = {
                        val mismatch = signUp.confirmPassword != signUp.password
                        val passwordProblem = PasswordRules.problem(signUp.password)
                        signUp = signUp.copy(
                            passwordError = passwordProblem,
                            confirmPasswordError = if (mismatch) "Passwords don't match" else null,
                            rulesError = if (!signUp.agreedToRules) "Agree to the community rules to continue" else null,
                        )
                        if (!mismatch && passwordProblem == null && signUp.agreedToRules) {
                            login = login.copy(email = signUp.email)
                            notice = "Account created! We sent a link to ${signUp.email}. Tap it, then sign in here."
                            tab = AuthTab.SIGN_IN
                        }
                    },
                ),
            )
        }
    }
}
