package edu.bu.terriertrade.ui.screens.newlisting

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import edu.bu.terriertrade.data.model.PriceUnit
import edu.bu.terriertrade.ui.components.TTActionTile
import edu.bu.terriertrade.ui.components.TTBannerTone
import edu.bu.terriertrade.ui.components.TTChoiceChips
import edu.bu.terriertrade.ui.components.TTInfoBanner
import edu.bu.terriertrade.ui.components.TTPhotoThumb
import edu.bu.terriertrade.ui.components.TTSectionCard
import edu.bu.terriertrade.ui.components.TTTextField
import edu.bu.terriertrade.ui.model.icon
import edu.bu.terriertrade.ui.theme.Spacing

/*
 * Pieces of the New listing form that more than one post type uses.
 */

/**
 * Photos, laid out like the proposal: "Add photo" and "Scan book" side by side, turning green
 * once done, with the thumbnails underneath. Services only get "Add photo".
 */
@Composable
internal fun PhotosSection(uiState: NewListingUiState, actions: NewListingActions, showScan: Boolean) {
    TTSectionCard(title = "Photos", icon = Icons.Outlined.PhotoCamera) {
        val photoCount = uiState.photos.size
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            TTActionTile(
                icon = Icons.Outlined.AddAPhoto,
                label = when (photoCount) {
                    0 -> "Add photo"
                    1 -> "Photo added"
                    else -> "$photoCount photos added"
                },
                onClick = actions.onAddPhoto,
                done = photoCount > 0,
                enabled = uiState.canAddPhoto,
                modifier = Modifier.weight(1f),
            )
            if (showScan) {
                TTActionTile(
                    icon = Icons.Outlined.QrCodeScanner,
                    label = when {
                        uiState.isLookingUpBook -> "Looking up…"
                        uiState.isbn != null -> "Book scanned"
                        else -> "Scan book"
                    },
                    onClick = actions.onScanClick,
                    done = uiState.isbn != null,
                    loading = uiState.isLookingUpBook,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        if (uiState.scanMessage != null && !uiState.showScanner) {
            TTInfoBanner(
                text = uiState.scanMessage,
                tone = if (uiState.scanFailed) TTBannerTone.Warning else TTBannerTone.Success,
                icon = if (uiState.scanFailed) Icons.Outlined.ErrorOutline else Icons.AutoMirrored.Outlined.MenuBook,
            )
        }

        if (photoCount > 0) {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                uiState.photos.forEachIndexed { index, uri ->
                    TTPhotoThumb(model = uri, onRemove = { actions.onRemovePhoto(index) }, isCover = index == 0)
                }
            }
        }

        Text(
            text = if (showScan) {
                "Selling a textbook? Scan the barcode to fill in the title and author. " +
                    "Up to ${NewListingViewModel.MAX_PHOTOS} photos, the first is the cover."
            } else {
                "Up to ${NewListingViewModel.MAX_PHOTOS} photos, the first is the cover."
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
internal fun DescriptionField(
    uiState: NewListingUiState,
    actions: NewListingActions,
    label: String = "Description (optional)",
    placeholder: String = "Size, age, any marks or missing parts",
) {
    TTTextField(
        value = uiState.description,
        onValueChange = actions.onDescriptionChange,
        label = label,
        placeholder = placeholder,
        helperText = "${uiState.description.length} / ${NewListingViewModel.MAX_DESCRIPTION_LENGTH}",
        singleLine = false,
        minLines = 3,
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
internal fun CategoryChips(uiState: NewListingUiState, actions: NewListingActions) {
    TTChoiceChips(
        label = "Category",
        options = NewListingViewModel.categoriesFor(uiState.type),
        selected = uiState.category,
        onSelect = actions.onCategorySelect,
        optionLabel = { it.label },
        optionIcon = { it.icon },
        errorMessage = uiState.categoryError,
    )
}

@Composable
internal fun MeetSpotChips(uiState: NewListingUiState, actions: NewListingActions, label: String = "Meet at") {
    TTChoiceChips(
        label = label,
        options = NewListingViewModel.inPersonMeetSpots,
        selected = uiState.meetSpot,
        onSelect = actions.onMeetSpotSelect,
        optionLabel = { it },
        optionIcon = { Icons.Outlined.Place },
        errorMessage = uiState.meetSpotError,
    )
}

@Composable
internal fun PriceUnitChips(
    uiState: NewListingUiState,
    actions: NewListingActions,
    options: List<PriceUnit>,
    label: String,
) {
    TTChoiceChips(
        label = label,
        options = options,
        selected = uiState.priceUnit,
        onSelect = actions.onPriceUnitSelect,
        optionLabel = { it.label },
    )
}

internal val PriceUnit.label: String
    get() = when (this) {
        PriceUnit.NONE -> "Total"
        PriceUnit.PER_HOUR -> "Per hour"
        PriceUnit.PER_SESSION -> "Per session"
    }
