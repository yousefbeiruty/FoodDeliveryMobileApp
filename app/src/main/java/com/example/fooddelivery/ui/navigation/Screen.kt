package com.example.fooddelivery.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Home : Screen("home")
    object RestaurantDetail : Screen("restaurant/{restaurantId}") {
        fun createRoute(restaurantId: String) = "restaurant/$restaurantId"
    }
    object Cart : Screen("cart")
    object Tracking : Screen("tracking")
    object Profile : Screen("profile")
}
