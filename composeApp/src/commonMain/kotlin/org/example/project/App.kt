package org.example.project

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import org.example.project.screens.WishDetailScreen
import org.example.project.screens.WishlistDetailScreen
import org.example.project.screens.WishlistScreen

@Serializable object Wishlists
@Serializable data class WishlistDetail(val listId: Long)
@Serializable data class WishDetail(val listId: Long, val wishId: Long)

@Composable
fun App() {
    MaterialTheme {
        val navController = rememberNavController()

        NavHost(navController = navController, startDestination = Wishlists) {

            composable<Wishlists> {
                WishlistScreen(
                    onOpen = { listId -> navController.navigate(WishlistDetail(listId)) }
                )
            }

            composable<WishlistDetail> { back ->
                val route = back.toRoute<WishlistDetail>()
                WishlistDetailScreen(
                    listId = route.listId,
                    onBack = { navController.popBackStack() },
                    onWish = { wishId -> navController.navigate(WishDetail(route.listId, wishId)) },
                    onAddWish = { navController.navigate(WishDetail(route.listId, 0L)) }
                )
            }

            composable<WishDetail> { back ->
                val route = back.toRoute<WishDetail>()
                WishDetailScreen(
                    listId = route.listId,
                    wishId = route.wishId,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}