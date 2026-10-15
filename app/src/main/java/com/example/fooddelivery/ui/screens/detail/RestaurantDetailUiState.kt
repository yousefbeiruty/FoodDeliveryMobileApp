package com.example.fooddelivery.ui.screens.detail

import com.example.fooddelivery.data.model.Restaurant

data class RestaurantDetailUiState(
    val isLoading: Boolean = true,
    val restaurant: Restaurant? = null,
    val itemQuantities: Map<String, Int> = emptyMap(),
    val errorMessage: String? = null
)
