package edu.bu.terriertrade.ui.screens.chat

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import edu.bu.terriertrade.ui.components.TTPreviewColumn
import edu.bu.terriertrade.ui.theme.Spacing

/** Draws one message in the chat based on its kind. */
@Composable
fun MessageItem(
    message: MessageUi,
    onOpenDeal: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (message.kind) {
        MessageKind.MINE -> TextBubble(message = message, isMine = true, modifier = modifier)
        MessageKind.THEIRS -> TextBubble(message = message, isMine = false, modifier = modifier)
        MessageKind.EVENT -> EventLine(text = message.text, modifier = modifier)
        MessageKind.SUGGESTION -> SuggestionCard(message = message, onOpenDeal = onOpenDeal, modifier = modifier)
    }
}

@Composable
private fun TextBubble(message: MessageUi, isMine: Boolean, modifier: Modifier) {
    val colors = MaterialTheme.colorScheme
    // The corner nearest the sender is less rounded, like a speech bubble's tail
    val shape = if (isMine) {
        RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomEnd = 4.dp, bottomStart = 18.dp)
    } else {
        RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomEnd = 18.dp, bottomStart = 4.dp)
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start,
    ) {
        Column(
            horizontalAlignment = if (isMine) Alignment.End else Alignment.Start,
            modifier = Modifier
                .widthIn(max = 280.dp)
                .semantics(mergeDescendants = true) {},
        ) {
            Surface(
                shape = shape,
                color = if (isMine) colors.primary else colors.surface,
                contentColor = if (isMine) colors.onPrimary else colors.onSurface,
                border = if (isMine) null else BorderStroke(1.dp, colors.outline),
            ) {
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                )
            }
            if (message.timeLabel.isNotEmpty()) {
                Text(
                    text = message.timeLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp, start = 4.dp, end = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun EventLine(text: String, modifier: Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.xs),
    )
}

@Composable
private fun SuggestionCard(message: MessageUi, onOpenDeal: () -> Unit, modifier: Modifier) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = colors.primaryContainer.copy(alpha = 0.35f),
        border = BorderStroke(1.dp, colors.primary),
    ) {
        Row(
            modifier = Modifier.padding(start = Spacing.md, top = Spacing.sm, bottom = Spacing.sm, end = Spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            Icon(Icons.Outlined.SwapHoriz, contentDescription = null, tint = colors.primary)
            Column(modifier = Modifier.weight(1f)) {
                Text(text = message.text, style = MaterialTheme.typography.titleSmall)
                if (message.timeLabel.isNotEmpty()) {
                    Text(
                        text = message.timeLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.onSurfaceVariant,
                    )
                }
            }
            TextButton(onClick = onOpenDeal) {
                Text("View")
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun MessageItemPreview() {
    val messages = listOf(
        MessageUi("1", "You requested TI-84 calculator", MessageKind.EVENT),
        MessageUi("2", "Hey! It is still available.", MessageKind.THEIRS, "35m"),
        MessageUi("3", "Great. Can we meet at GSU tomorrow? Would you do \$40?", MessageKind.MINE, "30m"),
        MessageUi("4", "Sam suggested changes to the deal", MessageKind.SUGGESTION, "2m"),
    )
    TTPreviewColumn {
        messages.forEach { MessageItem(message = it, onOpenDeal = {}) }
    }
}