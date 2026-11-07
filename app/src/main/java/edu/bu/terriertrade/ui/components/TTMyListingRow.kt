package edu.bu.terriertrade.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import edu.bu.terriertrade.data.model.Category
import edu.bu.terriertrade.data.model.ListingType
import edu.bu.terriertrade.ui.model.ListingUi
import edu.bu.terriertrade.ui.model.SellerUi
import edu.bu.terriertrade.ui.model.icon
import edu.bu.terriertrade.ui.theme.Spacing
import edu.bu.terriertrade.ui.theme.TTTheme

/**
 * One of your own listings on your profile: what it is, Live or Sold,
 * and buttons to mark it sold or remove it. Tapping the row opens it.
 */
@Composable
fun TTMyListingRow(
    listing: ListingUi,
    isSold: Boolean,
    onClick: () -> Unit,
    onMarkSold: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = colors.surface,
        border = BorderStroke(1.dp, colors.outlineVariant),
    ) {
        Row(
            modifier = Modifier.padding(start = Spacing.md, top = Spacing.sm, bottom = Spacing.sm, end = Spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(colors.primaryContainer, MaterialTheme.shapes.medium),
                contentAlignment = Alignment.Center,
            ) {
                Icon(listing.category.icon, contentDescription = null, tint = colors.primary)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = listing.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "${listing.type.label} · ${listing.priceLabel}",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                )
            }
            StatusBadge(isSold = isSold)
            if (!isSold) {
                IconButton(onClick = onMarkSold) {
                    Icon(Icons.Outlined.CheckCircle, contentDescription = "Mark ${listing.title} as sold", tint = colors.primary)
                }
            }
            IconButton(onClick = onRemove) {
                Icon(Icons.Outlined.DeleteOutline, contentDescription = "Remove ${listing.title}", tint = colors.onSurfaceVariant)
            }
        }
    }
}

/** Green "Live" or grey "Sold". */
@Composable
private fun StatusBadge(isSold: Boolean) {
    val (background, content) = if (isSold) {
        MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    } else {
        TTTheme.colors.successContainer to TTTheme.colors.onSuccessContainer
    }
    Text(
        text = if (isSold) "Sold" else "Live",
        style = MaterialTheme.typography.labelSmall,
        color = content,
        modifier = Modifier
            .background(background, MaterialTheme.shapes.small)
            .padding(horizontal = Spacing.sm, vertical = 2.dp),
    )
}

@PreviewLightDark
@Composable
private fun TTMyListingRowPreview() {
    val lamp = ListingUi(
        id = "m1", title = "Desk lamp", priceLabel = "$12", type = ListingType.ITEM, category = Category.FURNITURE,
        meetSpot = "GSU", postedAgo = "3d", condition = "Like new", description = "",
        seller = SellerUi(name = "Alex", initials = "AM", rating = "4.8", tradeCount = 12),
    )
    TTPreviewColumn {
        TTMyListingRow(listing = lamp, isSold = false, onClick = {}, onMarkSold = {}, onRemove = {})
        TTMyListingRow(listing = lamp.copy(title = "Bike lock"), isSold = true, onClick = {}, onMarkSold = {}, onRemove = {})
    }
}
