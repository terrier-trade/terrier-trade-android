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
    /** Why opening the chat failed, shown above the button. Null when there's nothing to show. */
    val chatError: String? = null,
    /** One-time signal to open this chat. The screen opens it, then calls onChatOpened(). */
    val openChatId: String? = null,
) {
    val firstName: String get() = header?.name?.trim()?.substringBefore(' ').orEmpty()
}
