package com.example.lavegatest.auth

import android.content.Context
import com.example.lavegatest.data.repository.AuthRepository
import com.example.lavegatest.data.repository.AuthRepositoryImpl
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class AuthManager(context: Context) {
    
    private val authRepository: AuthRepository = AuthRepositoryImpl(context)
    
    private val _oAuthCallbacks = MutableSharedFlow<String>()
    val oAuthCallbacks: SharedFlow<String> = _oAuthCallbacks.asSharedFlow()
    
    suspend fun handleOAuthCallback(uri: String) {
        _oAuthCallbacks.emit(uri)
    }
    
    fun getAuthRepository(): AuthRepository = authRepository
}
