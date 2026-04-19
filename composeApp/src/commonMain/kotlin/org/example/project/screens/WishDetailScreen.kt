@file:OptIn(ExperimentalMaterial3Api::class)

package org.example.project.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import domain.Wish
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import presentation.WishDetailViewModel

@Composable
fun WishDetailScreen(
    listId: Long,
    wishId: Long,
    onBack: () -> Unit,
    vm: WishDetailViewModel = koinViewModel(parameters = { parametersOf(wishId) })
) {
    val existing by vm.wish.collectAsStateWithLifecycle()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var link by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf("") }

    // Populate fields when editing an existing wish
    LaunchedEffect(existing) {
        existing?.let {
            title = it.title
            description = it.description ?: ""
            link = it.link ?: ""
            imageUri = it.imageUri ?: ""
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
                title = { Text(if (isNew) "Новое желание" else "Редактировать") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    if (!isNew) {
                        IconButton(onClick = {
                            vm.delete()
                            onBack()
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Удалить")
                        }
                    }
                    TextButton(onClick = ::save, enabled = title.isNotBlank()) {
                        Text("Сохранить")
                    }
                }
            )
        }
    ) { padding ->
        Column(
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
                label = { Text("Название *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Описание") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = link,
                onValueChange = { link = it },
                label = { Text("Ссылка") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = imageUri,
                onValueChange = { imageUri = it },
                label = { Text("URL картинки") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))
        }
    }
}