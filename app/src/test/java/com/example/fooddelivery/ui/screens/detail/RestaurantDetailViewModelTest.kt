package com.example.fooddelivery.ui.screens.detail

import com.example.fooddelivery.MainDispatcherRule
import com.example.fooddelivery.data.repository.FakeFoodRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RestaurantDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var fakeRepository: FakeFoodRepository
    private lateinit var viewModel: RestaurantDetailViewModel

    @Before
    fun setUp() {
        fakeRepository = FakeFoodRepository()
        viewModel = RestaurantDetailViewModel(fakeRepository)
    }

    @Test
    fun `loadRestaurant fetches restaurant details successfully`() = runTest {
        viewModel.loadRestaurant("r1")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.restaurant)
        assertEquals("r1", state.restaurant?.id)
    }

    @Test
    fun `addItem adds item to cart and updates quantities map`() = runTest {
        val menuItem = fakeRepository.testMenuItem

        viewModel.addItem(menuItem)
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.itemQuantities[menuItem.id])
    }

    @Test
    fun `updateQuantity updates item quantity in cart`() = runTest {
        val menuItem = fakeRepository.testMenuItem

        viewModel.addItem(menuItem)
        viewModel.updateQuantity(menuItem, 4)
        advanceUntilIdle()

        assertEquals(4, viewModel.uiState.value.itemQuantities[menuItem.id])

        viewModel.updateQuantity(menuItem, 0)
        advanceUntilIdle()

        assertEquals(null, viewModel.uiState.value.itemQuantities[menuItem.id])
    }
}
