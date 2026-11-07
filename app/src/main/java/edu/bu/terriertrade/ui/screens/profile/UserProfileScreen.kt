package edu.bu.terriertrade.ui.screens.profile

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
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.lifecycle.viewmodel.compose.viewModel
import edu.bu.terriertrade.ui.components.TTBackButton
import edu.bu.terriertrade.ui.components.TTBottomActions
import edu.bu.terriertrade.ui.components.TTEmptyState
import edu.bu.terriertrade.ui.components.TTErrorText
import edu.bu.terriertrade.ui.components.TTListingRow
import edu.bu.terriertrade.ui.components.TTLoadingState
import edu.bu.terriertrade.ui.components.TTPrimaryButton
import edu.bu.terriertrade.ui.components.TTProfileHeaderCard
import edu.bu.terriertrade.ui.components.TTReviewCard
import edu.bu.terriertrade.ui.components.TTSectionHeader
import edu.bu.terriertrade.ui.model.ProfileHeaderUi
import edu.bu.terriertrade.ui.model.ReviewUi
import edu.bu.terriertrade.ui.preview.PreviewData
import edu.bu.terriertrade.ui.theme.Spacing
import edu.bu.terriertrade.ui.theme.TerrierTradeTheme

/** Stateful entry point used by navigation. Loads the profile for [userId]. */
@Composable
fun UserProfileRoute(
    userId: String,
    onBack: () -> Unit,
    onListingClick: (listingId: String) -> Unit,
    onOpenChat: (chatId: String) -> Unit,
    viewModel: UserProfileViewModel = viewModel(factory = UserProfileViewModel.Factory),
) {
    LaunchedEffect(userId) { viewModel.load(userId) }

    // When the chat is ready, open it once and clear the signal
    val openChatId = viewModel.uiState.openChatId
    LaunchedEffect(openChatId) {
        if (openChatId != null) {
            onOpenChat(openChatId)
            viewModel.onChatOpened()
        }
    }

    UserProfileScreen(
        uiState = viewModel.uiState,
        onBack = onBack,
        onListingClick = onListingClick,
        onToggleFavorite = viewModel::onToggleFavorite,
        onOpenChat = viewModel::onOpenChat,
    )
}

/** Stateless UI matching "Other profile" in the proposal. */
@Composable
fun UserProfileScreen(
    uiState: UserProfileUiState,
    onBack: () -> Unit,
    onListingClick: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onOpenChat: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.lg, vertical = Spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            ) {
                TTBackButton(onClick = onBack)
                Text(
                    text = if (uiState.firstName.isBlank()) "Profile" else "${uiState.firstName}'s profile",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }

            val header = uiState.header
            when {
                uiState.isLoading -> TTLoadingState()
                uiState.notFound || header == null -> TTEmptyState(
                    title = "Profile not found",
                    message = "This student may have left Terrier Trade.",
                )
                else -> {
                    UserProfileContent(
                        header = header,
                        uiState = uiState,
                        onListingClick = onListingClick,
                        onToggleFavorite = onToggleFavorite,
                        modifier = Modifier.weight(1f),
                    )
                    if (!uiState.isMe) {
                        TTBottomActions {
                            TTErrorText(message = uiState.chatError)
                            TTPrimaryButton(
                                text = "Open chat with ${uiState.firstName}",
                                onClick = onOpenChat,
                                loading = uiState.isOpeningChat,
                                leadingIcon = Icons.AutoMirrored.Outlined.Chat,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UserProfileContent(
    header: ProfileHeaderUi,
    uiState: UserProfileUiState,
    onListingClick: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val side = Modifier.padding(horizontal = Spacing.lg)
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(vertical = Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        item { TTProfileHeaderCard(header = header, modifier = side) }

        item {
            TTSectionHeader(
                title = "${uiState.firstName}'s listings",
                trailingText = "${uiState.listings.size} live",
                modifier = side,
            )
        }
        if (uiState.listings.isEmpty()) {
            item { TTEmptyState(title = "Nothing listed right now") }
        }
        items(uiState.listings, key = { it.id }) { listing ->
            TTListingRow(
                listing = listing,
                onClick = { onListingClick(listing.id) },
                isFavorite = listing.id in uiState.favoriteIds,
                onToggleFavorite = { onToggleFavorite(listing.id) },
                modifier = side,
            )
        }

        item {
            TTSectionHeader(
                title = "Reviews",
                trailingText = when (uiState.reviews.size) {
                    0 -> "None yet"
                    1 -> "${header.ratingLabel} · 1 review"
                    else -> "${header.ratingLabel} · ${uiState.reviews.size} reviews"
                },
                modifier = side,
            )
        }
        if (uiState.reviews.isEmpty()) {
            item { TTEmptyState(title = "No reviews yet", message = "Reviews show up after a finished trade.") }
        }
        items(uiState.reviews, key = { it.id }) { review ->
            TTReviewCard(review = review, modifier = side)
        }
    }
}

@PreviewLightDark
@Composable
private fun UserProfileScreenPreview() {
    TerrierTradeTheme {
        UserProfileScreen(
            uiState = UserProfileUiState(
                isLoading = false,
                header = ProfileHeaderUi(
                    name = "Sam K.", initials = "SK", subtitle = "Physics · Senior", isVerified = true,
                    ratingLabel = "4.9", tradeCount = 14, classOf = "'26", buDates = "Fall 2022 to May 2026",
                    bio = "Physics senior clearing out my desk before graduation. Usually free after 3pm.", meetSpot = "GSU",
                ),
                listings = PreviewData.listings.take(2),
                reviews = listOf(
                    ReviewUi("r3", "Priya B.", "PB", 5, listOf("Friendly"), "Super quick and friendly.", "Oct 21"),
                ),
            ),
            onBack = {}, onListingClick = {}, onToggleFavorite = {}, onOpenChat = {},
        )
    }
}
