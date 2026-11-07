package edu.bu.terriertrade.ui.screens.newlisting

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.outlined.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import edu.bu.terriertrade.data.model.PriceUnit
import edu.bu.terriertrade.ui.components.TTPriceField
import edu.bu.terriertrade.ui.components.TTSectionCard
import edu.bu.terriertrade.ui.components.TTTextField

/** "Looking for" form: a sticky note on Browse that says what you need and your budget. */
@Composable
internal fun WantedForm(uiState: NewListingUiState, actions: NewListingActions) {
    TTSectionCard(title = "What do you need?", icon = Icons.Outlined.Search) {
        TTTextField(
            value = uiState.title,
            onValueChange = actions.onTitleChange,
            label = "Looking for",
            placeholder = "Math tutor for MA 124, CS 501 textbook…",
            errorMessage = uiState.titleError,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth(),
        )
        CategoryChips(uiState, actions)
        DescriptionField(
            uiState, actions,
            label = "More details (optional)",
            placeholder = "Edition, when you need it, anything else",
        )
    }

    TTSectionCard(title = "Your budget", icon = Icons.Outlined.Savings) {
        TTPriceField(
            value = uiState.price,
            onValueChange = actions.onPriceChange,
            label = "Up to",
            errorMessage = uiState.priceError,
            helperText = "The most you'd pay, in whole dollars.",
            modifier = Modifier.fillMaxWidth(),
        )
        PriceUnitChips(uiState, actions, options = listOf(PriceUnit.NONE, PriceUnit.PER_HOUR), label = "Budget is")
        MeetSpotChips(uiState, actions, label = "Meet at (optional)")
    }
}
