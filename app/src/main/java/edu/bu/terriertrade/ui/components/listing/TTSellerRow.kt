package edu.bu.terriertrade.ui.components.listing

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import edu.bu.terriertrade.ui.components.profile.TTAvatar
import edu.bu.terriertrade.ui.model.SellerUi
import edu.bu.terriertrade.ui.preview.TTPreviewColumn
import edu.bu.terriertrade.ui.theme.Spacing
import edu.bu.terriertrade.ui.theme.TTTheme

/** Avatar, name, and "BU verified · 4.8 stars · 6 trades", with a green shield if verified. Tappable when [onClick] is set. */
@Composable
fun TTSellerRow(
    seller: SellerUi,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val subtitle = buildList {
        if (seller.isVerified) add("BU verified")
        add("${seller.rating} stars")
        add("${seller.tradeCount} trades")
    }.joinToString(" · ")

    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClickLabel = "Open ${seller.name}'s profile", onClick = onClick) else Modifier),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        TTAvatar(initials = seller.initials)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = seller.name,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (seller.isVerified) {
            Icon(
                imageVector = Icons.Outlined.VerifiedUser,
                contentDescription = "BU verified",
                tint = TTTheme.colors.success,
            )
        }
        if (onClick != null) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun TTSellerRowPreview() {
    TTPreviewColumn {
        TTSellerRow(seller = SellerUi(name = "Maya K.", initials = "MK", rating = "4.8", tradeCount = 6))
        TTSellerRow(seller = SellerUi(name = "Sam K.", initials = "SK", rating = "4.9", tradeCount = 14), onClick = {})
    }
}
