package edu.bu.terriertrade.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import edu.bu.terriertrade.ui.model.ListingCategory
import edu.bu.terriertrade.ui.model.ListingType
import edu.bu.terriertrade.ui.model.ListingUi
import edu.bu.terriertrade.ui.model.SellerUi
import edu.bu.terriertrade.ui.model.icon
import edu.bu.terriertrade.ui.theme.Spacing
import edu.bu.terriertrade.ui.theme.TTTheme

/**
 * Horizontal listing card from the proposal: picture with type tag on the left;
 * title, heart, price, details, seller, and Request on the right. The whole card opens the listing.
 */
@Composable
fun TTListingRow(
    listing: ListingUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isFavorite: Boolean = false,
    onToggleFavorite: (() -> Unit)? = null,
    isRequested: Boolean = false,
    onRequest: (() -> Unit)? = null,
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Row(
            modifier = Modifier.padding(Spacing.md),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.medium),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = listing.category.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(32.dp),
                )
                TTTag(
                    text = listing.type.label,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(Spacing.xs),
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Text(
                        text = listing.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                            .padding(top = Spacing.xs),
                    )
                    if (onToggleFavorite != null) {
                        TTFavoriteButton(isFavorite = isFavorite, onToggle = onToggleFavorite)
                    }
                }
                TTPriceText(text = listing.priceLabel)
                Text(
                    text = "${listing.condition} · ${listing.meetSpot} · ${listing.postedAgo}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(
                    modifier = Modifier.padding(top = Spacing.xs),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                ) {
                    TTAvatar(initials = listing.seller.initials, size = 24.dp)
                    Text(
                        text = listing.seller.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "★ ${listing.seller.rating}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    if (isRequested) {
                        Text(
                            text = "Requested",
                            style = MaterialTheme.typography.labelLarge,
                            color = TTTheme.colors.success,
                        )
                    } else if (onRequest != null) {
                        TTPrimaryButton(text = "Request", onClick = onRequest)
                    }
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun TTListingRowPreview() {
    val listing = ListingUi(
        id = "1",
        title = "Calculus: Early Transcendentals",
        priceLabel = "$35",
        type = ListingType.ITEM,
        category = ListingCategory.BOOKS,
        meetSpot = "Mugar Library",
        postedAgo = "2h",
        condition = "Like new",
        description = "",
        seller = SellerUi(name = "Maya", initials = "MR", rating = "4.7", tradeCount = 6),
    )
    TTPreviewColumn {
        TTListingRow(listing = listing, onClick = {}, onToggleFavorite = {}, onRequest = {})
        TTListingRow(listing = listing, onClick = {}, isFavorite = true, onToggleFavorite = {}, isRequested = true)
    }
}