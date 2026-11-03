package edu.bu.terriertrade.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import edu.bu.terriertrade.ui.theme.Spacing

/**
 * Pick one option from a row of pill chips. Works with any type:
 * class year, condition, category, meetup place, ticket platform.
 * Chips wrap onto a new line on narrow phones.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun <T> TTChoiceChips(
    options: List<T>,
    selected: T?,
    onSelect: (T) -> Unit,
    optionLabel: (T) -> String,
    modifier: Modifier = Modifier,
    label: String? = null,
    errorMessage: String? = null,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        if (label != null) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            options.forEach { option ->
                FilterChip(
                    selected = option == selected,
                    onClick = { onSelect(option) },
                    label = { Text(optionLabel(option)) },
                    shape = CircleShape,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                )
            }
        }
        TTErrorText(message = errorMessage)
    }
}

@PreviewLightDark
@Composable
private fun TTChoiceChipsPreview() {
    TTPreviewColumn {
        TTChoiceChips(
            label = "Condition",
            options = listOf("Like new", "Good", "Fair"),
            selected = "Good",
            onSelect = {},
            optionLabel = { it },
        )
        TTChoiceChips(
            label = "Year",
            options = listOf("First year", "Sophomore", "Junior", "Senior", "Grad"),
            selected = null,
            onSelect = {},
            optionLabel = { it },
            errorMessage = "Pick your year",
        )
    }
}