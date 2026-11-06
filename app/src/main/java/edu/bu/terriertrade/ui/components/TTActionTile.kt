package edu.bu.terriertrade.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import edu.bu.terriertrade.ui.theme.Spacing
import edu.bu.terriertrade.ui.theme.TTTheme

/**
 * A small action button shaped like a tile, like "Photo added" and "Book scanned" in the proposal.
 * Before: white with a red icon. Once [done], it turns green with a tick.
 */
@Composable
fun TTActionTile(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    done: Boolean = false,
    loading: Boolean = false,
    enabled: Boolean = true,
) {
    val colors = MaterialTheme.colorScheme
    val tt = TTTheme.colors
    val content = if (done) tt.onSuccessContainer else colors.primary
    Surface(
        onClick = onClick,
        enabled = enabled && !loading,
        modifier = modifier.heightIn(min = 52.dp),
        shape = MaterialTheme.shapes.medium,
        color = if (done) tt.successContainer else colors.surface,
        border = BorderStroke(1.dp, if (done) tt.success.copy(alpha = 0.5f) else colors.outlineVariant),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (loading) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = content)
            } else {
                Icon(
                    imageVector = if (done) Icons.Outlined.CheckCircle else icon,
                    contentDescription = null,
                    tint = content,
                    modifier = Modifier.size(20.dp),
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = content,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun TTActionTilePreview() {
    TTPreviewColumn {
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            TTActionTile(Icons.Outlined.AddAPhoto, "Add photo", onClick = {}, modifier = Modifier.weight(1f))
            TTActionTile(Icons.Outlined.QrCodeScanner, "Scan book", onClick = {}, modifier = Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            TTActionTile(Icons.Outlined.AddAPhoto, "Photo added", onClick = {}, done = true, modifier = Modifier.weight(1f))
            TTActionTile(Icons.Outlined.QrCodeScanner, "Book scanned", onClick = {}, done = true, modifier = Modifier.weight(1f))
        }
    }
}
