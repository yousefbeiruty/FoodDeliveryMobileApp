package com.example.fooddelivery.data.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CategoryDto(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
    @SerialName("icon_name") val iconName: String
)

@Serializable
data class BannerDto(
    @SerialName("id") val id: String,
    @SerialName("title") val title: String,
    @SerialName("subtitle") val subtitle: String,
    @SerialName("promo_code") val promoCode: String,
    @SerialName("image_url") val imageUrl: String
)

@Serializable
data class MenuItemDto(
    @SerialName("id") val id: String,
    @SerialName("restaurant_id") val restaurantId: String,
    @SerialName("name") val name: String,
    @SerialName("description") val description: String,
    @SerialName("price") val price: Double,
    @SerialName("image_url") val imageUrl: String,
    @SerialName("calories") val calories: Int,
    @SerialName("category") val category: String,
    @SerialName("is_vegan") val isVegan: Boolean = false,
    @SerialName("is_eco_packaged") val isEcoPackaged: Boolean = true,
    @SerialName("rating") val rating: Double = 4.8
)

@Serializable
data class RestaurantDto(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
    @SerialName("rating") val rating: Double,
    @SerialName("review_count") val reviewCount: Int,
    @SerialName("delivery_time_minutes") val deliveryTimeMinutes: Int,
    @SerialName("delivery_fee") val deliveryFee: Double,
    @SerialName("distance_km") val distanceKm: Double,
    @SerialName("image_url") val imageUrl: String,
    @SerialName("tags") val tags: List<String>,
    @SerialName("menu_items") val menuItems: List<MenuItemDto> = emptyList()
)

@Serializable
data class OrderRequestDto(
    @SerialName("restaurant_id") val restaurantId: String,
    @SerialName("items") val items: List<OrderItemDto>,
    @SerialName("tip_amount") val tipAmount: Double,
    @SerialName("delivery_address") val deliveryAddress: String
)

@Serializable
data class OrderItemDto(
    @SerialName("menu_item_id") val menuItemId: String,
    @SerialName("quantity") val quantity: Int
)

@Serializable
data class OrderResponseDto(
    @SerialName("order_id") val orderId: String,
    @SerialName("status") val status: String,
    @SerialName("estimated_delivery_minutes") val estimatedDeliveryMinutes: Int,
    @SerialName("total") val total: Double
)

@Serializable
data class UserProfileDto(
    @SerialName("name") val name: String,
    @SerialName("email") val email: String,
    @SerialName("phone") val phone: String,
    @SerialName("default_address") val defaultAddress: String,
    @SerialName("plastic_saved_kg") val plasticSavedKg: Double,
    @SerialName("co2_offset_kg") val co2OffsetKg: Double,
    @SerialName("trees_planted") val treesPlanted: Int,
    @SerialName("saved_addresses") val savedAddresses: List<String>,
    @SerialName("payment_methods") val paymentMethods: List<String>
)
