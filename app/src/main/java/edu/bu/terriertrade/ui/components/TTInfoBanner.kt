package edu.bu.terriertrade.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.PreviewLightDark
import edu.bu.terriertrade.ui.theme.Spacing
import edu.bu.terriertrade.ui.theme.TTTheme

/** Success: green (verified, deal done). Warning: amber (offline, needs action). Neutral: grey (tips). */
enum class TTBannerTone { Success, Warning, Neutral }

/** Tinted box with an icon and a short message. */
@Composable
fun TTInfoBanner(
    text: String,
    modifier: Modifier = Modifier,
    tone: TTBannerTone = TTBannerTone.Neutral,
    icon: ImageVector = Icons.Outlined.Info,
) {
    val (container, content) = when (tone) {
        TTBannerTone.Success -> TTTheme.colors.successContainer to TTTheme.colors.onSuccessContainer
        TTBannerTone.Warning -> TTTheme.colors.warningContainer to TTTheme.colors.onWarningContainer
        TTBannerTone.Neutral -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(container, MaterialTheme.shapes.medium)
            .padding(Spacing.md),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = content)
        Text(text = text, style = MaterialTheme.typography.bodyMedium, color = content)
    }
}

@PreviewLightDark
@Composable
private fun TTInfoBannerPreview() {
    TTPreviewColumn {
        TTInfoBanner(
            text = "Once verified, your profile shows a BU verified badge.",
            tone = TTBannerTone.Success,
            icon = Icons.Outlined.VerifiedUser,
        )
        TTInfoBanner(text = "You're offline. Showing listings saved on this phone.", tone = TTBannerTone.Warning)
        TTInfoBanner(text = "Pay in person when you meet. Terrier Trade never handles money.")
    }
}