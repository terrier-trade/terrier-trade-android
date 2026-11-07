package edu.bu.terriertrade.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** The password rules the sign-up form checks before anything is sent to Firebase. */
class PasswordRulesTest {

    @Test
    fun short_isTooShort() {
        assertEquals("Use at least 8 characters", PasswordRules.problem("ab!1"))
    }

    @Test
    fun longButNoSpecial_needsOne() {
        assertEquals("Add a special character, like ! or @", PasswordRules.problem("terriers2026"))
    }

    @Test
    fun spacesDontCountAsSpecial() {
        assertEquals("Add a special character, like ! or @", PasswordRules.problem("terriers 2026"))
    }

    @Test
    fun withSpecial_isFine() {
        assertNull(PasswordRules.problem("terriers2026!"))
        assertNull(PasswordRules.problem("go@terriers"))
    }
}
