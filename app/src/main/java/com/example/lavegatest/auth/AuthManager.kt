package com.example.lavegatest.auth

import com.example.lavegatest.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthManager @Inject constructor(
    private val authRepository: AuthRepository
) {
    
    private val _oAuthCallbacks = MutableStateFlow("")
    val oAuthCallbacks: SharedFlow<String> = _oAuthCallbacks.asSharedFlow()
    
    fun handleOAuthCallback(uri: String) {
        _oAuthCallbacks.value = uri
    }
    
    fun getAuthRepository(): AuthRepository = authRepository
}
