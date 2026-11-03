package edu.bu.terriertrade.ui.screens.post

import edu.bu.terriertrade.data.books.BookInfo
import edu.bu.terriertrade.data.books.BookLookup

/** A BookLookup for tests: returns whatever result you set and remembers the ISBN it was asked for. */
class TestBookLookup(
    var result: Result<BookInfo> = Result.success(
        BookInfo(
            isbn = "9780262046305",
            title = "Introduction to Algorithms",
            authors = listOf("Thomas H. Cormen", "Charles E. Leiserson"),
            publishYear = "2022",
            coverUrl = "https://covers.openlibrary.org/b/id/1-M.jpg",
        ),
    ),
) : BookLookup {

    var lastIsbn: String? = null

    override suspend fun findByIsbn(isbn: String): Result<BookInfo> {
        lastIsbn = isbn
        return result
    }
}
