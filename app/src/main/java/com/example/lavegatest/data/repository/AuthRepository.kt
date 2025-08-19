package com.example.lavegatest.data.repository

import android.content.Context
import com.example.lavegatest.auth.OAuthService
import com.example.lavegatest.auth.OAuthState
import com.example.lavegatest.auth.TokenStorage
import com.example.lavegatest.auth.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

interface AuthRepository {
    val isSignedIn: Flow<Boolean>
    fun isUserSignedIn(): Boolean
    fun getStoredUserProfile(): UserProfile?
    suspend fun startSignIn(): Result<String>
    suspend fun handleOAuthCallback(uri: String): Result<UserProfile>
    suspend fun signOut()
    suspend fun refreshUserProfile(): Result<UserProfile>
}

class AuthRepositoryImpl(
    private val context: Context
) : AuthRepository {
    
    private val oAuthService = OAuthService(context)
    private val tokenStorage = TokenStorage(context)
    private var currentOAuthState: OAuthState? = null
    
    private val _isSignedIn = MutableStateFlow(tokenStorage.isTokenValid())
    override val isSignedIn: Flow<Boolean> = _isSignedIn.asStateFlow()
    
    override fun isUserSignedIn(): Boolean {
        val signedIn = tokenStorage.isTokenValid()
        _isSignedIn.value = signedIn
        return signedIn
    }
    
    override fun getStoredUserProfile(): UserProfile? {
        return tokenStorage.getUserProfile()
    }
    
    override suspend fun startSignIn(): Result<String> {
        return try {
            currentOAuthState = OAuthState.create()
            val authUrl = oAuthService.buildAuthorizationUrl(
                currentOAuthState!!.codeVerifier,
                currentOAuthState!!.state
            )
            oAuthService.launchAuthorization(authUrl)
            Result.success("Sign-in process started")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun handleOAuthCallback(uri: String): Result<UserProfile> {
        return try {
            val uriObj = android.net.Uri.parse(uri)
            val code = uriObj.getQueryParameter("code")
            val state = uriObj.getQueryParameter("state")
            val error = uriObj.getQueryParameter("error")
            
            if (error != null) {
                return Result.failure(Exception("OAuth error: $error"))
            }
            
            if (code == null || state == null) {
                return Result.failure(Exception("Invalid OAuth callback"))
            }
            
            val oauthState = currentOAuthState
            if (oauthState == null || !oauthState.isValid()) {
                return Result.failure(Exception("OAuth state expired or invalid"))
            }
            
            if (state != oauthState.state) {
                return Result.failure(Exception("State mismatch"))
            }
            
            val tokenResult = oAuthService.exchangeCodeForToken(code, oauthState.codeVerifier)
            if (tokenResult.isFailure) {
                return Result.failure(tokenResult.exceptionOrNull() ?: Exception("Token exchange failed"))
            }
            
            val profileResult = oAuthService.fetchUserProfile()
            if (profileResult.isSuccess) {
                _isSignedIn.value = true
                Result.success(profileResult.getOrThrow())
            } else {
                Result.failure(profileResult.exceptionOrNull() ?: Exception("Failed to fetch profile"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun signOut() {
        tokenStorage.clearTokens()
        currentOAuthState = null
        _isSignedIn.value = false
    }
    
    override suspend fun refreshUserProfile(): Result<UserProfile> {
        return if (isUserSignedIn()) {
            oAuthService.fetchUserProfile()
        } else {
            Result.failure(Exception("User not signed in"))
        }
    }
}
