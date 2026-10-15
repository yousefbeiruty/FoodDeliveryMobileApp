package com.example.fooddelivery.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fooddelivery.data.repository.FoodRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val foodRepository: FoodRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadHomeData()
    }

    fun loadHomeData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val categoriesRes = foodRepository.getCategories()
            val bannersRes = foodRepository.getBanners()
            val restaurantsRes = foodRepository.getRestaurants(
                searchQuery = _uiState.value.searchQuery,
                categoryId = _uiState.value.selectedCategoryId
            )

            val categories = categoriesRes.getOrDefault(emptyList())
            val banners = bannersRes.getOrDefault(emptyList())
            val restaurants = restaurantsRes.getOrDefault(emptyList())

            _uiState.update {
                it.copy(
                    isLoading = false,
                    categories = categories,
                    banners = banners,
                    restaurants = restaurants
                )
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300) // Debounce search
            fetchRestaurants()
        }
    }

    fun onCategorySelected(categoryId: String) {
        _uiState.update { state ->
            val updatedCategories = state.categories.map { c ->
                c.copy(isSelected = c.id == categoryId)
            }
            state.copy(selectedCategoryId = categoryId, categories = updatedCategories)
        }
        viewModelScope.launch {
            fetchRestaurants()
        }
    }

    private suspend fun fetchRestaurants() {
        val res = foodRepository.getRestaurants(
            searchQuery = _uiState.value.searchQuery,
            categoryId = _uiState.value.selectedCategoryId
        )
        if (res.isSuccess) {
            _uiState.update { it.copy(restaurants = res.getOrDefault(emptyList())) }
        }
    }
}
