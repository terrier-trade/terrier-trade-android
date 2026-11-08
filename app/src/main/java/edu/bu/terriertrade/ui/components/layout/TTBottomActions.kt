package edu.bu.terriertrade.ui.components.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import edu.bu.terriertrade.ui.components.buttons.TTInlineLink
import edu.bu.terriertrade.ui.components.buttons.TTPrimaryButton
import edu.bu.terriertrade.ui.preview.TTPreviewColumn
import edu.bu.terriertrade.ui.theme.Spacing

/**
 * The area for buttons pinned to the bottom of a screen.
 * Stays above the system navigation bar and moves up with the keyboard.
 */
@Composable
fun TTBottomActions(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .imePadding()
            .padding(horizontal = Spacing.xl, vertical = Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        horizontalAlignment = Alignment.CenterHorizontally,
        content = content,
    )
}

@PreviewLightDark
@Composable
private fun TTBottomActionsPreview() {
    TTPreviewColumn {
        TTBottomActions {
            TTPrimaryButton(text = "Send verification email", onClick = {}, modifier = Modifier.fillMaxWidth())
            TTInlineLink(prompt = "Already have an account?", actionText = "Sign in", onClick = {})
        }
    }
}