package edu.bu.terriertrade.ui.screens.listing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MarkChatUnread
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import edu.bu.terriertrade.ui.components.TTBackButton
import edu.bu.terriertrade.ui.components.TTBannerTone
import edu.bu.terriertrade.ui.components.TTBottomActions
import edu.bu.terriertrade.ui.components.TTEmptyState
import edu.bu.terriertrade.ui.components.TTFactGrid
import edu.bu.terriertrade.ui.components.TTFavoriteButton
import edu.bu.terriertrade.ui.components.TTInfoBanner
import edu.bu.terriertrade.ui.components.TTLoadingState
import edu.bu.terriertrade.ui.components.TTPhotoPlaceholder
import edu.bu.terriertrade.ui.components.TTPriceText
import edu.bu.terriertrade.ui.components.TTPrimaryButton
import edu.bu.terriertrade.ui.components.TTSellerRow
import edu.bu.terriertrade.ui.components.TTTag
import edu.bu.terriertrade.ui.model.ListingUi
import edu.bu.terriertrade.ui.theme.Spacing
import edu.bu.terriertrade.ui.theme.TerrierTradeTheme
import edu.bu.terriertrade.ui.model.SampleData

/** Stateful entry point for navigation. Loads the listing for [listingId]. */
@Composable
fun ListingDetailRoute(
    listingId: String,
    onBack: () -> Unit,
    viewModel: ListingDetailViewModel = viewModel(),
) {
    LaunchedEffect(listingId) { viewModel.load(listingId) }
    ListingDetailScreen(
        uiState = viewModel.uiState,
        onBack = onBack,
        onToggleFavorite = viewModel::onToggleFavorite,
        onRequest = viewModel::onRequest,
    )
}

/** Stateless UI: loading, not found, or the listing. */
@Composable
fun ListingDetailScreen(
    uiState: ListingDetailUiState,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
    onRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        val listing = uiState.listing
        when {
            uiState.notFound -> Column(modifier = Modifier.fillMaxSize()) {
                TTBackButton(
                    onClick = onBack,
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.statusBars)
                        .padding(Spacing.lg),
                )
                TTEmptyState(
                    title = "This listing is gone",
                    message = "It may have sold or been removed by the seller.",
                )
            }
            listing == null -> TTLoadingState()
            else -> ListingDetailContent(
                listing = listing,
                isFavorite = uiState.isFavorite,
                requestSent = uiState.requestSent,
                onBack = onBack,
                onToggleFavorite = onToggleFavorite,
                onRequest = onRequest,
            )
        }
    }
}

@Composable
private fun ListingDetailContent(
    listing: ListingUi,
    isFavorite: Boolean,
    requestSent: Boolean,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
    onRequest: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        ) {
            Box {
                TTPhotoPlaceholder(label = "Seller's photo", height = 280.dp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.statusBars)
                        .padding(Spacing.lg),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    TTBackButton(onClick = onBack)
                    TTFavoriteButton(isFavorite = isFavorite, onToggle = onToggleFavorite, onPhoto = true)
                }
            }
            Column(
                modifier = Modifier.padding(horizontal = Spacing.xl, vertical = Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(Spacing.md),
            ) {
                TTTag(text = listing.type.label)
                Text(
                    text = listing.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                TTPriceText(text = listing.priceLabel, large = true)
                TTFactGrid(
                    facts = listOf("Condition" to listing.condition, "Meet near" to listing.meetSpot) + listing.details,
                )
                TTSellerRow(seller = listing.seller)
                Text(
                    text = listing.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        TTBottomActions {
            TTInfoBanner(text = "Pay in person when you meet. Terrier Trade never handles money.")
            if (requestSent) {
                TTInfoBanner(
                    text = "Request sent. A chat opens once ${listing.seller.name} accepts.",
                    tone = TTBannerTone.Success,
                    icon = Icons.Outlined.MarkChatUnread,
                )
            } else {
                TTPrimaryButton(
                    text = "Request item",
                    onClick = onRequest,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun ListingDetailScreenPreview() {
    TerrierTradeTheme {
        ListingDetailScreen(
            uiState = ListingDetailUiState(listing = SampleData.listings.first()),
            onBack = {}, onToggleFavorite = {}, onRequest = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun ListingDetailRequestedPreview() {
    TerrierTradeTheme {
        ListingDetailScreen(
            uiState = ListingDetailUiState(listing = SampleData.listings.first(), isFavorite = true, requestSent = true),
            onBack = {}, onToggleFavorite = {}, onRequest = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun ListingDetailNotFoundPreview() {
    TerrierTradeTheme {
        ListingDetailScreen(
            uiState = ListingDetailUiState(notFound = true),
            onBack = {}, onToggleFavorite = {}, onRequest = {},
        )
    }
}