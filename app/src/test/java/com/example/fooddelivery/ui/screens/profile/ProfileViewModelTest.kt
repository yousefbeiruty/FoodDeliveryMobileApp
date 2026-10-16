package com.example.fooddelivery.ui.screens.profile

import com.example.fooddelivery.MainDispatcherRule
import com.example.fooddelivery.data.auth.TokenManager
import com.example.fooddelivery.data.repository.FakeFastApiFoodDeliveryApi
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
class ProfileViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var fakeRepository: FakeFoodRepository
    private lateinit var fakeApi: FakeFastApiFoodDeliveryApi
    private lateinit var tokenManager: TokenManager
    private lateinit var viewModel: ProfileViewModel

    @Before
    fun setUp() {
        fakeRepository = FakeFoodRepository()
        fakeApi = FakeFastApiFoodDeliveryApi()
        tokenManager = TokenManager(null)
        viewModel = ProfileViewModel(fakeRepository, fakeApi, tokenManager)
    }

    @Test
    fun `observes user profile flow from repository`() = runTest {
        advanceUntilIdle()

        val profile = viewModel.uiState.value.userProfile
        assertEquals("Test User", profile.name)
        assertEquals("test@eco.org", profile.email)
        assertFalse(profile.isDarkMode)
    }

    @Test
    fun `toggleDarkMode toggles dark mode in userProfile state`() = runTest {
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.userProfile.isDarkMode)

        viewModel.toggleDarkMode()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.userProfile.isDarkMode)

        viewModel.toggleDarkMode()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.userProfile.isDarkMode)
    }
}
