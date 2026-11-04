package edu.bu.terriertrade.ui.screens.browse

import edu.bu.terriertrade.ui.model.ListingCategory
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
    val category: ListingCategory? = null,
    val filter: BrowseFilter = BrowseFilter.ALL,
    val listings: List<ListingUi> = emptyList(),
    val wanted: List<WantedPostUi> = emptyList(),
    val favoriteIds: Set<String> = emptySet(),
    val requestedIds: Set<String> = emptySet(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
) {
    /** The sideways strip of notes only shows on the unfiltered feed. */
    val showWantedStrip: Boolean
        get() = filter == BrowseFilter.ALL && query.isBlank() && category == null && wanted.isNotEmpty()

    /** "Looking for" chip: show the notes as a full list instead of listings. */
    val showWantedList: Boolean
        get() = filter == BrowseFilter.LOOKING_FOR

    /** Back clears these first, before leaving Browse. */
    val hasActiveFilters: Boolean
        get() = query.isNotBlank() || category != null || filter != BrowseFilter.ALL
}
