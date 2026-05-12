package com.novelwechat.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Contacts : Screen("contacts")
    object Discover : Screen("discover")
    object Profile : Screen("profile")
    object Reading : Screen("reading/{bookId}") {
        fun createRoute(bookId: Long) = "reading/$bookId"
    }
    object Import : Screen("import")
}
