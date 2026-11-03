package edu.bu.terriertrade.ui.screens.post

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Handshake
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Publish
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Sell
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import edu.bu.terriertrade.data.model.Category
import edu.bu.terriertrade.data.model.Condition
import edu.bu.terriertrade.data.model.ListingType
import edu.bu.terriertrade.data.model.PriceUnit
import edu.bu.terriertrade.data.model.ServiceMode
import edu.bu.terriertrade.ui.components.TTBannerTone
import edu.bu.terriertrade.ui.components.TTErrorText
import edu.bu.terriertrade.ui.components.TTInfoBanner
import edu.bu.terriertrade.ui.components.TTOptionTile
import edu.bu.terriertrade.ui.components.TTPrimaryButton
import edu.bu.terriertrade.ui.components.TTSectionCard
import edu.bu.terriertrade.ui.theme.Spacing
import edu.bu.terriertrade.ui.theme.TerrierTradeTheme

/** Stateful entry point used by navigation. Owns the ViewModel, the photo picker and the scanner. */
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

    val actions = NewListingActions(
        onTypeSelect = viewModel::onTypeSelect,
        onTitleChange = viewModel::onTitleChange,
        onPriceChange = viewModel::onPriceChange,
        onPriceUnitSelect = viewModel::onPriceUnitSelect,
        onDescriptionChange = viewModel::onDescriptionChange,
        onCategorySelect = viewModel::onCategorySelect,
        onMeetSpotSelect = viewModel::onMeetSpotSelect,
        onAddPhoto = { pickPhotos.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
        onRemovePhoto = viewModel::onRemovePhoto,
        onConditionSelect = viewModel::onConditionSelect,
        onAuthorChange = viewModel::onAuthorChange,
        onScanClick = viewModel::onScanClick,
        onServiceModeSelect = viewModel::onServiceModeSelect,
        onEventDateChange = viewModel::onEventDateChange,
        onSectionChange = viewModel::onSectionChange,
        onQuantityChange = viewModel::onQuantityChange,
        onPlatformSelect = viewModel::onPlatformSelect,
        onPost = { viewModel.post(onPosted) },
    )

    NewListingScreen(uiState = viewModel.uiState, actions = actions, modifier = modifier)

    if (viewModel.uiState.showScanner) {
        IsbnScannerDialog(
            onIsbn = viewModel::onIsbnScanned,
            onDismiss = viewModel::onScannerDismiss,
            errorMessage = viewModel.uiState.scanMessage.takeIf { viewModel.uiState.scanFailed },
        )
    }
}

/** Stateless UI. The app header and bottom bar come from the app frame. Each post type has its own form file. */
@Composable
fun NewListingScreen(
    uiState: NewListingUiState,
    actions: NewListingActions,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()

    // After a post the form clears, so jump back to the top where the "Posted!" message is.
    LaunchedEffect(uiState.postedMessage) {
        if (uiState.postedMessage != null) scrollState.animateScrollTo(0)
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            NewListingTitle(type = uiState.type)

            if (uiState.postedMessage != null) {
                TTInfoBanner(
                    text = uiState.postedMessage,
                    tone = TTBannerTone.Success,
                    icon = Icons.Outlined.CheckCircle,
                )
            }

            TypeSection(selected = uiState.type, onSelect = actions.onTypeSelect)

            when (uiState.type) {
                ListingType.ITEM -> ItemForm(uiState, actions)
                ListingType.SERVICE -> ServiceForm(uiState, actions)
                ListingType.TICKET -> TicketForm(uiState, actions)
                ListingType.WANTED -> WantedForm(uiState, actions)
            }

            TTInfoBanner(
                text = "No fees and no payments in the app. You pay or get paid in person at the meetup.",
                icon = Icons.Outlined.Handshake,
            )
            TTErrorText(message = uiState.submitError)
            TTPrimaryButton(
                text = uiState.postButtonText,
                onClick = actions.onPost,
                loading = uiState.isPosting,
                leadingIcon = Icons.Outlined.Publish,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun NewListingTitle(type: ListingType) {
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
                text = type.screenTitle.uppercase(),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = type.screenSubtitle,
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

private val ListingType.icon: ImageVector
    get() = when (this) {
        ListingType.ITEM -> Icons.Outlined.Inventory2
        ListingType.SERVICE -> Icons.Outlined.School
        ListingType.TICKET -> Icons.Outlined.ConfirmationNumber
        ListingType.WANTED -> Icons.Outlined.Search
    }

/** Short hint under each post type, matching the proposal. */
private val ListingType.hint: String
    get() = when (this) {
        ListingType.ITEM -> "Books, furniture, notes"
        ListingType.SERVICE -> "Tutoring, help"
        ListingType.TICKET -> "BU events"
        ListingType.WANTED -> "Ask for something"
    }

private val ListingType.screenTitle: String
    get() = when (this) {
        ListingType.ITEM -> "New listing"
        ListingType.SERVICE -> "Offer a service"
        ListingType.TICKET -> "Sell tickets"
        ListingType.WANTED -> "Looking for"
    }

private val ListingType.screenSubtitle: String
    get() = when (this) {
        ListingType.ITEM -> "Takes about a minute"
        ListingType.SERVICE -> "Tutoring, help, skills"
        ListingType.TICKET -> "Only events with an official transfer"
        ListingType.WANTED -> "Post a wanted note with your budget"
    }

@PreviewLightDark
@Composable
private fun NewListingItemPreview() {
    TerrierTradeTheme {
        NewListingScreen(
            uiState = NewListingUiState(
                title = "Desk chair",
                price = "40",
                category = Category.FURNITURE,
                condition = Condition.GOOD,
                meetSpot = "West Campus",
            ),
            actions = NewListingActions(),
        )
    }
}

@PreviewLightDark
@Composable
private fun NewListingScannedBookPreview() {
    TerrierTradeTheme {
        NewListingScreen(
            uiState = NewListingUiState(
                title = "Introduction to Algorithms",
                author = "Thomas H. Cormen, Charles E. Leiserson · 2022",
                category = Category.BOOKS,
                isbn = "9780262046305",
                scanMessage = NewListingViewModel.SCAN_FILLED_MESSAGE,
            ),
            actions = NewListingActions(),
        )
    }
}

@PreviewLightDark
@Composable
private fun NewListingServicePreview() {
    TerrierTradeTheme {
        NewListingScreen(
            uiState = NewListingUiState(
                type = ListingType.SERVICE,
                title = "CS 330 tutoring",
                price = "20",
                priceUnit = PriceUnit.PER_HOUR,
                category = Category.TUTORING,
                serviceMode = ServiceMode.EITHER,
                meetSpot = "Mugar Library",
            ),
            actions = NewListingActions(),
        )
    }
}

@PreviewLightDark
@Composable
private fun NewListingTicketPreview() {
    TerrierTradeTheme {
        NewListingScreen(
            uiState = NewListingUiState(
                type = ListingType.TICKET,
                title = "BU Hockey vs Boston College",
                eventDateMillis = 1_794_700_800_000L,
                section = "Student, 112",
                quantity = 2,
                price = "25",
                category = Category.TICKETS,
                platform = TicketPlatform.TICKETMASTER,
            ),
            actions = NewListingActions(),
        )
    }
}

@PreviewLightDark
@Composable
private fun NewListingWantedPreview() {
    TerrierTradeTheme {
        NewListingScreen(
            uiState = NewListingUiState(
                type = ListingType.WANTED,
                title = "Math tutor for MA 124",
                price = "25",
                priceUnit = PriceUnit.PER_HOUR,
                category = Category.TUTORING,
            ),
            actions = NewListingActions(),
        )
    }
}

@PreviewLightDark
@Composable
private fun NewListingErrorsPreview() {
    TerrierTradeTheme {
        NewListingScreen(
            uiState = NewListingUiState(
                titleError = "Give your listing a title",
                priceError = "Enter a price, or 0 for free",
                categoryError = "Pick a category",
                conditionError = "Pick the condition",
                meetSpotError = "Pick where you'll meet",
            ),
            actions = NewListingActions(),
        )
    }
}
