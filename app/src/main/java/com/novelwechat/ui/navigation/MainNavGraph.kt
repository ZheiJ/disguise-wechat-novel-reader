package com.novelwechat.ui.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.novelwechat.ui.components.TabItem
import com.novelwechat.ui.components.WeChatBottomTabBar
import com.novelwechat.ui.contacts.ContactsScreen
import com.novelwechat.ui.discover.DiscoverScreen
import com.novelwechat.ui.home.HomeScreen
import com.novelwechat.ui.profile.ProfileScreen
import com.novelwechat.ui.reading.ReadingScreen

@Composable
fun MainNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    var selectedTab by remember { mutableStateOf(0) }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute != Screen.Reading.route

    val tabs = listOf(
        TabItem(label = "微信"),
        TabItem(label = "通讯录"),
        TabItem(label = "发现"),
        TabItem(label = "我"),
    )

    Column(modifier = modifier) {
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.weight(1f),
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onBookClick = { bookId ->
                        navController.navigate(Screen.Reading.createRoute(bookId))
                    }
                )
            }
            composable(Screen.Contacts.route) {
                ContactsScreen(
                    onBookClick = { bookId ->
                        navController.navigate(Screen.Reading.createRoute(bookId))
                    }
                )
            }
            composable(Screen.Discover.route) {
                DiscoverScreen()
            }
            composable(Screen.Profile.route) {
                ProfileScreen()
            }
            composable(
                route = Screen.Reading.route,
                arguments = listOf(navArgument("bookId") { type = NavType.LongType }),
            ) { backStackEntry ->
                val bookId = backStackEntry.arguments?.getLong("bookId") ?: 0L
                ReadingScreen(
                    bookId = bookId,
                    onBack = { navController.popBackStack() },
                )
            }
        }

        if (showBottomBar) {
            WeChatBottomTabBar(
                tabs = tabs,
                selectedIndex = selectedTab,
                onTabSelected = { index ->
                    selectedTab = index
                    val route = when (index) {
                        0 -> Screen.Home.route
                        1 -> Screen.Contacts.route
                        2 -> Screen.Discover.route
                        3 -> Screen.Profile.route
                        else -> Screen.Home.route
                    }
                    navController.navigate(route) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                        launchSingleTop = true
                    }
                },
            )
        }
    }
}
