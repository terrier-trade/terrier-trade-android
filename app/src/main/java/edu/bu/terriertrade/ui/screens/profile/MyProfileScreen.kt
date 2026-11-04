package edu.bu.terriertrade.ui.screens.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.RateReview
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import edu.bu.terriertrade.ui.components.TTAvatar
import edu.bu.terriertrade.ui.components.TTBannerTone
import edu.bu.terriertrade.ui.components.TTChipRow
import edu.bu.terriertrade.ui.components.TTEmptyState
import edu.bu.terriertrade.ui.components.TTErrorState
import edu.bu.terriertrade.ui.components.TTInfoBanner
import edu.bu.terriertrade.ui.components.TTListingRow
import edu.bu.terriertrade.ui.components.TTLoadingState
import edu.bu.terriertrade.ui.components.TTMyListingRow
import edu.bu.terriertrade.ui.components.TTReviewCard
import edu.bu.terriertrade.ui.components.TTStatTile
import edu.bu.terriertrade.ui.model.MyListingUi
import edu.bu.terriertrade.ui.model.ProfileHeaderUi
import edu.bu.terriertrade.ui.model.ReviewUi
import edu.bu.terriertrade.ui.model.SampleData
import edu.bu.terriertrade.ui.theme.Spacing
import edu.bu.terriertrade.ui.theme.TTTheme
import edu.bu.terriertrade.ui.theme.TerrierTradeTheme

/** Stateful entry point used by navigation. Owns the ViewModel and the "Remove listing?" dialog. */
@Composable
fun MyProfileRoute(
    onListingClick: (listingId: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MyProfileViewModel = viewModel(factory = MyProfileViewModel.Factory),
) {
    MyProfileScreen(
        uiState = viewModel.uiState,
        onTabSelect = viewModel::onTabSelect,
        onListingClick = onListingClick,
        onMarkSold = viewModel::onMarkSold,
        onRemoveClick = viewModel::onRemoveClick,
        onToggleSaved = viewModel::onToggleSaved,
        modifier = modifier,
    )

    viewModel.uiState.confirmRemove?.let { item ->
        AlertDialog(
            onDismissRequest = viewModel::onDismissRemove,
            title = { Text("Remove this listing?") },
            text = { Text("\"${item.listing.title}\" will disappear from Browse. You can't undo this.") },
            confirmButton = { TextButton(onClick = viewModel::onConfirmRemove) { Text("Remove") } },
            dismissButton = { TextButton(onClick = viewModel::onDismissRemove) { Text("Keep it") } },
        )
    }
}

/** Stateless UI matching "Your profile" in the proposal. The app header and bottom bar come from the app frame. */
@Composable
fun MyProfileScreen(
    uiState: MyProfileUiState,
    onTabSelect: (ProfileTab) -> Unit,
    onListingClick: (String) -> Unit,
    onMarkSold: (String) -> Unit,
    onRemoveClick: (MyListingUi) -> Unit,
    onToggleSaved: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        val header = uiState.header
        when {
            uiState.isLoading -> TTLoadingState(message = "Loading your profile…")
            uiState.isSignedOut -> TTEmptyState(
                title = "You're signed out",
                message = "Sign in with your BU email to see your profile.",
            )
            uiState.errorMessage != null && header == null -> TTErrorState(message = uiState.errorMessage)
            header == null -> TTEmptyState(title = "Profile not found", message = "Try signing out and back in.")
            else -> ProfileContent(
                header = header,
                uiState = uiState,
                onTabSelect = onTabSelect,
                onListingClick = onListingClick,
                onMarkSold = onMarkSold,
                onRemoveClick = onRemoveClick,
                onToggleSaved = onToggleSaved,
            )
        }
    }
}

@Composable
private fun ProfileContent(
    header: ProfileHeaderUi,
    uiState: MyProfileUiState,
    onTabSelect: (ProfileTab) -> Unit,
    onListingClick: (String) -> Unit,
    onMarkSold: (String) -> Unit,
    onRemoveClick: (MyListingUi) -> Unit,
    onToggleSaved: (String) -> Unit,
) {
    val side = Modifier.padding(horizontal = Spacing.lg)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        item { ProfileHeaderCard(header = header, modifier = side) }

        item {
            TTChipRow(
                options = ProfileTab.entries.toList(),
                selected = uiState.tab,
                onSelect = onTabSelect,
                optionLabel = { tab: ProfileTab -> "${tab.label} (${uiState.countFor(tab)})" },
                contentPadding = PaddingValues(horizontal = Spacing.lg),
            )
        }

        uiState.message?.let { note ->
            item {
                TTInfoBanner(text = note, tone = TTBannerTone.Success, icon = Icons.Outlined.CheckCircle, modifier = side)
            }
        }

        when (uiState.tab) {
            ProfileTab.LISTINGS -> {
                if (uiState.myListings.isEmpty()) {
                    item {
                        TTEmptyState(
                            title = "No listings yet",
                            message = "Tap Sell to post your first one.",
                            icon = Icons.Outlined.Inventory2,
                        )
                    }
                } else {
                    item {
                        Text(
                            text = "${uiState.liveCount} live · ${uiState.myListings.size - uiState.liveCount} sold",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = side,
                        )
                    }
                }
                items(uiState.myListings, key = { it.listing.id }) { item ->
                    TTMyListingRow(
                        listing = item.listing,
                        isSold = item.isSold,
                        onClick = { onListingClick(item.listing.id) },
                        onMarkSold = { onMarkSold(item.listing.id) },
                        onRemove = { onRemoveClick(item) },
                        modifier = side,
                    )
                }
            }

            ProfileTab.SAVED -> {
                if (uiState.saved.isEmpty()) {
                    item {
                        TTEmptyState(
                            title = "Nothing saved yet",
                            message = "Tap the heart on a listing to keep it here.",
                            icon = Icons.Outlined.FavoriteBorder,
                        )
                    }
                }
                items(uiState.saved, key = { it.id }) { listing ->
                    TTListingRow(
                        listing = listing,
                        onClick = { onListingClick(listing.id) },
                        isFavorite = true,
                        onToggleFavorite = { onToggleSaved(listing.id) },
                        modifier = side,
                    )
                }
            }

            ProfileTab.REVIEWS -> {
                if (uiState.reviews.isEmpty()) {
                    item {
                        TTEmptyState(
                            title = "No reviews yet",
                            message = "Reviews show up here after your first trade.",
                            icon = Icons.Outlined.RateReview,
                        )
                    }
                }
                items(uiState.reviews, key = { it.id }) { review ->
                    TTReviewCard(review = review, modifier = side)
                }
            }
        }
    }
}

/** Avatar, name, verified badge, stats, BU dates, bio and meetup spot. */
@Composable
private fun ProfileHeaderCard(header: ProfileHeaderUi, modifier: Modifier = Modifier) {
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
        Icon(
            Icons.Outlined.VerifiedUser,
            contentDescription = null,
            tint = TTTheme.colors.success,
            modifier = Modifier.padding(top = 1.dp),
        )
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

private val previewHeader = ProfileHeaderUi(
    name = "Alex Morgan", initials = "AM", subtitle = "Computer Science · Junior", isVerified = true,
    ratingLabel = "4.8", tradeCount = 12, classOf = "'27", buDates = "Fall 2024 to May 2027",
    bio = "CS junior selling stuff from my dorm. Happy to help with Python too.", meetSpot = "GSU",
)

@PreviewLightDark
@Composable
private fun MyProfileListingsPreview() {
    TerrierTradeTheme {
        MyProfileScreen(
            uiState = MyProfileUiState(
                isLoading = false,
                header = previewHeader,
                myListings = SampleData.listings.take(3).mapIndexed { i, l -> MyListingUi(l, isSold = i == 2) },
            ),
            onTabSelect = {}, onListingClick = {}, onMarkSold = {}, onRemoveClick = {}, onToggleSaved = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun MyProfileReviewsPreview() {
    TerrierTradeTheme {
        MyProfileScreen(
            uiState = MyProfileUiState(
                isLoading = false,
                header = previewHeader,
                tab = ProfileTab.REVIEWS,
                reviews = listOf(
                    ReviewUi("r1", "Maya R.", "MR", 5, listOf("On time", "Friendly"), "Easy to coordinate and super friendly.", "Oct 19"),
                ),
            ),
            onTabSelect = {}, onListingClick = {}, onMarkSold = {}, onRemoveClick = {}, onToggleSaved = {},
        )
    }
}
