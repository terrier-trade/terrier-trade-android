package edu.bu.terriertrade.navigation.graphs

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import edu.bu.terriertrade.navigation.Routes
import edu.bu.terriertrade.ui.screens.PlaceholderScreen
import edu.bu.terriertrade.ui.screens.welcome.WelcomeScreen

/** Welcome, sign in, sign up and email verification. */
fun NavGraphBuilder.authScreens(navController: NavHostController) {
    composable(Routes.WELCOME) {
        WelcomeScreen(
            onGetStarted = { navController.navigate(Routes.SIGN_UP) },
            onSignIn = { navController.navigate(Routes.SIGN_IN) },
        )
    }
    composable(Routes.SIGN_IN) {
        PlaceholderScreen(title = "Sign in", onBack = { navController.popBackStack() })
    }
    composable(Routes.SIGN_UP) {
        PlaceholderScreen(title = "Create account", onBack = { navController.popBackStack() })
    }
    composable(Routes.VERIFY_EMAIL) {
        PlaceholderScreen(title = "Verify your email", onBack = { navController.popBackStack() })
    }
}
