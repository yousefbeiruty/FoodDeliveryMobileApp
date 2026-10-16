package com.example.fooddelivery.data.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

@Serializable
data class RegisterUserRequest(
    @SerialName("email") val email: String,
    @SerialName("password") val password: String,
    @SerialName("full_name") val fullName: String,
    @SerialName("phone_number") val phoneNumber: String? = null
)

@Serializable
data class UserResponseDto(
    @SerialName("id") val id: Int,
    @SerialName("email") val email: String,
    @SerialName("full_name") val fullName: String,
    @SerialName("phone_number") val phoneNumber: String? = null,
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class UpdateProfileRequest(
    @SerialName("full_name") val fullName: String? = null,
    @SerialName("phone_number") val phoneNumber: String? = null
)

@Serializable
data class TokenResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("token_type") val tokenType: String = "bearer"
)

@Serializable
data class RestaurantResponseDto(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String,
    @SerialName("rating") val rating: Double,
    @SerialName("delivery_time") val deliveryTime: String,
    @SerialName("image_url") val imageUrl: String,
    @SerialName("category_tags") val categoryTags: List<String>
)

@Serializable
data class MenuItemDto(
    @SerialName("id") val id: Int,
    @SerialName("restaurant_id") val restaurantId: Int,
    @SerialName("name") val name: String,
    @SerialName("description") val description: String,
    @SerialName("price") val price: Double,
    @SerialName("calories") val calories: Int,
    @SerialName("image_url") val imageUrl: String,
    @SerialName("category") val category: String
)

@Serializable
data class RestaurantDetailResponseDto(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String,
    @SerialName("rating") val rating: Double,
    @SerialName("delivery_time") val deliveryTime: String,
    @SerialName("image_url") val imageUrl: String,
    @SerialName("category_tags") val categoryTags: List<String>,
    @SerialName("menu_items") val menuItems: List<MenuItemDto>
)

@Serializable
data class OrderQuoteItem(
    @SerialName("menu_item_id") val menuItemId: Int,
    @SerialName("quantity") val quantity: Int
)

@Serializable
data class OrderQuoteRequest(
    @SerialName("items") val items: List<OrderQuoteItem>,
    @SerialName("driver_tip") val driverTip: Double = 0.0,
    @SerialName("promo_code") val promoCode: String? = null
)

@Serializable
data class OrderQuoteResponse(
    @SerialName("subtotal") val subtotal: Double,
    @SerialName("delivery_fee") val deliveryFee: Double,
    @SerialName("driver_tip") val driverTip: Double,
    @SerialName("discount") val discount: Double,
    @SerialName("total_amount") val totalAmount: Double
)

@Serializable
data class PlaceOrderRequest(
    @SerialName("items") val items: List<OrderQuoteItem>,
    @SerialName("driver_tip") val driverTip: Double = 0.0,
    @SerialName("promo_code") val promoCode: String? = null,
    @SerialName("delivery_address") val deliveryAddress: String
)

@Serializable
data class OrderItemResponse(
    @SerialName("id") val id: Int,
    @SerialName("menu_item_id") val menuItemId: Int,
    @SerialName("name") val name: String,
    @SerialName("quantity") val quantity: Int,
    @SerialName("unit_price") val unitPrice: Double
)

@Serializable
data class OrderResponseDto(
    @SerialName("id") val id: Int,
    @SerialName("user_id") val userId: Int,
    @SerialName("restaurant_id") val restaurantId: Int,
    @SerialName("total_amount") val totalAmount: Double,
    @SerialName("delivery_fee") val deliveryFee: Double,
    @SerialName("driver_tip") val driverTip: Double,
    @SerialName("discount") val discount: Double,
    @SerialName("promo_code") val promoCode: String? = null,
    @SerialName("delivery_status") val deliveryStatus: String,
    @SerialName("delivery_address") val deliveryAddress: String,
    @SerialName("driver_name") val driverName: String? = null,
    @SerialName("estimated_time") val estimatedTime: String? = null,
    @SerialName("driver_latitude") val driverLatitude: Double? = null,
    @SerialName("driver_longitude") val driverLongitude: Double? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("items") val items: List<OrderItemResponse>
)

@Serializable
data class UpdateOrderStatusRequest(
    @SerialName("delivery_status") val deliveryStatus: String,
    @SerialName("driver_name") val driverName: String? = null,
    @SerialName("estimated_time") val estimatedTime: String? = null,
    @SerialName("driver_latitude") val driverLatitude: Double? = null,
    @SerialName("driver_longitude") val driverLongitude: Double? = null
)

@Serializable
data class OrderTrackingUpdate(
    @SerialName("order_id") val orderId: Int,
    @SerialName("delivery_status") val deliveryStatus: String,
    @SerialName("driver_name") val driverName: String? = null,
    @SerialName("estimated_time") val estimatedTime: String? = null,
    @SerialName("driver_latitude") val driverLatitude: Double? = null,
    @SerialName("driver_longitude") val driverLongitude: Double? = null
)

interface FastApiFoodDeliveryApi {

    @POST("api/auth/register")
    suspend fun registerUser(
        @Body request: RegisterUserRequest
    ): UserResponseDto

    @FormUrlEncoded
    @POST("api/auth/token")
    suspend fun loginForAccessToken(
        @Field("username") username: String,
        @Field("password") password: String
    ): TokenResponse

    @GET("api/users/me")
    suspend fun getUserProfile(): UserResponseDto

    @PATCH("api/users/me")
    suspend fun updateUserProfile(
        @Body request: UpdateProfileRequest
    ): UserResponseDto

    @GET("api/restaurants")
    suspend fun getRestaurants(
        @Query("search") search: String? = null,
        @Query("category") category: String? = null
    ): List<RestaurantResponseDto>

    @GET("api/restaurants/{restaurant_id}")
    suspend fun getRestaurantById(
        @Path("restaurant_id") restaurantId: Int
    ): RestaurantDetailResponseDto

    @POST("api/orders/quote")
    suspend fun getOrderQuote(
        @Body quoteRequest: OrderQuoteRequest
    ): OrderQuoteResponse

    @POST("api/orders/")
    suspend fun placeOrder(
        @Body orderRequest: PlaceOrderRequest
    ): OrderResponseDto

    @GET("api/orders/")
    suspend fun getOrders(): List<OrderResponseDto>

    @GET("api/orders/{order_id}")
    suspend fun getOrderById(
        @Path("order_id") orderId: Int
    ): OrderResponseDto

    @PATCH("api/orders/{order_id}/status")
    suspend fun updateOrderStatus(
        @Path("order_id") orderId: Int,
        @Body statusRequest: UpdateOrderStatusRequest
    ): OrderResponseDto
}
