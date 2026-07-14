@file:OptIn(ExperimentalMaterial3Api::class)

package org.example.project.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import domain.Wishlist
import org.koin.compose.viewmodel.koinViewModel
import presentation.AiChatViewModel
import presentation.ai.ChatMessage
import presentation.ai.Role
import presentation.ai.WishSuggestion

@Composable
fun AiChatScreen(onSettings: () -> Unit) {
    val vm: AiChatViewModel = koinViewModel()
    val messages by vm.messages.collectAsStateWithLifecycle()
    val isLoading by vm.isLoading.collectAsStateWithLifecycle()
    val wishlists by vm.wishlists.collectAsStateWithLifecycle()

    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    var pickerWish by remember { mutableStateOf<WishSuggestion?>(null) }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }

    pickerWish?.let { wish ->
        WishlistPickerSheet(
            wishlists = wishlists,
            onPick = { listId ->
                vm.addWishToList(listId, wish)
                pickerWish = null
            },
            onDismiss = { pickerWish = null }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Chat") },
                actions = {
                    IconButton(onClick = onSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                items(messages) { message ->
                    when (message.role) {
                        Role.USER -> UserBubble(message.text)
                        Role.ASSISTANT -> AssistantMessage(
                            message = message,
                            onAddToWishlist = { wish -> pickerWish = wish }
                        )
                    }
                }
                if (isLoading) {
                    item { TypingIndicator() }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("Describe what you're looking for...") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    enabled = !isLoading,
                    maxLines = 3
                )
                val canSend = inputText.isNotBlank() && !isLoading
                IconButton(
                    onClick = {
                        if (canSend) {
                            vm.sendMessage(inputText.trim())
                            inputText = ""
                        }
                    },
                    enabled = canSend,
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = if (canSend) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceVariant,
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = if (canSend) MaterialTheme.colorScheme.onPrimary
                               else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun UserBubble(text: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        Surface(
            shape = RoundedCornerShape(18.dp, 18.dp, 4.dp, 18.dp),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Text(
                text = text,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun AssistantMessage(
    message: ChatMessage,
    onAddToWishlist: (WishSuggestion) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.secondaryContainer,
            modifier = Modifier.size(32.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text("🤖", style = MaterialTheme.typography.bodySmall)
            }
        }
        Spacer(Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (message.text.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(18.dp, 18.dp, 18.dp, 4.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = message.text,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            message.wishes.forEach { wish ->
                WishCard(wish = wish, onAdd = { onAddToWishlist(wish) })
            }
        }
    }
}

@Composable
private fun WishCard(wish: WishSuggestion, onAdd: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("🎁", style = MaterialTheme.typography.headlineSmall)
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        wish.title,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1
                    )
                    if (wish.description.isNotBlank()) {
                        Text(
                            wish.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2
                        )
                    }
                    if (wish.link.isNotBlank()) {
                        Text(
                            "🔗 ${wish.link.removePrefix("https://").removePrefix("http://").substringBefore("/")}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            HorizontalDivider()
            TextButton(
                onClick = onAdd,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("+ Add to Wishlist")
            }
        }
    }
}

@Composable
private fun TypingIndicator() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.secondaryContainer,
            modifier = Modifier.size(32.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text("🤖", style = MaterialTheme.typography.bodySmall)
            }
        }
        Spacer(Modifier.width(8.dp))
        Surface(
            shape = RoundedCornerShape(18.dp, 18.dp, 18.dp, 4.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Text(
                "● ● ●",
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun WishlistPickerSheet(
    wishlists: List<Wishlist>,
    onPick: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(bottom = 32.dp)) {
            Text(
                "Add to Wishlist",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
            )
            HorizontalDivider()
            if (wishlists.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No wishlists yet. Create one first.")
                }
            } else {
                wishlists.forEach { wishlist ->
                    ListItem(
                        headlineContent = { Text(wishlist.name) },
                        leadingContent = { Text(wishlist.emoji, style = MaterialTheme.typography.headlineSmall) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPick(wishlist.id) }
                    )
                }
            }
        }
    }
}
