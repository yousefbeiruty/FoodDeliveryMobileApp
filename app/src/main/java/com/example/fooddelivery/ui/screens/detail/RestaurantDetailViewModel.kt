package com.example.fooddelivery.ui.screens.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fooddelivery.data.model.MenuItem
import com.example.fooddelivery.data.repository.FoodRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RestaurantDetailViewModel @Inject constructor(
    private val foodRepository: FoodRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RestaurantDetailUiState())
    val uiState: StateFlow<RestaurantDetailUiState> = _uiState.asStateFlow()

    init {
        observeCart()
    }

    private fun observeCart() {
        viewModelScope.launch {
            foodRepository.cartItemsFlow.collect { cartItems ->
                val quantities = cartItems.associate { it.menuItem.id to it.quantity }
                _uiState.update { it.copy(itemQuantities = quantities) }
            }
        }
    }

    fun loadRestaurant(restaurantId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = foodRepository.getRestaurantById(restaurantId)
            if (result.isSuccess) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        restaurant = result.getOrNull()
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to load restaurant details."
                    )
                }
            }
        }
    }

    fun addItem(menuItem: MenuItem) {
        foodRepository.addToCart(menuItem, 1)
    }

    fun updateQuantity(menuItem: MenuItem, newQuantity: Int) {
        foodRepository.updateQuantity(menuItem, newQuantity)
    }
}
