package edu.bu.terriertrade.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import edu.bu.terriertrade.navigation.graphs.authScreens
import edu.bu.terriertrade.navigation.graphs.chatScreens
import edu.bu.terriertrade.navigation.graphs.listingScreens
import edu.bu.terriertrade.navigation.graphs.profileScreens

/**
 * All screens, grouped by feature. Each group lives in its own file under navigation/graphs,
 * so a feature branch only edits its own group and this file rarely changes.
 */
@Composable
fun TTNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = Routes.FEED,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        authScreens(navController)
        listingScreens(navController)
        chatScreens(navController)
        profileScreens(navController)
    }
}
