package com.example.tracker

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.tracker.screens.EditHabitScreen
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
        composable(route = "login") {
            LogInScreen(modifier, navController, authViewModel)
        }
        composable(route = "signup") {
            SignUpScreen(modifier, navController, authViewModel)
        }
        composable(route = "home") {
            HomeScreen(modifier, navController, authViewModel)
        }
        composable(route = "editHabit/{habitId}",
            arguments = listOf(navArgument("habitId")
            { type = NavType.StringType })
        )
        { backStackEntry ->
                val habitId = backStackEntry.arguments?.getString("habitId")
                EditHabitScreen(modifier, navController, habitId = habitId)
        }
    }
}