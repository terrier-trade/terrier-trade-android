package edu.bu.terriertrade.ui.screens.post

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Chair
import androidx.compose.material.icons.outlined.Computer
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardCapitalization
import edu.bu.terriertrade.data.model.Category
import edu.bu.terriertrade.data.model.PriceUnit
import edu.bu.terriertrade.ui.components.TTAddPhotoTile
import edu.bu.terriertrade.ui.components.TTBannerTone
import edu.bu.terriertrade.ui.components.TTChoiceChips
import edu.bu.terriertrade.ui.components.TTInfoBanner
import edu.bu.terriertrade.ui.components.TTPhotoThumb
import edu.bu.terriertrade.ui.components.TTSecondaryButton
import edu.bu.terriertrade.ui.components.TTSectionCard
import edu.bu.terriertrade.ui.components.TTTextField
import edu.bu.terriertrade.ui.theme.Spacing

/*
 * Pieces of the New listing form that more than one post type uses.
 */

/** Photo thumbnails and the Add photo tile. Items also get the textbook scan button. */
@Composable
internal fun PhotosSection(uiState: NewListingUiState, actions: NewListingActions, showScan: Boolean) {
    TTSectionCard(title = "Photos", icon = Icons.Outlined.PhotoCamera) {
        if (showScan) {
            TTSecondaryButton(
                text = when {
                    uiState.isLookingUpBook -> "Looking up book…"
                    uiState.isbn != null -> "Scan a different book"
                    else -> "Selling a textbook? Scan the barcode"
                },
                onClick = actions.onScanClick,
                loading = uiState.isLookingUpBook,
                leadingIcon = Icons.Outlined.QrCodeScanner,
                modifier = Modifier.fillMaxWidth(),
            )
            if (uiState.scanMessage != null && !uiState.showScanner) {
                TTInfoBanner(
                    text = uiState.scanMessage,
                    tone = if (uiState.scanFailed) TTBannerTone.Warning else TTBannerTone.Success,
                    icon = if (uiState.scanFailed) Icons.Outlined.ErrorOutline else Icons.AutoMirrored.Outlined.MenuBook,
                )
            }
        }
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            uiState.photos.forEachIndexed { index, uri ->
                TTPhotoThumb(model = uri, onRemove = { actions.onRemovePhoto(index) }, isCover = index == 0)
            }
            if (uiState.canAddPhoto) TTAddPhotoTile(onClick = actions.onAddPhoto)
        }
        Text(
            text = "Up to ${NewListingViewModel.MAX_PHOTOS}. The first one is the cover. Listings with a photo get more requests.",
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

internal val Category.icon: ImageVector
    get() = when (this) {
        Category.BOOKS -> Icons.Outlined.Book
        Category.FURNITURE -> Icons.Outlined.Chair
        Category.TECH -> Icons.Outlined.Computer
        Category.NOTES -> Icons.Outlined.Description
        Category.TUTORING -> Icons.Outlined.School
        Category.TICKETS -> Icons.Outlined.ConfirmationNumber
        Category.OTHER -> Icons.Outlined.MoreHoriz
    }

internal val PriceUnit.label: String
    get() = when (this) {
        PriceUnit.NONE -> "Total"
        PriceUnit.PER_HOUR -> "Per hour"
        PriceUnit.PER_SESSION -> "Per session"
    }
