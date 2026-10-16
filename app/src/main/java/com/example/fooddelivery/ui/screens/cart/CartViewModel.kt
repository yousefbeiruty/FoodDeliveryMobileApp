package com.example.fooddelivery.ui.screens.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fooddelivery.data.api.FastApiFoodDeliveryApi
import com.example.fooddelivery.data.api.OrderQuoteItem
import com.example.fooddelivery.data.api.OrderQuoteRequest
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
    private val foodRepository: FoodRepository,
    private val api: FastApiFoodDeliveryApi
) : ViewModel() {

    private val _uiState = MutableStateFlow(CartUiState())
    val uiState: StateFlow<CartUiState> = _uiState.asStateFlow()

    init {
        observeCartAndProfile()
    }

    private fun observeCartAndProfile() {
        viewModelScope.launch {
            foodRepository.cartItemsFlow.collect { items ->
                _uiState.update { it.copy(cartItems = items) }
                fetchQuote()
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
        fetchQuote()
    }

    fun updatePromoCode(code: String) {
        _uiState.update { it.copy(promoCode = code) }
        fetchQuote()
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

    private fun fetchQuote() {
        val items = _uiState.value.cartItems
        if (items.isEmpty()) {
            _uiState.update {
                it.copy(subtotal = 0.0, deliveryFee = 0.0, ecoDiscount = 0.0, total = 0.0)
            }
            return
        }

        viewModelScope.launch {
            try {
                val quoteItems = items.map {
                    OrderQuoteItem(
                        menuItemId = it.menuItem.id.toIntOrNull() ?: 1,
                        quantity = it.quantity
                    )
                }
                val quote = api.getOrderQuote(
                    OrderQuoteRequest(
                        items = quoteItems,
                        driverTip = _uiState.value.selectedTipAmount,
                        promoCode = _uiState.value.promoCode.takeIf { it.isNotBlank() }
                    )
                )
                _uiState.update {
                    it.copy(
                        subtotal = quote.subtotal,
                        deliveryFee = quote.deliveryFee,
                        ecoDiscount = quote.discount,
                        total = quote.totalAmount
                    )
                }
            } catch (e: Exception) {
                // Fallback local calculation if offline
                val subtotal = items.sumOf { it.totalPrice }
                val deliveryFee = 2.5
                val discount = if (_uiState.value.promoCode.isNotBlank()) 1.7 else 0.0
                val total = (subtotal + deliveryFee + _uiState.value.selectedTipAmount - discount).coerceAtLeast(0.0)
                _uiState.update {
                    it.copy(
                        subtotal = subtotal,
                        deliveryFee = deliveryFee,
                        ecoDiscount = discount,
                        total = total
                    )
                }
            }
        }
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
}
