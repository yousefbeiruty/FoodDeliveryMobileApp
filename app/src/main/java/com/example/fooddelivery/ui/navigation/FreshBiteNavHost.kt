package com.example.fooddelivery.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.fooddelivery.data.repository.FoodRepository
import com.example.fooddelivery.ui.components.FreshBiteBottomBar
import com.example.fooddelivery.ui.screens.cart.CartCheckoutScreen
import com.example.fooddelivery.ui.screens.cart.CartViewModel
import com.example.fooddelivery.ui.screens.detail.RestaurantDetailScreen
import com.example.fooddelivery.ui.screens.detail.RestaurantDetailViewModel
import com.example.fooddelivery.ui.screens.home.HomeScreen
import com.example.fooddelivery.ui.screens.home.HomeViewModel
import com.example.fooddelivery.ui.screens.profile.ProfileScreen
import com.example.fooddelivery.ui.screens.profile.ProfileViewModel
import com.example.fooddelivery.ui.screens.splash.SplashScreen
import com.example.fooddelivery.ui.screens.tracking.OrderTrackingScreen
import com.example.fooddelivery.ui.screens.tracking.OrderTrackingViewModel

@Composable
fun FreshBiteMainApp(
    foodRepository: FoodRepository,
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val cartItems by foodRepository.cartItemsFlow.collectAsState()
    val totalCartCount = cartItems.sumOf { it.quantity }

    val showBottomBar = currentRoute in listOf(
        Screen.Home.route,
        Screen.Cart.route,
        Screen.Tracking.route,
        Screen.Profile.route
    )

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                FreshBiteBottomBar(
                    currentRoute = currentRoute,
                    cartItemCount = totalCartCount,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(Screen.Home.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Splash.route) {
                SplashScreen(
                    onGetStartedClick = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Home.route) {
                val homeViewModel: HomeViewModel = hiltViewModel()
                HomeScreen(
                    viewModel = homeViewModel,
                    onRestaurantClick = { restaurantId ->
                        navController.navigate(Screen.RestaurantDetail.createRoute(restaurantId))
                    }
                )
            }

            composable(
                route = Screen.RestaurantDetail.route,
                arguments = listOf(navArgument("restaurantId") { type = NavType.StringType })
            ) { backStackEntry ->
                val restaurantId = backStackEntry.arguments?.getString("restaurantId") ?: ""
                val detailViewModel: RestaurantDetailViewModel = hiltViewModel()
                RestaurantDetailScreen(
                    restaurantId = restaurantId,
                    viewModel = detailViewModel,
                    onBackClick = { navController.popBackStack() },
                    onViewCartClick = { navController.navigate(Screen.Cart.route) }
                )
            }

            composable(Screen.Cart.route) {
                val cartViewModel: CartViewModel = hiltViewModel()
                CartCheckoutScreen(
                    viewModel = cartViewModel,
                    onExploreClick = { navController.navigate(Screen.Home.route) },
                    onOrderPlaced = {
                        navController.navigate(Screen.Tracking.route) {
                            popUpTo(Screen.Home.route)
                        }
                    }
                )
            }

            composable(Screen.Tracking.route) {
                val trackingViewModel: OrderTrackingViewModel = hiltViewModel()
                OrderTrackingScreen(
                    viewModel = trackingViewModel,
                    onExploreClick = { navController.navigate(Screen.Home.route) }
                )
            }

            composable(Screen.Profile.route) {
                val profileViewModel: ProfileViewModel = hiltViewModel()
                ProfileScreen(viewModel = profileViewModel)
            }
        }
    }
}
