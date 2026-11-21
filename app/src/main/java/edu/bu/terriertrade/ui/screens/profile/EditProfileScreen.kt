package edu.bu.terriertrade.ui.screens.profile

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.lifecycle.viewmodel.compose.viewModel
import edu.bu.terriertrade.data.model.MeetSpots
import edu.bu.terriertrade.ui.components.buttons.TTPrimaryButton
import edu.bu.terriertrade.ui.components.feedback.TTErrorState
import edu.bu.terriertrade.ui.components.feedback.TTErrorText
import edu.bu.terriertrade.ui.components.feedback.TTLoadingState
import edu.bu.terriertrade.ui.components.inputs.TTTextField
import edu.bu.terriertrade.ui.components.layout.TTBottomActions
import edu.bu.terriertrade.ui.components.layout.TTScreenHeader
import edu.bu.terriertrade.ui.components.selection.TTChoiceChips
import edu.bu.terriertrade.ui.theme.Spacing
import edu.bu.terriertrade.ui.theme.TerrierTradeTheme

/** Every action the form can send, so the stateless screen takes one parameter instead of nine. */
data class EditProfileActions(
    val onNameChange: (String) -> Unit = {},
    val onMajorChange: (String) -> Unit = {},
    val onYearSelect: (String) -> Unit = {},
    val onStartTermChange: (String) -> Unit = {},
    val onGradTermChange: (String) -> Unit = {},
    val onBioChange: (String) -> Unit = {},
    val onMeetSpotSelect: (String) -> Unit = {},
    val onSave: () -> Unit = {},
    val onRetry: () -> Unit = {},
)

/**
 * Stateful entry point used by navigation. Owns the ViewModel and the "Discard changes?" dialog.
 * [onDone] closes the screen: after saving, or when leaving without changes.
 */
@Composable
fun EditProfileRoute(
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EditProfileViewModel = viewModel(factory = EditProfileViewModel.Factory),
) {
    var confirmDiscard by rememberSaveable { mutableStateOf(false) }
    val uiState = viewModel.uiState

    // Closes the screen at most once. Back during a save and the save finishing could both
    // try to close it, and closing twice would also pop the Profile screen underneath.
    var closed by remember { mutableStateOf(false) }
    val close: () -> Unit = {
        if (!closed) {
            closed = true
            onDone()
        }
    }

    val onBack: () -> Unit = {
        when {
            // Offline, Firestore keeps the save on the phone and sends it later, but the spinner
            // only stops once the server answers. Let the student leave instead of waiting.
            uiState.isSaving -> close()
            viewModel.hasUnsavedChanges() -> {
                confirmDiscard = true
            }
            else -> close()
        }
    }
    // The system back gesture asks too, instead of silently throwing edits away.
    BackHandler(onBack = onBack)

    EditProfileScreen(
        uiState = uiState,
        onBack = onBack,
        actions = EditProfileActions(
            onNameChange = viewModel::onNameChange,
            onMajorChange = viewModel::onMajorChange,
            onYearSelect = viewModel::onYearSelect,
            onStartTermChange = viewModel::onStartTermChange,
            onGradTermChange = viewModel::onGradTermChange,
            onBioChange = viewModel::onBioChange,
            onMeetSpotSelect = viewModel::onMeetSpotSelect,
            onSave = { viewModel.save(onSaved = close) },
            onRetry = viewModel::load,
        ),
        modifier = modifier,
    )

    if (confirmDiscard) {
        AlertDialog(
            onDismissRequest = { confirmDiscard = false },
            title = { Text("Discard changes?") },
            text = { Text("Your edits to your profile won't be saved.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmDiscard = false
                    close()
                }) { Text("Discard") }
            },
            dismissButton = { TextButton(onClick = { confirmDiscard = false }) { Text("Keep editing") } },
        )
    }
}

/** Stateless UI: loading, load error, or the form. */
@Composable
fun EditProfileScreen(
    uiState: EditProfileUiState,
    onBack: () -> Unit,
    actions: EditProfileActions,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize()) {
            TTScreenHeader(title = "Edit profile", subtitle = "What other students see", onBack = onBack)

            when {
                uiState.isLoading -> TTLoadingState(message = "Loading your profile…")
                uiState.loadError != null -> TTErrorState(message = uiState.loadError, onRetry = actions.onRetry)
                else -> {
                    EditProfileForm(uiState = uiState, actions = actions, modifier = Modifier.weight(1f))
                    TTBottomActions {
                        TTErrorText(message = uiState.saveError)
                        TTPrimaryButton(
                            text = "Save changes",
                            onClick = actions.onSave,
                            loading = uiState.isSaving,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EditProfileForm(
    uiState: EditProfileUiState,
    actions: EditProfileActions,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Spacing.xl, vertical = Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.lg),
    ) {
        TTTextField(
            value = uiState.name,
            onValueChange = actions.onNameChange,
            label = "Name",
            placeholder = "Alex Morgan",
            errorMessage = uiState.nameError,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth(),
        )
        TTTextField(
            value = uiState.major,
            onValueChange = actions.onMajorChange,
            label = "Major",
            placeholder = "Computer Science",
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth(),
        )
        TTChoiceChips(
            label = "Year",
            options = EditProfileViewModel.YEARS,
            selected = uiState.year,
            onSelect = actions.onYearSelect,
            optionLabel = { it },
        )
        TTTextField(
            value = uiState.startTerm,
            onValueChange = actions.onStartTermChange,
            label = "Started at BU",
            placeholder = "Fall 2024",
            errorMessage = uiState.startTermError,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth(),
        )
        TTTextField(
            value = uiState.gradTerm,
            onValueChange = actions.onGradTermChange,
            label = "Graduating",
            placeholder = "May 2027",
            errorMessage = uiState.gradTermError,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth(),
        )
        TTTextField(
            value = uiState.bio,
            onValueChange = actions.onBioChange,
            label = "Bio",
            placeholder = "What you sell, what you can help with, when you're free to meet",
            helperText = "${uiState.bio.length} / ${EditProfileViewModel.MAX_BIO_LENGTH}",
            singleLine = false,
            minLines = 3,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            modifier = Modifier.fillMaxWidth(),
        )
        TTChoiceChips(
            label = "Usual meet spot",
            options = MeetSpots.all,
            selected = uiState.meetSpot,
            onSelect = actions.onMeetSpotSelect,
            optionLabel = { it },
            optionIcon = { Icons.Outlined.Place },
        )
    }
}

private val previewForm = EditProfileUiState(
    isLoading = false,
    name = "Alex Morgan",
    major = "Computer Science",
    year = "Junior",
    startTerm = "Fall 2024",
    gradTerm = "May 2027",
    bio = "CS junior selling stuff from my dorm. Happy to help with Python too.",
    meetSpot = "GSU",
)

@PreviewLightDark
@Composable
private fun EditProfileScreenPreview() {
    TerrierTradeTheme {
        EditProfileScreen(uiState = previewForm, onBack = {}, actions = EditProfileActions())
    }
}

@PreviewLightDark
@Composable
private fun EditProfileErrorsPreview() {
    TerrierTradeTheme {
        EditProfileScreen(
            uiState = previewForm.copy(
                name = "",
                nameError = "Add your name",
                gradTerm = "May",
                gradTermError = "Add the year, like May 2027",
                saveError = "No connection. Check it and try again.",
            ),
            onBack = {},
            actions = EditProfileActions(),
        )
    }
}