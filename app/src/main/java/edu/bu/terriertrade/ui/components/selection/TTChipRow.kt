package edu.bu.terriertrade.ui.components.selection

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import edu.bu.terriertrade.ui.preview.TTPreviewColumn
import edu.bu.terriertrade.ui.theme.Spacing

/** One row of pill chips that scrolls sideways. Used for category filters. */
@Composable
fun <T> TTChipRow(
    options: List<T>,
    selected: T,
    onSelect: (T) -> Unit,
    optionLabel: (T) -> String,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    LazyRow(
        modifier = modifier,
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        items(options) { option ->
            TTSelectableChip(
                text = optionLabel(option),
                selected = option == selected,
                onClick = { onSelect(option) },
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun TTChipRowPreview() {
    TTPreviewColumn {
        TTChipRow(
            options = listOf("All", "Textbooks", "Dorm", "Tickets", "Tech", "Clothes"),
            selected = "All",
            onSelect = {},
            optionLabel = { it },
        )
    }
}