package edu.bu.terriertrade.ui.components.feedback

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewLightDark
import edu.bu.terriertrade.ui.preview.TTPreviewColumn

/**
 * Small red error line under a field or group. Draws nothing when [message] is null.
 * TalkBack reads it out when it appears.
 */
@Composable
fun TTErrorText(
    message: String?,
    modifier: Modifier = Modifier,
) {
    if (message == null) return
    Text(
        text = message,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.error,
        modifier = modifier.semantics { liveRegion = LiveRegionMode.Polite },
    )
}

@PreviewLightDark
@Composable
private fun TTErrorTextPreview() {
    TTPreviewColumn {
        TTErrorText(message = "Pick your year")
    }
}