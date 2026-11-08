package edu.bu.terriertrade.ui.components.listing

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import edu.bu.terriertrade.ui.components.buttons.TTSecondaryButton
import edu.bu.terriertrade.ui.preview.TTPreviewColumn
import edu.bu.terriertrade.ui.theme.Spacing
import edu.bu.terriertrade.ui.theme.TTTheme
import edu.bu.terriertrade.ui.model.WantedPostUi

/** Yellow sticky note or white index card. Alternate them in a row like the proposal. */
enum class TTNoteColor { Yellow, White }

/**
 * A "Students are looking for" post shown as a taped note, with an optional "I can help" button.
 * Default width is 170dp; pass Modifier.fillMaxWidth() to use it in a full-width list.
 */
@Composable
fun TTWantedNote(
    post: WantedPostUi,
    modifier: Modifier = Modifier,
    tilt: Float = 0f,
    color: TTNoteColor = TTNoteColor.Yellow,
    onHelp: (() -> Unit)? = null,
) {
    val isYellow = color == TTNoteColor.Yellow
    val background = if (isYellow) TTTheme.colors.stickyNote else MaterialTheme.colorScheme.surface
    val textColor = if (isYellow) TTTheme.colors.onStickyNote else MaterialTheme.colorScheme.onSurface
    val shape = MaterialTheme.shapes.extraSmall

    Box(
        modifier = modifier
            .width(170.dp)
            .graphicsLayer { rotationZ = tilt },
    ) {
        Column(
            modifier = Modifier
                .padding(top = 6.dp)
                .fillMaxWidth()
                .background(background, shape)
                .then(if (isYellow) Modifier else Modifier.border(1.dp, MaterialTheme.colorScheme.outline, shape))
                .padding(Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.xs),
        ) {
            Text(
                text = post.title,
                style = MaterialTheme.typography.titleMedium,
                color = textColor,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = post.budgetLabel,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = "${post.postedBy} · ${post.postedAgo}",
                style = MaterialTheme.typography.bodySmall,
                color = textColor.copy(alpha = 0.7f),
            )
            if (onHelp != null) {
                TTSecondaryButton(text = "I can help", onClick = onHelp)
            }
        }
        // Tape strip at the top
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .size(width = 44.dp, height = 12.dp)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
        )
    }
}

@PreviewLightDark
@Composable
private fun TTWantedNotePreview() {
    TTPreviewColumn {
        TTWantedNote(
            post = WantedPostUi(id = "w1", title = "Math tutor for MA 124", budgetLabel = "Up to $25/hr", postedBy = "Maya R.", postedAgo = "8h ago"),
            tilt = -1f,
            onHelp = {},
        )
        TTWantedNote(
            post = WantedPostUi(id = "w2", title = "CS 501 textbook", budgetLabel = "Up to $30", postedBy = "Sam K.", postedAgo = "1d ago"),
            tilt = 1f,
            color = TTNoteColor.White,
            onHelp = {},
        )
    }
}