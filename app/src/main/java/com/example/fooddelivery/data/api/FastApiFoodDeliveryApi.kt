package com.example.fooddelivery.data.api

import com.example.fooddelivery.data.api.dto.OrderRequestDto
import com.example.fooddelivery.data.api.dto.OrderResponseDto
import com.example.fooddelivery.data.api.dto.RestaurantDto
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
    @SerialName("username") val username: String,
    @SerialName("email") val email: String,
    @SerialName("password") val password: String
)

@Serializable
data class TokenResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("token_type") val tokenType: String = "bearer"
)

@Serializable
data class OrderQuoteRequest(
    @SerialName("restaurant_id") val restaurantId: String,
    @SerialName("items") val items: List<com.example.fooddelivery.data.api.dto.OrderItemDto>,
    @SerialName("promo_code") val promoCode: String? = null
)

@Serializable
data class OrderQuoteResponse(
    @SerialName("subtotal") val subtotal: Double,
    @SerialName("delivery_fee") val deliveryFee: Double,
    @SerialName("eco_discount") val ecoDiscount: Double,
    @SerialName("total") val total: Double
)

@Serializable
data class UpdateOrderStatusRequest(
    @SerialName("status") val status: String
)

interface FastApiFoodDeliveryApi {

    @POST("api/auth/register")
    suspend fun registerUser(
        @Body request: RegisterUserRequest
    ): Unit

    @FormUrlEncoded
    @POST("api/auth/token")
    suspend fun loginForAccessToken(
        @Field("username") username: String,
        @Field("password") password: String
    ): TokenResponse

    @GET("api/restaurants")
    suspend fun getRestaurants(
        @Query("search") search: String? = null,
        @Query("category") category: String? = null
    ): List<RestaurantDto>

    @GET("api/restaurants/{id}")
    suspend fun getRestaurantById(
        @Path("id") id: String
    ): RestaurantDto

    @POST("api/orders/quote")
    suspend fun getOrderQuote(
        @Body quoteRequest: OrderQuoteRequest
    ): OrderQuoteResponse

    @POST("api/orders/")
    suspend fun placeOrder(
        @Body orderRequest: OrderRequestDto
    ): OrderResponseDto

    @GET("api/orders/")
    suspend fun getOrders(): List<OrderResponseDto>

    @GET("api/orders/{id}")
    suspend fun getOrderById(
        @Path("id") id: String
    ): OrderResponseDto

    @PATCH("api/orders/{id}/status")
    suspend fun updateOrderStatus(
        @Path("id") id: String,
        @Body statusRequest: UpdateOrderStatusRequest
    ): OrderResponseDto
}
