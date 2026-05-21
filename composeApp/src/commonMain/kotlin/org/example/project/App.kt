package org.example.project

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import org.example.project.screens.AiChatScreen
import org.example.project.screens.FriendsScreen
import org.example.project.screens.WishDetailScreen
import org.example.project.screens.WishlistDetailScreen
import org.example.project.screens.WishlistScreen
import org.example.project.ui.AppTheme

@Serializable object Wishlists
@Serializable object Friends
@Serializable object AiChat
@Serializable data class WishlistDetail(val listId: Long)
@Serializable data class WishDetail(val listId: Long, val wishId: Long)

private data class BottomNavItem(
    val route: Any,
    val emoji: String,
    val label: String
)

private val bottomNavItems = listOf(
    BottomNavItem(Wishlists, "❤️", "Желания"),
    BottomNavItem(Friends,   "👥", "Друзья"),
    BottomNavItem(AiChat,    "🤖", "ИИ"),
)

@Composable
fun App() {
    AppTheme {
        val navController = rememberNavController()
        val backStack by navController.currentBackStackEntryAsState()

        val showBottomBar = backStack?.destination?.route?.let { route ->
            listOf(
                Wishlists::class.qualifiedName,
                Friends::class.qualifiedName,
                AiChat::class.qualifiedName
            ).any { route.startsWith(it ?: "") }
        } ?: true

        Scaffold(
            bottomBar = {
                if (showBottomBar) {
                    NavigationBar {
                        val currentRoute = backStack?.destination?.route
                        bottomNavItems.forEach { item ->
                            val itemRoute = item.route::class.qualifiedName ?: ""
                            val selected = currentRoute?.startsWith(itemRoute) == true
                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    if (!selected) {
                                        navController.navigate(item.route) {
                                            popUpTo(Wishlists) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                },
                                icon = { Text(item.emoji, fontSize = 22.sp) },
                                label = { Text(item.label) }
                            )
                        }
                    }
                }
            }
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = Wishlists,
                modifier = Modifier.padding(padding)
            ) {
                composable<Wishlists> {
                    WishlistScreen(
                        onOpen = { listId -> navController.navigate(WishlistDetail(listId)) }
                    )
                }

                composable<Friends> {
                    FriendsScreen()
                }

                composable<AiChat> {
                    AiChatScreen()
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
}
