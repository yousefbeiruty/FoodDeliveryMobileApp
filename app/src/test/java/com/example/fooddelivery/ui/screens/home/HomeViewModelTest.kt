package com.example.fooddelivery.ui.screens.home

import com.example.fooddelivery.MainDispatcherRule
import com.example.fooddelivery.data.repository.FakeFoodRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var fakeRepository: FakeFoodRepository
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setUp() {
        fakeRepository = FakeFoodRepository()
        viewModel = HomeViewModel(fakeRepository)
    }

    @Test
    fun `loadHomeData populates categories, banners, and restaurants in uiState`() = runTest {
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.categories.isNotEmpty())
        assertTrue(state.banners.isNotEmpty())
        assertTrue(state.restaurants.isNotEmpty())
        assertEquals(fakeRepository.testRestaurant.id, state.restaurants.first().id)
    }

    @Test
    fun `onSearchQueryChanged updates searchQuery and filters restaurants`() = runTest {
        advanceUntilIdle()

        viewModel.onSearchQueryChanged("Green")
        advanceUntilIdle()

        assertEquals("Green", viewModel.uiState.value.searchQuery)
        assertEquals(1, viewModel.uiState.value.restaurants.size)

        viewModel.onSearchQueryChanged("NonExistentEatery")
        advanceUntilIdle()

        assertEquals("NonExistentEatery", viewModel.uiState.value.searchQuery)
        assertTrue(viewModel.uiState.value.restaurants.isEmpty())
    }

    @Test
    fun `onCategorySelected updates selectedCategoryId and category state`() = runTest {
        advanceUntilIdle()

        viewModel.onCategorySelected("healthy")
        advanceUntilIdle()

        assertEquals("healthy", viewModel.uiState.value.selectedCategoryId)
        val selectedCategory = viewModel.uiState.value.categories.first { it.id == "healthy" }
        assertTrue(selectedCategory.isSelected)
    }
}
