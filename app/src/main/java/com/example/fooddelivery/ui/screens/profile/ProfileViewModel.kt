package com.example.fooddelivery.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fooddelivery.data.api.FastApiFoodDeliveryApi
import com.example.fooddelivery.data.api.UpdateProfileRequest
import com.example.fooddelivery.data.auth.TokenManager
import com.example.fooddelivery.data.repository.FoodRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val foodRepository: FoodRepository,
    private val api: FastApiFoodDeliveryApi,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        observeUserProfile()
        fetchLiveProfile()
    }

    private fun observeUserProfile() {
        viewModelScope.launch {
            foodRepository.userProfileFlow.collect { profile ->
                _uiState.update { it.copy(userProfile = profile) }
            }
        }
    }

    private fun fetchLiveProfile() {
        viewModelScope.launch {
            try {
                val userDto = api.getUserProfile()
                val current = _uiState.value.userProfile
                val updated = current.copy(
                    name = userDto.fullName,
                    email = userDto.email,
                    phone = userDto.phoneNumber ?: current.phone
                )
                _uiState.update { it.copy(userProfile = updated) }
            } catch (_: Exception) {
                // Keep repository profile if offline
            }
        }
    }

    fun updateProfile(fullName: String, phoneNumber: String) {
        viewModelScope.launch {
            try {
                val updatedDto = api.updateUserProfile(
                    UpdateProfileRequest(
                        fullName = fullName.takeIf { it.isNotBlank() },
                        phoneNumber = phoneNumber.takeIf { it.isNotBlank() }
                    )
                )
                val current = _uiState.value.userProfile
                _uiState.update {
                    it.copy(
                        userProfile = current.copy(
                            name = updatedDto.fullName,
                            email = updatedDto.email,
                            phone = updatedDto.phoneNumber ?: current.phone
                        )
                    )
                }
            } catch (_: Exception) {
                // Handle update error if needed
            }
        }
    }

    fun logout(onLogout: () -> Unit) {
        tokenManager.clearToken()
        onLogout()
    }

    fun toggleDarkMode() {
        foodRepository.toggleDarkMode()
    }
}
