package edu.bu.terriertrade.ui.screens.chat

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.bu.terriertrade.ui.preview.TTPreviewColumn
import edu.bu.terriertrade.ui.theme.Spacing
import edu.bu.terriertrade.ui.theme.TTTheme

/** Pinned bar at the top of a chat showing the deal at a glance. Tapping it opens the deal. */
@Composable
fun DealBar(
    deal: DealBarUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme

    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = colors.surface,
        border = BorderStroke(1.5.dp, colors.onSurface),
        shadowElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(colors.primary, MaterialTheme.shapes.medium),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Outlined.SwapHoriz, contentDescription = null, tint = colors.onPrimary)
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "DEAL",
                    style = MaterialTheme.typography.labelSmall,
                    letterSpacing = 1.sp,
                    color = colors.onSurfaceVariant,
                )
                Text(
                    text = "${deal.itemCountLabel} · ${deal.totalLabel}",
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            StatusPill(deal.status)
        }
    }
}

@Composable
private fun StatusPill(status: DealStatus) {
    val (text, container, content) = when (status) {
        DealStatus.REVIEW_CHANGES -> Triple(
            "Review", MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.onPrimary,
        )
        DealStatus.WAITING -> Triple(
            "Waiting", TTTheme.colors.warningContainer, TTTheme.colors.onWarningContainer,
        )
        DealStatus.OPEN -> Triple(
            "Open", TTTheme.colors.successContainer, TTTheme.colors.onSuccessContainer,
        )
    }
    Surface(color = container, contentColor = content, shape = CircleShape) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.xs),
        )
    }
}

@PreviewLightDark
@Composable
private fun DealBarPreview() {
    TTPreviewColumn {
        DealBar(DealBarUi("1 item", "You pay \$40", DealStatus.REVIEW_CHANGES), onClick = {})
        DealBar(DealBarUi("2 items", "Sam pays you \$10", DealStatus.WAITING), onClick = {})
        DealBar(DealBarUi("1 item", "No money", DealStatus.OPEN), onClick = {})
    }
}