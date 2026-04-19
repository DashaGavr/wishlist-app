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

    private val _wish = MutableStateFlow<Wish?>(null)
    val wish: StateFlow<Wish?> = _wish.asStateFlow()

    init {
        if (wishId != 0L) {
            viewModelScope.launch {
                _wish.value = repository.getById(wishId)
            }
        }
    }

    fun save(wish: Wish) {
        viewModelScope.launch {
            if (wish.id == 0L) repository.insert(wish)
            else repository.update(wish)
            _wish.value = wish
        }
    }

    fun delete() {
        viewModelScope.launch {
            _wish.value?.let { repository.delete(it) }
            _wish.value = null
        }
    }
}
