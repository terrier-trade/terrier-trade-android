package edu.bu.terriertrade.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import edu.bu.terriertrade.ui.components.TTEmptyState
import edu.bu.terriertrade.ui.components.TTErrorState
import edu.bu.terriertrade.ui.components.TTLoadingState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

/** What every screen that loads data can be showing. */
sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>

    data class Success<out T>(val data: T) : UiState<T>

    data class Error(val message: String) : UiState<Nothing>
}

/** Wraps a repository Flow: Loading first, then Success for each value, or Error if it fails. */
fun <T> Flow<T>.asUiState(): Flow<UiState<T>> =
    map<T, UiState<T>> { UiState.Success(it) }
        .onStart { emit(UiState.Loading) }
        .catch { emit(UiState.Error(it.message ?: "Something went wrong")) }

/**
 * Shows the shared loading, error and empty components, and [content] once data is ready.
 * Pass [isEmpty] for lists, for example `isEmpty = { it.isEmpty() }`.
 */
@Composable
fun <T> UiStateContent(
    state: UiState<T>,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null,
    isEmpty: (T) -> Boolean = { false },
    emptyTitle: String = "Nothing here yet",
    emptyMessage: String? = null,
    content: @Composable (T) -> Unit,
) {
    when (state) {
        UiState.Loading -> TTLoadingState(modifier = modifier)
        is UiState.Error -> TTErrorState(modifier = modifier, message = state.message, onRetry = onRetry)
        is UiState.Success -> {
            if (isEmpty(state.data)) {
                TTEmptyState(title = emptyTitle, message = emptyMessage, modifier = modifier)
            } else {
                content(state.data)
            }
        }
    }
}
