package edu.bu.terriertrade.ui.screens.post

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import edu.bu.terriertrade.data.model.Category
import edu.bu.terriertrade.data.model.Condition
import edu.bu.terriertrade.data.model.Listing
import edu.bu.terriertrade.data.model.ListingType
import edu.bu.terriertrade.data.model.MeetSpots
import edu.bu.terriertrade.data.repository.ListingRepository
import edu.bu.terriertrade.di.appContainer
import kotlinx.coroutines.launch

/**
 * Holds the New listing form, checks it, and saves it through ListingRepository.
 * TODO(photos): upload photos to storage and save their URLs. For now the picked Uris are saved as they are.
 */
class NewListingViewModel(
    private val listingRepository: ListingRepository,
) : ViewModel() {

    var uiState by mutableStateOf(NewListingUiState())
        private set

    fun onTypeSelect(type: ListingType) {
        uiState = uiState.copy(type = type, submitError = null)
    }

    /** Adds picked photos, keeping at most MAX_PHOTOS. */
    fun onPhotosPicked(uris: List<String>) {
        uiState = uiState.copy(photos = (uiState.photos + uris).distinct().take(MAX_PHOTOS))
    }

    fun onRemovePhoto(index: Int) {
        uiState = uiState.copy(photos = uiState.photos.filterIndexed { i, _ -> i != index })
    }

    fun onTitleChange(value: String) {
        uiState = uiState.copy(
            title = value.take(MAX_TITLE_LENGTH), titleError = null, submitError = null, postedMessage = null,
        )
    }

    fun onPriceChange(value: String) {
        uiState = uiState.copy(price = value, priceError = null, submitError = null)
    }

    fun onDescriptionChange(value: String) {
        uiState = uiState.copy(description = value.take(MAX_DESCRIPTION_LENGTH))
    }

    fun onCategorySelect(category: Category) {
        uiState = uiState.copy(category = category, categoryError = null)
    }

    fun onConditionSelect(condition: Condition) {
        uiState = uiState.copy(condition = condition, conditionError = null)
    }

    fun onMeetSpotSelect(spot: String) {
        uiState = uiState.copy(meetSpot = spot, meetSpotError = null)
    }

    /** Saves the listing when the form is OK, then clears the form and reports the new id. */
    fun post(onPosted: (listingId: String) -> Unit) {
        if (uiState.isPosting || !uiState.isFormReady || !validate()) return
        val form = uiState
        uiState = form.copy(isPosting = true, submitError = null)

        val listing = Listing(
            id = "",
            ownerId = "",
            type = ListingType.ITEM,
            title = form.title.trim(),
            price = form.price.toInt(),
            description = form.description.trim(),
            category = form.category ?: Category.OTHER,
            condition = form.condition,
            meetSpot = form.meetSpot.orEmpty(),
            photoUrls = form.photos,
        )

        viewModelScope.launch {
            listingRepository.createListing(listing)
                .onSuccess { id ->
                    uiState = NewListingUiState(postedMessage = "Posted! \"${listing.title}\" is live.")
                    onPosted(id)
                }
                .onFailure { error ->
                    uiState = uiState.copy(
                        isPosting = false,
                        submitError = error.message ?: "Couldn't post your listing. Try again.",
                    )
                }
        }
    }

    /** Shows an error under every field that's wrong. Returns true when the form is OK. */
    fun validate(): Boolean {
        val state = uiState
        val titleError = when {
            state.title.isBlank() -> "Give your listing a title"
            state.title.trim().length < MIN_TITLE_LENGTH -> "Use at least $MIN_TITLE_LENGTH characters"
            else -> null
        }
        val priceError = if (state.price.isBlank()) "Enter a price, or 0 for free" else null
        val categoryError = if (state.category == null) "Pick a category" else null
        val conditionError = if (state.condition == null) "Pick the condition" else null
        val meetSpotError = if (state.meetSpot == null) "Pick where you'll meet" else null

        uiState = state.copy(
            titleError = titleError,
            priceError = priceError,
            categoryError = categoryError,
            conditionError = conditionError,
            meetSpotError = meetSpotError,
        )
        return listOf(titleError, priceError, categoryError, conditionError, meetSpotError).all { it == null }
    }

    companion object {
        const val MAX_PHOTOS = 5
        const val MIN_TITLE_LENGTH = 3
        const val MAX_TITLE_LENGTH = 80
        const val MAX_DESCRIPTION_LENGTH = 500

        /** Categories that make sense for a physical item. Tutoring and tickets have their own post types. */
        val itemCategories: List<Category> = listOf(
            Category.BOOKS, Category.FURNITURE, Category.TECH, Category.NOTES, Category.OTHER,
        )

        /** Items are handed over in person, so "Online" isn't offered. */
        val itemMeetSpots: List<String> = MeetSpots.all.filterNot { it == "Online" }

        val Factory = viewModelFactory {
            initializer { NewListingViewModel(appContainer.listingRepository) }
        }
    }
}
