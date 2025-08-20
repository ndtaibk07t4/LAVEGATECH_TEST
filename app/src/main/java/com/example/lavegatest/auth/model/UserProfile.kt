package com.example.lavegatest.auth.model

import org.json.JSONObject

data class UserProfile(
    val id: String,
    val email: String,
    val verifiedEmail: Boolean,
    val name: String,
    val givenName: String,
    val familyName: String,
    val picture: String
) {
    companion object {
        fun fromJson(json: JSONObject): UserProfile {
            return UserProfile(
                id = json.getString("id"),
                email = json.getString("email"),
                verifiedEmail = json.getBoolean("verified_email"),
                name = json.getString("name"),
                givenName = json.getString("given_name"),
                familyName = json.getString("family_name"),
                picture = json.getString("picture")
            )
        }
    }
}