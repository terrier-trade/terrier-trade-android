package edu.bu.terriertrade.ui.screens.profile

import edu.bu.terriertrade.ui.model.ListingUi
import edu.bu.terriertrade.ui.model.ProfileHeaderUi
import edu.bu.terriertrade.ui.model.ReviewUi

/** Everything another student's profile draws. */
data class UserProfileUiState(
    val isLoading: Boolean = true,
    val notFound: Boolean = false,
    val header: ProfileHeaderUi? = null,
    /** Their active listings. */
    val listings: List<ListingUi> = emptyList(),
    val reviews: List<ReviewUi> = emptyList(),
    val favoriteIds: Set<String> = emptySet(),
    /** True when you opened your own profile from a listing. Hides "Open chat". */
    val isMe: Boolean = false,
    val isOpeningChat: Boolean = false,
    /** Shown after "Open chat", like "Chat with Sam is ready in Chats." */
    val chatMessage: String? = null,
) {
    val firstName: String get() = header?.name?.trim()?.substringBefore(' ').orEmpty()
}
