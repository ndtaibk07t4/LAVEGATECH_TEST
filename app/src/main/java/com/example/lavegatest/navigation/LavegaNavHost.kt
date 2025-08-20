package com.example.lavegatest.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.lavegatest.auth.AuthManager
import com.example.lavegatest.presentation.auth.SignInScreen
import com.example.lavegatest.presentation.auth.SignInViewModel
import com.example.lavegatest.presentation.profile.ProfileScreen
import com.example.lavegatest.presentation.profile.ProfileViewModel
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun LavegaNavHost(
    navController: NavHostController,
    authManager: AuthManager,
    startDestination: String = NavRoutes.SignIn.route
) {
    val authRepository = authManager.getAuthRepository()
    val isSignedIn by authRepository.isSignedIn.collectAsState(initial = authRepository.isUserSignedIn())
    
    LaunchedEffect(isSignedIn) {
        if (isSignedIn) {
            navController.navigate(NavRoutes.Profile.route) {
                popUpTo(NavRoutes.SignIn.route) { inclusive = true }
            }
        } else {
            navController.navigate(NavRoutes.SignIn.route) {
                popUpTo(NavRoutes.Profile.route) { inclusive = true }
            }
        }
    }
    
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(NavRoutes.SignIn.route) {
            val viewModel: SignInViewModel = hiltViewModel()
            val oAuthCallbacks by authManager.oAuthCallbacks.collectAsState(initial = "")
            
            LaunchedEffect(oAuthCallbacks) {
                if (oAuthCallbacks.isNotEmpty()) {
                    viewModel.handleOAuthCallback(oAuthCallbacks)
                }
            }
            
            SignInScreen(
                onCompletedAction = {
                    navController.navigate(NavRoutes.Profile.route) {
                        popUpTo(NavRoutes.SignIn.route) { inclusive = true }
                    }
                },
                viewModel = viewModel
            )
        }

        composable(NavRoutes.Profile.route) {
            ProfileScreen(
                onCompletedSigningOutAction = {
                    navController.navigate(NavRoutes.SignIn.route) {
                        popUpTo(NavRoutes.Profile.route) { inclusive = true }
                    }
                }
            )
        }
    }
}
