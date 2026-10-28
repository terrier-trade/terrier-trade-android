package edu.bu.terriertrade.ui.components

import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import edu.bu.terriertrade.ui.theme.Spacing
import edu.bu.terriertrade.ui.theme.TerrierTradeTheme

@Composable
fun TTTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    TextButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = Spacing.touchTarget),
        enabled = enabled,
    ) {
        TTButtonContent(text = text)
    }
}

@PreviewLightDark
@Composable
private fun TTTextButtonPreview() {
    TerrierTradeTheme {
        Surface {
            TTTextButton(
                text = "Create an account",
                onClick = {},
                modifier = Modifier.padding(Spacing.lg),
            )
        }
    }
}