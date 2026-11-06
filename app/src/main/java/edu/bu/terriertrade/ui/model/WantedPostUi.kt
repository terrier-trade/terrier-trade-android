package edu.bu.terriertrade.ui.model

/** A "Students are looking for" note, ready to display. */
data class WantedPostUi(
    val id: String,
    val title: String,
    val budgetLabel: String,
    val postedBy: String,
    val postedAgo: String,
)