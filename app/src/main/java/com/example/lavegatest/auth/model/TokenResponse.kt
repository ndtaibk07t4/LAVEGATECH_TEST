package com.example.lavegatest.auth.model

import org.json.JSONObject

data class TokenResponse(
    val accessToken: String,
    val tokenType: String,
    val expiresIn: Int,
    val refreshToken: String? = null,
    val scope: String
) {
    companion object {
        fun fromJson(json: JSONObject): TokenResponse {
            return TokenResponse(
                accessToken = json.getString("access_token"),
                tokenType = json.getString("token_type"),
                expiresIn = json.getInt("expires_in"),
                refreshToken = if (json.has("refresh_token")) json.getString("refresh_token") else null,
                scope = json.getString("scope")
            )
        }
    }
}