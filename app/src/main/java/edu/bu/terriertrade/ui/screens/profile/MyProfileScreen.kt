package edu.bu.terriertrade.ui.screens.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.RateReview
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.lifecycle.viewmodel.compose.viewModel
import edu.bu.terriertrade.ui.components.TTBannerTone
import edu.bu.terriertrade.ui.components.TTChipRow
import edu.bu.terriertrade.ui.components.TTEmptyState
import edu.bu.terriertrade.ui.components.TTErrorState
import edu.bu.terriertrade.ui.components.TTInfoBanner
import edu.bu.terriertrade.ui.components.TTListingRow
import edu.bu.terriertrade.ui.components.TTLoadingState
import edu.bu.terriertrade.ui.components.TTMyListingRow
import edu.bu.terriertrade.ui.components.TTProfileHeaderCard
import edu.bu.terriertrade.ui.components.TTReviewCard
import edu.bu.terriertrade.ui.components.TTSecondaryButton
import edu.bu.terriertrade.ui.model.MyListingUi
import edu.bu.terriertrade.ui.model.ProfileHeaderUi
import edu.bu.terriertrade.ui.model.ReviewUi
import edu.bu.terriertrade.ui.model.SampleData
import edu.bu.terriertrade.ui.theme.Spacing
import edu.bu.terriertrade.ui.theme.TerrierTradeTheme

/** Stateful entry point used by navigation. Owns the ViewModel and the "Remove listing?" dialog. */
@Composable
fun MyProfileRoute(
    onListingClick: (listingId: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MyProfileViewModel = viewModel(factory = MyProfileViewModel.Factory),
) {
    var confirmSignOut by rememberSaveable { mutableStateOf(false) }

    MyProfileScreen(
        uiState = viewModel.uiState,
        onTabSelect = viewModel::onTabSelect,
        onListingClick = onListingClick,
        onMarkSold = viewModel::onMarkSold,
        onRemoveClick = viewModel::onRemoveClick,
        onToggleSaved = viewModel::onToggleSaved,
        onSignOutClick = { confirmSignOut = true },
        modifier = modifier,
    )

    if (confirmSignOut) {
        AlertDialog(
            onDismissRequest = { confirmSignOut = false },
            title = { Text("Sign out?") },
            text = { Text("You'll need your BU email and password to sign back in.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmSignOut = false
                    viewModel.onSignOut()
                }) { Text("Sign out") }
            },
            dismissButton = { TextButton(onClick = { confirmSignOut = false }) { Text("Cancel") } },
        )
    }

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
    onSignOutClick: () -> Unit = {},
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
            header == null -> TTEmptyState(
                title = "Profile not found",
                message = "Try signing out and back in.",
                actionText = "Sign out",
                onAction = onSignOutClick,
            )
            else -> ProfileContent(
                header = header,
                uiState = uiState,
                onTabSelect = onTabSelect,
                onListingClick = onListingClick,
                onMarkSold = onMarkSold,
                onRemoveClick = onRemoveClick,
                onToggleSaved = onToggleSaved,
                onSignOutClick = onSignOutClick,
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
    onSignOutClick: () -> Unit,
) {
    val side = Modifier.padding(horizontal = Spacing.lg)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        item { TTProfileHeaderCard(header = header, modifier = side) }

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

        item {
            TTSecondaryButton(
                text = "Sign out",
                onClick = onSignOutClick,
                leadingIcon = Icons.AutoMirrored.Outlined.Logout,
                modifier = side.fillMaxWidth().padding(top = Spacing.lg),
            )
        }
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
