package edu.bu.terriertrade.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import edu.bu.terriertrade.ui.theme.Spacing
import edu.bu.terriertrade.ui.theme.TerrierTradeTheme

@Composable
fun TTSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    leadingIcon: ImageVector? = null,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = Spacing.touchTarget),
        enabled = enabled && !loading,
        shape = CircleShape,
        border = BorderStroke(
            width = 1.5.dp,
            color = if (enabled && !loading) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
        ),
    ) {
        TTButtonContent(text = text, loading = loading, leadingIcon = leadingIcon)
    }
}

@PreviewLightDark
@Composable
private fun TTSecondaryButtonPreview() {
    TerrierTradeTheme {
        Surface {
            Column(
                modifier = Modifier.padding(Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(Spacing.md),
            ) {
                TTSecondaryButton(text = "Save", onClick = {})
                TTSecondaryButton(text = "Declining", onClick = {}, loading = true)
                TTSecondaryButton(text = "Disabled", onClick = {}, enabled = false)
            }
        }
    }
}