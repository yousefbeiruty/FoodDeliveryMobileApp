package com.example.fooddelivery.data.repository

import com.example.fooddelivery.BuildConfig
import com.example.fooddelivery.data.api.FastApiFoodDeliveryApi
import com.example.fooddelivery.data.api.OrderQuoteItem
import com.example.fooddelivery.data.api.OrderQuoteRequest
import com.example.fooddelivery.data.api.PlaceOrderRequest
import com.example.fooddelivery.data.model.Banner
import com.example.fooddelivery.data.model.CartItem
import com.example.fooddelivery.data.model.Category
import com.example.fooddelivery.data.model.Driver
import com.example.fooddelivery.data.model.MenuItem
import com.example.fooddelivery.data.model.Order
import com.example.fooddelivery.data.model.OrderStatus
import com.example.fooddelivery.data.model.Restaurant
import com.example.fooddelivery.data.model.UserProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FoodRepositoryImpl @Inject constructor(
    private val api: FastApiFoodDeliveryApi
) : FoodRepository {

    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    override val cartItemsFlow: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    private val _activeOrder = MutableStateFlow<Order?>(null)
    override val activeOrderFlow: StateFlow<Order?> = _activeOrder.asStateFlow()

    private val _userProfile = MutableStateFlow(
        UserProfile(
            name = "Alex Green",
            email = "alex.green@eco.org",
            phone = "+1 (555) 234-5678",
            defaultAddress = "742 Evergreen Terrace, Green City",
            plasticSavedKg = 4.2,
            co2OffsetKg = 18.5,
            treesPlanted = 12,
            savedAddresses = listOf("742 Evergreen Terrace, Green City"),
            paymentMethods = listOf("Visa ending in 4242"),
            isDarkMode = false
        )
    )
    override val userProfileFlow: StateFlow<UserProfile> = _userProfile.asStateFlow()

    init {
        repositoryScope.launch {
            fetchUserProfile()
        }
    }

    private fun resolveImageUrl(rawUrl: String): String {
        if (rawUrl.isBlank()) return "https://images.unsplash.com/photo-1540420773420-3366772f4999?auto=format&fit=crop&w=800&q=80"
        if (rawUrl.startsWith("http://") || rawUrl.startsWith("https://")) {
            return rawUrl
        }
        val base = BuildConfig.BASE_URL.removeSuffix("/")
        val path = if (rawUrl.startsWith("/")) rawUrl else "/$rawUrl"
        return "$base$path"
    }

    private suspend fun fetchUserProfile() {
        try {
            val userDto = api.getUserProfile()
            _userProfile.value = UserProfile(
                name = userDto.fullName,
                email = userDto.email,
                phone = userDto.phoneNumber ?: "",
                defaultAddress = "742 Evergreen Terrace, Green City",
                plasticSavedKg = 4.2,
                co2OffsetKg = 18.5,
                treesPlanted = 12,
                savedAddresses = listOf("742 Evergreen Terrace, Green City"),
                paymentMethods = listOf("Visa ending in 4242"),
                isDarkMode = _userProfile.value.isDarkMode
            )
        } catch (_: Exception) {
            // Keep default if unauthenticated or offline
        }
    }

    override suspend fun getCategories(): Result<List<Category>> {
        return Result.success(
            listOf(
                Category("all", "All", "ic_all", isSelected = true),
                Category("healthy", "Healthy", "ic_healthy"),
                Category("burgers", "Burgers", "ic_burgers"),
                Category("vegan", "Vegan", "ic_vegan"),
                Category("desserts", "Desserts", "ic_desserts")
            )
        )
    }

    override suspend fun getBanners(): Result<List<Banner>> {
        return Result.success(emptyList())
    }

    override suspend fun getRestaurants(searchQuery: String, categoryId: String?): Result<List<Restaurant>> {
        return try {
            val dtos = api.getRestaurants(
                search = searchQuery.ifBlank { null },
                category = categoryId?.takeIf { it != "all" }
            )
            val restaurants = dtos.map { dto ->
                Restaurant(
                    id = dto.id.toString(),
                    name = dto.name,
                    rating = dto.rating,
                    reviewCount = 120,
                    deliveryTimeMinutes = 30,
                    deliveryFee = 2.50,
                    distanceKm = 1.5,
                    imageUrl = resolveImageUrl(dto.imageUrl),
                    tags = dto.categoryTags,
                    menuItems = emptyList()
                )
            }
            Result.success(restaurants)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getRestaurantById(id: String): Result<Restaurant> {
        return try {
            val intId = id.toIntOrNull() ?: 1
            val dto = api.getRestaurantById(intId)
            val restaurant = Restaurant(
                id = dto.id.toString(),
                name = dto.name,
                rating = dto.rating,
                reviewCount = 150,
                deliveryTimeMinutes = 30,
                deliveryFee = 2.50,
                distanceKm = 1.5,
                imageUrl = resolveImageUrl(dto.imageUrl),
                tags = dto.categoryTags,
                menuItems = dto.menuItems.map { m ->
                    MenuItem(
                        id = m.id.toString(),
                        restaurantId = m.restaurantId.toString(),
                        name = m.name,
                        description = m.description,
                        price = m.price,
                        imageUrl = resolveImageUrl(m.imageUrl),
                        calories = m.calories,
                        category = m.category,
                        isVegan = true,
                        isEcoPackaged = true,
                        rating = 4.8
                    )
                }
            )
            Result.success(restaurant)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun addToCart(menuItem: MenuItem, quantity: Int) {
        val current = _cartItems.value.toMutableList()
        val index = current.indexOfFirst { it.menuItem.id == menuItem.id }
        if (index >= 0) {
            val existing = current[index]
            current[index] = existing.copy(quantity = existing.quantity + quantity)
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
        val currentItems = _cartItems.value
        if (currentItems.isEmpty()) {
            return Result.failure(IllegalStateException("Cart is empty"))
        }

        val quoteItems = currentItems.map {
            OrderQuoteItem(
                menuItemId = it.menuItem.id.toIntOrNull() ?: 1,
                quantity = it.quantity
            )
        }

        return try {
            val quote = api.getOrderQuote(
                OrderQuoteRequest(
                    items = quoteItems,
                    driverTip = tipAmount,
                    promoCode = null
                )
            )

            val orderDto = api.placeOrder(
                PlaceOrderRequest(
                    items = quoteItems,
                    driverTip = tipAmount,
                    promoCode = null,
                    deliveryAddress = deliveryAddress
                )
            )

            val order = Order(
                orderId = orderDto.id.toString(),
                items = currentItems,
                subtotal = quote.subtotal,
                deliveryFee = orderDto.deliveryFee,
                ecoDiscount = orderDto.discount,
                tipAmount = orderDto.driverTip,
                total = orderDto.totalAmount,
                status = when (orderDto.deliveryStatus.lowercase()) {
                    "preparing" -> OrderStatus.PREPARING
                    "out_for_delivery", "out for delivery", "on_the_way" -> OrderStatus.OUT_FOR_DELIVERY
                    "delivered" -> OrderStatus.DELIVERED
                    else -> OrderStatus.PLACED
                },
                estimatedDeliveryMinutes = 25,
                driver = Driver(
                    id = "d1",
                    name = orderDto.driverName ?: "Alex",
                    photoUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=300&q=80",
                    rating = 4.9,
                    phone = "+962790000000",
                    vehicleInfo = "Eco E-Bike"
                ),
                deliveryAddress = orderDto.deliveryAddress
            )

            _activeOrder.value = order
            clearCart()
            Result.success(order)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun toggleDarkMode() {
        _userProfile.value = _userProfile.value.copy(isDarkMode = !_userProfile.value.isDarkMode)
    }
}
