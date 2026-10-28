package edu.bu.terriertrade.ui.components

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.bu.terriertrade.ui.theme.Spacing
import edu.bu.terriertrade.ui.theme.TerrierTradeTheme

/**
 * Big red header with the crest, used on Welcome.
 * Draws behind the status bar, so the screen using it should not add top padding.
 */
@Composable
fun TTBrandHeader(
    title: String,
    modifier: Modifier = Modifier,
    eyebrow: String? = null,
    tagline: String? = null,
) {
    val onRed = MaterialTheme.colorScheme.onPrimary
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp),
            )
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = Spacing.xl, vertical = Spacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        TTCrestLogo(modifier = Modifier.padding(bottom = Spacing.md))
        if (eyebrow != null) {
            Text(
                text = eyebrow,
                style = MaterialTheme.typography.bodyMedium,
                color = onRed.copy(alpha = 0.85f),
            )
        }
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.displaySmall,
            color = onRed,
            textAlign = TextAlign.Center,
        )
        if (tagline != null) {
            Text(
                text = tagline.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                letterSpacing = 1.5.sp,
                color = onRed.copy(alpha = 0.85f),
                modifier = Modifier.padding(top = Spacing.xs),
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun TTBrandHeaderPreview() {
    TerrierTradeTheme {
        TTBrandHeader(
            eyebrow = "Welcome to",
            title = "Terrier Trade",
            tagline = "For BU students · Est. 2026",
        )
    }
}