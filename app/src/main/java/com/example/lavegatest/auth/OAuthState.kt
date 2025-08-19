package com.example.lavegatest.auth

data class OAuthState(
    val codeVerifier: String,
    val state: String,
    val timestamp: Long = System.currentTimeMillis()
) {
    companion object {
        fun create(): OAuthState {
            return OAuthState(
                codeVerifier = PKCEUtil.generateCodeVerifier(),
                state = PKCEUtil.generateState()
            )
        }
    }
    
    fun isValid(): Boolean {
        // State is valid for 10 minutes
        val tenMinutesInMillis = 10 * 60 * 1000L
        return System.currentTimeMillis() - timestamp < tenMinutesInMillis
    }
}
