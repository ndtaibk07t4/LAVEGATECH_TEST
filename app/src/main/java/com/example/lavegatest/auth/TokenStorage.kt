package com.example.lavegatest.auth

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.lavegatest.auth.model.UserProfile
import org.json.JSONObject
import androidx.core.content.edit

private const val PREF_NAME = "oauth_preferences"
private const val KEY_ACCESS_TOKEN = "access_token"
private const val KEY_REFRESH_TOKEN = "refresh_token"
private const val KEY_TOKEN_EXPIRY = "token_expiry"
private const val KEY_USER_PROFILE = "user_profile"

class TokenStorage(context: Context) {
    
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    private val encryptedPrefs = EncryptedSharedPreferences.create(
        context,
        PREF_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    fun saveAccessToken(token: String) {
        encryptedPrefs.edit { putString(KEY_ACCESS_TOKEN, token) }
    }
    
    fun getAccessToken(): String? {
        return encryptedPrefs.getString(KEY_ACCESS_TOKEN, null)
    }
    
    fun saveRefreshToken(token: String) {
        encryptedPrefs.edit { putString(KEY_REFRESH_TOKEN, token) }
    }
    
    fun getRefreshToken(): String? {
        return encryptedPrefs.getString(KEY_REFRESH_TOKEN, null)
    }
    
    fun saveTokenExpiry(expiryTime: Long) {
        encryptedPrefs.edit { putLong(KEY_TOKEN_EXPIRY, expiryTime) }
    }
    
    fun getTokenExpiry(): Long {
        return encryptedPrefs.getLong(KEY_TOKEN_EXPIRY, 0L)
    }
    
    fun saveUserProfile(profile: UserProfile) {
        val jsonObject = JSONObject().apply {
            put("id", profile.id)
            put("email", profile.email)
            put("verified_email", profile.verifiedEmail)
            put("name", profile.name)
            put("given_name", profile.givenName)
            put("family_name", profile.familyName)
            put("picture", profile.picture)
        }
        encryptedPrefs.edit { putString(KEY_USER_PROFILE, jsonObject.toString()) }
    }
    
    fun getUserProfile(): UserProfile? {
        val jsonString = encryptedPrefs.getString(KEY_USER_PROFILE, null)
        return jsonString?.let { 
            val jsonObject = JSONObject(it)
            UserProfile.fromJson(jsonObject)
        }
    }
    
    fun isTokenValid(): Boolean {
        val token = getAccessToken()
        val expiry = getTokenExpiry()
        return !token.isNullOrEmpty() && System.currentTimeMillis() < expiry
    }
    
    fun clearTokens() {
        encryptedPrefs.edit {
            remove(KEY_ACCESS_TOKEN)
            remove(KEY_REFRESH_TOKEN)
            remove(KEY_TOKEN_EXPIRY)
            remove(KEY_USER_PROFILE)
        }
    }
}
