package edu.bu.terriertrade.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import edu.bu.terriertrade.ui.model.ProfileHeaderUi
import edu.bu.terriertrade.ui.theme.Spacing
import edu.bu.terriertrade.ui.theme.TTTheme

/**
 * The top of a profile, yours or someone else's: avatar, name, BU verified,
 * rating / trades / class-of tiles, BU dates, bio and usual meetup spot.
 */
@Composable
fun TTProfileHeaderCard(
    header: ProfileHeaderUi,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = colors.surface,
        border = BorderStroke(1.dp, colors.outlineVariant),
        shadowElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.padding(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            ) {
                TTAvatar(initials = header.initials, size = 64.dp)
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(text = header.name, style = MaterialTheme.typography.titleLarge, color = colors.onSurface)
                    if (header.subtitle.isNotBlank()) {
                        Text(text = header.subtitle, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
                    }
                    if (header.isVerified) VerifiedBadge()
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                TTStatTile(value = header.ratingLabel, label = "Rating", icon = Icons.Filled.Star, modifier = Modifier.weight(1f))
                TTStatTile(value = header.tradeCount.toString(), label = "Trades", modifier = Modifier.weight(1f))
                TTStatTile(value = header.classOf ?: "–", label = "Class of", modifier = Modifier.weight(1f))
            }

            header.buDates?.let { InfoLine(icon = Icons.Outlined.CalendarMonth, text = "At BU $it") }
            if (header.bio.isNotBlank()) {
                Text(text = header.bio, style = MaterialTheme.typography.bodyMedium, color = colors.onSurface)
            }
            if (header.meetSpot.isNotBlank()) {
                InfoLine(icon = Icons.Outlined.Place, text = "Usually meets at ${header.meetSpot}")
            }
        }
    }
}

@Composable
private fun VerifiedBadge() {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        Icon(Icons.Outlined.VerifiedUser, contentDescription = null, tint = TTTheme.colors.success)
        Text(text = "BU verified", style = MaterialTheme.typography.labelMedium, color = TTTheme.colors.success)
    }
}

@Composable
private fun InfoLine(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Text(text = text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@PreviewLightDark
@Composable
private fun TTProfileHeaderCardPreview() {
    TTPreviewColumn {
        TTProfileHeaderCard(
            header = ProfileHeaderUi(
                name = "Sam K.", initials = "SK", subtitle = "Physics · Senior", isVerified = true,
                ratingLabel = "4.9", tradeCount = 14, classOf = "'26", buDates = "Fall 2022 to May 2026",
                bio = "Physics senior clearing out my desk before graduation.", meetSpot = "GSU",
            ),
        )
    }
}
