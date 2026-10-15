package com.example.fooddelivery.ui.screens.cart

import com.example.fooddelivery.data.model.CartItem

data class CartUiState(
    val cartItems: List<CartItem> = emptyList(),
    val selectedTipAmount: Double = 3.0,
    val deliveryAddress: String = "742 Evergreen Terrace, Green City",
    val subtotal: Double = 0.0,
    val deliveryFee: Double = 2.99,
    val ecoDiscount: Double = 1.50,
    val total: Double = 0.0,
    val isPlacingOrder: Boolean = false,
    val orderPlacedSuccess: Boolean = false,
    val errorMessage: String? = null
)
