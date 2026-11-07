package edu.bu.terriertrade.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * The scanner passes along whatever barcode it reads. Only real ISBNs with a correct check digit
 * should reach Open Library.
 */
class IsbnTest {

    @Test
    fun validIsbn13_isKept() {
        assertEquals("9780262046305", Isbn.normalize("9780262046305"))
    }

    @Test
    fun dashesAndSpaces_areIgnored() {
        assertEquals("9780262046305", Isbn.normalize("978-0-262-04630 5"))
    }

    @Test
    fun isbn10_isConvertedTo13() {
        assertEquals("9780262033848", Isbn.normalize("0-262-03384-4"))
    }

    @Test
    fun isbn10_withXCheckDigit_isConverted() {
        assertEquals("9780804429573", Isbn.normalize("080442957X"))
    }

    @Test
    fun wrongCheckDigit_isRejected() {
        assertNull(Isbn.normalize("9780262046306"))
    }

    @Test
    fun productBarcode_isRejected() {
        // A valid EAN-13 that isn't a book (doesn't start with 978 or 979).
        assertNull(Isbn.normalize("5012345678900"))
    }

    @Test
    fun junk_isRejected() {
        assertNull(Isbn.normalize(""))
        assertNull(Isbn.normalize(null))
        assertNull(Isbn.normalize("12345"))
    }
}
