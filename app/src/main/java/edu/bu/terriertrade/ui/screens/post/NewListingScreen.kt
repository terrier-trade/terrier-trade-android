package edu.bu.terriertrade.ui.screens.post

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Chair
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Computer
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Handshake
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Publish
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Sell
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import edu.bu.terriertrade.data.model.Category
import edu.bu.terriertrade.data.model.Condition
import edu.bu.terriertrade.data.model.ListingType
import edu.bu.terriertrade.ui.components.TTAddPhotoTile
import edu.bu.terriertrade.ui.components.TTBannerTone
import edu.bu.terriertrade.ui.components.TTChoiceChips
import edu.bu.terriertrade.ui.components.TTErrorText
import edu.bu.terriertrade.ui.components.TTInfoBanner
import edu.bu.terriertrade.ui.components.TTOptionTile
import edu.bu.terriertrade.ui.components.TTPhotoThumb
import edu.bu.terriertrade.ui.components.TTPriceField
import edu.bu.terriertrade.ui.components.TTPrimaryButton
import edu.bu.terriertrade.ui.components.TTSectionCard
import edu.bu.terriertrade.ui.components.TTTextField
import edu.bu.terriertrade.ui.theme.Spacing
import edu.bu.terriertrade.ui.theme.TerrierTradeTheme

/** Stateful entry point used by navigation. Owns the ViewModel and the photo picker. */
@Composable
fun NewListingRoute(
    onPosted: (listingId: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NewListingViewModel = viewModel(factory = NewListingViewModel.Factory),
) {
    // Android's photo picker: no storage permission needed.
    val pickPhotos = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(NewListingViewModel.MAX_PHOTOS),
    ) { uris -> viewModel.onPhotosPicked(uris.map { it.toString() }) }

    NewListingScreen(
        uiState = viewModel.uiState,
        onTypeSelect = viewModel::onTypeSelect,
        onAddPhoto = { pickPhotos.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
        onRemovePhoto = viewModel::onRemovePhoto,
        onTitleChange = viewModel::onTitleChange,
        onPriceChange = viewModel::onPriceChange,
        onDescriptionChange = viewModel::onDescriptionChange,
        onCategorySelect = viewModel::onCategorySelect,
        onConditionSelect = viewModel::onConditionSelect,
        onMeetSpotSelect = viewModel::onMeetSpotSelect,
        onPost = { viewModel.post(onPosted) },
        modifier = modifier,
    )
}

/** Stateless UI built from shared components. The app header and bottom bar come from the app frame. */
@Composable
fun NewListingScreen(
    uiState: NewListingUiState,
    onTypeSelect: (ListingType) -> Unit,
    onAddPhoto: () -> Unit,
    onRemovePhoto: (index: Int) -> Unit,
    onTitleChange: (String) -> Unit,
    onPriceChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onCategorySelect: (Category) -> Unit,
    onConditionSelect: (Condition) -> Unit,
    onMeetSpotSelect: (String) -> Unit,
    onPost: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            NewListingTitle()

            if (uiState.postedMessage != null) {
                TTInfoBanner(
                    text = uiState.postedMessage,
                    tone = TTBannerTone.Success,
                    icon = Icons.Outlined.CheckCircle,
                )
            }

            TypeSection(selected = uiState.type, onSelect = onTypeSelect)

            if (uiState.isFormReady) {
                PhotosSection(
                    photos = uiState.photos,
                    canAddPhoto = uiState.canAddPhoto,
                    onAddPhoto = onAddPhoto,
                    onRemovePhoto = onRemovePhoto,
                )
                BasicsSection(
                    uiState = uiState,
                    onTitleChange = onTitleChange,
                    onPriceChange = onPriceChange,
                    onDescriptionChange = onDescriptionChange,
                )
                DetailsSection(
                    uiState = uiState,
                    onCategorySelect = onCategorySelect,
                    onConditionSelect = onConditionSelect,
                    onMeetSpotSelect = onMeetSpotSelect,
                )
                TTInfoBanner(
                    text = "No fees and no payments in the app. You get paid in person at the meetup.",
                    icon = Icons.Outlined.Handshake,
                )
                TTErrorText(message = uiState.submitError)
                TTPrimaryButton(
                    text = "Post listing",
                    onClick = onPost,
                    loading = uiState.isPosting,
                    leadingIcon = Icons.Outlined.Publish,
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                TTInfoBanner(text = "${uiState.type.label} posts are coming soon. For now you can post an item.")
            }
        }
    }
}

@Composable
private fun NewListingTitle() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(MaterialTheme.colorScheme.primary, MaterialTheme.shapes.medium),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Outlined.Sell, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
        }
        Column {
            Text(
                text = "New listing".uppercase(),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = "Takes about a minute",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** The four post types from the proposal, as a 2 x 2 grid. */
@Composable
private fun TypeSection(selected: ListingType, onSelect: (ListingType) -> Unit) {
    TTSectionCard(title = "What are you posting?", icon = Icons.Outlined.Dashboard) {
        listOf(
            listOf(ListingType.ITEM, ListingType.SERVICE),
            listOf(ListingType.TICKET, ListingType.WANTED),
        ).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                row.forEach { type ->
                    TTOptionTile(
                        icon = type.icon,
                        title = type.label,
                        subtitle = type.hint,
                        selected = type == selected,
                        onClick = { onSelect(type) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun PhotosSection(
    photos: List<String>,
    canAddPhoto: Boolean,
    onAddPhoto: () -> Unit,
    onRemovePhoto: (Int) -> Unit,
) {
    TTSectionCard(title = "Photos", icon = Icons.Outlined.PhotoCamera) {
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            photos.forEachIndexed { index, uri ->
                TTPhotoThumb(model = uri, onRemove = { onRemovePhoto(index) }, isCover = index == 0)
            }
            if (canAddPhoto) TTAddPhotoTile(onClick = onAddPhoto)
        }
        Text(
            text = "Up to ${NewListingViewModel.MAX_PHOTOS}. The first one is the cover. Listings with a photo get more requests.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun BasicsSection(
    uiState: NewListingUiState,
    onTitleChange: (String) -> Unit,
    onPriceChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
) {
    TTSectionCard(title = "Basics", icon = Icons.Outlined.EditNote) {
        TTTextField(
            value = uiState.title,
            onValueChange = onTitleChange,
            label = "Title",
            placeholder = "Desk chair, mini fridge, CS 330 notes…",
            errorMessage = uiState.titleError,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Next,
            ),
            modifier = Modifier.fillMaxWidth(),
        )
        TTPriceField(
            value = uiState.price,
            onValueChange = onPriceChange,
            errorMessage = uiState.priceError,
            modifier = Modifier.fillMaxWidth(),
        )
        TTTextField(
            value = uiState.description,
            onValueChange = onDescriptionChange,
            label = "Description (optional)",
            placeholder = "Size, age, any marks or missing parts",
            helperText = "${uiState.description.length} / ${NewListingViewModel.MAX_DESCRIPTION_LENGTH}",
            singleLine = false,
            minLines = 3,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun DetailsSection(
    uiState: NewListingUiState,
    onCategorySelect: (Category) -> Unit,
    onConditionSelect: (Condition) -> Unit,
    onMeetSpotSelect: (String) -> Unit,
) {
    TTSectionCard(title = "Details", icon = Icons.Outlined.Tune) {
        TTChoiceChips(
            label = "Category",
            options = NewListingViewModel.itemCategories,
            selected = uiState.category,
            onSelect = onCategorySelect,
            optionLabel = { it.label },
            optionIcon = { it.icon },
            errorMessage = uiState.categoryError,
        )
        TTChoiceChips(
            label = "Condition",
            options = Condition.entries,
            selected = uiState.condition,
            onSelect = onConditionSelect,
            optionLabel = { it.label },
            errorMessage = uiState.conditionError,
        )
        TTChoiceChips(
            label = "Meet at",
            options = NewListingViewModel.itemMeetSpots,
            selected = uiState.meetSpot,
            onSelect = onMeetSpotSelect,
            optionLabel = { it },
            optionIcon = { Icons.Outlined.Place },
            errorMessage = uiState.meetSpotError,
        )
    }
}

private val ListingType.icon: ImageVector
    get() = when (this) {
        ListingType.ITEM -> Icons.Outlined.Inventory2
        ListingType.SERVICE -> Icons.Outlined.School
        ListingType.TICKET -> Icons.Outlined.ConfirmationNumber
        ListingType.WANTED -> Icons.Outlined.Search
    }

private val Category.icon: ImageVector
    get() = when (this) {
        Category.BOOKS -> Icons.Outlined.Book
        Category.FURNITURE -> Icons.Outlined.Chair
        Category.TECH -> Icons.Outlined.Computer
        Category.NOTES -> Icons.Outlined.Description
        Category.TUTORING -> Icons.Outlined.School
        Category.TICKETS -> Icons.Outlined.ConfirmationNumber
        Category.OTHER -> Icons.Outlined.MoreHoriz
    }

/** Short hint under each post type, matching the proposal. */
private val ListingType.hint: String
    get() = when (this) {
        ListingType.ITEM -> "Books, furniture, notes"
        ListingType.SERVICE -> "Tutoring, help"
        ListingType.TICKET -> "BU events"
        ListingType.WANTED -> "Ask for something"
    }

@PreviewLightDark
@Composable
private fun NewListingScreenPreview() {
    TerrierTradeTheme {
        NewListingScreen(
            uiState = NewListingUiState(
                title = "Desk chair",
                price = "40",
                category = Category.FURNITURE,
                condition = Condition.GOOD,
                meetSpot = "West Campus",
            ),
            onTypeSelect = {}, onAddPhoto = {}, onRemovePhoto = {}, onTitleChange = {}, onPriceChange = {},
            onDescriptionChange = {}, onCategorySelect = {}, onConditionSelect = {}, onMeetSpotSelect = {}, onPost = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun NewListingScreenErrorsPreview() {
    TerrierTradeTheme {
        NewListingScreen(
            uiState = NewListingUiState(
                titleError = "Give your listing a title",
                priceError = "Enter a price, or 0 for free",
                categoryError = "Pick a category",
                conditionError = "Pick the condition",
                meetSpotError = "Pick where you'll meet",
            ),
            onTypeSelect = {}, onAddPhoto = {}, onRemovePhoto = {}, onTitleChange = {}, onPriceChange = {},
            onDescriptionChange = {}, onCategorySelect = {}, onConditionSelect = {}, onMeetSpotSelect = {}, onPost = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun NewListingScreenComingSoonPreview() {
    TerrierTradeTheme {
        NewListingScreen(
            uiState = NewListingUiState(type = ListingType.SERVICE),
            onTypeSelect = {}, onAddPhoto = {}, onRemovePhoto = {}, onTitleChange = {}, onPriceChange = {},
            onDescriptionChange = {}, onCategorySelect = {}, onConditionSelect = {}, onMeetSpotSelect = {}, onPost = {},
        )
    }
}
