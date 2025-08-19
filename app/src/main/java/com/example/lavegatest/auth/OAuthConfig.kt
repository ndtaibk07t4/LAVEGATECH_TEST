package com.example.lavegatest.auth

object OAuthConfig {
    const val GOOGLE_AUTH_ENDPOINT = "https://accounts.google.com/o/oauth2/v2/auth"
    const val GOOGLE_TOKEN_ENDPOINT = "https://oauth2.googleapis.com/token"
    const val GOOGLE_USER_INFO_ENDPOINT = "https://www.googleapis.com/oauth2/v2/userinfo"
    
    // parameters
    const val RESPONSE_TYPE = "code"
    const val SCOPE = "openid email profile"
    const val ACCESS_TYPE = "offline"
    
    // configuration
    const val REDIRECT_URI = "com.example.lavegatest:/oauth2redirect"
    const val STATE_PARAMETER = "state"
    const val CODE_VERIFIER_LENGTH = 128
    const val CODE_CHALLENGE_METHOD = "S256"
}
