package org.example.project

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.example.project.screens.WishDetailScreen
import org.example.project.screens.WishlistDetailScreen
import org.example.project.screens.WishlistScreen

@Composable
fun App() {
    MaterialTheme {
        val navController = rememberNavController()

        NavHost(navController = navController, startDestination = "wishlists") {

            composable("wishlists") {
                WishlistScreen(
                    onOpen = { listId -> navController.navigate("wishlist/$listId") }
                )
            }

            composable("wishlist/{listId}") { back ->
                val listId = back.arguments?.getString("listId")?.toLongOrNull() ?: return@composable
                WishlistDetailScreen(
                    listId = listId,
                    onBack = { navController.popBackStack() },
                    onWish = { wishId -> navController.navigate("wish/$listId/$wishId") },
                    onAddWish = { navController.navigate("wish/$listId/0") }
                )
            }

            composable("wish/{listId}/{wishId}") { back ->
                val listId = back.arguments?.getString("listId")?.toLongOrNull() ?: return@composable
                val wishId = back.arguments?.getString("wishId")?.toLongOrNull() ?: return@composable
                WishDetailScreen(
                    listId = listId,
                    wishId = wishId,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}