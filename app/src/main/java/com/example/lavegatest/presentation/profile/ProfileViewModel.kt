package com.example.lavegatest.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lavegatest.auth.model.UserProfile
import com.example.lavegatest.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()
    
    init {
        loadUserProfile()
    }
    
    private fun loadUserProfile() {
        viewModelScope.launch {
            if (authRepository.isUserSignedIn()) {
                val userProfile = authRepository.getStoredUserProfile()
                if (userProfile != null) {
                    _uiState.value = _uiState.value.copy(userProfile = userProfile)
                } else {
                    // Try to refresh profile if not cached
                    refreshProfile()
                }
            } else {
                _uiState.value = _uiState.value.copy(isSignedOut = true)
            }
        }
    }

    private fun refreshProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            val result = authRepository.refreshUserProfile()
            if (result.isSuccess) {
                val userProfile = result.getOrNull()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    userProfile = userProfile,
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = result.exceptionOrNull()?.message ?: "Failed to refresh profile"
                )
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )
            
            try {
                authRepository.signOut()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSignedOut = true,
                    userProfile = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Sign out failed"
                )
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}

data class ProfileUiState(
    val userProfile: UserProfile? = null,
    val isLoading: Boolean = false,
    val isSignedOut: Boolean = false,
    val errorMessage: String? = null
)
