package presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import data.AiChatRepository
import data.SettingsRepository
import data.WishRepository
import data.WishlistRepository
import domain.Wish
import domain.Wishlist
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import presentation.ai.ChatMessage
import presentation.ai.WishSuggestion

class AiChatViewModel(
    private val chatRepository: AiChatRepository,
    private val settingsRepository: SettingsRepository,
    wishlistRepository: WishlistRepository,
    private val wishRepository: WishRepository
) : ViewModel() {

    val messages: StateFlow<List<ChatMessage>> = chatRepository.messages

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val wishlists: StateFlow<List<Wishlist>> = wishlistRepository.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun sendMessage(text: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Read the key fresh each time — it may have been saved in Settings after this VM was created.
                chatRepository.sendMessage(text, settingsRepository.getApiKey())
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addWishToList(listId: Long, suggestion: WishSuggestion) {
        viewModelScope.launch {
            wishRepository.insert(
                Wish(
                    id = 0L,
                    title = suggestion.title,
                    description = suggestion.description.ifBlank { null },
                    link = suggestion.link.ifBlank { null },
                    imageUri = suggestion.imageUrl.ifBlank { null },
                    rank = 0.0,
                    listId = listId
                )
            )
        }
    }
}