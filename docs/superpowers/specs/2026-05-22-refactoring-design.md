# Refactoring (Subproject 1) Design

## Goal

Translate all UI strings from Russian to English and introduce proper `UiState<T>` state management across all three ViewModels, plus fix missing Koin initialization on web targets.

## Architecture

No structural changes to the domain, data, or database layers. Changes are limited to the presentation layer (`shared/commonMain/presentation/`) and UI layer (`composeApp/`). The `UiState<T>` sealed class is the single new abstraction introduced — it replaces bare `List<T>` flows with typed Loading/Success/Error states. Web DI is fixed by adding `initKoin()` to JS and Wasm entry points.

## Tech Stack

Kotlin Multiplatform, Compose Multiplatform, Koin 4.0.3, Coroutines StateFlow, Room (unchanged)

---

## Changes

### 1. English Translation

Replace all hardcoded Russian strings directly in screen files. No abstraction layer needed (single language).

**Files to update:**
- `composeApp/src/commonMain/kotlin/org/example/project/screens/WishlistScreen.kt`
- `composeApp/src/commonMain/kotlin/org/example/project/screens/WishlistDetailScreen.kt`
- `composeApp/src/commonMain/kotlin/org/example/project/screens/WishDetailScreen.kt`
- `composeApp/src/commonMain/kotlin/org/example/project/screens/FriendsScreen.kt`
- `composeApp/src/commonMain/kotlin/org/example/project/screens/AiChatScreen.kt`
- `composeApp/src/commonMain/kotlin/org/example/project/App.kt` (bottom nav labels)

**String replacements (complete list):**

| Russian | English |
|---------|---------|
| Мои вишлисты | My Wishlists |
| Желания | Wishes |
| Друзья | Friends |
| Чат с ИИ / ИИ | AI Chat |
| Добавить вишлист | Add Wishlist |
| Новый вишлист | New Wishlist |
| Название | Name |
| Добавить | Add |
| Отмена | Cancel |
| Удалить | Delete |
| Поделиться | Share |
| Добавить желание | Add Wish |
| Новое желание | New Wish |
| Описание | Description |
| Ссылка | Link |
| URL изображения | Image URL |
| Сохранить | Save |
| Назад | Back |

---

### 2. UiState sealed class

**New file:** `shared/src/commonMain/kotlin/presentation/UiState.kt`

```kotlin
sealed class UiState<out T> {
    data object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}
```

**ViewModels updated** (`shared/src/commonMain/kotlin/presentation/`):

- `WishlistViewModel` — `StateFlow<List<Wishlist>>` → `StateFlow<UiState<List<Wishlist>>>`
- `WishlistDetailViewModel` — `StateFlow<List<Wish>>` → `StateFlow<UiState<List<Wish>>>`
- `WishDetailViewModel` — `StateFlow<Wish?>` → `StateFlow<UiState<Wish?>>`

Each ViewModel wraps its repository flow:
```kotlin
val wishlists: StateFlow<UiState<List<Wishlist>>> = wishlistRepository
    .getAll()
    .map<List<Wishlist>, UiState<List<Wishlist>>> { UiState.Success(it) }
    .catch { emit(UiState.Error(it.message ?: "Unknown error")) }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState.Loading)
```

**Screens updated** — each screen replaces direct list collection with `when` on `UiState`:
```kotlin
when (val state = uiState) {
    is UiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
    is UiState.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Error: ${state.message}")
    }
    is UiState.Success -> { /* existing screen content using state.data */ }
}
```

---

### 3. Web DI Fix

**New files:**
- `composeApp/src/jsMain/kotlin/di/KoinJs.kt`
- `composeApp/src/wasmJsMain/kotlin/di/KoinWasmJs.kt`

Both files:
```kotlin
fun initKoin() = startKoin {
    modules(appModule, viewModelModule)
}
```

**Modified:** `composeApp/src/webMain/kotlin/main.kt` — add `initKoin()` call before `ComposeViewport`.

---

## Out of Scope

- Friends screen implementation (Subproject 3)
- AI Chat implementation (Subproject 4)
- Backend (Subproject 2)
- Localization / multi-language support
- Image picker
- Search/filter
- Analytics, logging
