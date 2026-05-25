package presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import data.WishRepository
import domain.Wish
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WishDetailViewModel(
    private val repository: WishRepository,
    private val wishId: Long          // 0L = new wish
) : ViewModel() {

    private val _wish = MutableStateFlow<UiState<Wish?>>(UiState.Loading)
    val wish: StateFlow<UiState<Wish?>> = _wish.asStateFlow()

    init {
        if (wishId != 0L) {
            viewModelScope.launch {
                try {
                    _wish.value = UiState.Success(repository.getById(wishId))
                } catch (e: Exception) {
                    _wish.value = UiState.Error(e.message ?: "Unknown error")
                }
            }
        } else {
            _wish.value = UiState.Success(null)
        }
    }

    fun save(wish: Wish) {
        viewModelScope.launch {
            try {
                if (wish.id == 0L) repository.insert(wish)
                else repository.update(wish)
                _wish.value = UiState.Success(wish)
            } catch (e: Exception) {
                _wish.value = UiState.Error(e.message ?: "Save failed")
            }
        }
    }

    fun delete() {
        viewModelScope.launch {
            val current = (_wish.value as? UiState.Success)?.data ?: return@launch
            try {
                repository.delete(current)
                _wish.value = UiState.Success(null)
            } catch (e: Exception) {
                _wish.value = UiState.Error(e.message ?: "Delete failed")
            }
        }
    }
}
