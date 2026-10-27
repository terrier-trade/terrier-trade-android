package edu.bu.terriertrade.navigation

import android.content.res.Configuration
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import edu.bu.terriertrade.ui.components.TTPreviewColumn

/**
 * Bottom navigation bar. Stateless: the caller passes the [currentRoute]
 * and decides what happens in [onNavigate].
 */
@Composable
fun TTBottomBar(
    currentRoute: String?,
    onNavigate: (TopLevelDestination) -> Unit,
    modifier: Modifier = Modifier,
    destinations: List<TopLevelDestination> = TopLevelDestination.entries,
) {
    NavigationBar(modifier = modifier) {
        destinations.forEach { destination ->
            val selected = currentRoute == destination.route
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(destination) },
                icon = {
                    Icon(
                        imageVector = if (selected) destination.selectedIcon else destination.unselectedIcon,
                        // The label below already describes the tab for TalkBack.
                        contentDescription = null,
                    )
                },
                label = { Text(destination.label) },
            )
        }
    }
}

@Preview(name = "Light", widthDp = 360)
@Preview(name = "Dark", widthDp = 360, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TTBottomBarPreview() {
    TTPreviewColumn {
        TTBottomBar(currentRoute = Routes.FEED, onNavigate = {})
        TTBottomBar(currentRoute = Routes.CHATS, onNavigate = {})
    }
}