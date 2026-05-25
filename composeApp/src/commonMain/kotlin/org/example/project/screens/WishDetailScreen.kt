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