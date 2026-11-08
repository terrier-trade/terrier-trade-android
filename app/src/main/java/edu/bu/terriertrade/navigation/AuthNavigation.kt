package edu.bu.terriertrade.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import edu.bu.terriertrade.di.rememberAppContainer
import edu.bu.terriertrade.navigation.TopLevelDestination

/*
 * Small helpers so the sign-in flow can be wired with a few lines in navigation.
 * currentUserId is only set once the email is verified, so "signed in" here always means verified.
 */

/** Where the app opens: Browse when signed in and verified, otherwise Welcome. Decided once at launch. */
@Composable
fun rememberStartRoute(): String {
    val auth = rememberAppContainer().authRepository
    return remember { if (auth.currentUserId.value != null) Routes.BROWSE else Routes.WELCOME }
}

/** After signing out from anywhere, go back to Welcome so Back can't return to the old account. */
@Composable
fun SignedOutRedirect(navController: NavHostController) {
    val auth = rememberAppContainer().authRepository
    val userId by auth.currentUserId.collectAsStateWithLifecycle()
    var wasSignedIn by remember { mutableStateOf(userId != null) }

    LaunchedEffect(userId) {
        when {
            userId != null -> wasSignedIn = true
            wasSignedIn -> {
                wasSignedIn = false
                navController.navigate(Routes.WELCOME) {
                    popUpTo(navController.graph.id) { inclusive = true }
                    launchSingleTop = true
                }
                // popUpTo only clears the screens on the back stack. Tabs you left earlier were
                // saved by navigateToTopLevel (saveState = true), ViewModels included, and
                // restoreState would hand them to the next account. Drop them so the next
                // student starts with fresh tabs.
                TopLevelDestination.entries.forEach { navController.clearBackStack(it.route) }
            }
        }
    }
}

/** Opens Browse and clears the sign-in screens, so Back from Browse leaves the app. */
fun NavHostController.openHomeAfterSignIn() {
    navigate(Routes.BROWSE) {
        popUpTo(graph.id) { inclusive = true }
        launchSingleTop = true
    }
}
