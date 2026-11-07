package edu.bu.terriertrade.navigation.graphs

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import edu.bu.terriertrade.navigation.Routes
import edu.bu.terriertrade.ui.screens.browse.BrowseRoute
import edu.bu.terriertrade.ui.screens.listing.ListingDetailRoute
import edu.bu.terriertrade.ui.screens.post.NewListingRoute

/** Browse, posting and the listing detail screen. */
fun NavGraphBuilder.listingScreens(navController: NavHostController) {
    composable(Routes.FEED) {
        BrowseRoute(
            onListingClick = { listingId -> navController.navigate(Routes.listingDetail(listingId)) },
            onOpenChat = { chatId -> navController.navigate(Routes.chat(chatId)) },
            // The app frame already draws the red header on tabs, so Browse leaves it out
            showAppHeader = false,
        )
    }
    composable(Routes.POST) {
        NewListingRoute(
            // After posting, show the new listing
            onPosted = { listingId -> navController.navigate(Routes.listingDetail(listingId)) },
        )
    }
    composable(
        route = Routes.LISTING_DETAIL,
        arguments = listOf(navArgument(Routes.ARG_LISTING_ID) { type = NavType.StringType }),
    ) { entry ->
        val listingId = entry.arguments?.getString(Routes.ARG_LISTING_ID).orEmpty()
        ListingDetailRoute(
            listingId = listingId,
            onBack = { navController.popBackStack() },
            onOpenChat = { chatId -> navController.navigate(Routes.chat(chatId)) },
            onSellerClick = { sellerId -> navController.navigate(Routes.userProfile(sellerId)) },
        )
    }
}