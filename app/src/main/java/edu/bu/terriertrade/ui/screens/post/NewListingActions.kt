package edu.bu.terriertrade.ui.screens.post

import edu.bu.terriertrade.data.model.Category
import edu.bu.terriertrade.data.model.Condition
import edu.bu.terriertrade.data.model.ListingType
import edu.bu.terriertrade.data.model.PriceUnit
import edu.bu.terriertrade.data.model.ServiceMode

/**
 * Everything the New listing screen can ask for, in one object.
 * Every action defaults to doing nothing, so previews can just pass NewListingActions().
 */
data class NewListingActions(
    val onTypeSelect: (ListingType) -> Unit = {},
    val onTitleChange: (String) -> Unit = {},
    val onPriceChange: (String) -> Unit = {},
    val onPriceUnitSelect: (PriceUnit) -> Unit = {},
    val onDescriptionChange: (String) -> Unit = {},
    val onCategorySelect: (Category) -> Unit = {},
    val onMeetSpotSelect: (String) -> Unit = {},
    val onAddPhoto: () -> Unit = {},
    val onRemovePhoto: (index: Int) -> Unit = {},
    val onConditionSelect: (Condition) -> Unit = {},
    val onAuthorChange: (String) -> Unit = {},
    val onScanClick: () -> Unit = {},
    val onServiceModeSelect: (ServiceMode) -> Unit = {},
    val onEventDateChange: (Long) -> Unit = {},
    val onSectionChange: (String) -> Unit = {},
    val onQuantityChange: (Int) -> Unit = {},
    val onPlatformSelect: (TicketPlatform) -> Unit = {},
    val onPost: () -> Unit = {},
)
