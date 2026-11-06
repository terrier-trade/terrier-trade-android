package edu.bu.terriertrade.ui.screens.auth

import edu.bu.terriertrade.util.MainDispatcherRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class VerifyEmailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val auth = TestAuthRepository()
    private val vm = VerifyEmailViewModel(auth)

    @Test
    fun verified_movesOn() {
        auth.verified = true
        var movedOn = false

        vm.checkVerified(onVerified = { movedOn = true })

        assertTrue(movedOn)
        assertNull(vm.uiState.errorMessage)
    }

    @Test
    fun notVerified_showsMessage() {
        var movedOn = false

        vm.checkVerified(onVerified = { movedOn = true })

        assertFalse(movedOn)
        assertEquals(AuthMessages.NOT_VERIFIED_YET, vm.uiState.errorMessage)
        assertFalse(vm.uiState.isChecking)
    }

    @Test
    fun silentCheck_notVerified_showsNothing() {
        vm.checkVerified(onVerified = {}, silent = true)

        assertNull(vm.uiState.errorMessage)
    }

    @Test
    fun resend_success_showsInfo() {
        vm.resend()

        assertEquals(1, auth.sendEmailCalls)
        assertEquals(AuthMessages.EMAIL_RESENT, vm.uiState.infoMessage)
        assertFalse(vm.uiState.isResending)
    }

    @Test
    fun resend_failure_showsError() {
        auth.sendEmailResult = Result.failure(Exception("Too many tries. Wait a few minutes and try again."))

        vm.resend()

        assertEquals("Too many tries. Wait a few minutes and try again.", vm.uiState.errorMessage)
        assertNull(vm.uiState.infoMessage)
    }

    @Test
    fun cancel_signsOut() {
        vm.cancel()

        assertEquals(1, auth.signOutCalls)
    }
    @Test
    fun verified_movesOnOnlyOnce() {
        auth.verified = true
        var moves = 0

        vm.checkVerified(onVerified = { moves++ }, silent = true)
        vm.checkVerified(onVerified = { moves++ }, silent = true)
        vm.checkVerified(onVerified = { moves++ })

        assertEquals(1, moves)
    }
}