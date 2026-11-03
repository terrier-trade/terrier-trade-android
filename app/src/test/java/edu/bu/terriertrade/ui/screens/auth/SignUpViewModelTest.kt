package edu.bu.terriertrade.ui.screens.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SignUpViewModelTest {

    private fun filledIn() = SignUpViewModel().apply {
        onNameChange("Priya Shah")
        onEmailChange("priya@bu.edu")
        onPasswordChange("terriers2026")
        onYearSelect(ClassYear.JUNIOR)
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
    fun emptyForm_showsEveryError() {
        val vm = SignUpViewModel()
        assertFalse(vm.validate())
        with(vm.uiState) {
            assertEquals("Enter your name", nameError)
            assertEquals("Enter your BU email", emailError)
            assertEquals("Pick your year", yearError)
            assertEquals("Agree to the community rules to continue", rulesError)
        }
    }

    @Test
    fun editingAField_clearsItsError() {
        val vm = SignUpViewModel()
        vm.validate()
        vm.onEmailChange("priya@bu.edu")
        assertNull(vm.uiState.emailError)
    }
}