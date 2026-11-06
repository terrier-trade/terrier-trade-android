package edu.bu.terriertrade.ui.screens.auth

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import edu.bu.terriertrade.ui.components.TTBackButton
import edu.bu.terriertrade.ui.components.TTBannerTone
import edu.bu.terriertrade.ui.components.TTBottomActions
import edu.bu.terriertrade.ui.components.TTErrorText
import edu.bu.terriertrade.ui.components.TTIconCircle
import edu.bu.terriertrade.ui.components.TTInfoBanner
import edu.bu.terriertrade.ui.components.TTInlineLink
import edu.bu.terriertrade.ui.components.TTPrimaryButton
import edu.bu.terriertrade.ui.theme.Spacing
import edu.bu.terriertrade.ui.theme.TerrierTradeTheme
import kotlinx.coroutines.delay

/** Stateful entry point used by navigation. Owns the ViewModel. */
@Composable
fun VerifyEmailRoute(
    onBack: () -> Unit,
    onVerified: () -> Unit,
    viewModel: VerifyEmailViewModel = viewModel(factory = VerifyEmailViewModel.Factory),
) {
    val cancelAndGoBack = {
        viewModel.cancel()
        onBack()
    }

    // While this screen is on screen, quietly ask Firebase every few seconds whether the link was tapped.
    // So it moves on by itself, whether the link was opened on this phone or on a laptop.
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val latestOnVerified by rememberUpdatedState(onVerified)
    LaunchedEffect(viewModel, lifecycle) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            while (true) {
                viewModel.checkVerified(onVerified = { latestOnVerified() }, silent = true)
                delay(VerifyEmailViewModel.AUTO_CHECK_MILLIS)
            }
        }
    }
    BackHandler(onBack = cancelAndGoBack)

    VerifyEmailScreen(
        uiState = viewModel.uiState,
        onCheck = { viewModel.checkVerified(onVerified = onVerified) },
        onResend = viewModel::resend,
        onBack = cancelAndGoBack,
    )
}

/** Stateless UI built from shared components. */
@Composable
fun VerifyEmailScreen(
    uiState: VerifyEmailUiState,
    onCheck: () -> Unit,
    onResend: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TTBackButton(
                onClick = onBack,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(start = Spacing.lg, top = Spacing.md),
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = Spacing.xl, vertical = Spacing.xl),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Spacing.lg),
            ) {
                TTIconCircle(icon = Icons.Outlined.Email)
                Text(
                    text = "Check your BU inbox".uppercase(),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = "We sent a link to your @bu.edu email. Tap it and this screen moves on by itself. " +
                        "It can take a minute, and BU mail sometimes puts it in Junk.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                TTErrorText(message = uiState.errorMessage)
                if (uiState.infoMessage != null) {
                    Text(
                        text = uiState.infoMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
                TTInfoBanner(
                    text = "Once verified, your profile shows a BU verified badge so other students know you're real.",
                    tone = TTBannerTone.Success,
                    icon = Icons.Outlined.VerifiedUser,
                )
            }

            TTBottomActions {
                TTPrimaryButton(
                    text = "I've verified",
                    onClick = onCheck,
                    loading = uiState.isChecking,
                    modifier = Modifier.fillMaxWidth(),
                )
                TTInlineLink(
                    prompt = "Didn't get it?",
                    actionText = if (uiState.isResending) "Sending…" else "Resend email",
                    onClick = onResend,
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun VerifyEmailScreenPreview() {
    TerrierTradeTheme {
        VerifyEmailScreen(uiState = VerifyEmailUiState(), onCheck = {}, onResend = {}, onBack = {})
    }
}

@PreviewLightDark
@Composable
private fun VerifyEmailScreenNotYetPreview() {
    TerrierTradeTheme {
        VerifyEmailScreen(
            uiState = VerifyEmailUiState(errorMessage = AuthMessages.NOT_VERIFIED_YET),
            onCheck = {}, onResend = {}, onBack = {},
        )
    }
}
