package com.example.fooddelivery.data.repository

import com.example.fooddelivery.data.model.Banner
import com.example.fooddelivery.data.model.CartItem
import com.example.fooddelivery.data.model.Category
import com.example.fooddelivery.data.model.MenuItem
import com.example.fooddelivery.data.model.Order
import com.example.fooddelivery.data.model.Restaurant
import com.example.fooddelivery.data.model.UserProfile
import kotlinx.coroutines.flow.StateFlow

interface FoodRepository {
    val cartItemsFlow: StateFlow<List<CartItem>>
    val activeOrderFlow: StateFlow<Order?>
    val userProfileFlow: StateFlow<UserProfile>

    suspend fun getCategories(): Result<List<Category>>
    suspend fun getBanners(): Result<List<Banner>>
    suspend fun getRestaurants(searchQuery: String = "", categoryId: String? = null): Result<List<Restaurant>>
    suspend fun getRestaurantById(id: String): Result<Restaurant>

    fun addToCart(menuItem: MenuItem, quantity: Int = 1)
    fun removeFromCart(menuItem: MenuItem)
    fun updateQuantity(menuItem: MenuItem, newQuantity: Int)
    fun clearCart()

    suspend fun placeOrder(deliveryAddress: String, tipAmount: Double): Result<Order>
    fun toggleDarkMode()
}
