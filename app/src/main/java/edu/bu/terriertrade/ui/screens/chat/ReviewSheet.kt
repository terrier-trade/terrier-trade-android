package edu.bu.terriertrade.ui.screens.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import edu.bu.terriertrade.data.model.ReviewTag
import edu.bu.terriertrade.data.review.ReviewRules
import edu.bu.terriertrade.ui.components.buttons.TTPrimaryButton
import edu.bu.terriertrade.ui.components.feedback.TTErrorText
import edu.bu.terriertrade.ui.components.inputs.TTTextField
import edu.bu.terriertrade.ui.components.selection.TTSelectableChip
import edu.bu.terriertrade.ui.theme.Spacing
import edu.bu.terriertrade.ui.theme.TerrierTradeTheme

/** Everything the review sheet can do, so the sheet takes one parameter for it. */
data class ReviewSheetActions(
    val onClose: () -> Unit = {},
    val onStarsChange: (Int) -> Unit = {},
    val onTagToggle: (ReviewTag) -> Unit = {},
    val onTextChange: (String) -> Unit = {},
    val onSend: () -> Unit = {},
)

/**
 * Rating the other person after a finished deal, as a bottom sheet styled like the deal sheet:
 * stars first, then optional tags and a short note. Send turns on once a star is picked.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewSheet(state: ReviewSheetState, actions: ReviewSheetActions) {
    if (!state.isOpen) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = actions.onClose,
        sheetState = sheetState,
        shape = SheetShape,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { DragHandle() },
    ) {
        ReviewSheetContent(state = state, actions = actions)
    }
}

/** The sheet's insides, without the sheet, so it can be previewed. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ReviewSheetContent(
    state: ReviewSheetState,
    actions: ReviewSheetActions,
    modifier: Modifier = Modifier,
) {
    val name = state.partnerFirstName

    Column(modifier.fillMaxWidth()) {
        SheetHeader(
            title = "Rate $name",
            rule = "Your review shows on $name's profile and can't be changed later.",
            onClose = actions.onClose,
        )

        Column(
            modifier = Modifier
                .weight(1f, fill = false)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = Spacing.md),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            // Stars come first: they're the only thing you have to pick
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Spacing.xs),
            ) {
                Text("How was trading with $name?", style = MaterialTheme.typography.titleMedium)
                StarPicker(stars = state.stars, onStarsChange = actions.onStarsChange)
                Text(
                    text = starWord(state.stars),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                SectionLabel("WHAT WENT WELL", MaterialTheme.colorScheme.primary)
                // Chips wrap onto a second line on narrow phones
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                    verticalArrangement = Arrangement.spacedBy(Spacing.xs),
                ) {
                    ReviewTag.entries.forEach { tag ->
                        TTSelectableChip(
                            text = tag.label,
                            selected = tag in state.tags,
                            onClick = { actions.onTagToggle(tag) },
                        )
                    }
                }
            }

            TTTextField(
                value = state.text,
                onValueChange = actions.onTextChange,
                label = "Anything else? (optional)",
                placeholder = "Easy to meet, item as described…",
                singleLine = false,
                minLines = 3,
                helperText = "${state.text.length} / ${ReviewRules.MAX_TEXT_LENGTH}",
                modifier = Modifier.fillMaxWidth(),
            )

            TTErrorText(state.error)
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
        TTPrimaryButton(
            text = "Send review",
            onClick = actions.onSend,
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = 20.dp, end = 20.dp, top = Spacing.md, bottom = 20.dp),
            enabled = state.canSend,
            loading = state.isSaving,
        )
    }
}

/** Five big stars. Tapping the third gives 3. Each star is its own button, so TalkBack reads "3 stars". */
@Composable
private fun StarPicker(stars: Int, onStarsChange: (Int) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        for (value in 1..5) {
            IconButton(onClick = { onStarsChange(value) }, modifier = Modifier.size(52.dp)) {
                Icon(
                    imageVector = if (value <= stars) Icons.Filled.Star else Icons.Outlined.StarOutline,
                    contentDescription = if (value == 1) "1 star" else "$value stars",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp),
                )
            }
        }
    }
}

/** A word under the stars, so the number means the same thing to everyone. */
private fun starWord(stars: Int): String = when (stars) {
    1 -> "Not good"
    2 -> "Could be better"
    3 -> "Okay"
    4 -> "Good"
    5 -> "Great"
    else -> "Tap a star"
}

// Previews ----------------------------------------------------------------------------------------

@PreviewLightDark
@Composable
private fun ReviewSheetEmptyPreview() {
    TerrierTradeTheme {
        Surface { ReviewSheetContent(ReviewSheetState(isOpen = true, partnerFirstName = "Maya"), ReviewSheetActions()) }
    }
}

@PreviewLightDark
@Composable
private fun ReviewSheetFilledPreview() {
    TerrierTradeTheme {
        Surface {
            ReviewSheetContent(
                ReviewSheetState(
                    isOpen = true,
                    partnerFirstName = "Maya",
                    stars = 4,
                    tags = setOf(ReviewTag.ON_TIME, ReviewTag.FRIENDLY),
                    text = "Met at Mugar, book was exactly as described.",
                    canSend = true,
                ),
                ReviewSheetActions(),
            )
        }
    }
}