package edu.bu.terriertrade.data.model

/** What we know about a book from its ISBN. Used to fill in a textbook listing. */
data class BookInfo(
    val isbn: String,
    val title: String,
    val authors: List<String> = emptyList(),
    val publishYear: String? = null,
    val coverUrl: String? = null,
) {
    /** "Thomas H. Cormen, Charles E. Leiserson · 2022", or blank when we know neither. */
    val authorLine: String
        get() = listOfNotNull(
            authors.takeIf { it.isNotEmpty() }?.joinToString(", "),
            publishYear,
        ).joinToString(" · ")
}
