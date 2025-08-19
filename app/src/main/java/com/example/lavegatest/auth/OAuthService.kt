package com.example.lavegatest.auth

import android.content.Context
import android.content.Intent
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import com.example.lavegatest.BuildConfig
import com.example.lavegatest.auth.model.OAuthError
import com.example.lavegatest.auth.model.TokenResponse
import com.example.lavegatest.auth.model.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class OAuthService(private val context: Context) {
    
    private val tokenStorage = TokenStorage(context)
    
    fun buildAuthorizationUrl(codeVerifier: String, state: String): String {
        val codeChallenge = PKCEUtil.generateCodeChallenge(codeVerifier)
        
        return OAuthConfig.GOOGLE_AUTH_ENDPOINT.toUri()
            .buildUpon()
            .appendQueryParameter("client_id", BuildConfig.GOOGLE_CLIENT_ID)
            .appendQueryParameter("redirect_uri", OAuthConfig.REDIRECT_URI)
            .appendQueryParameter("response_type", OAuthConfig.RESPONSE_TYPE)
            .appendQueryParameter("scope", OAuthConfig.SCOPE)
            .appendQueryParameter("access_type", OAuthConfig.ACCESS_TYPE)
            .appendQueryParameter("state", state)
            .appendQueryParameter("code_challenge", codeChallenge)
            .appendQueryParameter("code_challenge_method", OAuthConfig.CODE_CHALLENGE_METHOD)
            .build()
            .toString()
    }
    
    fun launchAuthorization(url: String) {
        val customTabsIntent = CustomTabsIntent.Builder().build()
        customTabsIntent.intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        customTabsIntent.launchUrl(context, url.toUri())
    }
    
    suspend fun exchangeCodeForToken(authorizationCode: String, codeVerifier: String): Result<TokenResponse> {
        return try {
            val url = URL(OAuthConfig.GOOGLE_TOKEN_ENDPOINT)
            val connection = withContext(Dispatchers.IO) {
                url.openConnection()
            } as HttpURLConnection
            
            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
            
            val postData = buildString {
                append("client_id=${URLEncoder.encode(BuildConfig.GOOGLE_CLIENT_ID, "UTF-8")}")
                append("&code=${URLEncoder.encode(authorizationCode, "UTF-8")}")
                append("&code_verifier=${URLEncoder.encode(codeVerifier, "UTF-8")}")
                append("&grant_type=authorization_code")
                append("&redirect_uri=${URLEncoder.encode(OAuthConfig.REDIRECT_URI, "UTF-8")}")
            }
            
            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(postData)
                writer.flush()
            }
            
            val responseCode = connection.responseCode
            val response = if (responseCode == HttpURLConnection.HTTP_OK) {
                connection.inputStream
            } else {
                connection.errorStream
            }
            
            val responseBody = BufferedReader(InputStreamReader(response, StandardCharsets.UTF_8)).use { reader ->
                reader.readText()
            }
            
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val jsonObject = JSONObject(responseBody)
                val tokenResponse = TokenResponse.fromJson(jsonObject)
                saveTokens(tokenResponse)
                Result.success(tokenResponse)
            } else {
                val jsonObject = JSONObject(responseBody)
                val error = OAuthError.fromJson(jsonObject)
                Result.failure(Exception("OAuth error: ${error.error} - ${error.errorDescription}"))
            }
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun fetchUserProfile(): Result<UserProfile> {
        return try {
            val accessToken = tokenStorage.getAccessToken()
            if (accessToken.isNullOrEmpty()) {
                return Result.failure(Exception("No access token available"))
            }
            
            val url = URL(OAuthConfig.GOOGLE_USER_INFO_ENDPOINT)
            val connection = withContext(Dispatchers.IO) {
                url.openConnection()
            } as HttpURLConnection
            
            connection.requestMethod = "GET"
            connection.setRequestProperty("Authorization", "Bearer $accessToken")
            
            val responseCode = connection.responseCode
            val response = if (responseCode == HttpURLConnection.HTTP_OK) {
                connection.inputStream
            } else {
                connection.errorStream
            }
            
            val responseBody = BufferedReader(InputStreamReader(response, StandardCharsets.UTF_8)).use { reader ->
                reader.readText()
            }
            
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val jsonObject = JSONObject(responseBody)
                val userProfile = UserProfile.fromJson(jsonObject)
                tokenStorage.saveUserProfile(userProfile)
                Result.success(userProfile)
            } else {
                val jsonObject = JSONObject(responseBody)
                val error = OAuthError.fromJson(jsonObject)
                Result.failure(Exception("API error: ${error.error} - ${error.errorDescription}"))
            }
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun saveTokens(tokenResponse: TokenResponse) {
        tokenStorage.saveAccessToken(tokenResponse.accessToken)
        tokenResponse.refreshToken?.let { tokenStorage.saveRefreshToken(it) }
        
        val expiryTime = System.currentTimeMillis() + (tokenResponse.expiresIn * 1000L)
        tokenStorage.saveTokenExpiry(expiryTime)
    }
    
    fun isUserSignedIn(): Boolean {
        return tokenStorage.isTokenValid()
    }
    
    fun signOut() {
        tokenStorage.clearTokens()
    }
    
    fun getStoredUserProfile(): UserProfile? {
        return tokenStorage.getUserProfile()
    }
}
