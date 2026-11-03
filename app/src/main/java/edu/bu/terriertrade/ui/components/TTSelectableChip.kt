package edu.bu.terriertrade.ui.components

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark

/** One pill chip: red when selected, outlined when not. Used by TTChoiceChips and TTChipRow. */
@Composable
fun TTSelectableChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text) },
        modifier = modifier,
        shape = CircleShape,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
        ),
    )
}

@PreviewLightDark
@Composable
private fun TTSelectableChipPreview() {
    TTPreviewColumn {
        TTSelectableChip(text = "All", selected = true, onClick = {})
        TTSelectableChip(text = "Textbooks", selected = false, onClick = {})
    }
}