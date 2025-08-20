package com.example.lavegatest

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.lavegatest.auth.AuthManager
import com.example.lavegatest.navigation.LavegaNavHost
import com.example.lavegatest.ui.theme.LavegatestTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var authManager: AuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Handle OAuth callback from intent
        handleOAuthCallback(intent)

        setContent {
            LavegatestTheme {
                LavegaApp(authManager)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleOAuthCallback(intent)
    }

    private fun handleOAuthCallback(intent: Intent?) {
        intent?.data?.let { uri ->
            if (uri.host == "fir-demo-b9ae6.web.app") {
                authManager.handleOAuthCallback(uri.toString())
            }
        }
    }
}

@Composable
fun LavegaApp(authManager: AuthManager) {
    val navController = rememberNavController()
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        LavegaNavHost(navController = navController, authManager = authManager)
    }
}