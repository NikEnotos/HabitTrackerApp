package com.example.tracker.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.tracker.ui.theme.DangerousButton
import com.example.tracker.viewmodels.HabitViewModel
import com.example.tracker.viewmodels.HabitViewModelMode
import com.example.tracker.viewmodels.HabitOperationStatus

@Composable
fun EditHabitScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    habitId: String?,
    viewModel: HabitViewModel = viewModel()
) {
    val context = LocalContext.current

    // Collect state from ViewModel
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Initialize ViewModel in EDIT mode with the habitId when the screen is shown
    LaunchedEffect(habitId) {
        viewModel.initialize(HabitViewModelMode.EDIT, habitId)
    }

    // Handle side effects like showing Toasts and navigating back
    LaunchedEffect(uiState.status, uiState.navigateBackEvent) {
        when (uiState.status) {
            HabitOperationStatus.SUCCESS -> {
                Toast.makeText(context, "Habit updated successfully!", Toast.LENGTH_SHORT).show()
                // Navigation handled below based on navigateBackEvent
            }

            HabitOperationStatus.DELETED -> {
                Toast.makeText(context, "Habit deleted successfully", Toast.LENGTH_SHORT).show()
                // Navigation handled below based on navigateBackEvent
            }

            HabitOperationStatus.ERROR -> {
                Toast.makeText(
                    context,
                    uiState.errorMessage ?: "An error occurred",
                    Toast.LENGTH_SHORT
                ).show()
                viewModel.consumeStatusEvent()
            }

            else -> { /* No action needed for other statuses */
            }
        }

        // Handle navigation event
        if (uiState.navigateBackEvent) {
            navController.popBackStack()
            viewModel.consumeNavigationEvent()
        }
    }

    // Determine if UI should be interactive
    val isInteractive =
        uiState.status == HabitOperationStatus.LOADED || uiState.status == HabitOperationStatus.ERROR

    Box(modifier = modifier.fillMaxSize()) {
        when (uiState.status) {
            HabitOperationStatus.IDLE, HabitOperationStatus.LOADING -> {
                // Show loading indicator
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            HabitOperationStatus.LOADED, HabitOperationStatus.SAVING,
            HabitOperationStatus.DELETING, HabitOperationStatus.ERROR -> {
                EditHabitForm(
                    uiState = uiState,
                    viewModel = viewModel,
                    navController = navController,
                    isInteractive = isInteractive
                )
            }
            // SUCCESS and DELETED states are handled by LaunchedEffect for navigation
            HabitOperationStatus.SUCCESS, HabitOperationStatus.DELETED -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@Composable
private fun EditHabitForm(
    uiState: com.example.tracker.viewmodels.HabitUiState,
    viewModel: HabitViewModel,
    navController: NavController,
    isInteractive: Boolean
) {
    val showUpdateProgress = uiState.status == HabitOperationStatus.SAVING
    val showDeleteProgress = uiState.status == HabitOperationStatus.DELETING

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Edit habit",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )

                    // 🚮 Delete button
                    IconButton(
                        onClick = { viewModel.deleteHabit() },
                        enabled = isInteractive && !showDeleteProgress && !showUpdateProgress,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = DangerousButton,
                            disabledContainerColor = DangerousButton.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.padding(start = 10.dp)
                    ) {
                        if (showDeleteProgress) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "Delete habit",
                                tint = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                // Habit Title Input
                OutlinedTextField(
                    label = {
                        Text(
                            text = "Habit title",
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    textStyle = TextStyle(fontWeight = FontWeight.Bold),
                    value = uiState.habitTitle,
                    onValueChange = { viewModel.updateTitle(it) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = isInteractive && !showDeleteProgress && !showUpdateProgress
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Habit Description Input
                OutlinedTextField(
                    label = {
                        Text(
                            text = "Habit description (optional)",
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    },
                    value = uiState.habitDescription,
                    onValueChange = { viewModel.updateDescription(it) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4,
                    maxLines = 4,
                    enabled = isInteractive && !showDeleteProgress && !showUpdateProgress
                )

                Spacer(modifier = Modifier.height(30.dp))

                // 📆 Row of the Days of the Week
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                    for (i in days.indices) {
                        DaySelector(
                            day = days[i],
                            isSelected = uiState.selectedDays[i],
                            enabled = isInteractive && !showDeleteProgress && !showUpdateProgress,
                            onClick = { viewModel.toggleDay(i) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // ❌ Cancel button
            Button(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                onClick = { navController.popBackStack() },
                enabled = !showDeleteProgress && !showUpdateProgress
            ) {
                Text(
                    text = "Cancel",
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSecondary
                )
            }

            // ✅ Update button
            Button(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                onClick = { viewModel.saveOrUpdateHabit() },
                enabled = uiState.isSaveEnabled && isInteractive && !showDeleteProgress && !showUpdateProgress
            ) {
                if (showUpdateProgress) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Update",
                        fontSize = 18.sp,
                        color = Color.White
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

    }
}


// Helper Composable for Day Selector
@Composable
fun DaySelector(day: String, isSelected: Boolean, enabled: Boolean, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val backgroundColor =
        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
    val contentColor = MaterialTheme.colorScheme.onTertiary

    Box(
        modifier = Modifier
            .size(40.dp)
            .background(
                // Adjust alpha if disabled
                color = if (enabled) backgroundColor else backgroundColor.copy(alpha = 0.5f),
                shape = RoundedCornerShape(30),
            )
            .clickable(
                enabled = enabled,
                indication = null,
                interactionSource = interactionSource, // Prevents click animations
                onClick = onClick
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = day,
            // Adjust alpha if disabled
            color = if (enabled) contentColor else contentColor.copy(alpha = 0.5f),
            fontWeight = FontWeight.Normal
        )
    }
}


//@Composable
//fun DeleteHabitConfirmation(
//    db: FirebaseFirestore,
//    userId: String,
//    habitId: String,
//    onDeletionComplete: (Boolean) -> Unit,
//    onDismiss: () -> Unit // Callback to dismiss the dialog
//) {
//    val context = LocalContext.current
//
//    AlertDialog(
//        onDismissRequest = { onDismiss() }, // Call onDismiss when dialog is dismissed
//        title = { Text("Delete Habit?") },
//        text = { Text("Are you sure you want to delete this habit?") },
//        confirmButton = {
//            TextButton(
//                onClick = {
//                    DeleteHabit(db, userId, habitId) { success ->
//                        onDeletionComplete(success)
//                    }
//                    onDismiss() // Dismiss the dialog after deletion
//                }
//            ) {
//                Text("Delete")
//            }
//        },
//        dismissButton = {
//            TextButton(
//                onClick = { onDismiss() } // Dismiss the dialog
//            ) {
//                Text("Cancel")
//            }
//        }
//    )
//}