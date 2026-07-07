package data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import presentation.ai.ChatMessage
import presentation.ai.Role
import presentation.ai.WishSuggestion

class AiChatRepository(private val apiService: ClaudeApiService) {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val systemPrompt = """
        You are a gift discovery assistant. When the user describes what they want,
        find 2-4 specific real products matching their request.
        Always reply with a short friendly sentence, then a JSON block in this exact format:
        Found X great options! 🎁
        ```json
        [{"title":"Product Name","description":"Brief description","link":"https://example.com","imageUrl":"https://example.com/image.jpg"}]
        ```
        If no relevant products exist, reply with your sentence then:
        ```json
        []
        ```
        Use real product names and actual website URLs.
    """.trimIndent()

    suspend fun sendMessage(userText: String, apiKey: String) {
        addMessage(ChatMessage(role = Role.USER, text = userText))

        if (apiKey.isBlank()) {
            addMessage(assistantError("Please add your Anthropic API key in Settings first."))
            return
        }

        try {
            val history = _messages.value.map { msg ->
                msg.role.name.lowercase() to msg.text
            }
            val raw = apiService.sendMessage(apiKey, systemPrompt, history)
            if (raw.isBlank()) {
                addMessage(assistantError("Claude sent an empty response. Try again."))
            } else {
                addMessage(parseAssistantMessage(raw))
            }
        } catch (e: Exception) {
            addMessage(assistantError("Couldn't reach Claude. Check your API key and internet connection."))
        }
    }

    fun clearHistory() {
        _messages.value = emptyList()
    }

    private fun addMessage(message: ChatMessage) {
        _messages.value = _messages.value + message
    }

    private fun assistantError(text: String) = ChatMessage(role = Role.ASSISTANT, text = text)

    private fun parseAssistantMessage(raw: String): ChatMessage {
        val parts = raw.split("```json", ignoreCase = true)
        val text = parts[0].trim().ifBlank { "Here are some suggestions!" }
        val wishes = if (parts.size > 1) {
            parseWishes(parts[1].substringBefore("```").trim())
        } else {
            emptyList()
        }
        return ChatMessage(role = Role.ASSISTANT, text = text, wishes = wishes)
    }

    private fun parseWishes(jsonString: String): List<WishSuggestion> {
        return try {
            Json.parseToJsonElement(jsonString).jsonArray.map { element ->
                val obj = element.jsonObject
                WishSuggestion(
                    title = obj["title"]?.jsonPrimitive?.content ?: "",
                    description = obj["description"]?.jsonPrimitive?.content ?: "",
                    link = obj["link"]?.jsonPrimitive?.content ?: "",
                    imageUrl = obj["imageUrl"]?.jsonPrimitive?.content ?: ""
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}