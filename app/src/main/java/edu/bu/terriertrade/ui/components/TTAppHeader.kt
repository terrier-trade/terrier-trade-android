package edu.bu.terriertrade.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.bu.terriertrade.ui.theme.Spacing
import edu.bu.terriertrade.ui.theme.TerrierTradeTheme

/**
 * Shared red header on every bottom-bar tab: logo, app name, and the notifications bell.
 * Draws behind the status bar. Nested screens (detail, chat) use their own top bar instead.
 */
@Composable
fun TTAppHeader(
    onNotificationsClick: () -> Unit,
    modifier: Modifier = Modifier,
    hasUnread: Boolean = false,
) {
    val onRed = MaterialTheme.colorScheme.onPrimary
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
            )
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(start = Spacing.lg, end = Spacing.sm, top = Spacing.sm, bottom = Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        TTCrestLogo(size = 40.dp, contentDescription = null)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Terrier Trade",
                style = MaterialTheme.typography.titleLarge,
                color = onRed,
                modifier = Modifier.semantics { heading() },
            )
            Text(
                text = "STUDENT MARKETPLACE · BU",
                style = MaterialTheme.typography.labelSmall,
                letterSpacing = 1.5.sp,
                color = onRed.copy(alpha = 0.85f),
            )
        }
        IconButton(onClick = onNotificationsClick) {
            BadgedBox(
                badge = {
                    if (hasUnread) Badge(containerColor = Color.White)
                },
            ) {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = if (hasUnread) "Notifications, new updates" else "Notifications",
                    tint = onRed,
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun TTAppHeaderPreview() {
    TerrierTradeTheme {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
            TTAppHeader(onNotificationsClick = {})
            TTAppHeader(onNotificationsClick = {}, hasUnread = true)
        }
    }
}
