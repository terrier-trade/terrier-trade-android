package edu.bu.terriertrade.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.PreviewLightDark
import edu.bu.terriertrade.ui.theme.Spacing

/**
 * Pick one option from pill chips that wrap onto new lines. For forms:
 * class year, condition, meetup place, ticket platform.
 * optionIcon is optional: give it to show an icon on each chip.
 * For a single scrolling row (like Browse categories) use TTChipRow.
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
    optionIcon: ((T) -> ImageVector?)? = null,
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
                TTSelectableChip(
                    text = optionLabel(option),
                    selected = option == selected,
                    onClick = { onSelect(option) },
                    leadingIcon = optionIcon?.invoke(option),
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
