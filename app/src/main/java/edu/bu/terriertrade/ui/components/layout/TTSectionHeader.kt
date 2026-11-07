package edu.bu.terriertrade.ui.components.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewLightDark
import edu.bu.terriertrade.ui.components.buttons.TTTextButton
import edu.bu.terriertrade.ui.preview.TTPreviewColumn

/** Section title on the left; either a small grey note or a "See all"-style action on the right. */
@Composable
fun TTSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    trailingText: String? = null,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.semantics { heading() },
        )
        when {
            actionText != null && onAction != null -> TTTextButton(text = actionText, onClick = onAction)
            trailingText != null -> Text(
                text = trailingText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun TTSectionHeaderPreview() {
    TTPreviewColumn {
        TTSectionHeader(title = "Fresh on campus", trailingText = "6 listings")
        TTSectionHeader(title = "My listings", actionText = "See all", onAction = {})
    }
}