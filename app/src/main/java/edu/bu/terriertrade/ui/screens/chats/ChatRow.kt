package edu.bu.terriertrade.ui.screens.chats

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import edu.bu.terriertrade.ui.components.TTAvatar
import edu.bu.terriertrade.ui.components.TTPreviewColumn
import edu.bu.terriertrade.ui.theme.Spacing
import edu.bu.terriertrade.ui.theme.TTTheme

/** One conversation in the chats list. Stateless: tapping it calls [onClick]. */
@Composable
fun ChatRow(
    row: ChatRowUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val needsReply = row.group == ChatGroup.NEEDS_REPLY
    val colors = MaterialTheme.colorScheme

    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = if (needsReply) colors.primaryContainer.copy(alpha = 0.35f) else colors.surface,
        border = BorderStroke(1.dp, if (needsReply) colors.primary else colors.outline),
    ) {
        Row(
            modifier = Modifier.padding(Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            TTAvatar(
                initials = row.partnerInitials,
                containerColor = colors.primary,
                contentColor = colors.onPrimary,
                showDot = needsReply,
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = row.partnerName,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = row.timeLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.onSurfaceVariant,
                    )
                }
                Text(
                    text = row.lastMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (row.dealTag != null) {
                    DealTagPill(tag = row.dealTag, partnerFirstName = row.partnerFirstName)
                }
            }
        }
    }
}

@Composable
private fun DealTagPill(tag: DealTag, partnerFirstName: String) {
    val (text, container, content) = when (tag) {
        DealTag.REVIEW_CHANGES -> Triple(
            "Review changes", MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.onPrimary,
        )
        DealTag.WAITING -> Triple(
            "Waiting for $partnerFirstName", TTTheme.colors.warningContainer, TTTheme.colors.onWarningContainer,
        )
        DealTag.OPEN -> Triple(
            "Deal open", TTTheme.colors.successContainer, TTTheme.colors.onSuccessContainer,
        )
    }
    Surface(color = container, contentColor = content, shape = CircleShape) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = Spacing.sm, vertical = 2.dp),
        )
    }
}

@PreviewLightDark
@Composable
private fun ChatRowPreview() {
    TTPreviewColumn {
        ChatRow(
            row = ChatRowUi("c1", "Sam K.", "Sam", "SK", "Sam suggested changes to the deal", "2m", ChatGroup.NEEDS_REPLY, DealTag.REVIEW_CHANGES),
            onClick = {},
        )
        ChatRow(
            row = ChatRowUi("c2", "Nina P.", "Nina", "NP", "Thursday works, see you then!", "1h", ChatGroup.ACTIVE, DealTag.OPEN),
            onClick = {},
        )
        ChatRow(
            row = ChatRowUi("c3", "Maya R.", "Maya", "MR", "Thanks for the quick trade!", "Oct 12", ChatGroup.EARLIER, null),
            onClick = {},
        )
    }
}