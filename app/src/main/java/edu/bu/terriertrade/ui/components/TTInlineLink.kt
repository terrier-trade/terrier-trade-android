package edu.bu.terriertrade.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark

/** Grey prompt followed by a red text button: "Already have an account? Sign in". */
@Composable
fun TTInlineLink(
    prompt: String,
    actionText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = prompt,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        TTTextButton(text = actionText, onClick = onClick)
    }
}

@PreviewLightDark
@Composable
private fun TTInlineLinkPreview() {
    TTPreviewColumn {
        TTInlineLink(prompt = "Already have an account?", actionText = "Sign in", onClick = {})
    }
}