package edu.bu.terriertrade.navigation.graphs

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import edu.bu.terriertrade.navigation.Routes
import edu.bu.terriertrade.ui.screens.placeholder.PlaceholderScreen
import edu.bu.terriertrade.ui.screens.profile.MyProfileRoute
import edu.bu.terriertrade.ui.screens.profile.UserProfileRoute

/** Your profile, editing it, and another student's profile. */
fun NavGraphBuilder.profileScreens(navController: NavHostController) {
    composable(Routes.PROFILE) {
        MyProfileRoute(
            onListingClick = { listingId -> navController.navigate(Routes.listingDetail(listingId)) },
        )
    }
    composable(Routes.EDIT_PROFILE) {
        PlaceholderScreen(title = "Edit profile", onBack = { navController.popBackStack() })
    }
    composable(
        route = Routes.USER_PROFILE,
        arguments = listOf(navArgument(Routes.ARG_USER_ID) { type = NavType.StringType }),
    ) { entry ->
        val userId = entry.arguments?.getString(Routes.ARG_USER_ID).orEmpty()
        UserProfileRoute(
            userId = userId,
            onBack = { navController.popBackStack() },
            onListingClick = { listingId -> navController.navigate(Routes.listingDetail(listingId)) },
            onOpenChat = { chatId -> navController.navigate(Routes.chat(chatId)) },
        )
    }
}