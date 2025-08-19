package com.example.lavegatest.auth.model

import org.json.JSONObject

data class OAuthError(
    val error: String,
    val errorDescription: String? = null
) {
    companion object {
        fun fromJson(json: JSONObject): OAuthError {
            return OAuthError(
                error = json.getString("error"),
                errorDescription = if (json.has("error_description")) json.getString("error_description") else null
            )
        }
    }
}