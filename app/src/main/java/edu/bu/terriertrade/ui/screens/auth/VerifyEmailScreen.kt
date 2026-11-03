package edu.bu.terriertrade.ui.screens.auth

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.lifecycle.viewmodel.compose.viewModel
import edu.bu.terriertrade.ui.components.TTBackButton
import edu.bu.terriertrade.ui.components.TTBannerTone
import edu.bu.terriertrade.ui.components.TTBottomActions
import edu.bu.terriertrade.ui.components.TTCodeField
import edu.bu.terriertrade.ui.components.TTErrorText
import edu.bu.terriertrade.ui.components.TTIconCircle
import edu.bu.terriertrade.ui.components.TTInfoBanner
import edu.bu.terriertrade.ui.components.TTInlineLink
import edu.bu.terriertrade.ui.components.TTPrimaryButton
import edu.bu.terriertrade.ui.theme.Spacing
import edu.bu.terriertrade.ui.theme.TerrierTradeTheme

/** Stateful entry point used by navigation. Owns the ViewModel. */
@Composable
fun VerifyEmailRoute(
    email: String,
    onBack: () -> Unit,
    onVerified: () -> Unit,
    viewModel: VerifyEmailViewModel = viewModel(),
) {
    VerifyEmailScreen(
        email = email,
        uiState = viewModel.uiState,
        onCodeChange = viewModel::onCodeChange,
        onVerify = { if (viewModel.verify()) onVerified() },
        onResend = viewModel::resend,
        onBack = onBack,
    )
}

/** Stateless UI built from shared components. */
@Composable
fun VerifyEmailScreen(
    email: String,
    uiState: VerifyEmailUiState,
    onCodeChange: (String) -> Unit,
    onVerify: () -> Unit,
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
                    text = buildAnnotatedString {
                        append("We sent a ${VerifyEmailViewModel.CODE_LENGTH}-digit code to ")
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)) {
                            append(email)
                        }
                        append(". It expires in 10 minutes.")
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                TTCodeField(
                    code = uiState.code,
                    onCodeChange = onCodeChange,
                    length = VerifyEmailViewModel.CODE_LENGTH,
                    isError = uiState.errorMessage != null,
                    onDone = onVerify,
                    modifier = Modifier.fillMaxWidth(),
                )
                TTErrorText(message = uiState.errorMessage)
                TTInlineLink(prompt = "Didn't get it?", actionText = "Resend code", onClick = onResend)
                if (uiState.infoMessage != null) {
                    Text(
                        text = uiState.infoMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                    text = "Verify and continue",
                    onClick = onVerify,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun VerifyEmailScreenPreview() {
    TerrierTradeTheme {
        VerifyEmailScreen(
            email = "priya02@bu.edu",
            uiState = VerifyEmailUiState(code = "4820"),
            onCodeChange = {}, onVerify = {}, onResend = {}, onBack = {},
        )
    }
}