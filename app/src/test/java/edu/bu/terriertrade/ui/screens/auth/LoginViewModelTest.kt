package edu.bu.terriertrade.ui.screens.auth

import edu.bu.terriertrade.util.MainDispatcherRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class LoginViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val auth = TestAuthRepository()

    private fun filledIn() = LoginViewModel(auth).apply {
        onEmailChange("priya02@bu.edu")
        onPasswordChange("terriers2026")
    }

    @Test
    fun emptyForm_showsErrors_andDoesNotCallFirebase() {
        val vm = LoginViewModel(auth)

        vm.submit { }

        assertEquals("Enter your BU email", vm.uiState.emailError)
        assertEquals(AuthMessages.ENTER_PASSWORD, vm.uiState.passwordError)
        assertEquals(0, auth.signInCalls)
    }

    @Test
    fun nonBuEmail_showsError() {
        val vm = filledIn().apply { onEmailChange("priya@gmail.com") }

        vm.submit { }

        assertEquals("Use your @bu.edu email", vm.uiState.emailError)
        assertEquals(0, auth.signInCalls)
    }

    @Test
    fun verifiedStudent_reportsVerified() {
        auth.verified = true
        var result: Boolean? = null

        filledIn().submit { result = it }

        assertEquals(true, result)
    }

    @Test
    fun unverifiedStudent_reportsNotVerified() {
        var result: Boolean? = null

        filledIn().submit { result = it }

        assertEquals(false, result)
    }

    @Test
    fun wrongPassword_showsMessage() {
        auth.signInResult = Result.failure(Exception("Wrong email or password."))
        var result: Boolean? = null
        val vm = filledIn()

        vm.submit { result = it }

        assertNull(result)
        assertEquals("Wrong email or password.", vm.uiState.submitError)
        assertFalse(vm.uiState.isSubmitting)
    }

    @Test
    fun typing_clearsSubmitError() {
        auth.signInResult = Result.failure(Exception("Wrong email or password."))
        val vm = filledIn()
        vm.submit { }

        vm.onPasswordChange("terriers2027")

        assertNull(vm.uiState.submitError)
    }

    @Test
    fun forgotPassword_sendsResetToTheBuEmail() {
        val reset = TestPasswordReset()
        val vm = LoginViewModel(auth, reset).apply { onEmailChange("priya02@bu.edu ") }

        vm.onForgotPassword()

        assertEquals(listOf("priya02@bu.edu"), reset.sentTo)
        assertEquals(AuthMessages.resetSent("priya02@bu.edu"), vm.uiState.infoMessage)
        assertFalse(vm.uiState.isSendingReset)
    }

    @Test
    fun forgotPassword_withoutEmail_asksForItFirst() {
        val reset = TestPasswordReset()
        val vm = LoginViewModel(auth, reset)

        vm.onForgotPassword()

        assertEquals(AuthMessages.ENTER_EMAIL_FOR_RESET, vm.uiState.emailError)
        assertEquals(0, reset.sentTo.size)
    }

    @Test
    fun forgotPassword_nonBuEmail_isNotSent() {
        val reset = TestPasswordReset()
        val vm = LoginViewModel(auth, reset).apply { onEmailChange("priya@gmail.com") }

        vm.onForgotPassword()

        assertEquals("Use your @bu.edu email", vm.uiState.emailError)
        assertEquals(0, reset.sentTo.size)
    }

    @Test
    fun forgotPassword_failure_showsMessage() {
        val reset = TestPasswordReset().apply { result = Result.failure(Exception("No internet connection. Check it and try again.")) }
        val vm = LoginViewModel(auth, reset).apply { onEmailChange("priya02@bu.edu") }

        vm.onForgotPassword()

        assertEquals("No internet connection. Check it and try again.", vm.uiState.submitError)
        assertNull(vm.uiState.infoMessage)
    }
}
