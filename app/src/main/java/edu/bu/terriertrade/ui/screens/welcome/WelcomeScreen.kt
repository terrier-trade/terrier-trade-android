package edu.bu.terriertrade.ui.screens.welcome

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import edu.bu.terriertrade.ui.components.TTBrandHeader
import edu.bu.terriertrade.ui.components.TTFeatureRow
import edu.bu.terriertrade.ui.components.TTPrimaryButton
import edu.bu.terriertrade.ui.components.TTSecondaryButton
import edu.bu.terriertrade.ui.theme.Spacing
import edu.bu.terriertrade.ui.theme.TerrierTradeTheme

/**
 * First screen of the app. Stateless: navigation decides what the buttons do.
 * The header scrolls with the content on small phones; the buttons stay at the bottom.
 */
@Composable
fun WelcomeScreen(
    onGetStarted: () -> Unit,
    onSignIn: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
            ) {
                TTBrandHeader(
                    eyebrow = "Welcome to",
                    title = "Terrier Trade",
                    tagline = "For BU students · Est. 2026",
                )
                Column(
                    modifier = Modifier.padding(horizontal = Spacing.xl, vertical = Spacing.xl),
                    verticalArrangement = Arrangement.spacedBy(Spacing.lg),
                ) {
                    Text(
                        text = "Buy, sell, and swap with fellow Terriers",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    TTFeatureRow(icon = Icons.Outlined.VerifiedUser, text = "Verified BU students only")
                    TTFeatureRow(icon = Icons.Outlined.PhotoCamera, text = "Snap a photo or scan a book, set a price")
                    TTFeatureRow(icon = Icons.Outlined.SwapHoriz, text = "Trade skills too, not just stuff")
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(horizontal = Spacing.xl, vertical = Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(Spacing.md),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                TTPrimaryButton(
                    text = "Get started",
                    onClick = onGetStarted,
                    modifier = Modifier.fillMaxWidth(),
                )
                TTSecondaryButton(
                    text = "I already have an account",
                    onClick = onSignIn,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = "Sign up with your @bu.edu email",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun WelcomeScreenPreview() {
    TerrierTradeTheme {
        WelcomeScreen(onGetStarted = {}, onSignIn = {})
    }
}