package com.example.lavegatest.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.lavegatest.presentation.auth.SignInScreen
import com.example.lavegatest.presentation.profile.ProfileScreen

@Composable
fun LavegaNavHost(
    navController: NavHostController,
    startDestination: String = NavRoutes.SignIn.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(NavRoutes.SignIn.route) {
            SignInScreen(
                onCompletedAction = {
                    navController.navigate(NavRoutes.Profile.route) {
                        popUpTo(NavRoutes.SignIn.route) { inclusive = true }
                    }
                }
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
