package com.example.tracker

//import android.os.Bundle
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.activity.enableEdgeToEdge
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.padding
//import androidx.compose.material3.Scaffold
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.tooling.preview.Preview
//import com.example.tracker.ui.theme.TrackerTheme
//import android.widget.Button
//import android.widget.TextView

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.tracker.ui.theme.TrackerTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.compose.material.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val authViewModel : AuthViewModel by viewModels()
        setContent {
            TrackerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MyAppNavigation(modifier = Modifier.padding(innerPadding), authViewModel)
                }
            }
        }
    }
}

//@Composable
//fun Navigation() {
//    val navController = rememberNavController()
//    NavHost(navController, startDestination = "home") {
//        composable("home") { HomeScreen(navController) }
//        composable("details") { DetailsScreen() }
//    }
//}
//
//@Composable
//fun HomeScreen(navController: NavController) {
//    Button(onClick = { navController.navigate("details") }) {
//        Text("Go to Details")
//    }
//}
//
//@Composable
//fun DetailsScreen() {
//    Text("Details Screen")
//}