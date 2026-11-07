package edu.bu.terriertrade.ui.screens.browse

import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import edu.bu.terriertrade.data.model.Category
import edu.bu.terriertrade.ui.components.TTAppHeader
import edu.bu.terriertrade.ui.components.TTCategoryIcon
import edu.bu.terriertrade.ui.components.TTChipRow
import edu.bu.terriertrade.ui.components.TTEmptyState
import edu.bu.terriertrade.ui.components.TTErrorState
import edu.bu.terriertrade.ui.components.TTListingRow
import edu.bu.terriertrade.ui.components.TTLoadingState
import edu.bu.terriertrade.ui.components.TTNoteColor
import edu.bu.terriertrade.ui.components.TTSearchBar
import edu.bu.terriertrade.ui.components.TTSectionHeader
import edu.bu.terriertrade.ui.components.TTWantedNote
import edu.bu.terriertrade.ui.model.ListingUi
import edu.bu.terriertrade.ui.model.SampleData
import edu.bu.terriertrade.ui.model.WantedPostUi
import edu.bu.terriertrade.ui.model.icon
import edu.bu.terriertrade.ui.theme.Spacing
import edu.bu.terriertrade.ui.theme.TerrierTradeTheme
import edu.bu.terriertrade.ui.components.TTAppHeaderCornerOverlap
import edu.bu.terriertrade.ui.components.joinAppHeader

/**
 * Stateful entry point for navigation.
 * showAppHeader = false when the app frame already draws TTAppHeader above (the bottom-bar tabs do).
 */
@Composable
fun BrowseRoute(
    onListingClick: (listingId: String) -> Unit,
    onOpenChat: (chatId: String) -> Unit,
    modifier: Modifier = Modifier,
    onNotificationsClick: () -> Unit = {},
    showAppHeader: Boolean = true,
    viewModel: BrowseViewModel = viewModel(factory = BrowseViewModel.Factory),
) {
    // Back clears search and chips first, then leaves Browse.
    BackHandler(enabled = viewModel.uiState.hasActiveFilters) { viewModel.clearFilters() }

    // When a request succeeds, open the chat once and clear the signal
    val openChatId = viewModel.uiState.openChatId
    LaunchedEffect(openChatId) {
        if (openChatId != null) {
            onOpenChat(openChatId)
            viewModel.onChatOpened()
        }
    }

    // When a request fails, show the reason briefly, then clear it
    val context = LocalContext.current
    val requestError = viewModel.uiState.requestError
    LaunchedEffect(requestError) {
        if (requestError != null) {
            Toast.makeText(context, requestError, Toast.LENGTH_SHORT).show()
            viewModel.onRequestErrorShown()
        }
    }

    BrowseScreen(
        uiState = viewModel.uiState,
        onQueryChange = viewModel::onQueryChange,
        onCategorySelect = viewModel::onCategorySelect,
        onFilterSelect = viewModel::onFilterSelect,
        onToggleFavorite = viewModel::onToggleFavorite,
        onRequest = viewModel::onRequest,
        onListingClick = onListingClick,
        onHelpClick = viewModel::onRequest, // "I can help" is a request on a Looking for post
        onNotificationsClick = onNotificationsClick,
        onRetry = viewModel::load,
        onFilterClick = viewModel::onOpenFilters,
        showAppHeader = showAppHeader,
        modifier = modifier,
    )

    val state = viewModel.uiState
    if (state.showFilterSheet) {
        BrowseFilterSheet(
            filters = state.draftFilters,
            resultCount = state.draftResultCount,
            onFiltersChange = viewModel::onDraftFiltersChange,
            onReset = viewModel::onResetDraftFilters,
            onApply = viewModel::onApplyFilters,
            onDismiss = viewModel::onDismissFilters,
        )
    }
}

/** Stateless UI matching the proposal's Browse screen. */
@Composable
fun BrowseScreen(
    uiState: BrowseUiState,
    onQueryChange: (String) -> Unit,
    onCategorySelect: (Category) -> Unit,
    onFilterSelect: (BrowseFilter) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onRequest: (String) -> Unit,
    onListingClick: (String) -> Unit,
    onHelpClick: (String) -> Unit,
    onNotificationsClick: () -> Unit,
    modifier: Modifier = Modifier,
    onRetry: () -> Unit = {},
    showAppHeader: Boolean = true,
    onFilterClick: () -> Unit = {},
) {
    Surface(
        // Inside the app frame, slide up behind its header so our red search block joins it.
        modifier = (if (showAppHeader) modifier else modifier.joinAppHeader()).fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            BrowseHeader(
                query = uiState.query,
                onQueryChange = onQueryChange,
                onNotificationsClick = onNotificationsClick,
                onFilterClick = onFilterClick,
                showAppHeader = showAppHeader,
            )

            when {
                uiState.isLoading -> TTLoadingState(message = "Loading listings…")
                uiState.errorMessage != null && uiState.listings.isEmpty() && uiState.wanted.isEmpty() ->
                    TTErrorState(message = uiState.errorMessage, onRetry = onRetry)
                else -> BrowseFeed(
                    uiState = uiState,
                    onCategorySelect = onCategorySelect,
                    onFilterSelect = onFilterSelect,
                    onToggleFavorite = onToggleFavorite,
                    onRequest = onRequest,
                    onListingClick = onListingClick,
                    onHelpClick = onHelpClick,
                )
            }
        }
    }
}

/** The scrolling feed under the header: categories, wanted notes, type chips, listings. */
@Composable
private fun BrowseFeed(
    uiState: BrowseUiState,
    onCategorySelect: (Category) -> Unit,
    onFilterSelect: (BrowseFilter) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onRequest: (String) -> Unit,
    onListingClick: (String) -> Unit,
    onHelpClick: (String) -> Unit,
) {
    val sidePadding = Modifier.padding(horizontal = Spacing.lg)
    val wanted: List<WantedPostUi> = uiState.wanted
    val listings: List<ListingUi> = uiState.listings

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
                items(Category.entries.toList()) { category ->
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
                TTSectionHeader(
                    title = "Fresh listings",
                    trailingText = uiState.filters.let { f ->
                        if (f.activeCount == 0) f.sort.headerText
                        else "${f.sort.headerText} · ${f.activeCount} filter${if (f.activeCount == 1) "" else "s"}"
                    },
                    modifier = sidePadding,
                )            }
            if (listings.isEmpty()) {
                item {
                    TTEmptyState(
                        title = "Nothing matches yet",
                        message = if (uiState.filters.isDefault) {
                            "Try another word, or tap the category again to clear it."
                        } else {
                            "Try fewer filters. Tap the filter button to change them."
                        },                    )
                }
            }
            items(listings) { listing ->
                TTListingRow(
                    listing = listing,
                    onClick = { onListingClick(listing.id) },
                    modifier = sidePadding,
                    isFavorite = listing.id in uiState.favoriteIds,
                    onToggleFavorite = { onToggleFavorite(listing.id) },
                    isRequested = listing.id == uiState.requestingId,
                    onRequest = { onRequest(listing.id) },
                )
            }
        }
    }
}

/**
 * The search bar at the top of Browse.
 * With showAppHeader, it sits under TTAppHeader on one red background.
 * Without it (inside the app frame, which already draws the red header), it sits on the page
 * with a thin border, so the two rounded red shapes don't stack.
 */
@Composable
private fun BrowseHeader(
    query: String,
    onQueryChange: (String) -> Unit,
    onNotificationsClick: () -> Unit,
    onFilterClick: () -> Unit,
    showAppHeader: Boolean,
) {
    if (showAppHeader) {
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
                onFilterClick = onFilterClick,
                modifier = Modifier.padding(start = Spacing.lg, end = Spacing.lg, bottom = Spacing.lg),
            )
        }
    } else {
        // Starts behind the app frame's header (see joinAppHeader), so the two reds join into one shape.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
                )
                .padding(start = Spacing.lg, end = Spacing.lg, top = TTAppHeaderCornerOverlap, bottom = Spacing.lg),
        ) {
            TTSearchBar(query = query, onQueryChange = onQueryChange, onFilterClick = onFilterClick)
        }    }
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
private fun BrowseScreenInTabPreview() {
    TerrierTradeTheme {
        BrowseScreen(
            uiState = BrowseUiState(listings = SampleData.listings, wanted = SampleData.wanted),
            onQueryChange = {}, onCategorySelect = {}, onFilterSelect = {}, onToggleFavorite = {},
            onRequest = {}, onListingClick = {}, onHelpClick = {}, onNotificationsClick = {},
            showAppHeader = false,
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
private fun BrowseScreenLoadingPreview() {
    TerrierTradeTheme {
        BrowseScreen(
            uiState = BrowseUiState(isLoading = true),
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
