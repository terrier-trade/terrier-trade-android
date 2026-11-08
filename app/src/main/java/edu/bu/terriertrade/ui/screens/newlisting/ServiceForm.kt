package edu.bu.terriertrade.ui.screens.newlisting

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Laptop
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import edu.bu.terriertrade.data.model.PriceUnit
import edu.bu.terriertrade.data.model.ServiceMode
import edu.bu.terriertrade.ui.components.inputs.TTPriceField
import edu.bu.terriertrade.ui.components.inputs.TTTextField
import edu.bu.terriertrade.ui.components.layout.TTSectionCard
import edu.bu.terriertrade.ui.components.selection.TTChoiceChips

/** Service form: what you offer and your rate, then in person or online and where. */
@Composable
internal fun ServiceForm(uiState: NewListingUiState, actions: NewListingActions) {
    TTSectionCard(title = "What you offer", icon = Icons.Outlined.EditNote) {
        TTTextField(
            value = uiState.title,
            onValueChange = actions.onTitleChange,
            label = "Title",
            placeholder = "CS 330 tutoring, resume review, bike repair…",
            errorMessage = uiState.titleError,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth(),
        )
        TTPriceField(
            value = uiState.price,
            onValueChange = actions.onPriceChange,
            label = "Your rate",
            errorMessage = uiState.priceError,
            helperText = "Whole dollars. Enter 0 to help for free.",
            modifier = Modifier.fillMaxWidth(),
        )
        PriceUnitChips(
            uiState, actions,
            options = listOf(PriceUnit.PER_HOUR, PriceUnit.PER_SESSION, PriceUnit.NONE),
            label = "Charged",
        )
        DescriptionField(
            uiState, actions,
            label = "Details (optional)",
            placeholder = "Courses you've taken, your grade, when you're free",
        )
    }

    TTSectionCard(title = "How and where", icon = Icons.Outlined.Tune) {
        CategoryChips(uiState, actions)
        TTChoiceChips(
            label = "How you'll meet",
            options = ServiceMode.entries,
            selected = uiState.serviceMode,
            onSelect = actions.onServiceModeSelect,
            optionLabel = { it.label },
            optionIcon = {
                when (it) {
                    ServiceMode.IN_PERSON -> Icons.Outlined.People
                    ServiceMode.ONLINE -> Icons.Outlined.Laptop
                    ServiceMode.EITHER -> Icons.Outlined.SwapHoriz
                }
            },
            errorMessage = uiState.serviceModeError,
        )
        if (uiState.needsMeetSpot) MeetSpotChips(uiState, actions)
    }

    PhotosSection(uiState, actions, showScan = false)
}
