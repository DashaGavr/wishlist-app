# AI Chat (Subproject 4) Design

## Goal

Build an AI-powered gift discovery assistant: users describe what they want, Claude returns structured wish suggestions as tappable cards, and users can add them directly to a wishlist.

## Architecture

No changes to domain/data/database layers. New components live in `shared/commonMain/presentation/` (ViewModel, repository, API service, settings) and `composeApp/commonMain/screens/` (chat screen, settings screen). Chat history is in-memory only — cleared on app restart.

## Tech Stack

Kotlin Multiplatform, Compose Multiplatform, Ktor Client 3.4.3 (HTTP), Multiplatform Settings (key/value storage), Koin 4.0.3, Coroutines StateFlow

---

## Data Models

**New file:** `shared/src/commonMain/kotlin/presentation/ai/ChatMessage.kt`

```kotlin
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
```

---

## Components

### 1. SettingsRepository

**New file:** `shared/src/commonMain/kotlin/data/SettingsRepository.kt`

Stores and retrieves the Anthropic API key using Multiplatform Settings (`com.russhwolf:multiplatform-settings`).

```kotlin
class SettingsRepository(private val settings: Settings) {
    fun getApiKey(): String = settings.getString("anthropic_api_key", "")
    fun setApiKey(key: String) { settings.putString("anthropic_api_key", key) }
}
```

### 2. ClaudeApiService

**New file:** `shared/src/commonMain/kotlin/data/ClaudeApiService.kt`

Ktor HTTP client that calls `https://api.anthropic.com/v1/messages`. Takes message history and returns Claude's raw response string. Knows nothing about wish parsing.

- Model: `claude-haiku-4-5-20251001` (fast, cheap for gift suggestions)
- Max tokens: 1024
- System prompt instructs Claude to reply with a short sentence then a JSON block:
  ```
  Found 3 great options 👜
  ```json
  [{"title":"...","description":"...","link":"...","imageUrl":"..."}]
  ```

### 3. AiChatRepository

**New file:** `shared/src/commonMain/kotlin/data/AiChatRepository.kt`

Holds the in-memory message list. Calls `ClaudeApiService`, parses the JSON block from the response, builds `ChatMessage` objects, appends to the list.

Parsing strategy: split response on `\`\`\`json` delimiter. Everything before is the text. The JSON block is parsed into `List<WishSuggestion>`. If parsing fails or array is empty, `wishes` is an empty list.

Exposes:
```kotlin
val messages: StateFlow<List<ChatMessage>>
suspend fun sendMessage(userText: String, apiKey: String)
fun clearHistory()
```

### 4. AiChatViewModel

**New file:** `shared/src/commonMain/kotlin/presentation/AiChatViewModel.kt`

```kotlin
class AiChatViewModel(
    private val chatRepository: AiChatRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    val messages: StateFlow<List<ChatMessage>> = chatRepository.messages
    val isLoading: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val apiKey: StateFlow<String> // from settingsRepository, updated on save

    fun sendMessage(text: String)  // checks key, sets loading, calls repo
    fun saveApiKey(key: String)    // delegates to settingsRepository
}
```

---

## Screens

### AiChatScreen

**Modified:** `composeApp/src/commonMain/kotlin/org/example/project/screens/AiChatScreen.kt`

Layout:
- **Top bar**: "AI Chat" title + settings icon (opens SettingsScreen)
- **Message list** (scrollable, fills remaining space):
  - User messages: purple bubble, right-aligned
  - Assistant messages: robot avatar + text bubble + wish cards stacked below
  - Each wish card: emoji placeholder | title + description + link | "Add to Wishlist" button
  - Loading indicator: typing dots bubble when `isLoading = true`
- **Input bar** (bottom): text field + send button; disabled when `isLoading = true`

No API key banner behavior: if key is empty when send is tapped, add an assistant message: "Please add your Anthropic API key in Settings first."

### SettingsScreen

**New file:** `composeApp/src/commonMain/kotlin/org/example/project/screens/SettingsScreen.kt`

Simple screen (no bottom nav, back arrow to return to AiChat):
- Masked text field: "Anthropic API Key"
- "Save" button — calls `vm.saveApiKey()`
- Confirmation snackbar: "API key saved"

---

## Navigation

Add `Settings` route to `App.kt`. Navigated to from AiChatScreen settings icon; back arrow returns to AiChat. Not in bottom nav.

---

## DI

**Modified:** `shared/src/commonMain/kotlin/di/AppModule.kt` (or equivalent)

Add bindings:
- `SettingsRepository` — singleton, needs `Settings` instance (platform-provided)
- `ClaudeApiService` — singleton, needs `HttpClient`
- `AiChatRepository` — singleton, needs `ClaudeApiService`
- `AiChatViewModel` — factory

---

## Error Handling

| Situation | Behavior |
|-----------|----------|
| No API key | Assistant message: "Please add your Anthropic API key in Settings first." |
| Network error | Assistant message: "Couldn't reach the server. Try again." |
| Malformed JSON | Show text portion only, `wishes = emptyList()` |
| Empty JSON array `[]` | Show text bubble only, no cards |
| User taps "Add to Wishlist" | Opens wishlist picker bottom sheet → creates Wish in chosen list |

---

## Out of Scope

- Streaming responses
- Persistent chat history (across restarts)
- Real image loading from imageUrl (placeholder icon only)
- Search/filter within chat
- Multiple AI providers
