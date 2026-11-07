package edu.bu.terriertrade.ui.components.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import edu.bu.terriertrade.ui.components.buttons.TTBackButton
import edu.bu.terriertrade.ui.components.buttons.TTBackButtonStyle
import edu.bu.terriertrade.ui.theme.Spacing
import edu.bu.terriertrade.ui.theme.TerrierTradeTheme
import edu.bu.terriertrade.ui.theme.TTTheme

/**
 * Red screen header with rounded bottom corners, optional back button, title, and subtitle.
 * Draws behind the status bar, so the screen using it should not add top padding.
 */
@Composable
fun TTScreenHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
) {
    val onRed = TTTheme.colors.onHeader
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = TTTheme.colors.header,
                shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
            )
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(start = Spacing.lg, end = Spacing.lg, top = Spacing.md, bottom = Spacing.xl),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        if (onBack != null) {
            TTBackButton(onClick = onBack, style = TTBackButtonStyle.OnRed)
        }
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.headlineLarge,
            color = onRed,
            modifier = Modifier.padding(horizontal = Spacing.sm),
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = onRed.copy(alpha = 0.85f),
                modifier = Modifier.padding(horizontal = Spacing.sm),
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun TTScreenHeaderPreview() {
    TerrierTradeTheme {
        TTScreenHeader(
            title = "Create your account",
            subtitle = "Step 1 of 2 · Only @bu.edu emails can join",
            onBack = {},
        )
    }
}