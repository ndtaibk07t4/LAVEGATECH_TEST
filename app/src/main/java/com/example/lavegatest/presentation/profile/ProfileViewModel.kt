package com.example.lavegatest.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun signOut() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, isSignedOut = false) }

            try {
                // Simulate API call
                delay(1000)

                // Success - will trigger navigation in UI
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isSignedOut = true
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isSignedOut = false
                    )
                }
            }
        }
    }
}

data class ProfileUiState(
    val userName: String = "John Doe",
    val userEmail: String = "johndoe@example.com",
    val isLoading: Boolean = false,
    val isSignedOut: Boolean = false
)
