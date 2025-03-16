package com.example.tracker

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.tracker.screens.HomeScreen
import com.example.tracker.screens.LogInScreen
import com.example.tracker.screens.SignUpScreen
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

@Composable
fun MyAppNavigation(modifier: Modifier = Modifier, authViewModel: AuthViewModel) {
    val navController = rememberNavController()

    val isLoggedIn = Firebase.auth.currentUser != null
    val firstPage = if (isLoggedIn) "home" else "login"

    NavHost(navController = navController, startDestination = firstPage) {
        //NavHost(navController = navController, startDestination = firstPage)
        composable("login") {
            LogInScreen(modifier, navController, authViewModel)
        }
        composable("signup") {
            SignUpScreen(modifier, navController, authViewModel)
        }
        composable("home") {
            HomeScreen(modifier, navController, authViewModel)
        }
    }
}