# Refactoring (Subproject 1) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Translate all UI strings from Russian to English and add proper `UiState<T>` loading/error state management across all three ViewModels, plus initialize Koin on web targets.

**Architecture:** `UiState<T>` sealed class lives in `shared/commonMain/presentation/` alongside the ViewModels. Each ViewModel wraps its repository flow with `map { UiState.Success(it) }.catch { UiState.Error(...) }.stateIn(..., UiState.Loading)`. Screens switch on the three states and show a spinner, error text, or content accordingly.

**Tech Stack:** Kotlin Multiplatform 2.3.20, Compose Multiplatform 1.10.3, Koin 4.0.3, Coroutines StateFlow, Room (unchanged)

---

## File Map

| Action | Path | Responsibility |
|--------|------|----------------|
| Create | `shared/src/commonMain/kotlin/presentation/UiState.kt` | Sealed class Loading / Success / Error |
| Modify | `shared/src/commonMain/kotlin/presentation/WishlistViewModel.kt` | `StateFlow<UiState<List<Wishlist>>>` |
| Modify | `shared/src/commonMain/kotlin/presentation/WishlistDetailViewModel.kt` | `StateFlow<UiState<List<Wish>>>` for wishes |
| Modify | `shared/src/commonMain/kotlin/presentation/WishDetailViewModel.kt` | `StateFlow<UiState<Wish?>>` |
| Modify | `composeApp/src/commonMain/kotlin/org/example/project/screens/WishlistScreen.kt` | UiState handling + English strings |
| Modify | `composeApp/src/commonMain/kotlin/org/example/project/screens/WishlistDetailScreen.kt` | UiState handling + English strings |
| Modify | `composeApp/src/commonMain/kotlin/org/example/project/screens/WishDetailScreen.kt` | English strings |
| Modify | `composeApp/src/commonMain/kotlin/org/example/project/screens/FriendsScreen.kt` | English strings |
| Modify | `composeApp/src/commonMain/kotlin/org/example/project/screens/AiChatScreen.kt` | English strings |
| Modify | `composeApp/src/commonMain/kotlin/org/example/project/App.kt` | English bottom nav labels |
| Create | `composeApp/src/webMain/kotlin/org/example/project/di/KoinWeb.kt` | Web Koin initializer |
| Modify | `composeApp/src/webMain/kotlin/org/example/project/main.kt` | Call `initKoin()` before App() |
| Create | `composeApp/src/commonTest/kotlin/org/example/project/UiStateTest.kt` | Tests for UiState |

---

## Task 1: Create UiState sealed class

**Files:**
- Create: `shared/src/commonMain/kotlin/presentation/UiState.kt`
- Create: `composeApp/src/commonTest/kotlin/org/example/project/UiStateTest.kt`

- [ ] **Step 1: Write the failing test**

Create `composeApp/src/commonTest/kotlin/org/example/project/UiStateTest.kt`:

```kotlin
package org.example.project

import presentation.UiState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class UiStateTest {

    @Test
    fun `Loading is the initial state type`() {
        val state: UiState<List<String>> = UiState.Loading
        assertIs<UiState.Loading>(state)
    }

    @Test
    fun `Success wraps data correctly`() {
        val data = listOf("a", "b", "c")
        val state = UiState.Success(data)
        assertIs<UiState.Success<List<String>>>(state)
        assertEquals(data, state.data)
    }

    @Test
    fun `Error holds the message`() {
        val state = UiState.Error("network failure")
        assertIs<UiState.Error>(state)
        assertEquals("network failure", state.message)
    }

    @Test
    fun `Success with null data is valid`() {
        val state: UiState<String?> = UiState.Success(null)
        assertIs<UiState.Success<String?>>(state)
        assertEquals(null, state.data)
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
./gradlew :composeApp:jvmTest --tests "org.example.project.UiStateTest" 2>&1 | tail -20
```

Expected: FAIL — `error: unresolved reference: UiState`

- [ ] **Step 3: Create UiState.kt**

Create `shared/src/commonMain/kotlin/presentation/UiState.kt`:

```kotlin
package presentation

sealed class UiState<out T> {
    data object Loading : UiState<Nothing>()
    data class Success<out T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}
```

- [ ] **Step 4: Run test to verify it passes**

```bash
./gradlew :composeApp:jvmTest --tests "org.example.project.UiStateTest" 2>&1 | tail -20
```

Expected: `BUILD SUCCESSFUL` — all 4 tests pass

- [ ] **Step 5: Commit**

```bash
git add shared/src/commonMain/kotlin/presentation/UiState.kt \
        composeApp/src/commonTest/kotlin/org/example/project/UiStateTest.kt
git commit -m "feat: add UiState sealed class with tests"
```

---

## Task 2: Update WishlistViewModel

**Files:**
- Modify: `shared/src/commonMain/kotlin/presentation/WishlistViewModel.kt`

- [ ] **Step 1: Replace the file contents**

```kotlin
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
```

- [ ] **Step 2: Verify it compiles**

```bash
./gradlew :shared:compileKotlinJvm 2>&1 | tail -20
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 3: Commit**

```bash
git add shared/src/commonMain/kotlin/presentation/WishlistViewModel.kt
git commit -m "feat: wrap WishlistViewModel wishlists in UiState"
```

---

## Task 3: Update WishlistDetailViewModel

**Files:**
- Modify: `shared/src/commonMain/kotlin/presentation/WishlistDetailViewModel.kt`

- [ ] **Step 1: Replace the file contents**

```kotlin
package presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import data.WishRepository
import data.WishlistRepository
import domain.Wish
import domain.Wishlist
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
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

    val wishes: StateFlow<UiState<List<Wish>>> = wishRepository.getByList(listId)
        .map<List<Wish>, UiState<List<Wish>>> { UiState.Success(it) }
        .catch { emit(UiState.Error(it.message ?: "Unknown error")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState.Loading)

    fun addWish(title: String) {
        viewModelScope.launch {
            val currentWishes = (wishes.value as? UiState.Success)?.data ?: emptyList()
            val nextRank = (currentWishes.maxOfOrNull { it.rank } ?: 0.0) + 1.0
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
```

- [ ] **Step 2: Verify it compiles**

```bash
./gradlew :shared:compileKotlinJvm 2>&1 | tail -20
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 3: Commit**

```bash
git add shared/src/commonMain/kotlin/presentation/WishlistDetailViewModel.kt
git commit -m "feat: wrap WishlistDetailViewModel wishes in UiState"
```

---

## Task 4: Update WishDetailViewModel

**Files:**
- Modify: `shared/src/commonMain/kotlin/presentation/WishDetailViewModel.kt`

- [ ] **Step 1: Replace the file contents**

```kotlin
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
```

- [ ] **Step 2: Verify it compiles**

```bash
./gradlew :shared:compileKotlinJvm 2>&1 | tail -20
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 3: Commit**

```bash
git add shared/src/commonMain/kotlin/presentation/WishDetailViewModel.kt
git commit -m "feat: wrap WishDetailViewModel wish in UiState"
```

---

## Task 5: Update WishlistScreen

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/org/example/project/screens/WishlistScreen.kt`

- [ ] **Step 1: Replace the file contents**

```kotlin
@file:OptIn(ExperimentalMaterial3Api::class)

package org.example.project.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import domain.Wishlist
import org.koin.compose.viewmodel.koinViewModel
import presentation.UiState
import presentation.WishlistViewModel

@Composable
fun WishlistScreen(
    onOpen: (listId: Long) -> Unit,
    vm: WishlistViewModel = koinViewModel()
) {
    val state by vm.wishlists.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("My Wishlists") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Create wishlist")
            }
        }
    ) { padding ->
        when (val s = state) {
            is UiState.Loading -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }

            is UiState.Error -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Error: ${s.message}", color = MaterialTheme.colorScheme.error)
            }

            is UiState.Success -> {
                if (s.data.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(padding),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No wishlists yet. Create your first one!", style = MaterialTheme.typography.bodyLarge)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(padding),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(s.data, key = { it.id }) { wishlist ->
                            WishlistCard(
                                wishlist = wishlist,
                                onClick = { onOpen(wishlist.id) },
                                onDelete = { vm.delete(wishlist) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        CreateWishlistDialog(
            onDismiss = { showDialog = false },
            onCreate = { name, emoji ->
                vm.create(name, emoji)
                showDialog = false
            }
        )
    }
}

@Composable
private fun WishlistCard(
    wishlist: Wishlist,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(wishlist.emoji, style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.width(16.dp))
            Text(
                wishlist.name,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
        }
    }
}

@Composable
private fun CreateWishlistDialog(
    onDismiss: () -> Unit,
    onCreate: (name: String, emoji: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var emoji by remember { mutableStateOf("🎁") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Wishlist") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = emoji,
                    onValueChange = { emoji = it },
                    label = { Text("Emoji") },
                    singleLine = true,
                    modifier = Modifier.width(100.dp)
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (name.isNotBlank()) onCreate(name.trim(), emoji) },
                enabled = name.isNotBlank()
            ) { Text("Create") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
```

- [ ] **Step 2: Verify it compiles**

```bash
./gradlew :composeApp:compileKotlinJvm 2>&1 | tail -20
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 3: Commit**

```bash
git add composeApp/src/commonMain/kotlin/org/example/project/screens/WishlistScreen.kt
git commit -m "feat: WishlistScreen — UiState handling + English strings"
```

---

## Task 6: Update WishlistDetailScreen

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/org/example/project/screens/WishlistDetailScreen.kt`

- [ ] **Step 1: Replace the file contents**

```kotlin
@file:OptIn(ExperimentalMaterial3Api::class)

package org.example.project.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import domain.Wish
import org.example.project.rememberShareText
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import presentation.UiState
import presentation.WishlistDetailViewModel

@Composable
fun WishlistDetailScreen(
    listId: Long,
    onBack: () -> Unit,
    onWish: (wishId: Long) -> Unit,
    onAddWish: () -> Unit,
    vm: WishlistDetailViewModel = koinViewModel(parameters = { parametersOf(listId) })
) {
    val wishlist by vm.wishlist.collectAsStateWithLifecycle()
    val wishesState by vm.wishes.collectAsStateWithLifecycle()
    val shareText = rememberShareText()

    val wishes = (wishesState as? UiState.Success)?.data ?: emptyList()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("${wishlist?.emoji ?: ""} ${wishlist?.name ?: ""}".trim())
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val text = buildShareText(wishlist?.name, wishlist?.emoji, wishes)
                            shareText(text)
                        },
                        enabled = wishes.isNotEmpty()
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddWish) {
                Icon(Icons.Default.Add, contentDescription = "Add wish")
            }
        }
    ) { padding ->
        when (val s = wishesState) {
            is UiState.Loading -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }

            is UiState.Error -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Error: ${s.message}", color = MaterialTheme.colorScheme.error)
            }

            is UiState.Success -> {
                if (s.data.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(padding),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No wishes yet. Add your first!", style = MaterialTheme.typography.bodyLarge)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(padding),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(s.data, key = { it.id }) { wish ->
                            WishCard(
                                wish = wish,
                                onClick = { onWish(wish.id) },
                                onDelete = { vm.deleteWish(wish) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WishCard(
    wish: Wish,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!wish.imageUri.isNullOrBlank()) {
                AsyncImage(
                    model = wish.imageUri,
                    contentDescription = wish.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
                Spacer(Modifier.width(12.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(wish.title, style = MaterialTheme.typography.titleMedium)
                wish.description?.takeIf { it.isNotBlank() }?.let { desc ->
                    Spacer(Modifier.height(4.dp))
                    Text(
                        desc,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }
                if (!wish.link.isNullOrBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "🔗 Link",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
        }
    }
}

private fun buildShareText(name: String?, emoji: String?, wishes: List<Wish>): String {
    val header = "${emoji ?: ""} ${name ?: "Wishlist"}".trim()
    val items = wishes.joinToString("\n\n") { wish ->
        buildString {
            append("• ${wish.title}")
            wish.description?.takeIf { it.isNotBlank() }?.let { append("\n  $it") }
            wish.link?.takeIf { it.isNotBlank() }?.let { append("\n  🔗 $it") }
        }
    }
    return "$header\n\n$items"
}
```

- [ ] **Step 2: Verify it compiles**

```bash
./gradlew :composeApp:compileKotlinJvm 2>&1 | tail -20
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 3: Commit**

```bash
git add composeApp/src/commonMain/kotlin/org/example/project/screens/WishlistDetailScreen.kt
git commit -m "feat: WishlistDetailScreen — UiState handling + English strings"
```

---

## Task 7: Update WishDetailScreen

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/org/example/project/screens/WishDetailScreen.kt`

- [ ] **Step 1: Replace the file contents**

```kotlin
@file:OptIn(ExperimentalMaterial3Api::class)

package org.example.project.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import domain.Wish
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import presentation.UiState
import presentation.WishDetailViewModel

@Composable
fun WishDetailScreen(
    listId: Long,
    wishId: Long,
    onBack: () -> Unit,
    vm: WishDetailViewModel = koinViewModel(parameters = { parametersOf(wishId) })
) {
    val uiState by vm.wish.collectAsStateWithLifecycle()
    val existing = (uiState as? UiState.Success)?.data

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var link by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf("") }

    LaunchedEffect(uiState) {
        if (uiState is UiState.Success) {
            existing?.let {
                title = it.title
                description = it.description ?: ""
                link = it.link ?: ""
                imageUri = it.imageUri ?: ""
            }
        }
    }

    val isNew = wishId == 0L

    fun save() {
        if (title.isBlank()) return
        val wish = Wish(
            id = if (isNew) 0L else wishId,
            title = title.trim(),
            description = description.trim().ifBlank { null },
            link = link.trim().ifBlank { null },
            imageUri = imageUri.trim().ifBlank { null },
            rank = existing?.rank ?: 0.0,
            listId = listId
        )
        vm.save(wish)
        onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isNew) "New Wish" else "Edit Wish") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (!isNew) {
                        IconButton(onClick = {
                            vm.delete()
                            onBack()
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }
                    TextButton(onClick = ::save, enabled = title.isNotBlank()) {
                        Text("Save")
                    }
                }
            )
        }
    ) { padding ->
        when (val s = uiState) {
            is UiState.Loading -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }

            is UiState.Error -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Error: ${s.message}", color = MaterialTheme.colorScheme.error)
            }

            is UiState.Success -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = link,
                    onValueChange = { link = it },
                    label = { Text("Link") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = imageUri,
                    onValueChange = { imageUri = it },
                    label = { Text("Image URL") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (imageUri.isNotBlank()) {
                    AsyncImage(
                        model = imageUri,
                        contentDescription = "Preview",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .align(Alignment.CenterHorizontally)
                    )
                }

                Spacer(Modifier.height(8.dp))
            }
        }
    }
}
```

- [ ] **Step 2: Verify it compiles**

```bash
./gradlew :composeApp:compileKotlinJvm 2>&1 | tail -20
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 3: Commit**

```bash
git add composeApp/src/commonMain/kotlin/org/example/project/screens/WishDetailScreen.kt
git commit -m "feat: WishDetailScreen — UiState handling + English strings"
```

---

## Task 8: Translate App.kt, FriendsScreen, AiChatScreen

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/org/example/project/App.kt`
- Modify: `composeApp/src/commonMain/kotlin/org/example/project/screens/FriendsScreen.kt`
- Modify: `composeApp/src/commonMain/kotlin/org/example/project/screens/AiChatScreen.kt`

- [ ] **Step 1: Update bottom nav labels in App.kt**

In `App.kt`, change lines 37–41:

```kotlin
private val bottomNavItems = listOf(
    BottomNavItem(Wishlists, "❤️", "Wishes"),
    BottomNavItem(Friends,   "👥", "Friends"),
    BottomNavItem(AiChat,    "🤖", "AI Chat"),
)
```

- [ ] **Step 2: Update FriendsScreen.kt**

Replace contents of `composeApp/src/commonMain/kotlin/org/example/project/screens/FriendsScreen.kt`:

```kotlin
package org.example.project.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun FriendsScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("👥 Friends", style = MaterialTheme.typography.titleLarge)
    }
}
```

- [ ] **Step 3: Update AiChatScreen.kt**

Replace contents of `composeApp/src/commonMain/kotlin/org/example/project/screens/AiChatScreen.kt`:

```kotlin
package org.example.project.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun AiChatScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("🤖 AI Chat", style = MaterialTheme.typography.titleLarge)
    }
}
```

- [ ] **Step 4: Verify all compile**

```bash
./gradlew :composeApp:compileKotlinJvm 2>&1 | tail -20
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 5: Commit**

```bash
git add composeApp/src/commonMain/kotlin/org/example/project/App.kt \
        composeApp/src/commonMain/kotlin/org/example/project/screens/FriendsScreen.kt \
        composeApp/src/commonMain/kotlin/org/example/project/screens/AiChatScreen.kt
git commit -m "feat: translate App.kt, FriendsScreen, AiChatScreen to English"
```

---

## Task 9: Web DI init

**Files:**
- Create: `composeApp/src/webMain/kotlin/org/example/project/di/KoinWeb.kt`
- Modify: `composeApp/src/webMain/kotlin/org/example/project/main.kt`

- [ ] **Step 1: Create KoinWeb.kt**

Create `composeApp/src/webMain/kotlin/org/example/project/di/KoinWeb.kt`:

```kotlin
package org.example.project.di

import di.appModule
import org.koin.core.context.startKoin

fun initKoin() = startKoin {
    modules(appModule, viewModelModule)
}
```

- [ ] **Step 2: Update main.kt to call initKoin()**

Replace contents of `composeApp/src/webMain/kotlin/org/example/project/main.kt`:

```kotlin
package org.example.project

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import org.example.project.di.initKoin

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    initKoin()
    ComposeViewport {
        App()
    }
}
```

- [ ] **Step 3: Verify JS target compiles**

```bash
./gradlew :composeApp:compileKotlinJs 2>&1 | tail -20
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 4: Run all JVM tests to confirm nothing broken**

```bash
./gradlew :composeApp:jvmTest 2>&1 | tail -20
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 5: Commit**

```bash
git add composeApp/src/webMain/kotlin/org/example/project/di/KoinWeb.kt \
        composeApp/src/webMain/kotlin/org/example/project/main.kt
git commit -m "feat: initialize Koin on web targets"
```

---

## Self-Review

**Spec coverage:**
- ✅ English translation — Tasks 5, 6, 7, 8 cover all 5 screens + App.kt
- ✅ UiState sealed class — Task 1
- ✅ WishlistViewModel wrapped — Task 2
- ✅ WishlistDetailViewModel wrapped — Task 3
- ✅ WishDetailViewModel wrapped — Task 4
- ✅ Screens handle Loading/Error/Success — Tasks 5, 6, 7
- ✅ Web DI fix — Task 9

**Type consistency:**
- `UiState<List<Wishlist>>` used in Task 2 and Task 5 — ✅
- `UiState<List<Wish>>` used in Task 3 and Task 6 — ✅
- `UiState<Wish?>` used in Task 4 and Task 7 — ✅
- `wishes.value` cast to `(UiState.Success)?.data` in Task 3 matches `UiState.Success<List<Wish>>` — ✅

**No placeholders:** All steps contain full code. ✅
