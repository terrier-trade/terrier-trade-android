package edu.bu.terriertrade.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import edu.bu.terriertrade.di.rememberAppContainer
import edu.bu.terriertrade.navigation.Routes
import edu.bu.terriertrade.navigation.TTBottomBar
import edu.bu.terriertrade.navigation.TTNavHost
import edu.bu.terriertrade.navigation.TopLevelDestination
import edu.bu.terriertrade.navigation.navigateToTopLevel
import edu.bu.terriertrade.ui.components.TTAppHeader
import edu.bu.terriertrade.ui.components.TTOfflineBanner

/**
 * Root of the app. Bottom-bar tabs get the shared header and the bottom bar.
 * Screens opened on top of a tab (detail, chat, edit profile) fill the screen.
 */
@Composable
fun TerrierTradeApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val isTopLevel = TopLevelDestination.entries.any { it.route == currentRoute }

    val container = rememberAppContainer()
    val isOnline by container.networkMonitor.isOnline.collectAsStateWithLifecycle(initialValue = true)

    Scaffold(
        topBar = {
            if (isTopLevel) {
                // hasUnread is wired up by the notifications feature.
                TTAppHeader(onNotificationsClick = { navController.navigate(Routes.NOTIFICATIONS) })
            }
        },
        bottomBar = {
            if (isTopLevel) {
                TTBottomBar(
                    currentRoute = currentRoute,
                    onNavigate = { destination -> navController.navigateToTopLevel(destination) },
                )
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding),
        ) {
            if (!isOnline) TTOfflineBanner()
            TTNavHost(
                navController = navController,
                modifier = Modifier.weight(1f),
            )
        }
    }
}
