package presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import data.WishlistRepository
import data.WishRepository
import domain.Wish
import domain.Wishlist
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WishlistDetailViewModel(
    private val wishlistRepository: WishlistRepository,
    private val wishRepository: WishRepository,
    val listId: Long
) : ViewModel() {

    val wishlist: StateFlow<Wishlist?> = wishlistRepository.getAll()
        .map { lists -> lists.find { it.id == listId } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val wishes: StateFlow<List<Wish>> = wishRepository.getByList(listId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun addWish(title: String) {
        viewModelScope.launch {
            val nextRank = (wishes.value.maxOfOrNull { it.rank } ?: 0.0) + 1.0
            wishRepository.insert(
                Wish(
                    id = 0,
                    title = title,
                    description = null,
                    imageUri = null,
                    link = null,
                    rank = nextRank,
                    listId = listId
                )
            )
        }
    }

    fun deleteWish(wish: Wish) {
        viewModelScope.launch { wishRepository.delete(wish) }
    }

    fun reorder(wish: Wish, newRank: Double) {
        viewModelScope.launch { wishRepository.update(wish.copy(rank = newRank)) }
    }
}
