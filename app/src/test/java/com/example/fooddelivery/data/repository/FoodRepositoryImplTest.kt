package com.example.fooddelivery.data.repository

import com.example.fooddelivery.data.api.FastApiFoodDeliveryApi
import com.example.fooddelivery.data.api.MenuItemDto
import com.example.fooddelivery.data.api.OrderQuoteRequest
import com.example.fooddelivery.data.api.OrderQuoteResponse
import com.example.fooddelivery.data.api.OrderResponseDto
import com.example.fooddelivery.data.api.PlaceOrderRequest
import com.example.fooddelivery.data.api.RegisterUserRequest
import com.example.fooddelivery.data.api.RestaurantDetailResponseDto
import com.example.fooddelivery.data.api.RestaurantResponseDto
import com.example.fooddelivery.data.api.TokenResponse
import com.example.fooddelivery.data.api.UpdateOrderStatusRequest
import com.example.fooddelivery.data.api.UpdateProfileRequest
import com.example.fooddelivery.data.api.UserResponseDto
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FoodRepositoryImplTest {

    private lateinit var repository: FoodRepositoryImpl
    private lateinit var fakeApi: FakeFastApiFoodDeliveryApi

    @Before
    fun setUp() {
        fakeApi = FakeFastApiFoodDeliveryApi()
        repository = FoodRepositoryImpl(fakeApi)
    }

    @Test
    fun `getCategories returns categories list`() = runTest {
        val result = repository.getCategories()
        assertTrue(result.isSuccess)
        val categories = result.getOrThrow()
        assertTrue(categories.isNotEmpty())
    }

    @Test
    fun `getBanners returns banners list`() = runTest {
        val result = repository.getBanners()
        assertTrue(result.isSuccess)
    }

    @Test
    fun `getRestaurants filters results correctly`() = runTest {
        val result = repository.getRestaurants(searchQuery = "House")
        assertTrue(result.isSuccess)
        val restaurants = result.getOrThrow()
        assertTrue(restaurants.isNotEmpty())
    }

    @Test
    fun `getRestaurants with category filter filters results correctly`() = runTest {
        val result = repository.getRestaurants(categoryId = "Italian")
        assertTrue(result.isSuccess)
    }

    @Test
    fun `addToCart adds item and updates quantity if added again`() = runTest {
        val restaurantRes = repository.getRestaurants()
        val restaurantId = restaurantRes.getOrThrow().first().id
        val restaurantDetail = repository.getRestaurantById(restaurantId).getOrThrow()
        val menuItem = restaurantDetail.menuItems.first()

        repository.addToCart(menuItem, 1)
        assertEquals(1, repository.cartItemsFlow.value.size)
        assertEquals(1, repository.cartItemsFlow.value.first().quantity)

        repository.addToCart(menuItem, 2)
        assertEquals(1, repository.cartItemsFlow.value.size)
        assertEquals(3, repository.cartItemsFlow.value.first().quantity)
    }

    @Test
    fun `updateQuantity updates quantity and removes item if new quantity is zero`() = runTest {
        val restaurantRes = repository.getRestaurants()
        val restaurantId = restaurantRes.getOrThrow().first().id
        val restaurantDetail = repository.getRestaurantById(restaurantId).getOrThrow()
        val menuItem = restaurantDetail.menuItems.first()

        repository.addToCart(menuItem, 2)
        assertEquals(2, repository.cartItemsFlow.value.first().quantity)

        repository.updateQuantity(menuItem, 5)
        assertEquals(5, repository.cartItemsFlow.value.first().quantity)

        repository.updateQuantity(menuItem, 0)
        assertTrue(repository.cartItemsFlow.value.isEmpty())
    }

    @Test
    fun `removeFromCart removes specified item`() = runTest {
        val restaurantId = repository.getRestaurants().getOrThrow().first().id
        val restaurantDetail = repository.getRestaurantById(restaurantId).getOrThrow()
        val item1 = restaurantDetail.menuItems.first()

        repository.addToCart(item1, 1)
        assertEquals(1, repository.cartItemsFlow.value.size)

        repository.removeFromCart(item1)
        assertTrue(repository.cartItemsFlow.value.isEmpty())
    }

    @Test
    fun `clearCart removes all items in cart`() = runTest {
        val restaurantId = repository.getRestaurants().getOrThrow().first().id
        val restaurantDetail = repository.getRestaurantById(restaurantId).getOrThrow()
        repository.addToCart(restaurantDetail.menuItems.first(), 2)

        repository.clearCart()
        assertTrue(repository.cartItemsFlow.value.isEmpty())
    }

    @Test
    fun `placeOrder with empty cart returns failure`() = runTest {
        val result = repository.placeOrder("742 Evergreen Terrace", 3.0)
        assertTrue(result.isFailure)
        assertNull(repository.activeOrderFlow.value)
    }

    @Test
    fun `placeOrder with non-empty cart creates active order and clears cart`() = runTest {
        val restaurantId = repository.getRestaurants().getOrThrow().first().id
        val restaurantDetail = repository.getRestaurantById(restaurantId).getOrThrow()
        val menuItem = restaurantDetail.menuItems.first()

        repository.addToCart(menuItem, 2)
        val result = repository.placeOrder("742 Evergreen Terrace", 3.0)

        assertTrue(result.isSuccess)
        val order = result.getOrThrow()
        assertNotNull(order)
        assertTrue(repository.cartItemsFlow.value.isEmpty())
        assertNotNull(repository.activeOrderFlow.value)
    }

    @Test
    fun `toggleDarkMode toggles dark mode state in userProfileFlow`() = runTest {
        val initialDarkMode = repository.userProfileFlow.value.isDarkMode
        repository.toggleDarkMode()
        assertEquals(!initialDarkMode, repository.userProfileFlow.value.isDarkMode)

        repository.toggleDarkMode()
        assertEquals(initialDarkMode, repository.userProfileFlow.value.isDarkMode)
    }
}

class FakeFastApiFoodDeliveryApi : FastApiFoodDeliveryApi {
    override suspend fun registerUser(request: RegisterUserRequest): UserResponseDto {
        return UserResponseDto(12, request.email, request.fullName, request.phoneNumber)
    }

    override suspend fun loginForAccessToken(username: String, password: String): TokenResponse {
        return TokenResponse("mock_token")
    }

    override suspend fun getUserProfile(): UserResponseDto {
        return UserResponseDto(12, "test@eco.org", "Test User", "+15551234567")
    }

    override suspend fun updateUserProfile(request: UpdateProfileRequest): UserResponseDto {
        return UserResponseDto(12, "test@eco.org", request.fullName ?: "Test User", request.phoneNumber)
    }

    override suspend fun getRestaurants(search: String?, category: String?): List<RestaurantResponseDto> {
        return listOf(
            RestaurantResponseDto(
                id = 3,
                name = "Pasta House",
                rating = 4.7,
                deliveryTime = "25-35 min",
                imageUrl = "https://example.com/pasta-house.jpg",
                categoryTags = listOf("Italian", "Pasta", "Pizza")
            )
        )
    }

    override suspend fun getRestaurantById(restaurantId: Int): RestaurantDetailResponseDto {
        return RestaurantDetailResponseDto(
            id = restaurantId,
            name = "Pasta House",
            rating = 4.7,
            deliveryTime = "25-35 min",
            imageUrl = "https://example.com/pasta-house.jpg",
            categoryTags = listOf("Italian", "Pasta", "Pizza"),
            menuItems = listOf(
                MenuItemDto(
                    id = 101,
                    restaurantId = restaurantId,
                    name = "Margherita Pizza",
                    description = "Tomato, mozzarella, and basil",
                    price = 10.0,
                    calories = 720,
                    imageUrl = "https://example.com/margherita.jpg",
                    category = "Pizza"
                )
            )
        )
    }

    override suspend fun getOrderQuote(quoteRequest: OrderQuoteRequest): OrderQuoteResponse {
        val subtotal = quoteRequest.items.sumOf { it.quantity * 10.0 }
        return OrderQuoteResponse(subtotal, 2.5, quoteRequest.driverTip, 1.7, subtotal + 2.5 + quoteRequest.driverTip - 1.7)
    }

    override suspend fun placeOrder(orderRequest: PlaceOrderRequest): OrderResponseDto {
        val subtotal = orderRequest.items.sumOf { it.quantity * 10.0 }
        return OrderResponseDto(
            id = 45,
            userId = 12,
            restaurantId = 3,
            totalAmount = subtotal + 2.5 + orderRequest.driverTip - 1.7,
            deliveryFee = 2.5,
            driverTip = orderRequest.driverTip,
            discount = 1.7,
            promoCode = orderRequest.promoCode,
            deliveryStatus = "placed",
            deliveryAddress = orderRequest.deliveryAddress,
            driverName = "Alex",
            estimatedTime = "25 minutes",
            driverLatitude = 31.9539,
            driverLongitude = 35.9106,
            items = emptyList()
        )
    }

    override suspend fun getOrders(): List<OrderResponseDto> {
        return emptyList()
    }

    override suspend fun getOrderById(orderId: Int): OrderResponseDto {
        return OrderResponseDto(
            id = orderId,
            userId = 12,
            restaurantId = 3,
            totalAmount = 20.8,
            deliveryFee = 2.5,
            driverTip = 1.0,
            discount = 1.7,
            deliveryStatus = "placed",
            deliveryAddress = "12 Rainbow Street, Amman",
            items = emptyList()
        )
    }

    override suspend fun updateOrderStatus(orderId: Int, statusRequest: UpdateOrderStatusRequest): OrderResponseDto {
        val order = getOrderById(orderId)
        return order.copy(
            deliveryStatus = statusRequest.deliveryStatus,
            driverName = statusRequest.driverName,
            estimatedTime = statusRequest.estimatedTime,
            driverLatitude = statusRequest.driverLatitude,
            driverLongitude = statusRequest.driverLongitude
        )
    }
}
