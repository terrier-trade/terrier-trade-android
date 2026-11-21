package edu.bu.terriertrade.ui.screens.profile

import edu.bu.terriertrade.data.fake.FakeAuthRepository
import edu.bu.terriertrade.data.fake.FakeUserRepository
import edu.bu.terriertrade.data.model.User
import edu.bu.terriertrade.data.repository.DataError
import edu.bu.terriertrade.data.repository.UserRepository
import edu.bu.terriertrade.testing.MainDispatcherRule
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Edit profile: the form starts from the saved profile, checks name and terms, saves only the
 * fields on the form, and handles load and save failures. Signed in as "alex" from SampleData.
 */
class EditProfileViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val auth = FakeAuthRepository()
    private val users = FlakyUserRepository()
    private fun viewModel() = EditProfileViewModel(auth, users)

    @Test
    fun load_fillsTheFormFromTheProfile() {
        val vm = viewModel()

        assertFalse(vm.uiState.isLoading)
        assertEquals("Alex Morgan", vm.uiState.name)
        assertEquals("Computer Science", vm.uiState.major)
        assertEquals("Junior", vm.uiState.year)
        assertEquals("May 2027", vm.uiState.gradTerm)
        assertEquals("GSU", vm.uiState.meetSpot)
    }

    @Test
    fun save_writesTheEditedFieldsAndKeepsTheRest() = runTest {
        val vm = viewModel()
        vm.onNameChange("  Alex M.  ")
        vm.onYearSelect("Senior")
        vm.onBioChange("Selling my desk chair before winter break.")
        var closed = false

        vm.save { closed = true }

        assertTrue(closed)
        val saved = users.observeUser("alex").first()!!
        assertEquals("Alex M.", saved.name)
        assertEquals("Senior", saved.year)
        assertEquals("Selling my desk chair before winter break.", saved.bio)
        // Not on the form, so unchanged
        assertEquals("alex@bu.edu", saved.email)
        assertTrue(saved.verified)
        assertEquals(12, saved.reviewCount)
    }

    @Test
    fun blankName_showsErrorAndDoesNotSave() {
        val vm = viewModel()
        vm.onNameChange("   ")
        var closed = false

        vm.save { closed = true }

        assertEquals("Add your name", vm.uiState.nameError)
        assertFalse(closed)
        assertEquals(0, users.saveCalls)
    }

    @Test
    fun termWithoutYear_showsError() {
        val vm = viewModel()
        vm.onGradTermChange("May")

        vm.save {}

        assertEquals("Add the year, like May 2027", vm.uiState.gradTermError)
        assertEquals(0, users.saveCalls)
    }

    @Test
    fun typingClearsTheFieldError() {
        val vm = viewModel()
        vm.onNameChange("")
        vm.save {}

        vm.onNameChange("A")

        assertNull(vm.uiState.nameError)
    }

    @Test
    fun emptyTerms_areAllowed() {
        val vm = viewModel()
        vm.onStartTermChange("")
        vm.onGradTermChange("")
        var closed = false

        vm.save { closed = true }

        assertTrue(closed)
    }

    @Test
    fun hasUnsavedChanges_onlyAfterARealEdit() {
        val vm = viewModel()
        assertFalse(vm.hasUnsavedChanges())

        // Extra spaces are trimmed on save, so they don't count as a change
        vm.onNameChange("Alex Morgan  ")
        assertFalse(vm.hasUnsavedChanges())

        vm.onMeetSpotSelect("Mugar Library")
        assertTrue(vm.hasUnsavedChanges())
    }

    @Test
    fun longBio_isCutAtTheLimit() {
        val vm = viewModel()

        vm.onBioChange("a".repeat(EditProfileViewModel.MAX_BIO_LENGTH + 50))

        assertEquals(EditProfileViewModel.MAX_BIO_LENGTH, vm.uiState.bio.length)
    }

    @Test
    fun saveFails_keepsTheEditsAndShowsWhy() {
        users.saveResult = Result.failure(DataError("No connection. Check it and try again."))
        val vm = viewModel()
        vm.onNameChange("Alex M.")
        var closed = false

        vm.save { closed = true }

        assertFalse(closed)
        assertFalse(vm.uiState.isSaving)
        assertEquals("No connection. Check it and try again.", vm.uiState.saveError)
        assertEquals("Alex M.", vm.uiState.name)
    }

    @Test
    fun loadFails_showsErrorAndRetryRecovers() {
        users.loadError = DataError("You're offline.")
        val vm = viewModel()
        assertEquals("You're offline.", vm.uiState.loadError)

        users.loadError = null
        vm.load()

        assertNull(vm.uiState.loadError)
        assertEquals("Alex Morgan", vm.uiState.name)
    }

    @Test
    fun signedOut_showsError() {
        auth.signOut()

        val vm = viewModel()

        assertNotNull(vm.uiState.loadError)
        assertFalse(vm.uiState.isLoading)
    }

    /** The sample users, plus switches to make loading or saving fail. Counts saves. */
    private class FlakyUserRepository(private val real: UserRepository = FakeUserRepository()) : UserRepository {
        var loadError: Throwable? = null
        var saveResult: Result<Unit>? = null
        var saveCalls = 0

        override fun observeUser(userId: String): Flow<User?> = flow {
            loadError?.let { throw it }
            emitAll(real.observeUser(userId))
        }

        override suspend fun updateProfile(user: User): Result<Unit> {
            saveCalls++
            return saveResult ?: real.updateProfile(user)
        }
    }
}