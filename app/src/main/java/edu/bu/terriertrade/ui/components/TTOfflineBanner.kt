package edu.bu.terriertrade.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewLightDark
import edu.bu.terriertrade.ui.theme.Spacing

/** Shown at the top of a screen while offline. TalkBack announces it when it appears. */
@Composable
fun TTOfflineBanner(
    modifier: Modifier = Modifier,
    message: String = "You're offline. Showing saved listings.",
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.inverseSurface,
        contentColor = MaterialTheme.colorScheme.inverseOnSurface,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier
                .padding(horizontal = Spacing.lg, vertical = Spacing.sm)
                .semantics { liveRegion = LiveRegionMode.Polite },
        )
    }
}

@PreviewLightDark
@Composable
private fun TTOfflineBannerPreview() {
    TTPreviewColumn {
        TTOfflineBanner()
    }
}