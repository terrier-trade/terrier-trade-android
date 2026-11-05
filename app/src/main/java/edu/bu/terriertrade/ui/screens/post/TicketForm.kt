package edu.bu.terriertrade.ui.screens.post

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.PhoneIphone
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import edu.bu.terriertrade.ui.components.TTChoiceChips
import edu.bu.terriertrade.ui.components.TTDateTimeField
import edu.bu.terriertrade.ui.components.TTPriceField
import edu.bu.terriertrade.ui.components.TTQuantityStepper
import edu.bu.terriertrade.ui.components.TTSectionCard
import edu.bu.terriertrade.ui.components.TTTextField
import edu.bu.terriertrade.ui.components.formatDateTime
import edu.bu.terriertrade.ui.theme.Spacing

/** Ticket form from the proposal: event, date, section, quantity, price each, and where the ticket lives. */
@Composable
internal fun TicketForm(uiState: NewListingUiState, actions: NewListingActions) {
    TTSectionCard(title = "Event", icon = Icons.Outlined.Event) {
        TTTextField(
            value = uiState.title,
            onValueChange = actions.onTitleChange,
            label = "Event",
            placeholder = "BU Hockey vs Boston College",
            errorMessage = uiState.titleError,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth(),
        )
        TTDateTimeField(
            valueMillis = uiState.eventDateMillis,
            onValueChange = actions.onEventDateChange,
            errorMessage = uiState.eventDateError,
            modifier = Modifier.fillMaxWidth(),
        )
        TTTextField(
            value = uiState.section,
            onValueChange = actions.onSectionChange,
            label = "Section (optional)",
            placeholder = "Student, 112",
            modifier = Modifier.fillMaxWidth(),
        )
    }

    TTSectionCard(title = "Tickets", icon = Icons.Outlined.ConfirmationNumber) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            verticalAlignment = Alignment.Top,
        ) {
            TTQuantityStepper(
                value = uiState.quantity,
                onValueChange = actions.onQuantityChange,
                label = "Quantity",
                range = 1..NewListingViewModel.MAX_TICKETS,
            )
            // Label above the box, like Quantity, so both boxes start at the same height.
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                Text(
                    text = "Price each",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                TTPriceField(
                    value = uiState.price,
                    onValueChange = actions.onPriceChange,
                    label = "",
                    errorMessage = uiState.priceError,
                    helperText = null,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }

    TTSectionCard(title = "Where the ticket lives", icon = Icons.Outlined.PhoneIphone) {
        TTChoiceChips(
            options = TicketPlatform.entries,
            selected = uiState.platform,
            onSelect = actions.onPlatformSelect,
            optionLabel = { it.label },
            errorMessage = uiState.platformError,
        )
        Text(
            text = "Paper or PDF tickets aren't allowed: they can't be transferred safely.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }

    if (uiState.eventDateMillis != null) {
        Text(
            text = "This listing hides on its own after ${formatDateTime(uiState.eventDateMillis)}.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
