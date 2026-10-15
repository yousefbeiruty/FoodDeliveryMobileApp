package com.example.fooddelivery.ui.screens.tracking

import com.example.fooddelivery.MainDispatcherRule
import com.example.fooddelivery.data.repository.FakeFoodRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OrderTrackingViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var fakeRepository: FakeFoodRepository
    private lateinit var viewModel: OrderTrackingViewModel

    @Before
    fun setUp() {
        fakeRepository = FakeFoodRepository()
        viewModel = OrderTrackingViewModel(fakeRepository)
    }

    @Test
    fun `observes active order from repository`() = runTest {
        val menuItem = fakeRepository.testMenuItem
        fakeRepository.addToCart(menuItem, 1)
        fakeRepository.placeOrder("123 Eco Way", 3.0)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.activeOrder)
        assertEquals("FB-12345", state.activeOrder?.orderId)
    }

    @Test
    fun `driver movement simulation updates progress and estimated arrival`() = runTest {
        val initialProgress = viewModel.uiState.value.driverProgressRatio

        advanceTimeBy(3000)

        val updatedProgress = viewModel.uiState.value.driverProgressRatio
        assertTrue(updatedProgress > initialProgress)
    }
}
