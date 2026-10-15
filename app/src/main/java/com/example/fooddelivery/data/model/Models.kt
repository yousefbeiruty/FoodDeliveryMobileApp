package com.example.fooddelivery.data.model

data class Category(
    val id: String,
    val name: String,
    val iconName: String,
    val isSelected: Boolean = false
)

data class Banner(
    val id: String,
    val title: String,
    val subtitle: String,
    val promoCode: String,
    val imageUrl: String
)

data class MenuItem(
    val id: String,
    val restaurantId: String,
    val name: String,
    val description: String,
    val price: Double,
    val imageUrl: String,
    val calories: Int,
    val category: String,
    val isVegan: Boolean = false,
    val isEcoPackaged: Boolean = true,
    val rating: Double = 4.8
)

data class Restaurant(
    val id: String,
    val name: String,
    val rating: Double,
    val reviewCount: Int,
    val deliveryTimeMinutes: Int,
    val deliveryFee: Double,
    val distanceKm: Double,
    val imageUrl: String,
    val tags: List<String>,
    val menuItems: List<MenuItem> = emptyList()
)

data class CartItem(
    val menuItem: MenuItem,
    val quantity: Int
) {
    val totalPrice: Double get() = menuItem.price * quantity
}

data class Driver(
    val id: String,
    val name: String,
    val photoUrl: String,
    val rating: Double,
    val phone: String,
    val vehicleInfo: String
)

enum class OrderStatus {
    PLACED,
    PREPARING,
    OUT_FOR_DELIVERY,
    DELIVERED
}

data class Order(
    val orderId: String,
    val items: List<CartItem>,
    val subtotal: Double,
    val deliveryFee: Double,
    val ecoDiscount: Double,
    val tipAmount: Double,
    val total: Double,
    val status: OrderStatus,
    val estimatedDeliveryMinutes: Int,
    val driver: Driver,
    val deliveryAddress: String
)

data class UserProfile(
    val name: String,
    val email: String,
    val phone: String,
    val defaultAddress: String,
    val plasticSavedKg: Double,
    val co2OffsetKg: Double,
    val treesPlanted: Int,
    val savedAddresses: List<String>,
    val paymentMethods: List<String>,
    val isDarkMode: Boolean = false
)
