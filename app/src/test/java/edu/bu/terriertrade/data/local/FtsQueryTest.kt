package edu.bu.terriertrade.data.local

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** What the search bar text becomes in the FTS MATCH query. */
class FtsQueryTest {

    @Test
    fun everyWordBecomesAPrefix() {
        assertEquals("calc* text*", toFtsQuery("calc text"))
    }

    @Test
    fun caseAndExtraSpacesAreIgnored() {
        assertEquals("calculus*", toFtsQuery("   CALCULUS  "))
    }

    @Test
    fun punctuationSplitsWords() {
        assertEquals("cs* 330*", toFtsQuery("CS-330!"))
    }

    /** Quotes and operators can't change the query: "OR" is just a word, lowercased. */
    @Test
    fun ftsSyntaxIsStripped() {
        assertEquals("desk* or* chair*", toFtsQuery("\"desk\" OR chair*"))
    }

    @Test
    fun nonEnglishLettersAreKept() {
        assertEquals("café*", toFtsQuery("Café"))
    }

    @Test
    fun nothingToSearch_isNull() {
        assertNull(toFtsQuery(""))
        assertNull(toFtsQuery("   "))
        assertNull(toFtsQuery("!!! --"))
    }
}