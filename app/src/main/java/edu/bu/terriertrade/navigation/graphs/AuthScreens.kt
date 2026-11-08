package edu.bu.terriertrade.navigation.graphs

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import edu.bu.terriertrade.navigation.Routes
import edu.bu.terriertrade.navigation.openHomeAfterSignIn
import edu.bu.terriertrade.ui.screens.auth.AuthRoute
import edu.bu.terriertrade.ui.screens.auth.AuthTab
import edu.bu.terriertrade.ui.screens.auth.VerifyEmailRoute

/**
 * Sign in / Create account, then email verification. Verified students land on Browse.
 * WELCOME is the first screen for signed-out students (see rememberStartRoute).
 */
fun NavGraphBuilder.authScreens(navController: NavHostController) {
    val onSignedIn: (Boolean) -> Unit = { isVerified ->
        if (isVerified) navController.openHomeAfterSignIn() else navController.navigate(Routes.VERIFY_EMAIL)
    }
    composable(Routes.WELCOME) { AuthRoute(onSignedIn = onSignedIn) }
    composable(Routes.SIGN_IN) { AuthRoute(onSignedIn = onSignedIn) }
    composable(Routes.SIGN_UP) { AuthRoute(onSignedIn = onSignedIn, initialTab = AuthTab.CREATE_ACCOUNT) }
    composable(Routes.VERIFY_EMAIL) {
        VerifyEmailRoute(
            onBack = { navController.popBackStack(Routes.WELCOME, inclusive = false) },
            onVerified = { navController.openHomeAfterSignIn() },
        )
    }
}
