package edu.bu.terriertrade.ui.screens.browse

import edu.bu.terriertrade.data.model.Category
import edu.bu.terriertrade.ui.model.ListingUi
import edu.bu.terriertrade.ui.model.WantedPostUi

/** The type chips under the wanted notes. */
enum class BrowseFilter(val label: String) {
    ALL("All"),
    ITEMS("Items"),
    SERVICES("Services"),
    TICKETS("Tickets"),
    LOOKING_FOR("Looking for"),
}

/** Everything the Browse screen draws. listings and wanted are already filtered. */
data class BrowseUiState(
    val query: String = "",
    val category: Category? = null,
    val filter: BrowseFilter = BrowseFilter.ALL,
    val listings: List<ListingUi> = emptyList(),
    val wanted: List<WantedPostUi> = emptyList(),
    val favoriteIds: Set<String> = emptySet(),
    /** The listing or Looking for post whose request is being saved. Only one at a time. */
    val requestingId: String? = null,
    /** One-time message after a failed request. The screen shows it, then clears it. */
    val requestError: String? = null,
    /** One-time signal to open this chat. The screen opens it, then calls onChatOpened(). */
    val openChatId: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    /** What's applied to the feed. */
    val filters: BrowseFilters = BrowseFilters(),
    /** The Filters sheet edits a copy, so the feed only changes when "Show results" is tapped. */
    val draftFilters: BrowseFilters = BrowseFilters(),
    val showFilterSheet: Boolean = false,
    /** How many listings the sheet's current choices would show. */
    val draftResultCount: Int = 0,
) {
    /** The sideways strip of notes only shows on the unfiltered feed. */
    val showWantedStrip: Boolean
        get() = filter == BrowseFilter.ALL && query.isBlank() && category == null && filters.isDefault && wanted.isNotEmpty()

    /** "Looking for" chip: show the notes as a full list instead of listings. */
    val showWantedList: Boolean
        get() = filter == BrowseFilter.LOOKING_FOR

    /** Back clears these first, before leaving Browse. */
    val hasActiveFilters: Boolean
        get() = query.isNotBlank() || category != null || filter != BrowseFilter.ALL || !filters.isDefault
}
