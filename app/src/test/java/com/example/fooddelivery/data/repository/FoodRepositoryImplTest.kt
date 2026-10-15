package com.example.fooddelivery.data.repository

import com.example.fooddelivery.data.api.FoodDeliveryApi
import com.example.fooddelivery.data.api.dto.BannerDto
import com.example.fooddelivery.data.api.dto.CategoryDto
import com.example.fooddelivery.data.api.dto.MenuItemDto
import com.example.fooddelivery.data.api.dto.OrderRequestDto
import com.example.fooddelivery.data.api.dto.OrderResponseDto
import com.example.fooddelivery.data.api.dto.RestaurantDto
import com.example.fooddelivery.data.api.dto.UserProfileDto
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FoodRepositoryImplTest {

    private lateinit var repository: FoodRepositoryImpl
    private lateinit var fakeApi: FakeFoodDeliveryApi

    @Before
    fun setUp() {
        fakeApi = FakeFoodDeliveryApi()
        repository = FoodRepositoryImpl(fakeApi)
    }

    @Test
    fun `getCategories returns categories list from mock`() = runTest {
        val result = repository.getCategories()
        assertTrue(result.isSuccess)
        val categories = result.getOrThrow()
        assertTrue(categories.isNotEmpty())
    }

    @Test
    fun `getBanners returns banners list from mock`() = runTest {
        val result = repository.getBanners()
        assertTrue(result.isSuccess)
        val banners = result.getOrThrow()
        assertTrue(banners.isNotEmpty())
    }

    @Test
    fun `getRestaurants with search query filters results correctly`() = runTest {
        val result = repository.getRestaurants(searchQuery = "Burger")
        assertTrue(result.isSuccess)
        val restaurants = result.getOrThrow()
        assertTrue(restaurants.all { it.name.contains("Burger", ignoreCase = true) || it.tags.any { tag -> tag.contains("Burger", ignoreCase = true) } })
    }

    @Test
    fun `getRestaurants with category filter filters results correctly`() = runTest {
        val result = repository.getRestaurants(categoryId = "healthy")
        assertTrue(result.isSuccess)
        val restaurants = result.getOrThrow()
        assertTrue(restaurants.isNotEmpty())
    }

    @Test
    fun `addToCart adds item and updates quantity if added again`() = runTest {
        val restaurantRes = repository.getRestaurants()
        val menuItem = restaurantRes.getOrThrow().first().menuItems.first()

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
        val menuItem = restaurantRes.getOrThrow().first().menuItems.first()

        repository.addToCart(menuItem, 2)
        assertEquals(2, repository.cartItemsFlow.value.first().quantity)

        repository.updateQuantity(menuItem, 5)
        assertEquals(5, repository.cartItemsFlow.value.first().quantity)

        repository.updateQuantity(menuItem, 0)
        assertTrue(repository.cartItemsFlow.value.isEmpty())
    }

    @Test
    fun `removeFromCart removes specified item`() = runTest {
        val menuItems = repository.getRestaurants().getOrThrow().first().menuItems
        val item1 = menuItems[0]
        val item2 = menuItems[1]

        repository.addToCart(item1, 1)
        repository.addToCart(item2, 1)
        assertEquals(2, repository.cartItemsFlow.value.size)

        repository.removeFromCart(item1)
        assertEquals(1, repository.cartItemsFlow.value.size)
        assertEquals(item2.id, repository.cartItemsFlow.value.first().menuItem.id)
    }

    @Test
    fun `clearCart removes all items in cart`() = runTest {
        val menuItems = repository.getRestaurants().getOrThrow().first().menuItems
        repository.addToCart(menuItems[0], 2)
        repository.addToCart(menuItems[1], 1)

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
    fun `placeOrder with non-empty cart creates active order, clears cart and updates plastic saved`() = runTest {
        val menuItem = repository.getRestaurants().getOrThrow().first().menuItems.first()
        val initialPlasticSaved = repository.userProfileFlow.value.plasticSavedKg

        repository.addToCart(menuItem, 2)
        val result = repository.placeOrder("742 Evergreen Terrace", 3.0)

        assertTrue(result.isSuccess)
        val order = result.getOrThrow()
        assertNotNull(order)
        assertTrue(repository.cartItemsFlow.value.isEmpty())
        assertNotNull(repository.activeOrderFlow.value)
        assertEquals(order.orderId, repository.activeOrderFlow.value?.orderId)
        assertTrue(repository.userProfileFlow.value.plasticSavedKg > initialPlasticSaved)
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

class FakeFoodDeliveryApi : FoodDeliveryApi {
    override suspend fun getCategories(): List<CategoryDto> {
        throw Exception("Net Error")
    }

    override suspend fun getBanners(): List<BannerDto> {
        throw Exception("Net Error")
    }

    override suspend fun getRestaurants(searchQuery: String?, categoryId: String?): List<RestaurantDto> {
        throw Exception("Net Error")
    }

    override suspend fun getRestaurantById(id: String): RestaurantDto {
        throw Exception("Net Error")
    }

    override suspend fun placeOrder(orderRequest: OrderRequestDto): OrderResponseDto {
        throw Exception("Net Error")
    }

    override suspend fun getUserProfile(): UserProfileDto {
        throw Exception("Net Error")
    }
}
