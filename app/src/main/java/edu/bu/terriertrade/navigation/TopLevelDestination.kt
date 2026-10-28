package edu.bu.terriertrade.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.ui.graphics.vector.ImageVector

/** Tabs shown in the bottom bar, in display order (matches the prototype). */
enum class TopLevelDestination(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
) {
    FEED(Routes.FEED, "Browse", Icons.Filled.Home, Icons.Outlined.Home),
    POST(Routes.POST, "Sell", Icons.Filled.AddCircle, Icons.Outlined.AddCircle),
    SKILLS(Routes.SKILLS, "Skills", Icons.Filled.Refresh, Icons.Outlined.Refresh),
    CHATS(Routes.CHATS, "Chats", Icons.Filled.Email, Icons.Outlined.Email),
    PROFILE(Routes.PROFILE, "Profile", Icons.Filled.Person, Icons.Outlined.Person),
}