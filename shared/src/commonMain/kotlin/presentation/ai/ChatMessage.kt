package presentation.ai

enum class Role { USER, ASSISTANT }

data class WishSuggestion(
    val title: String,
    val description: String,
    val link: String,
    val imageUrl: String
)

data class ChatMessage(
    val role: Role,
    val text: String,
    val wishes: List<WishSuggestion> = emptyList()
)
