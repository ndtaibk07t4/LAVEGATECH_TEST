package com.example.lavegatest.data.repository

import android.content.Context
import com.example.lavegatest.auth.OAuthService
import com.example.lavegatest.auth.OAuthState
import com.example.lavegatest.auth.OAuthStateManager
import com.example.lavegatest.auth.TokenStorage
import com.example.lavegatest.auth.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.core.net.toUri

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
    
    init {
        // Initialize OAuth state manager
        OAuthStateManager.initialize(context)
    }
    
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
            val oauthState = OAuthStateManager.createOAuthState()
            OAuthStateManager.saveOAuthState(context, oauthState)
            
            val authUrl = oAuthService.buildAuthorizationUrl(
                oauthState.codeVerifier,
                oauthState.state
            )
            oAuthService.launchAuthorization(authUrl)
            Result.success("Sign-in process started")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun handleOAuthCallback(uri: String): Result<UserProfile> {
        return try {
            val uriObj = uri.toUri()
            val code = uriObj.getQueryParameter("code")
            val state = uriObj.getQueryParameter("state")
            val error = uriObj.getQueryParameter("error")
            
            if (error != null) {
                return Result.failure(Exception("OAuth error: $error"))
            }
            
            if (code == null || state == null) {
                return Result.failure(Exception("Invalid OAuth callback"))
            }

            val oauthState = OAuthStateManager.getCurrentOAuthState()
            if (oauthState == null || !oauthState.isValid()) {
                return Result.failure(Exception("OAuth state expired or invalid"))
            }
            
            
            if (!oauthState.isValid()) {
                return Result.failure(Exception("OAuth state expired - please try signing in again"))
            }
            
            if (state != oauthState.state) {
                return Result.failure(Exception("State mismatch - possible CSRF attack"))
            }
            
            val tokenResult = oAuthService.exchangeCodeForToken(code, oauthState.codeVerifier)
            if (tokenResult.isFailure) {
                return Result.failure(tokenResult.exceptionOrNull() ?: Exception("Token exchange failed"))
            }
            
            val profileResult = oAuthService.fetchUserProfile()
            if (profileResult.isSuccess) {
                _isSignedIn.value = true
                OAuthStateManager.clearOAuthState()
                OAuthStateManager.clearStoredOAuthState(context)
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
        OAuthStateManager.clearOAuthState()
        OAuthStateManager.clearStoredOAuthState(context)
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
