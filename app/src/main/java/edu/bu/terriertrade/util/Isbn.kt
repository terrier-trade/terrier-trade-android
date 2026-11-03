package edu.bu.terriertrade.util

/**
 * Checks ISBNs from a barcode or typed by hand, and turns them into ISBN-13.
 * Plain Kotlin with no Android imports, so it runs in fast unit tests.
 */
object Isbn {

    /** Returns the ISBN-13 for a scanned or typed code, or null when it isn't a valid ISBN. Dashes and spaces are ignored. */
    fun normalize(raw: String?): String? {
        val code = raw?.uppercase()?.filter { it.isDigit() || it == 'X' } ?: return null
        return when (code.length) {
            13 -> code.takeIf { (it.startsWith("978") || it.startsWith("979")) && isValid13(it) }
            10 -> code.takeIf(::isValid10)?.let(::tenToThirteen)
            else -> null
        }
    }

    private fun isValid13(code: String): Boolean =
        code.all(Char::isDigit) && weightedSum13(code) % 10 == 0

    private fun isValid10(code: String): Boolean {
        if (!code.take(9).all(Char::isDigit)) return false
        val sum = code.mapIndexed { i, ch ->
            val value = if (ch == 'X') { if (i != 9) return false; 10 } else ch - '0'
            value * (10 - i)
        }.sum()
        return sum % 11 == 0
    }

    private fun tenToThirteen(code: String): String {
        val base = "978" + code.take(9)
        val check = (10 - weightedSum13(base) % 10) % 10
        return base + check
    }

    /** ISBN-13 weights digits 1, 3, 1, 3… */
    private fun weightedSum13(digits: String): Int =
        digits.mapIndexed { i, ch -> (ch - '0') * if (i % 2 == 0) 1 else 3 }.sum()
}
