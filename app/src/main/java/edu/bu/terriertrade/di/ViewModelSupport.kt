package edu.bu.terriertrade.di

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import edu.bu.terriertrade.TerrierTradeApplication

/**
 * Gives a ViewModel factory access to the repositories:
 *
 * ```
 * companion object {
 *     val Factory = viewModelFactory {
 *         initializer { FeedViewModel(appContainer.listingRepository) }
 *     }
 * }
 * ```
 * Then in the screen: `viewModel: FeedViewModel = viewModel(factory = FeedViewModel.Factory)`.
 */
val CreationExtras.appContainer: AppContainer
    get() {
        val app = requireNotNull(this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]) {
            "No Application in CreationExtras"
        }
        return (app as TerrierTradeApplication).container
    }

/** The container from a Context, for the few places outside ViewModels that need it. */
val Context.appContainer: AppContainer
    get() = (applicationContext as TerrierTradeApplication).container

/** The container inside a composable. Prefer passing data through a ViewModel. */
@Composable
fun rememberAppContainer(): AppContainer = LocalContext.current.appContainer
