package edu.bu.terriertrade.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import edu.bu.terriertrade.ui.screens.chats.ChatsScreen
import edu.bu.terriertrade.ui.screens.feed.FeedScreen
import edu.bu.terriertrade.ui.screens.post.PostListingScreen
import edu.bu.terriertrade.ui.screens.profile.ProfileScreen

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
        composable(Routes.FEED) { FeedScreen() }
        composable(Routes.POST) { PostListingScreen() }
        composable(Routes.CHATS) { ChatsScreen() }
        composable(Routes.PROFILE) { ProfileScreen() }
    }
}