package presentation

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

sealed class UiState<out T> {
    data object Loading : UiState<Nothing>()
    data class Success<out T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}

fun <T> Flow<T>.asUiState(scope: CoroutineScope): StateFlow<UiState<T>> =
    map<T, UiState<T>> { UiState.Success(it) }
        .catch { emit(UiState.Error(it.message ?: "Unknown error")) }
        .stateIn(scope, SharingStarted.WhileSubscribed(5_000), UiState.Loading)
