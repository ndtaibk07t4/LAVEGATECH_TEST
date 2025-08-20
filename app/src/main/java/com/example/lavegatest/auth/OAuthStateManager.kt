package com.example.lavegatest.auth

import android.content.Context
import androidx.core.content.edit

object OAuthStateManager {
    private var currentOAuthState: OAuthState? = null
    private var authorizationStartTime: Long = 0L
    
    fun createOAuthState(): OAuthState {
        currentOAuthState = OAuthState.create()
        authorizationStartTime = System.currentTimeMillis()
        return currentOAuthState!!
    }
    
    fun getCurrentOAuthState(): OAuthState? {
        return currentOAuthState
    }
    
    fun clearOAuthState() {
        currentOAuthState = null
    }
    
    fun saveOAuthState(context: Context, oauthState: OAuthState) {
        val sharedPrefs = context.getSharedPreferences("oauth_state", Context.MODE_PRIVATE)
        sharedPrefs.edit {
            putString("code_verifier", oauthState.codeVerifier)
            putString("state", oauthState.state)
            putLong("timestamp", oauthState.timestamp)
        }
    }
    
    fun loadOAuthState(context: Context): OAuthState? {
        val sharedPrefs = context.getSharedPreferences("oauth_state", Context.MODE_PRIVATE)
        val codeVerifier = sharedPrefs.getString("code_verifier", null)
        val state = sharedPrefs.getString("state", null)
        val timestamp = sharedPrefs.getLong("timestamp", 0L)
        
        return if (codeVerifier != null && state != null && timestamp > 0L) {
            OAuthState(codeVerifier, state, timestamp)
        } else {
            null
        }
    }
    
    fun clearStoredOAuthState(context: Context) {
        val sharedPrefs = context.getSharedPreferences("oauth_state", Context.MODE_PRIVATE)
        sharedPrefs.edit { clear() }
    }
    
    fun initialize(context: Context) {
        if (currentOAuthState == null) {
            currentOAuthState = loadOAuthState(context)
        }
    }
}
