package edu.bu.terriertrade.ui.screens.auth

import edu.bu.terriertrade.testing.MainDispatcherRule
import edu.bu.terriertrade.testing.TestAuthRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * The Create account form: each field's validation, then what submit does (create the account, send
 * the email, or show an error).
 */
class SignUpViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val auth = TestAuthRepository()

    private fun filledIn() = SignUpViewModel(auth).apply {
        onNameChange("  Priya Shah ")
        onEmailChange("priya@bu.edu")
        onPasswordChange("terriers2026!")
        onConfirmPasswordChange("terriers2026!")
        onAgreeChange(true)
    }

    @Test
    fun completeForm_isValid() {
        assertTrue(filledIn().validate())
    }

    @Test
    fun gmail_showsBuEmailError() {
        val vm = filledIn().apply { onEmailChange("priya@gmail.com") }
        assertFalse(vm.validate())
        assertEquals("Use your @bu.edu email", vm.uiState.emailError)
    }

    @Test
    fun shortPassword_showsError() {
        val vm = filledIn().apply { onPasswordChange("short") }
        assertFalse(vm.validate())
        assertEquals("Use at least 8 characters", vm.uiState.passwordError)
    }

    @Test
    fun passwordWithoutSpecialCharacter_showsError() {
        val vm = filledIn().apply {
            onPasswordChange("terriers2026")
            onConfirmPasswordChange("terriers2026")
        }
        assertFalse(vm.validate())
        assertEquals("Add a special character, like ! or @", vm.uiState.passwordError)
    }

    @Test
    fun passwordsThatDontMatch_showError() {
        val vm = filledIn().apply { onConfirmPasswordChange("terriers2025") }
        assertFalse(vm.validate())
        assertEquals("Passwords don't match", vm.uiState.confirmPasswordError)
    }

    @Test
    fun changingPassword_clearsMatchError() {
        val vm = filledIn().apply { onConfirmPasswordChange("nope") }
        vm.validate()
        vm.onPasswordChange("terriers2027!")
        assertNull(vm.uiState.confirmPasswordError)
    }

    @Test
    fun emptyForm_showsEveryError() {
        val vm = SignUpViewModel(auth)
        assertFalse(vm.validate())
        with(vm.uiState) {
            assertEquals("Enter your name", nameError)
            assertEquals("Enter your BU email", emailError)
            assertEquals("Type your password again", confirmPasswordError)
            assertEquals("Agree to the community rules to continue", rulesError)
        }
    }

    @Test
    fun editingAField_clearsItsError() {
        val vm = SignUpViewModel(auth)
        vm.validate()
        vm.onEmailChange("priya@bu.edu")
        assertNull(vm.uiState.emailError)
    }

    @Test
    fun submit_validForm_createsAccountAndSendsEmail() {
        var created = false
        val vm = filledIn()

        vm.submit { created = true }

        assertTrue(created)
        assertEquals(1, auth.signUpCalls)
        assertEquals(1, auth.sendEmailCalls)
        assertEquals("Priya Shah", auth.lastSignUpName)
        assertFalse(vm.uiState.isSubmitting)
    }

    @Test
    fun submit_invalidForm_doesNotCallFirebase() {
        var created = false
        SignUpViewModel(auth).submit { created = true }

        assertFalse(created)
        assertEquals(0, auth.signUpCalls)
    }

    @Test
    fun submit_failure_showsMessageAndStays() {
        auth.signUpResult = Result.failure(Exception("There's already an account with this email. Sign in instead."))
        var created = false
        val vm = filledIn()

        vm.submit { created = true }

        assertFalse(created)
        assertEquals("There's already an account with this email. Sign in instead.", vm.uiState.submitError)
        assertFalse(vm.uiState.isSubmitting)
    }

    @Test
    fun emailFailsToSend_stillMovesOn() {
        auth.sendEmailResult = Result.failure(Exception("offline"))
        var created = false

        filledIn().submit { created = true }

        assertTrue(created)
    }
}
