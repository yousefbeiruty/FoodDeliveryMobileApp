package com.example.fooddelivery.data.repository

import com.example.fooddelivery.data.model.Banner
import com.example.fooddelivery.data.model.CartItem
import com.example.fooddelivery.data.model.Category
import com.example.fooddelivery.data.model.Driver
import com.example.fooddelivery.data.model.MenuItem
import com.example.fooddelivery.data.model.Order
import com.example.fooddelivery.data.model.OrderStatus
import com.example.fooddelivery.data.model.Restaurant
import com.example.fooddelivery.data.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeFoodRepository : FoodRepository {

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    override val cartItemsFlow: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    private val _activeOrder = MutableStateFlow<Order?>(null)
    override val activeOrderFlow: StateFlow<Order?> = _activeOrder.asStateFlow()

    private val _userProfile = MutableStateFlow(
        UserProfile(
            name = "Test User",
            email = "test@eco.org",
            phone = "+15551234567",
            defaultAddress = "123 Eco Way",
            plasticSavedKg = 1.0,
            co2OffsetKg = 2.0,
            treesPlanted = 5,
            savedAddresses = listOf("123 Eco Way"),
            paymentMethods = listOf("Visa 4242"),
            isDarkMode = false
        )
    )
    override val userProfileFlow: StateFlow<UserProfile> = _userProfile.asStateFlow()

    val testCategories = listOf(
        Category("all", "All", "ic_all", isSelected = true),
        Category("healthy", "Healthy", "ic_healthy")
    )

    val testBanners = listOf(
        Banner("b1", "Test Discount", "20% off", "TEST20", "https://example.com/b1.jpg")
    )

    val testMenuItem = MenuItem(
        id = "m1",
        restaurantId = "r1",
        name = "Organic Bowl",
        description = "Fresh green bowl",
        price = 10.0,
        imageUrl = "https://example.com/m1.jpg",
        calories = 300,
        category = "healthy",
        isVegan = true,
        isEcoPackaged = true,
        rating = 4.8
    )

    val testRestaurant = Restaurant(
        id = "r1",
        name = "Green Bowl Bar",
        rating = 4.9,
        reviewCount = 100,
        deliveryTimeMinutes = 20,
        deliveryFee = 2.0,
        distanceKm = 1.0,
        imageUrl = "https://example.com/r1.jpg",
        tags = listOf("Healthy", "Vegan"),
        menuItems = listOf(testMenuItem)
    )

    var shouldReturnError = false

    override suspend fun getCategories(): Result<List<Category>> {
        return if (shouldReturnError) Result.failure(Exception("Error")) else Result.success(testCategories)
    }

    override suspend fun getBanners(): Result<List<Banner>> {
        return if (shouldReturnError) Result.failure(Exception("Error")) else Result.success(testBanners)
    }

    override suspend fun getRestaurants(searchQuery: String, categoryId: String?): Result<List<Restaurant>> {
        if (shouldReturnError) return Result.failure(Exception("Error"))
        val filtered = if (searchQuery.isNotBlank()) {
            listOf(testRestaurant).filter { it.name.contains(searchQuery, ignoreCase = true) }
        } else {
            listOf(testRestaurant)
        }
        return Result.success(filtered)
    }

    override suspend fun getRestaurantById(id: String): Result<Restaurant> {
        return if (shouldReturnError) Result.failure(Exception("Error")) else Result.success(testRestaurant)
    }

    override fun addToCart(menuItem: MenuItem, quantity: Int) {
        val current = _cartItems.value.toMutableList()
        val index = current.indexOfFirst { it.menuItem.id == menuItem.id }
        if (index >= 0) {
            current[index] = current[index].copy(quantity = current[index].quantity + quantity)
        } else {
            current.add(CartItem(menuItem, quantity))
        }
        _cartItems.value = current
    }

    override fun removeFromCart(menuItem: MenuItem) {
        _cartItems.value = _cartItems.value.filter { it.menuItem.id != menuItem.id }
    }

    override fun updateQuantity(menuItem: MenuItem, newQuantity: Int) {
        if (newQuantity <= 0) {
            removeFromCart(menuItem)
            return
        }
        val current = _cartItems.value.toMutableList()
        val index = current.indexOfFirst { it.menuItem.id == menuItem.id }
        if (index >= 0) {
            current[index] = current[index].copy(quantity = newQuantity)
            _cartItems.value = current
        }
    }

    override fun clearCart() {
        _cartItems.value = emptyList()
    }

    override suspend fun placeOrder(deliveryAddress: String, tipAmount: Double): Result<Order> {
        if (_cartItems.value.isEmpty()) {
            return Result.failure(IllegalStateException("Cart is empty"))
        }
        val subtotal = _cartItems.value.sumOf { it.totalPrice }
        val order = Order(
            orderId = "FB-12345",
            items = _cartItems.value,
            subtotal = subtotal,
            deliveryFee = 2.99,
            ecoDiscount = 1.50,
            tipAmount = tipAmount,
            total = subtotal + 2.99 + tipAmount - 1.50,
            status = OrderStatus.PLACED,
            estimatedDeliveryMinutes = 25,
            driver = Driver("d1", "Marcus", "https://example.com/d.jpg", 4.9, "123456", "Bike"),
            deliveryAddress = deliveryAddress
        )
        _activeOrder.value = order
        clearCart()
        return Result.success(order)
    }

    override fun toggleDarkMode() {
        _userProfile.value = _userProfile.value.copy(isDarkMode = !_userProfile.value.isDarkMode)
    }
}
