package edu.bu.terriertrade.data.books

/** Finds a book by its ISBN-13. Fails with BookNotFound when nobody has heard of it. */
interface BookLookup {
    suspend fun findByIsbn(isbn: String): Result<BookInfo>
}

class BookNotFound(isbn: String) : Exception("We couldn't find ISBN $isbn. Fill in the details yourself.")
