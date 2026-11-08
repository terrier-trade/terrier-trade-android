package edu.bu.terriertrade.ui.components.listing

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import edu.bu.terriertrade.ui.preview.TTPreviewColumn
import edu.bu.terriertrade.ui.theme.Spacing

/** Small blush label in capitals, like "TEXTBOOK". */
@Composable
fun TTTag(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onPrimaryContainer,
        modifier = modifier
            .background(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.shapes.extraSmall)
            .padding(horizontal = Spacing.sm, vertical = 2.dp),
    )
}

@PreviewLightDark
@Composable
private fun TTTagPreview() {
    TTPreviewColumn {
        TTTag(text = "Textbook")
    }
}