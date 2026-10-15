package com.example.fooddelivery.ui.screens.tracking

import com.example.fooddelivery.data.model.Order

data class OrderTrackingUiState(
    val activeOrder: Order? = null,
    val currentStepIndex: Int = 1, // Preparing
    val driverProgressRatio: Float = 0.3f, // 30% along delivery route
    val estimatedArrivalMinutes: Int = 20
)
