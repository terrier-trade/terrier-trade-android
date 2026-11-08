package edu.bu.terriertrade.data.repository

import edu.bu.terriertrade.data.model.BookInfo

/** Finds a book by its ISBN-13. Fails with a DataError when the book isn't found or can't be reached. */
interface BookLookup {
    suspend fun findByIsbn(isbn: String): Result<BookInfo>
}
