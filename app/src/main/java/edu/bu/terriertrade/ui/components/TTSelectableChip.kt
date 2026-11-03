package edu.bu.terriertrade.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Chair
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp

/** One pill chip: red when selected, outlined when not. Used by TTChoiceChips and TTChipRow. */
@Composable
fun TTSelectableChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text) },
        modifier = modifier,
        leadingIcon = leadingIcon?.let { icon ->
            { Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp)) }
        },
        shape = CircleShape,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
            selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary,
            iconColor = MaterialTheme.colorScheme.primary,
        ),
    )
}

@PreviewLightDark
@Composable
private fun TTSelectableChipPreview() {
    TTPreviewColumn {
        TTSelectableChip(text = "All", selected = true, onClick = {})
        TTSelectableChip(text = "Textbooks", selected = false, onClick = {})
        TTSelectableChip(text = "Furniture", selected = true, onClick = {}, leadingIcon = Icons.Outlined.Chair)
        TTSelectableChip(text = "Furniture", selected = false, onClick = {}, leadingIcon = Icons.Outlined.Chair)
    }
}
