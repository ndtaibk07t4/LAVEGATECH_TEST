package com.example.lavegatest.navigation

sealed class NavRoutes(val route: String) {
    data object SignIn : NavRoutes("signIn")
    data object Profile : NavRoutes("profile")
}
