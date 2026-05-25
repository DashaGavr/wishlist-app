package presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import data.WishlistRepository
import domain.Wishlist
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WishlistViewModel(
    private val repository: WishlistRepository
) : ViewModel() {

    val wishlists: StateFlow<UiState<List<Wishlist>>> = repository.getAll()
        .map<List<Wishlist>, UiState<List<Wishlist>>> { UiState.Success(it) }
        .catch { emit(UiState.Error(it.message ?: "Unknown error")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState.Loading)

    fun create(name: String, emoji: String) {
        viewModelScope.launch {
            repository.insert(Wishlist(id = 0, name = name, emoji = emoji))
        }
    }

    fun update(wishlist: Wishlist) {
        viewModelScope.launch { repository.update(wishlist) }
    }

    fun delete(wishlist: Wishlist) {
        viewModelScope.launch { repository.delete(wishlist) }
    }
}
