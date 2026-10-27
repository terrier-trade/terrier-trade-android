package edu.bu.terriertrade.navigation

import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController

/**
 * Switches bottom-bar tabs the standard way:
 * - doesn't pile up copies of the same tab on the back stack
 * - remembers each tab's scroll position and state when you come back
 * - pressing Back from any tab returns to Home, then exits
 */
fun NavHostController.navigateToTopLevel(destination: TopLevelDestination) {
    navigate(destination.route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}