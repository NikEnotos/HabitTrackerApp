package com.example.tracker.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.tracker.AuthViewModel

@Composable
fun UserProfilePage(modifier: Modifier = Modifier, navController: NavController, authViewModel: AuthViewModel) {

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "User Profile", fontSize = 32.sp)

        TextButton(onClick = {
            authViewModel.logout()
            navController.navigate("login"){
                popUpTo("home"){inclusive = true}
            }
        }) {
            Text(text = "Logout")
        }
    }
}