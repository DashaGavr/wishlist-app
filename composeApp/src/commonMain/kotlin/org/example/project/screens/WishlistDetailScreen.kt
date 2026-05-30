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
import androidx.lifecycle.viewmodel.compose.viewModel
import domain.Wish
import org.example.project.rememberShareText
import org.koin.compose.getKoin
import presentation.UiState
import presentation.WishlistDetailViewModel

@Composable
fun WishlistDetailScreen(
    listId: Long,
    onBack: () -> Unit,
    onWish: (wishId: Long) -> Unit,
    onAddWish: () -> Unit,
) {
    val koin = getKoin()
    val vm: WishlistDetailViewModel = viewModel(key = "wishlist_detail_$listId") {
        WishlistDetailViewModel(koin.get(), koin.get(), listId)
    }
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
