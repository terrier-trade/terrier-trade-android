package edu.bu.terriertrade.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark

/** Price in red. Large uses the condensed title font (listing detail, offers). */
@Composable
fun TTPriceText(
    text: String,
    modifier: Modifier = Modifier,
    large: Boolean = false,
) {
    Text(
        text = text,
        modifier = modifier,
        style = if (large) MaterialTheme.typography.headlineLarge else MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
    )
}

@PreviewLightDark
@Composable
private fun TTPriceTextPreview() {
    TTPreviewColumn {
        TTPriceText(text = "$40")
        TTPriceText(text = "$40", large = true)
    }
}