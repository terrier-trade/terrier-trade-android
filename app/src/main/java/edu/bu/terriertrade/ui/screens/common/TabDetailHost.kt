package edu.bu.terriertrade.ui.screens.common

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.viewmodel.compose.viewModel
import edu.bu.terriertrade.ui.screens.listing.ListingDetailRoute
import edu.bu.terriertrade.ui.screens.listing.ListingDetailViewModel
import edu.bu.terriertrade.ui.screens.profile.UserProfileRoute
import edu.bu.terriertrade.ui.screens.profile.UserProfileViewModel

/** What can be opened on top of a tab: a listing, or a student's profile. */
sealed interface TabDetail {
    data class Listing(val id: String) : TabDetail
    data class User(val id: String) : TabDetail
}

/**
 * Shows a tab's own screen, and lets it open listings and profiles on top, with Back going one step back.
 * A small stand-in for navigation, so the tabs work before the real routes are wired up.
 * TODO(nav): navigation can open Routes.listingDetail / Routes.userProfile instead, then this file can go.
 */
@Composable
fun TabDetailHost(
    root: @Composable (open: (TabDetail) -> Unit) -> Unit,
) {
    val stack = rememberSaveable(saver = TabDetailStackSaver) { mutableStateListOf<TabDetail>() }
    val open: (TabDetail) -> Unit = { stack.add(it) }
    val pop: () -> Unit = { if (stack.isNotEmpty()) stack.removeAt(stack.lastIndex) }

    when (val top = stack.lastOrNull()) {
        null -> root(open)
        is TabDetail.Listing -> {
            BackHandler(onBack = pop)
            ListingDetailRoute(
                listingId = top.id,
                onBack = pop,
                onOpenChat = { /* Connected to the chat screen later */ },
                onSellerClick = { open(TabDetail.User(it)) },
                // One ViewModel per page, so going back shows the previous page straight away.
                viewModel = viewModel(key = "listing:${top.id}", factory = ListingDetailViewModel.Factory),
            )
        }
        is TabDetail.User -> {
            BackHandler(onBack = pop)
            UserProfileRoute(
                userId = top.id,
                onBack = pop,
                onListingClick = { open(TabDetail.Listing(it)) },
                onOpenChat = { /* Connected to the chat screen later */ },
                viewModel = viewModel(key = "user:${top.id}", factory = UserProfileViewModel.Factory),
            )
        }
    }
}

/** Saves the stack as strings like "listing:l1" and "user:sam", so it survives rotation. */
private val TabDetailStackSaver = listSaver<SnapshotStateList<TabDetail>, String>(
    save = { stack ->
        stack.map {
            when (it) {
                is TabDetail.Listing -> "listing:${it.id}"
                is TabDetail.User -> "user:${it.id}"
            }
        }
    },
    restore = { saved ->
        mutableStateListOf<TabDetail>().apply {
            saved.forEach { entry ->
                val id = entry.substringAfter(':')
                add(if (entry.startsWith("user:")) TabDetail.User(id) else TabDetail.Listing(id))
            }
        }
    },
)
