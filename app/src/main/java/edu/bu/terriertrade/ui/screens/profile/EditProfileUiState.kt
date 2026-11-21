package edu.bu.terriertrade.ui.screens.profile

import edu.bu.terriertrade.data.model.User

/** Everything the Edit profile form draws. Text is kept exactly as typed; it's trimmed when saved. */
data class EditProfileUiState(
    val isLoading: Boolean = true,
    /** Why the profile couldn't be loaded, shown with Try again. */
    val loadError: String? = null,
    val name: String = "",
    val major: String = "",
    /** One of EditProfileViewModel.YEARS, or null when not picked. */
    val year: String? = null,
    val startTerm: String = "",
    val gradTerm: String = "",
    val bio: String = "",
    /** One of MeetSpots.all, or null when not picked. */
    val meetSpot: String? = null,
    // Errors are null when the field is fine
    val nameError: String? = null,
    val startTermError: String? = null,
    val gradTermError: String? = null,
    /** True while saving: the button shows a spinner and ignores taps. */
    val isSaving: Boolean = false,
    /** Why saving failed, shown above the button. */
    val saveError: String? = null,
) {
    companion object {
        /** The form filled in from a saved profile. */
        fun from(user: User) = EditProfileUiState(
            isLoading = false,
            name = user.name,
            major = user.major,
            year = user.year.ifBlank { null },
            startTerm = user.startTerm,
            gradTerm = user.gradTerm,
            bio = user.bio,
            meetSpot = user.meetSpot.ifBlank { null },
        )
    }
}