package edu.bu.terriertrade.navigation.graphs

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import edu.bu.terriertrade.navigation.Routes
import edu.bu.terriertrade.ui.screens.PlaceholderScreen
import edu.bu.terriertrade.ui.screens.chats.ChatsScreen

/** Chat list, a single chat with its deal, and notifications. */
fun NavGraphBuilder.chatScreens(navController: NavHostController) {
    composable(Routes.CHATS) {
        ChatsScreen()
    }
    composable(
        route = Routes.CHAT,
        arguments = listOf(navArgument(Routes.ARG_CHAT_ID) { type = NavType.StringType }),
    ) { entry ->
        val chatId = entry.arguments?.getString(Routes.ARG_CHAT_ID).orEmpty()
        PlaceholderScreen(
            title = "Chat",
            message = "Messages and the deal for chat $chatId will show here.",
            onBack = { navController.popBackStack() },
        )
    }
    composable(Routes.NOTIFICATIONS) {
        PlaceholderScreen(title = "Notifications", onBack = { navController.popBackStack() })
    }
}
