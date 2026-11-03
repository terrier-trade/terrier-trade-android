package edu.bu.terriertrade.ui.screens.post

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.PhoneIphone
import androidx.compose.material.icons.outlined.SyncAlt
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import edu.bu.terriertrade.ui.components.TTBannerTone
import edu.bu.terriertrade.ui.components.TTChoiceChips
import edu.bu.terriertrade.ui.components.TTDateTimeField
import edu.bu.terriertrade.ui.components.TTInfoBanner
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
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            verticalAlignment = Alignment.Top,
        ) {
            TTQuantityStepper(
                value = uiState.quantity,
                onValueChange = actions.onQuantityChange,
                label = "Quantity",
                range = 1..NewListingViewModel.MAX_TICKETS,
            )
            TTPriceField(
                value = uiState.price,
                onValueChange = actions.onPriceChange,
                label = "Price each",
                errorMessage = uiState.priceError,
                helperText = null,
                modifier = Modifier.weight(1f),
            )
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

    TTInfoBanner(
        text = "How the handoff works: 1. Agree on a deal in chat. 2. Send the tickets with the app's Transfer button. " +
                "3. You tap Sent, they tap Received. Screenshots don't count as a transfer.",
        tone = TTBannerTone.Warning,
        icon = Icons.Outlined.SyncAlt,
    )
    if (uiState.eventDateMillis != null) {
        Text(
            text = "This listing hides on its own after ${formatDateTime(uiState.eventDateMillis)}.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
