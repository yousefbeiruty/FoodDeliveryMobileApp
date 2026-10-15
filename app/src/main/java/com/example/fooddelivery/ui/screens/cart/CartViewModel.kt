package com.example.fooddelivery.ui.screens.cart

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
class CartViewModel @Inject constructor(
    private val foodRepository: FoodRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CartUiState())
    val uiState: StateFlow<CartUiState> = _uiState.asStateFlow()

    init {
        observeCartAndProfile()
    }

    private fun observeCartAndProfile() {
        viewModelScope.launch {
            foodRepository.cartItemsFlow.collect { items ->
                calculateSummary(items, _uiState.value.selectedTipAmount)
            }
        }
        viewModelScope.launch {
            foodRepository.userProfileFlow.collect { profile ->
                _uiState.update { it.copy(deliveryAddress = profile.defaultAddress) }
            }
        }
    }

    fun updateTip(tip: Double) {
        _uiState.update { it.copy(selectedTipAmount = tip) }
        calculateSummary(_uiState.value.cartItems, tip)
    }

    fun updateQuantity(menuItem: MenuItem, newQuantity: Int) {
        foodRepository.updateQuantity(menuItem, newQuantity)
    }

    fun removeItem(menuItem: MenuItem) {
        foodRepository.removeFromCart(menuItem)
    }

    fun updateAddress(address: String) {
        _uiState.update { it.copy(deliveryAddress = address) }
    }

    fun placeOrder(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isPlacingOrder = true, errorMessage = null) }
            val res = foodRepository.placeOrder(
                deliveryAddress = _uiState.value.deliveryAddress,
                tipAmount = _uiState.value.selectedTipAmount
            )
            if (res.isSuccess) {
                _uiState.update { it.copy(isPlacingOrder = false, orderPlacedSuccess = true) }
                onSuccess()
            } else {
                _uiState.update {
                    it.copy(
                        isPlacingOrder = false,
                        errorMessage = res.exceptionOrNull()?.message ?: "Failed to place order."
                    )
                }
            }
        }
    }

    private fun calculateSummary(items: List<com.example.fooddelivery.data.model.CartItem>, tip: Double) {
        val subtotal = items.sumOf { it.totalPrice }
        val deliveryFee = if (items.isEmpty()) 0.0 else 2.99
        val ecoDiscount = if (items.isEmpty()) 0.0 else 1.50
        val total = (subtotal + deliveryFee + tip - ecoDiscount).coerceAtLeast(0.0)

        _uiState.update {
            it.copy(
                cartItems = items,
                subtotal = subtotal,
                deliveryFee = deliveryFee,
                ecoDiscount = ecoDiscount,
                total = total
            )
        }
    }
}
