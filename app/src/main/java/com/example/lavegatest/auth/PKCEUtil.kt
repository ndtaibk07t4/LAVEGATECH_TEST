package com.example.lavegatest.auth

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

object PKCEUtil {
    
    private val secureRandom = SecureRandom()
    private val allowedChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-._~"
    
    fun generateCodeVerifier(): String {
        val codeVerifier = StringBuilder(OAuthConfig.CODE_VERIFIER_LENGTH)
        repeat(OAuthConfig.CODE_VERIFIER_LENGTH) {
            val randomIndex = secureRandom.nextInt(allowedChars.length)
            codeVerifier.append(allowedChars[randomIndex])
        }
        return codeVerifier.toString()
    }
    
    fun generateCodeChallenge(codeVerifier: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(codeVerifier.toByteArray(Charsets.US_ASCII))
        return Base64.getUrlEncoder().withoutPadding().encodeToString(hash)
    }
    
    fun generateState(): String {
        val state = StringBuilder(32)
        repeat(32) {
            val randomIndex = secureRandom.nextInt(allowedChars.length)
            state.append(allowedChars[randomIndex])
        }
        return state.toString()
    }
}
