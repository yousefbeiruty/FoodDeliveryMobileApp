package com.example.fooddelivery.data.api

import com.example.fooddelivery.data.api.dto.BannerDto
import com.example.fooddelivery.data.api.dto.CategoryDto
import com.example.fooddelivery.data.api.dto.OrderRequestDto
import com.example.fooddelivery.data.api.dto.OrderResponseDto
import com.example.fooddelivery.data.api.dto.RestaurantDto
import com.example.fooddelivery.data.api.dto.UserProfileDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface FoodDeliveryApi {

    @GET("categories")
    suspend fun getCategories(): List<CategoryDto>

    @GET("banners")
    suspend fun getBanners(): List<BannerDto>

    @GET("restaurants")
    suspend fun getRestaurants(
        @Query("search") searchQuery: String? = null,
        @Query("category") categoryId: String? = null
    ): List<RestaurantDto>

    @GET("restaurants/{id}")
    suspend fun getRestaurantById(
        @Path("id") id: String
    ): RestaurantDto

    @POST("orders")
    suspend fun placeOrder(
        @Body orderRequest: OrderRequestDto
    ): OrderResponseDto

    @GET("user/profile")
    suspend fun getUserProfile(): UserProfileDto
}
