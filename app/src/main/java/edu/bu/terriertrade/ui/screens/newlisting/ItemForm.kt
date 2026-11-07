package edu.bu.terriertrade.ui.screens.newlisting

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import edu.bu.terriertrade.data.model.Condition
import edu.bu.terriertrade.ui.components.TTChoiceChips
import edu.bu.terriertrade.ui.components.TTPriceField
import edu.bu.terriertrade.ui.components.TTSectionCard
import edu.bu.terriertrade.ui.components.TTTextField

/** Item form: photos (with textbook scan), basics, then category, condition and meet spot. */
@Composable
internal fun ItemForm(uiState: NewListingUiState, actions: NewListingActions) {
    PhotosSection(uiState, actions, showScan = true)

    TTSectionCard(title = "Basics", icon = Icons.Outlined.EditNote) {
        TTTextField(
            value = uiState.title,
            onValueChange = actions.onTitleChange,
            label = "Title",
            placeholder = "Desk chair, mini fridge, CS 330 notes…",
            errorMessage = uiState.titleError,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth(),
        )
        if (uiState.isBook) {
            TTTextField(
                value = uiState.author,
                onValueChange = actions.onAuthorChange,
                label = "Author / edition",
                placeholder = "Cormen et al. · 4th edition",
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        TTPriceField(
            value = uiState.price,
            onValueChange = actions.onPriceChange,
            errorMessage = uiState.priceError,
            modifier = Modifier.fillMaxWidth(),
        )
        DescriptionField(uiState, actions)
    }

    TTSectionCard(title = "Details", icon = Icons.Outlined.Tune) {
        CategoryChips(uiState, actions)
        TTChoiceChips(
            label = "Condition",
            options = Condition.entries,
            selected = uiState.condition,
            onSelect = actions.onConditionSelect,
            optionLabel = { it.label },
            errorMessage = uiState.conditionError,
        )
        MeetSpotChips(uiState, actions)
    }
}
