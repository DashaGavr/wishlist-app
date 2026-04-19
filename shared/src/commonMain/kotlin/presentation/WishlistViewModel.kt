package presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import data.WishlistRepository
import domain.Wishlist
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WishlistViewModel(
    private val repository: WishlistRepository
) : ViewModel() {

    val wishlists: StateFlow<List<Wishlist>> = repository.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

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
