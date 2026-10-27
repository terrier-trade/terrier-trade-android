package edu.bu.terriertrade.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import edu.bu.terriertrade.navigation.TTBottomBar
import edu.bu.terriertrade.navigation.TTNavHost
import edu.bu.terriertrade.navigation.navigateToTopLevel

/** Root of the app: bottom bar + the screen for the current route. */
@Composable
fun TerrierTradeApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            TTBottomBar(
                currentRoute = currentRoute,
                onNavigate = { destination -> navController.navigateToTopLevel(destination) },
            )
        },
    ) { innerPadding ->
        TTNavHost(
            navController = navController,
            modifier = Modifier.padding(innerPadding),
        )
    }
}