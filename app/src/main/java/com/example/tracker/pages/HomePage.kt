package com.example.tracker.pages

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.tracker.ui.components.AddNewHabitBanner
import com.example.tracker.ui.components.HabitItem
import com.example.tracker.viewmodels.HomeViewModel

@Composable
fun HomePage(modifier: Modifier = Modifier, navController: NavController) {
    val context = LocalContext.current
    val viewModel: HomeViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()

    // Check if user is authenticated
    if (viewModel.currentUser == null) {
        Toast.makeText(context, "User not logged in!", Toast.LENGTH_SHORT).show()
        return
    }

    // Show loading indicator when loading
    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    // Show error message if any
    uiState.error?.let { errorMsg ->
        Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
    }

    // Show "AddNewHabitBanner" if no habits
    if (viewModel.hasNoHabits()) {
        AddNewHabitBanner(modifier)
        return
    }

    val sortedHabits = viewModel.getSortedHabits()

    // Show list of habits
    LazyColumn(modifier.fillMaxSize()) {
        items(items = sortedHabits) { habit ->
            HabitItem(
                habit = habit,
                onMarkAsDone = viewModel::markHabitAsDone,
                navController = navController
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}