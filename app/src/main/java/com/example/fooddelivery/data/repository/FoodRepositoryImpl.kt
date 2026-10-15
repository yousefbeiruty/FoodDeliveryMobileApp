package com.example.fooddelivery.data.repository

import com.example.fooddelivery.data.api.FoodDeliveryApi
import com.example.fooddelivery.data.model.Banner
import com.example.fooddelivery.data.model.CartItem
import com.example.fooddelivery.data.model.Category
import com.example.fooddelivery.data.model.Driver
import com.example.fooddelivery.data.model.MenuItem
import com.example.fooddelivery.data.model.Order
import com.example.fooddelivery.data.model.OrderStatus
import com.example.fooddelivery.data.model.Restaurant
import com.example.fooddelivery.data.model.UserProfile
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FoodRepositoryImpl @Inject constructor(
    private val api: FoodDeliveryApi
) : FoodRepository {

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
            savedAddresses = listOf(
                "742 Evergreen Terrace, Green City",
                "100 Eco Tech Park, Suite 400",
                "15 Pine Crest Way, San Francisco"
            ),
            paymentMethods = listOf(
                "Visa ending in 4242 (Eco Card)",
                "Apple Pay",
                "FreshBite Eco Wallet ($45.00)"
            ),
            isDarkMode = false
        )
    )
    override val userProfileFlow: StateFlow<UserProfile> = _userProfile.asStateFlow()

    override suspend fun getCategories(): Result<List<Category>> {
        delay(400) // Simulate fast response
        return try {
            val apiCategories = api.getCategories()
            Result.success(apiCategories.map { Category(it.id, it.name, it.iconName) })
        } catch (e: Exception) {
            // Fallback mock categories
            Result.success(mockCategories)
        }
    }

    override suspend fun getBanners(): Result<List<Banner>> {
        delay(300)
        return try {
            val apiBanners = api.getBanners()
            Result.success(apiBanners.map { Banner(it.id, it.title, it.subtitle, it.promoCode, it.imageUrl) })
        } catch (e: Exception) {
            Result.success(mockBanners)
        }
    }

    override suspend fun getRestaurants(searchQuery: String, categoryId: String?): Result<List<Restaurant>> {
        delay(600) // Realistic net delay for shimmer testing
        return try {
            val apiList = api.getRestaurants(searchQuery, categoryId)
            val domainList = apiList.map { dto ->
                Restaurant(
                    id = dto.id,
                    name = dto.name,
                    rating = dto.rating,
                    reviewCount = dto.reviewCount,
                    deliveryTimeMinutes = dto.deliveryTimeMinutes,
                    deliveryFee = dto.deliveryFee,
                    distanceKm = dto.distanceKm,
                    imageUrl = dto.imageUrl,
                    tags = dto.tags,
                    menuItems = dto.menuItems.map { m ->
                        MenuItem(
                            id = m.id,
                            restaurantId = m.restaurantId,
                            name = m.name,
                            description = m.description,
                            price = m.price,
                            imageUrl = m.imageUrl,
                            calories = m.calories,
                            category = m.category,
                            isVegan = m.isVegan,
                            isEcoPackaged = m.isEcoPackaged,
                            rating = m.rating
                        )
                    }
                )
            }
            Result.success(filterRestaurants(domainList, searchQuery, categoryId))
        } catch (e: Exception) {
            Result.success(filterRestaurants(mockRestaurants, searchQuery, categoryId))
        }
    }

    override suspend fun getRestaurantById(id: String): Result<Restaurant> {
        delay(400)
        return try {
            val dto = api.getRestaurantById(id)
            val restaurant = Restaurant(
                id = dto.id,
                name = dto.name,
                rating = dto.rating,
                reviewCount = dto.reviewCount,
                deliveryTimeMinutes = dto.deliveryTimeMinutes,
                deliveryFee = dto.deliveryFee,
                distanceKm = dto.distanceKm,
                imageUrl = dto.imageUrl,
                tags = dto.tags,
                menuItems = dto.menuItems.map { m ->
                    MenuItem(
                        id = m.id,
                        restaurantId = m.restaurantId,
                        name = m.name,
                        description = m.description,
                        price = m.price,
                        imageUrl = m.imageUrl,
                        calories = m.calories,
                        category = m.category,
                        isVegan = m.isVegan,
                        isEcoPackaged = m.isEcoPackaged,
                        rating = m.rating
                    )
                }
            )
            Result.success(restaurant)
        } catch (e: Exception) {
            val found = mockRestaurants.firstOrNull { it.id == id } ?: mockRestaurants.first()
            Result.success(found)
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
        delay(1200) // Simulate processing order
        val currentItems = _cartItems.value
        if (currentItems.isEmpty()) {
            return Result.failure(IllegalStateException("Cart is empty"))
        }

        val subtotal = currentItems.sumOf { it.totalPrice }
        val deliveryFee = 2.99
        val ecoDiscount = 1.50 // Special green discount
        val total = (subtotal + deliveryFee + tipAmount - ecoDiscount).coerceAtLeast(0.0)

        val order = Order(
            orderId = "FB-" + (10000..99999).random(),
            items = currentItems,
            subtotal = subtotal,
            deliveryFee = deliveryFee,
            ecoDiscount = ecoDiscount,
            tipAmount = tipAmount,
            total = total,
            status = OrderStatus.PLACED,
            estimatedDeliveryMinutes = 25,
            driver = mockDriver,
            deliveryAddress = deliveryAddress.ifBlank { _userProfile.value.defaultAddress }
        )

        _activeOrder.value = order
        clearCart()

        // Update Eco Impact stats
        val updatedProfile = _userProfile.value.copy(
            plasticSavedKg = _userProfile.value.plasticSavedKg + 0.25,
            co2OffsetKg = _userProfile.value.co2OffsetKg + 1.2
        )
        _userProfile.value = updatedProfile

        return Result.success(order)
    }

    override fun toggleDarkMode() {
        _userProfile.value = _userProfile.value.copy(isDarkMode = !_userProfile.value.isDarkMode)
    }

    private fun filterRestaurants(
        list: List<Restaurant>,
        searchQuery: String,
        categoryId: String?
    ): List<Restaurant> {
        return list.filter { r ->
            val matchesQuery = searchQuery.isBlank() ||
                    r.name.contains(searchQuery, ignoreCase = true) ||
                    r.tags.any { it.contains(searchQuery, ignoreCase = true) } ||
                    r.menuItems.any { it.name.contains(searchQuery, ignoreCase = true) }

            val matchesCategory = categoryId.isNullOrBlank() || categoryId == "all" ||
                    r.tags.any { it.equals(categoryId, ignoreCase = true) } ||
                    r.menuItems.any { it.category.equals(categoryId, ignoreCase = true) }

            matchesQuery && matchesCategory
        }
    }

    companion object {
        val mockCategories = listOf(
            Category("all", "All", "ic_all", isSelected = true),
            Category("healthy", "Healthy", "ic_healthy"),
            Category("burgers", "Burgers", "ic_burgers"),
            Category("vegan", "Vegan", "ic_vegan"),
            Category("desserts", "Desserts", "ic_desserts"),
            Category("drinks", "Eco Drinks", "ic_drinks")
        )

        val mockBanners = listOf(
            Banner(
                id = "b1",
                title = "20% Eco Discount!",
                subtitle = "On all plant-based meals today",
                promoCode = "FRESH20",
                imageUrl = "https://images.unsplash.com/photo-1540420773420-3366772f4999?auto=format&fit=crop&w=800&q=80"
            ),
            Banner(
                id = "b2",
                title = "Zero Plastic Delivery",
                subtitle = "100% biodegradable packaging guaranteed",
                promoCode = "GREENBITE",
                imageUrl = "https://images.unsplash.com/photo-1512621776951-a57141f2eefd?auto=format&fit=crop&w=800&q=80"
            )
        )

        val mockDriver = Driver(
            id = "d1",
            name = "Marcus Vance",
            photoUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=300&q=80",
            rating = 4.9,
            phone = "+1 (555) 987-6543",
            vehicleInfo = "Eco E-Bike (Green Bolt)"
        )

        val mockRestaurants = listOf(
            Restaurant(
                id = "r1",
                name = "Green Harvest Bowl Bar",
                rating = 4.9,
                reviewCount = 382,
                deliveryTimeMinutes = 20,
                deliveryFee = 1.99,
                distanceKm = 1.2,
                imageUrl = "https://images.unsplash.com/photo-1512621776951-a57141f2eefd?auto=format&fit=crop&w=800&q=80",
                tags = listOf("Healthy", "Vegan", "Eco Packaged"),
                menuItems = listOf(
                    MenuItem(
                        id = "m1",
                        restaurantId = "r1",
                        name = "Avocado & Quinoa Power Bowl",
                        description = "Organic quinoa, fresh Hass avocado, roasted sweet potatoes, kale, pumpkin seeds & tahini dressing.",
                        price = 12.99,
                        imageUrl = "https://images.unsplash.com/photo-1540420773420-3366772f4999?auto=format&fit=crop&w=600&q=80",
                        calories = 420,
                        category = "healthy",
                        isVegan = true,
                        isEcoPackaged = true,
                        rating = 4.9
                    ),
                    MenuItem(
                        id = "m2",
                        restaurantId = "r1",
                        name = "Mediterranean Chickpea Crunch",
                        description = "Crispy spiced chickpeas, cucumbers, heirloom tomatoes, kalamata olives and vegan feta.",
                        price = 11.49,
                        imageUrl = "https://images.unsplash.com/photo-1511690656952-34342bb7c2f2?auto=format&fit=crop&w=600&q=80",
                        calories = 380,
                        category = "healthy",
                        isVegan = true,
                        isEcoPackaged = true,
                        rating = 4.8
                    ),
                    MenuItem(
                        id = "m3",
                        restaurantId = "r1",
                        name = "Green Energy Matcha Smoothie",
                        description = "Ceremonial matcha, oat milk, spinach, banana and chia seed booster.",
                        price = 6.99,
                        imageUrl = "https://images.unsplash.com/photo-1553530666-ba11a7da3888?auto=format&fit=crop&w=600&q=80",
                        calories = 210,
                        category = "drinks",
                        isVegan = true,
                        isEcoPackaged = true,
                        rating = 4.9
                    )
                )
            ),
            Restaurant(
                id = "r2",
                name = "EcoBurger & Plant Kitchen",
                rating = 4.7,
                reviewCount = 215,
                deliveryTimeMinutes = 25,
                deliveryFee = 2.49,
                distanceKm = 2.1,
                imageUrl = "https://images.unsplash.com/photo-1550547660-d9450f859349?auto=format&fit=crop&w=800&q=80",
                tags = listOf("Burgers", "Vegan", "Comfort Food"),
                menuItems = listOf(
                    MenuItem(
                        id = "m4",
                        restaurantId = "r2",
                        name = "The Ultimate Eco Smash Burger",
                        description = "Beyond meat double patty, vegan cheddar, caramelized onions, eco sauce on brioche bun.",
                        price = 14.99,
                        imageUrl = "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?auto=format&fit=crop&w=600&q=80",
                        calories = 610,
                        category = "burgers",
                        isVegan = true,
                        isEcoPackaged = true,
                        rating = 4.9
                    ),
                    MenuItem(
                        id = "m5",
                        restaurantId = "r2",
                        name = "Sweet Potato Fries & Garlic Aioli",
                        description = "Hand-cut organic sweet potato fries served with house-made vegan garlic aioli.",
                        price = 5.99,
                        imageUrl = "https://images.unsplash.com/photo-1573080496219-bb080dd4f877?auto=format&fit=crop&w=600&q=80",
                        calories = 310,
                        category = "burgers",
                        isVegan = true,
                        isEcoPackaged = true,
                        rating = 4.7
                    )
                )
            ),
            Restaurant(
                id = "r3",
                name = "Zen Blossom Vegan Sushi",
                rating = 4.9,
                reviewCount = 420,
                deliveryTimeMinutes = 30,
                deliveryFee = 2.99,
                distanceKm = 3.0,
                imageUrl = "https://images.unsplash.com/photo-1579871494447-9811cf80d66c?auto=format&fit=crop&w=800&q=80",
                tags = listOf("Vegan", "Asian", "Fresh"),
                menuItems = listOf(
                    MenuItem(
                        id = "m6",
                        restaurantId = "r3",
                        name = "Spicy Avocado & Mango Roll",
                        description = "Ripe mango, spicy mayo, avocado, tempura crunch wrapped in purple forbidden rice.",
                        price = 13.50,
                        imageUrl = "https://images.unsplash.com/photo-1611143669185-af224c5e3252?auto=format&fit=crop&w=600&q=80",
                        calories = 340,
                        category = "vegan",
                        isVegan = true,
                        isEcoPackaged = true,
                        rating = 4.9
                    ),
                    MenuItem(
                        id = "m7",
                        restaurantId = "r3",
                        name = "Edamame Sea Salt Pods",
                        description = "Steamed organic soy beans sprinkled with coarse pink Himalayan sea salt.",
                        price = 4.99,
                        imageUrl = "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?auto=format&fit=crop&w=600&q=80",
                        calories = 180,
                        category = "vegan",
                        isVegan = true,
                        isEcoPackaged = true,
                        rating = 4.8
                    )
                )
            ),
            Restaurant(
                id = "r4",
                name = "Organic Berry & Acai Treats",
                rating = 4.8,
                reviewCount = 189,
                deliveryTimeMinutes = 18,
                deliveryFee = 1.49,
                distanceKm = 0.9,
                imageUrl = "https://images.unsplash.com/photo-1590301157890-4810ed352733?auto=format&fit=crop&w=800&q=80",
                tags = listOf("Desserts", "Healthy", "Organic"),
                menuItems = listOf(
                    MenuItem(
                        id = "m8",
                        restaurantId = "r4",
                        name = "Superfood Dragonfruit Acai Bowl",
                        description = "Pure pitaya & acai base topped with coconut flakes, chia seeds, sliced strawberries & hemp hearts.",
                        price = 10.99,
                        imageUrl = "https://images.unsplash.com/photo-1590301157890-4810ed352733?auto=format&fit=crop&w=600&q=80",
                        calories = 290,
                        category = "desserts",
                        isVegan = true,
                        isEcoPackaged = true,
                        rating = 4.9
                    ),
                    MenuItem(
                        id = "m9",
                        restaurantId = "r4",
                        name = "Raw Cacao Energy Truffles",
                        description = "3 pieces of artisan date, almond and organic cacao truffles.",
                        price = 5.49,
                        imageUrl = "https://images.unsplash.com/photo-1606313564200-e75d5e30476c?auto=format&fit=crop&w=600&q=80",
                        calories = 220,
                        category = "desserts",
                        isVegan = true,
                        isEcoPackaged = true,
                        rating = 4.8
                    )
                )
            )
        )
    }
}
