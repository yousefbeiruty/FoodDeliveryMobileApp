package com.example.fooddelivery.ui.screens.cart

import com.example.fooddelivery.MainDispatcherRule
import com.example.fooddelivery.data.repository.FakeFastApiFoodDeliveryApi
import com.example.fooddelivery.data.repository.FakeFoodRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CartViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var fakeRepository: FakeFoodRepository
    private lateinit var fakeApi: FakeFastApiFoodDeliveryApi
    private lateinit var viewModel: CartViewModel

    @Before
    fun setUp() {
        fakeRepository = FakeFoodRepository()
        fakeApi = FakeFastApiFoodDeliveryApi()
        viewModel = CartViewModel(fakeRepository, fakeApi)
    }

    @Test
    fun `observes cart and calculates payment summary correctly`() = runTest {
        val menuItem = fakeRepository.testMenuItem
        fakeRepository.addToCart(menuItem, 2)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.cartItems.size)
        assertEquals(20.0, state.subtotal, 0.01)
        assertEquals(2.5, state.deliveryFee, 0.01)
        assertEquals(1.7, state.ecoDiscount, 0.01)
        assertEquals(1.0, state.selectedTipAmount, 0.01)
        assertEquals(21.8, state.total, 0.01)
    }

    @Test
    fun `updateTip recalculates payment total`() = runTest {
        val menuItem = fakeRepository.testMenuItem
        fakeRepository.addToCart(menuItem, 2)
        advanceUntilIdle()

        viewModel.updateTip(5.0)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(5.0, state.selectedTipAmount, 0.01)
        assertEquals(25.8, state.total, 0.01)
    }

    @Test
    fun `placeOrder succeeds when cart has items and calls callback`() = runTest {
        val menuItem = fakeRepository.testMenuItem
        fakeRepository.addToCart(menuItem, 1)
        advanceUntilIdle()

        var callbackCalled = false
        viewModel.placeOrder {
            callbackCalled = true
        }
        advanceUntilIdle()

        assertTrue(callbackCalled)
        assertTrue(viewModel.uiState.value.orderPlacedSuccess)
    }
}
