package edu.bu.terriertrade.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import edu.bu.terriertrade.ui.theme.Spacing

/**
 * A tappable tile with a title and a short hint, and an optional icon on top.
 * Selected tiles get a red border, a light pink fill and red text, like the proposal.
 * Used for "What are you posting?" (Item, Service, Ticket, Looking for).
 */
@Composable
fun TTOptionTile(
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = modifier
            .heightIn(min = if (icon != null) 112.dp else 64.dp)
            .selectable(selected = selected, onClick = onClick, role = Role.RadioButton),
        shape = MaterialTheme.shapes.medium,
        color = if (selected) colors.primaryContainer.copy(alpha = 0.45f) else colors.surface,
        border = BorderStroke(
            width = if (selected) 1.5.dp else 1.dp,
            color = if (selected) colors.primary else colors.outlineVariant,
        ),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            if (icon != null) {
                Box(
                    modifier = Modifier
                        .padding(bottom = Spacing.xs)
                        .size(40.dp)
                        .background(
                            color = if (selected) colors.primary else colors.primaryContainer,
                            shape = MaterialTheme.shapes.medium,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (selected) colors.onPrimary else colors.primary,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = if (selected) colors.primary else colors.onSurface,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = if (selected) colors.primary.copy(alpha = 0.8f) else colors.onSurfaceVariant,
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun TTOptionTilePreview() {
    TTPreviewColumn {
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            TTOptionTile(
                title = "Item", subtitle = "Books, furniture, notes",
                selected = true, onClick = {}, modifier = Modifier.width(150.dp),
            )
            TTOptionTile(
                title = "Service", subtitle = "Tutoring help",
                selected = false, onClick = {}, modifier = Modifier.width(150.dp),
            )
        }
        TTOptionTile(
            icon = Icons.Outlined.Inventory2, title = "With an icon", subtitle = "Optional",
            selected = false, onClick = {}, modifier = Modifier.width(150.dp),
        )
    }
}
