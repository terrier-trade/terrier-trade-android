package edu.bu.terriertrade.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.PreviewLightDark
import edu.bu.terriertrade.ui.theme.Spacing
import edu.bu.terriertrade.ui.theme.TerrierTradeTheme
import androidx.compose.foundation.shape.CircleShape

@Composable
fun TTPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    leadingIcon: ImageVector? = null,
) {
    Button(
        onClick = onClick,
        modifier = modifier.heightIn(min = Spacing.touchTarget),
        enabled = enabled && !loading,
        shape = CircleShape,
    ) {
        TTButtonContent(text = text, loading = loading, leadingIcon = leadingIcon)
    }
}

@PreviewLightDark
@Composable
private fun TTPrimaryButtonPreview() {
    TerrierTradeTheme {
        Surface {
            Column(
                modifier = Modifier.padding(Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(Spacing.md),
            ) {
                TTPrimaryButton(text = "Request item", onClick = {})
                TTPrimaryButton(text = "Log in", onClick = {}, modifier = Modifier.fillMaxWidth())
                TTPrimaryButton(text = "Posting", onClick = {}, loading = true)
                TTPrimaryButton(text = "Disabled", onClick = {}, enabled = false)
            }
        }
    }
}