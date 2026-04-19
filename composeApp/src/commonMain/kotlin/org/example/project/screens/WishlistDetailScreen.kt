@file:OptIn(ExperimentalMaterial3Api::class)

package org.example.project.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import domain.Wish
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
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
    val wishes by vm.wishes.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("${wishlist?.emoji ?: ""} ${wishlist?.name ?: ""}".trim())
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddWish) {
                Icon(Icons.Default.Add, contentDescription = "Добавить желание")
            }
        }
    ) { padding ->
        if (wishes.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Список пуст. Добавь первое желание!", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(wishes, key = { it.id }) { wish ->
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

@Composable
private fun WishCard(
    wish: Wish,
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
                        "🔗 Ссылка",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Удалить")
            }
        }
    }
}
