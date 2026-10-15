package com.example.fooddelivery.ui.screens.tracking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fooddelivery.data.repository.FoodRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrderTrackingViewModel @Inject constructor(
    private val foodRepository: FoodRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OrderTrackingUiState())
    val uiState: StateFlow<OrderTrackingUiState> = _uiState.asStateFlow()

    init {
        observeActiveOrder()
        simulateDriverMovement()
    }

    private fun observeActiveOrder() {
        viewModelScope.launch {
            foodRepository.activeOrderFlow.collect { order ->
                _uiState.update { it.copy(activeOrder = order) }
            }
        }
    }

    private fun simulateDriverMovement() {
        viewModelScope.launch {
            var progress = 0.3f
            while (progress <= 0.95f) {
                delay(2500)
                progress += 0.05f
                val remainingMins = ((1f - progress) * 25).toInt().coerceAtLeast(2)
                val step = when {
                    progress < 0.2f -> 0 // Order Placed
                    progress < 0.5f -> 1 // Preparing
                    progress < 0.9f -> 2 // Out for Delivery
                    else -> 3 // Delivered
                }
                _uiState.update {
                    it.copy(
                        driverProgressRatio = progress,
                        estimatedArrivalMinutes = remainingMins,
                        currentStepIndex = step
                    )
                }
            }
        }
    }
}
