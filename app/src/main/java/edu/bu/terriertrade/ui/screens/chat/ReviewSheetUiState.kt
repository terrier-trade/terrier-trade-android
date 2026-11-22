package edu.bu.terriertrade.ui.screens.chat

import edu.bu.terriertrade.data.model.ReviewTag

/** The review sheet: closed, or open with what's been picked so far. */
data class ReviewSheetState(
    val isOpen: Boolean = false,
    /** For "How was trading with Maya?" */
    val partnerFirstName: String = "",
    /** 0 until a star is tapped. */
    val stars: Int = 0,
    val tags: Set<ReviewTag> = emptySet(),
    val text: String = "",
    /** True when ReviewRules would accept this review, so Send matches what the repository allows. */
    val canSend: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
)