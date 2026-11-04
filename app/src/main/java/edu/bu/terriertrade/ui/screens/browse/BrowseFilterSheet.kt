package edu.bu.terriertrade.ui.screens.browse

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import edu.bu.terriertrade.ui.components.TTChoiceChips
import edu.bu.terriertrade.ui.components.TTPrimaryButton
import edu.bu.terriertrade.ui.components.TTTextButton
import edu.bu.terriertrade.ui.theme.Spacing
import edu.bu.terriertrade.ui.theme.TerrierTradeTheme

/** The Filters sheet from the proposal: sort, price, condition and seller rating in one place. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowseFilterSheet(
    filters: BrowseFilters,
    resultCount: Int,
    onFiltersChange: (BrowseFilters) -> Unit,
    onReset: () -> Unit,
    onApply: () -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.background,
    ) {
        BrowseFilterContent(
            filters = filters,
            resultCount = resultCount,
            onFiltersChange = onFiltersChange,
            onReset = onReset,
            onApply = onApply,
        )
    }
}

/** The sheet's content, split out so it can be previewed. */
@Composable
private fun BrowseFilterContent(
    filters: BrowseFilters,
    resultCount: Int,
    onFiltersChange: (BrowseFilters) -> Unit,
    onReset: () -> Unit,
    onApply: () -> Unit,
) {
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(start = Spacing.xl, end = Spacing.xl, bottom = Spacing.xl),
        verticalArrangement = Arrangement.spacedBy(Spacing.lg),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "Filters",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
            TTTextButton(text = "Reset", onClick = onReset, enabled = !filters.isDefault)
        }

        TTChoiceChips(
            label = "Sort by",
            options = SortOrder.entries,
            selected = filters.sort,
            onSelect = { onFiltersChange(filters.copy(sort = it)) },
            optionLabel = { it.label },
        )
        TTChoiceChips(
            label = "Price",
            options = PriceLimit.entries,
            selected = filters.price,
            onSelect = { onFiltersChange(filters.copy(price = it)) },
            optionLabel = { it.label },
        )
        TTChoiceChips(
            label = "Condition",
            options = ConditionFilter.entries,
            selected = filters.condition,
            onSelect = { onFiltersChange(filters.copy(condition = it)) },
            optionLabel = { it.label },
        )
        if (filters.condition != ConditionFilter.ANY) {
            Text(
                text = "Shows items in this condition or better. Services and tickets are hidden.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        TTChoiceChips(
            label = "Seller rating",
            options = RatingFilter.entries,
            selected = filters.rating,
            onSelect = { onFiltersChange(filters.copy(rating = it)) },
            optionLabel = { it.label },
        )

        TTPrimaryButton(
            text = when (resultCount) {
                0 -> "No results"
                1 -> "Show 1 result"
                else -> "Show $resultCount results"
            },
            onClick = onApply,
            enabled = resultCount > 0,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@PreviewLightDark
@Composable
private fun BrowseFilterContentPreview() {
    TerrierTradeTheme {
        Surface {
            BrowseFilterContent(
                filters = BrowseFilters(price = PriceLimit.UNDER_50, condition = ConditionFilter.GOOD),
                resultCount = 9,
                onFiltersChange = {}, onReset = {}, onApply = {},
            )
        }
    }
}
