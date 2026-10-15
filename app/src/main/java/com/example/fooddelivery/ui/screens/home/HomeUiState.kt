package com.example.fooddelivery.ui.screens.home

import com.example.fooddelivery.data.model.Banner
import com.example.fooddelivery.data.model.Category
import com.example.fooddelivery.data.model.Restaurant

data class HomeUiState(
    val isLoading: Boolean = true,
    val categories: List<Category> = emptyList(),
    val selectedCategoryId: String = "all",
    val banners: List<Banner> = emptyList(),
    val restaurants: List<Restaurant> = emptyList(),
    val searchQuery: String = "",
    val selectedLocation: String = "742 Evergreen Terrace, Green City",
    val errorMessage: String? = null
)
