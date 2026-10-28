package edu.bu.terriertrade.navigation

/** Every screen's route string lives here. Never type a route string anywhere else. */
object Routes {
    // Bottom bar tabs
    const val FEED = "feed"
    const val POST = "post"
    const val SKILLS = "skills"
    const val CHATS = "chats"
    const val PROFILE = "profile"

    // Sign-in flow
    const val WELCOME = "welcome"
    const val SIGN_IN = "sign_in"
    const val SIGN_UP = "sign_up"
    const val VERIFY_EMAIL = "verify_email"

    // Screens opened on top of a tab
    const val NOTIFICATIONS = "notifications"
    const val EDIT_PROFILE = "edit_profile"

    // Screens that need an id. Navigate with the helper functions below.
    const val ARG_LISTING_ID = "listingId"
    const val ARG_CHAT_ID = "chatId"
    const val ARG_USER_ID = "userId"

    const val LISTING_DETAIL = "listing/{listingId}"
    const val CHAT = "chat/{chatId}"
    const val USER_PROFILE = "user/{userId}"

    fun listingDetail(listingId: String) = "listing/$listingId"
    fun chat(chatId: String) = "chat/$chatId"
    fun userProfile(userId: String) = "user/$userId"
}
