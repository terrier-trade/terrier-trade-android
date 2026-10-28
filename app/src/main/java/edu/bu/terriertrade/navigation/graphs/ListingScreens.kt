package edu.bu.terriertrade.navigation.graphs

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import edu.bu.terriertrade.navigation.Routes
import edu.bu.terriertrade.ui.screens.PlaceholderScreen
import edu.bu.terriertrade.ui.screens.feed.FeedScreen
import edu.bu.terriertrade.ui.screens.post.PostListingScreen
import edu.bu.terriertrade.ui.screens.skills.SkillsScreen

/** Browse, posting and the listing detail screen. */
fun NavGraphBuilder.listingScreens(navController: NavHostController) {
    composable(Routes.FEED) {
        FeedScreen()
    }
    composable(Routes.POST) {
        PostListingScreen()
    }
    composable(Routes.SKILLS) {
        SkillsScreen()
    }
    composable(
        route = Routes.LISTING_DETAIL,
        arguments = listOf(navArgument(Routes.ARG_LISTING_ID) { type = NavType.StringType }),
    ) { entry ->
        val listingId = entry.arguments?.getString(Routes.ARG_LISTING_ID).orEmpty()
        PlaceholderScreen(
            title = "Listing",
            message = "Details for listing $listingId will show here.",
            onBack = { navController.popBackStack() },
        )
    }
}
