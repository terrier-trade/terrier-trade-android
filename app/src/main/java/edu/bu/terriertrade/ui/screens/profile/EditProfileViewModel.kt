package edu.bu.terriertrade.ui.screens.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import edu.bu.terriertrade.data.model.User
import edu.bu.terriertrade.data.repository.AuthRepository
import edu.bu.terriertrade.data.repository.UserRepository
import edu.bu.terriertrade.di.appContainer
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

/**
 * Edit profile: loads the signed-in student's profile once, lets them change it, and saves it.
 * Only the fields on the form change; email, verified, rating and trade count are kept as they are.
 */
class EditProfileViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
) : ViewModel() {

    var uiState by mutableStateOf(EditProfileUiState())
        private set

    // The profile as it was loaded. Saving copies the form onto it, so untouched fields survive.
    private var original: User? = null

    init {
        load()
    }

    /** Loads the profile (again, after an error). Read once, so a live update can't overwrite typing. */
    fun load() {
        val uid = authRepository.currentUserId.value
        if (uid == null) {
            uiState = uiState.copy(isLoading = false, loadError = "Sign in to edit your profile.")
            return
        }
        uiState = uiState.copy(isLoading = true, loadError = null)
        viewModelScope.launch {
            try {
                // A brand-new account may not have a profile document yet: start from a blank one.
                val user = userRepository.observeUser(uid).first() ?: User(id = uid, name = "")
                original = user
                uiState = EditProfileUiState.from(user)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                uiState = uiState.copy(isLoading = false, loadError = e.message ?: "Couldn't load your profile.")
            }
        }
    }

    fun onNameChange(value: String) {
        uiState = uiState.copy(name = value.take(MAX_NAME_LENGTH), nameError = null, saveError = null)
    }

    fun onMajorChange(value: String) {
        uiState = uiState.copy(major = value.take(MAX_MAJOR_LENGTH), saveError = null)
    }

    fun onYearSelect(value: String) {
        uiState = uiState.copy(year = value, saveError = null)
    }

    fun onStartTermChange(value: String) {
        uiState = uiState.copy(startTerm = value.take(MAX_TERM_LENGTH), startTermError = null, saveError = null)
    }

    fun onGradTermChange(value: String) {
        uiState = uiState.copy(gradTerm = value.take(MAX_TERM_LENGTH), gradTermError = null, saveError = null)
    }

    fun onBioChange(value: String) {
        uiState = uiState.copy(bio = value.take(MAX_BIO_LENGTH), saveError = null)
    }

    fun onMeetSpotSelect(value: String) {
        uiState = uiState.copy(meetSpot = value, saveError = null)
    }

    /** True when the form differs from the saved profile. Back asks before throwing these away. */
    fun hasUnsavedChanges(): Boolean {
        val saved = original ?: return false
        return applyForm(saved) != saved
    }

    /** Checks the form, saves it, then calls [onSaved] so the screen can close. */
    fun save(onSaved: () -> Unit) {
        val saved = original ?: return
        if (uiState.isSaving || !validate()) return

        uiState = uiState.copy(isSaving = true, saveError = null)
        viewModelScope.launch {
            val updated = applyForm(saved)
            userRepository.updateProfile(updated)
                .onSuccess {
                    original = updated
                    uiState = uiState.copy(isSaving = false)
                    onSaved()
                }
                .onFailure { error ->
                    uiState = uiState.copy(
                        isSaving = false,
                        saveError = error.message ?: "Couldn't save your profile. Try again.",
                    )
                }
        }
    }

    /** Shows an error under each field that needs fixing. Returns true when the form can be saved. */
    private fun validate(): Boolean {
        val form = uiState
        val nameError = if (form.name.isBlank()) "Add your name" else null
        val startTermError = termError(form.startTerm, example = "Fall 2024")
        val gradTermError = termError(form.gradTerm, example = "May 2027")
        uiState = form.copy(nameError = nameError, startTermError = startTermError, gradTermError = gradTermError)
        return nameError == null && startTermError == null && gradTermError == null
    }

    private fun applyForm(user: User): User = user.copy(
        name = uiState.name.trim(),
        major = uiState.major.trim(),
        year = uiState.year.orEmpty(),
        startTerm = uiState.startTerm.trim(),
        gradTerm = uiState.gradTerm.trim(),
        bio = uiState.bio.trim(),
        meetSpot = uiState.meetSpot.orEmpty(),
    )

    companion object {
        const val MAX_NAME_LENGTH = 50
        const val MAX_MAJOR_LENGTH = 60
        const val MAX_TERM_LENGTH = 20
        const val MAX_BIO_LENGTH = 200

        val YEARS: List<String> = listOf("First year", "Sophomore", "Junior", "Senior", "Grad")

        private val YEAR_NUMBER = Regex("\\b\\d{4}\\b")

        /** Terms are optional, but if filled in they need a year: the profile shows "Class of '27" from it. */
        internal fun termError(term: String, example: String): String? =
            if (term.isNotBlank() && !YEAR_NUMBER.containsMatchIn(term)) "Add the year, like $example" else null

        val Factory = viewModelFactory {
            initializer { EditProfileViewModel(appContainer.authRepository, appContainer.userRepository) }
        }
    }
}