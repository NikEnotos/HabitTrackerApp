package com.example.tracker.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.tracker.AuthViewModel
import com.example.tracker.ui.theme.DangerousButton
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun UserProfilePage(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel
) {

    val user = Firebase.auth.currentUser

    var userEmailAddress by remember {
        mutableStateOf("Can't load your email address")
    }
    var formattedDate by remember {
        mutableStateOf("can't load this info")
    }

    LaunchedEffect(user) {
        if (user != null) {

            userEmailAddress = Firebase.auth.currentUser!!.email.toString()
            val userMetadata = Firebase.auth.currentUser!!.metadata
            val creationTimestamp = userMetadata!!.creationTimestamp

            val creationDate = Date(creationTimestamp)
            val dateFormat = SimpleDateFormat("dd MM yyyy", Locale.getDefault()) // Format the date
            formattedDate = dateFormat.format(creationDate)

        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "User Profile",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(30.dp))

        OutlinedTextField(
            value = userEmailAddress,
            enabled = false,
            onValueChange = {},
            readOnly = true,
            label = {
                Text(
                    text = "Email address",
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(30.dp))

        Text(
            text = "Account created: $formattedDate",
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.weight(0.5f))

        TextButton(
            onClick = {
                authViewModel.logout()
                navController.navigate("login") {
                    popUpTo("home") { inclusive = true }
                }
            },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text(
                text = "Logout",
                fontSize = 20.sp,
                color = DangerousButton
            )
        }
        Spacer(modifier = Modifier.height(80.dp))
    }

}