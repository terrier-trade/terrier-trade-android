package edu.bu.terriertrade.ui.screens.browse

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import edu.bu.terriertrade.ui.components.TTAppHeader
import edu.bu.terriertrade.ui.components.TTCategoryIcon
import edu.bu.terriertrade.ui.components.TTChipRow
import edu.bu.terriertrade.ui.components.TTEmptyState
import edu.bu.terriertrade.ui.components.TTListingRow
import edu.bu.terriertrade.ui.components.TTNoteColor
import edu.bu.terriertrade.ui.components.TTSearchBar
import edu.bu.terriertrade.ui.components.TTSectionHeader
import edu.bu.terriertrade.ui.components.TTWantedNote
import edu.bu.terriertrade.ui.model.ListingCategory
import edu.bu.terriertrade.ui.model.ListingUi
import edu.bu.terriertrade.ui.model.SampleData
import edu.bu.terriertrade.ui.model.WantedPostUi
import edu.bu.terriertrade.ui.model.icon
import edu.bu.terriertrade.ui.theme.Spacing
import edu.bu.terriertrade.ui.theme.TerrierTradeTheme

/** Stateful entry point for navigation. */
@Composable
fun BrowseRoute(
    onListingClick: (listingId: String) -> Unit,
    modifier: Modifier = Modifier,
    onNotificationsClick: () -> Unit = {},
    onHelpClick: (wantedId: String) -> Unit = {},
    viewModel: BrowseViewModel = viewModel(),
) {
    BrowseScreen(
        uiState = viewModel.uiState,
        onQueryChange = viewModel::onQueryChange,
        onCategorySelect = viewModel::onCategorySelect,
        onFilterSelect = viewModel::onFilterSelect,
        onToggleFavorite = viewModel::onToggleFavorite,
        onRequest = viewModel::onRequest,
        onListingClick = onListingClick,
        onHelpClick = onHelpClick,
        onNotificationsClick = onNotificationsClick,
        modifier = modifier,
    )
}

/** Stateless UI matching the proposal's Browse screen. */
@Composable
fun BrowseScreen(
    uiState: BrowseUiState,
    onQueryChange: (String) -> Unit,
    onCategorySelect: (ListingCategory) -> Unit,
    onFilterSelect: (BrowseFilter) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onRequest: (String) -> Unit,
    onListingClick: (String) -> Unit,
    onHelpClick: (String) -> Unit,
    onNotificationsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sidePadding = Modifier.padding(horizontal = Spacing.lg)
    val wanted: List<WantedPostUi> = uiState.wanted
    val listings: List<ListingUi> = uiState.listings

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            BrowseHeader(
                query = uiState.query,
                onQueryChange = onQueryChange,
                onNotificationsClick = onNotificationsClick,
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(Spacing.md),
            ) {
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = Spacing.md),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                    ) {
                        items(ListingCategory.entries.toList()) { category ->
                            TTCategoryIcon(
                                icon = category.icon,
                                label = category.label,
                                selected = uiState.category == category,
                                onClick = { onCategorySelect(category) },
                            )
                        }
                    }
                }

                if (uiState.showWantedStrip) {
                    item {
                        TTSectionHeader(
                            title = "Students are looking for",
                            trailingText = "Swipe →",
                            modifier = sidePadding,
                        )
                    }
                    item {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = Spacing.lg, vertical = Spacing.sm),
                            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                        ) {
                            itemsIndexed(wanted) { index, post ->
                                TTWantedNote(
                                    post = post,
                                    tilt = if (index % 2 == 0) -1.5f else 1.5f,
                                    color = if (index % 2 == 0) TTNoteColor.Yellow else TTNoteColor.White,
                                    onHelp = { onHelpClick(post.id) },
                                )
                            }
                        }
                    }
                }

                item {
                    TTChipRow(
                        options = BrowseFilter.entries.toList(),
                        selected = uiState.filter,
                        onSelect = onFilterSelect,
                        optionLabel = { filter: BrowseFilter -> filter.label },
                        contentPadding = PaddingValues(horizontal = Spacing.lg),
                    )
                }

                if (uiState.showWantedList) {
                    item {
                        TTSectionHeader(
                            title = "Students are looking for",
                            trailingText = "${wanted.size} posts",
                            modifier = sidePadding,
                        )
                    }
                    if (wanted.isEmpty()) {
                        item { TTEmptyState(title = "No requests match", message = "Try another word.") }
                    }
                    itemsIndexed(wanted) { index, post ->
                        TTWantedNote(
                            post = post,
                            modifier = sidePadding.fillMaxWidth(),
                            color = if (index % 2 == 0) TTNoteColor.Yellow else TTNoteColor.White,
                            onHelp = { onHelpClick(post.id) },
                        )
                    }
                } else {
                    item {
                        TTSectionHeader(title = "Fresh listings", trailingText = "Newest first", modifier = sidePadding)
                    }
                    if (listings.isEmpty()) {
                        item {
                            TTEmptyState(
                                title = "Nothing matches yet",
                                message = "Try another word, or tap the category again to clear it.",
                            )
                        }
                    }
                    items(listings) { listing ->
                        TTListingRow(
                            listing = listing,
                            onClick = { onListingClick(listing.id) },
                            modifier = sidePadding,
                            isFavorite = listing.id in uiState.favoriteIds,
                            onToggleFavorite = { onToggleFavorite(listing.id) },
                            isRequested = listing.id in uiState.requestedIds,
                            onRequest = { onRequest(listing.id) },
                        )
                    }
                }
            }
        }
    }
}

/**
 * Teammate's TTAppHeader with the search bar underneath, on one red background.
 * Same red as TTAppHeader, so the two read as one header.
 */
@Composable
private fun BrowseHeader(
    query: String,
    onQueryChange: (String) -> Unit,
    onNotificationsClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
            ),
    ) {
        TTAppHeader(onNotificationsClick = onNotificationsClick)
        TTSearchBar(
            query = query,
            onQueryChange = onQueryChange,
            modifier = Modifier.padding(start = Spacing.lg, end = Spacing.lg, bottom = Spacing.lg),
        )
    }
}

@PreviewLightDark
@Composable
private fun BrowseScreenPreview() {
    TerrierTradeTheme {
        BrowseScreen(
            uiState = BrowseUiState(
                listings = SampleData.listings,
                wanted = SampleData.wanted,
                favoriteIds = setOf("cs330-tutoring"),
            ),
            onQueryChange = {}, onCategorySelect = {}, onFilterSelect = {}, onToggleFavorite = {},
            onRequest = {}, onListingClick = {}, onHelpClick = {}, onNotificationsClick = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun BrowseScreenLookingForPreview() {
    TerrierTradeTheme {
        BrowseScreen(
            uiState = BrowseUiState(filter = BrowseFilter.LOOKING_FOR, wanted = SampleData.wanted),
            onQueryChange = {}, onCategorySelect = {}, onFilterSelect = {}, onToggleFavorite = {},
            onRequest = {}, onListingClick = {}, onHelpClick = {}, onNotificationsClick = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun BrowseScreenEmptyPreview() {
    TerrierTradeTheme {
        BrowseScreen(
            uiState = BrowseUiState(query = "kayak"),
            onQueryChange = {}, onCategorySelect = {}, onFilterSelect = {}, onToggleFavorite = {},
            onRequest = {}, onListingClick = {}, onHelpClick = {}, onNotificationsClick = {},
        )
    }
}
@androidx.compose.ui.tooling.preview.Preview(showSystemUi = true)
@Composable
private fun BrowseLiveTryOut() {
    TerrierTradeTheme { BrowseRoute(onListingClick = {}) }
}
