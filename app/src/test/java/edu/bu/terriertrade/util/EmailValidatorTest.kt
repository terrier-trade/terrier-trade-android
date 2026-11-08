package edu.bu.terriertrade.util

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Sign-up only accepts @bu.edu addresses. These cover real BU emails, typing slips like spaces and
 * capitals, and look-alike domains that must be rejected.
 */
class EmailValidatorTest {

    @Test
    fun buEmail_isValid() {
        assertEquals(EmailCheck.Valid, EmailValidator.check("rhett@bu.edu"))
    }

    @Test
    fun buEmail_withSpacesAndCaps_isValid() {
        assertEquals(EmailCheck.Valid, EmailValidator.check("  Rhett.Terrier@BU.EDU "))
    }

    @Test
    fun blank_isEmpty() {
        assertEquals(EmailCheck.Empty, EmailValidator.check("   "))
    }

    @Test
    fun missingAt_isMalformed() {
        assertEquals(EmailCheck.Malformed, EmailValidator.check("rhett.bu.edu"))
    }

    @Test
    fun gmail_isNotBu() {
        assertEquals(EmailCheck.NotBuEmail, EmailValidator.check("rhett@gmail.com"))
    }

    @Test
    fun lookalikeDomains_areNotBu() {
        assertEquals(EmailCheck.NotBuEmail, EmailValidator.check("rhett@bu.edu.fake.com"))
        assertEquals(EmailCheck.NotBuEmail, EmailValidator.check("rhett@notbu.edu"))
    }
}