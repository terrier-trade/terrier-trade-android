package edu.bu.terriertrade.navigation.graphs

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import edu.bu.terriertrade.navigation.Routes
import edu.bu.terriertrade.ui.screens.chat.ChatRoute
import edu.bu.terriertrade.ui.screens.chats.ChatsRoute
import edu.bu.terriertrade.ui.screens.placeholder.PlaceholderScreen

/** Chat list, a single chat with its deal, and notifications. */
fun NavGraphBuilder.chatScreens(navController: NavHostController) {
    composable(Routes.CHATS) {
        ChatsRoute(
            onOpenChat = { chatId -> navController.navigate(Routes.chat(chatId)) },
        )
    }
    composable(
        route = Routes.CHAT,
        arguments = listOf(navArgument(Routes.ARG_CHAT_ID) { type = NavType.StringType }),
    ) {
        ChatRoute(
            onBack = { navController.popBackStack() },
            onOpenProfile = { userId -> navController.navigate(Routes.userProfile(userId)) },
            onOpenDeal = { /* The deal sheet comes with the deal window branch */ },
        )
    }
    composable(Routes.NOTIFICATIONS) {
        PlaceholderScreen(title = "Notifications", onBack = { navController.popBackStack() })
    }
}
