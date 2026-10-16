package com.example.fooddelivery.ui.screens.cart

import com.example.fooddelivery.data.model.CartItem

data class CartUiState(
    val cartItems: List<CartItem> = emptyList(),
    val selectedTipAmount: Double = 1.0,
    val promoCode: String = "",
    val deliveryAddress: String = "12 Rainbow Street, Amman",
    val subtotal: Double = 0.0,
    val deliveryFee: Double = 0.0,
    val ecoDiscount: Double = 0.0,
    val total: Double = 0.0,
    val isPlacingOrder: Boolean = false,
    val orderPlacedSuccess: Boolean = false,
    val errorMessage: String? = null
)
