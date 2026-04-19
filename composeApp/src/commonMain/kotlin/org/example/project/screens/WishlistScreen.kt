@file:OptIn(ExperimentalMaterial3Api::class)

package org.example.project.screens

import androidx.compose.foundation.clickable
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
import presentation.WishlistViewModel

@Composable
fun WishlistScreen(
    onOpen: (listId: Long) -> Unit,
    vm: WishlistViewModel = koinViewModel()
) {
    val lists by vm.wishlists.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Мои вишлисты") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Создать вишлист")
            }
        }
    ) { padding ->
        if (lists.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Нет вишлистов. Создай первый!", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(lists, key = { it.id }) { wishlist ->
                    WishlistCard(
                        wishlist = wishlist,
                        onClick = { onOpen(wishlist.id) },
                        onDelete = { vm.delete(wishlist) }
                    )
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
                Icon(Icons.Default.Delete, contentDescription = "Удалить")
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
        title = { Text("Новый вишлист") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = emoji,
                    onValueChange = { emoji = it },
                    label = { Text("Эмодзи") },
                    singleLine = true,
                    modifier = Modifier.width(100.dp)
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Название") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (name.isNotBlank()) onCreate(name.trim(), emoji) },
                enabled = name.isNotBlank()
            ) { Text("Создать") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )
}