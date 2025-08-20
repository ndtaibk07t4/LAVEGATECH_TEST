package com.example.lavegatest.auth

import android.content.Context
import com.example.lavegatest.data.repository.AuthRepository
import com.example.lavegatest.data.repository.AuthRepositoryImpl
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class AuthManager(context: Context) {
    
    private val authRepository: AuthRepository = AuthRepositoryImpl(context)
    
    private val _oAuthCallbacks = MutableStateFlow("")
    val oAuthCallbacks: SharedFlow<String> = _oAuthCallbacks.asSharedFlow()
    
    fun handleOAuthCallback(uri: String) {
        _oAuthCallbacks.value = uri
    }
    
    fun getAuthRepository(): AuthRepository = authRepository
}
