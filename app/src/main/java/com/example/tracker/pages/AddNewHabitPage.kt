package com.example.tracker.pages

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tracker.viewmodels.HabitViewModel
import com.example.tracker.viewmodels.HabitViewModelMode
import com.example.tracker.viewmodels.HabitOperationStatus

@Composable
fun AddNewHabitPage(
    modifier: Modifier = Modifier,
    viewModel: HabitViewModel = viewModel()
) {
    val context = LocalContext.current

    // Collect the UI state from the ViewModel
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Initialize the ViewModel in ADD mode on first time
    LaunchedEffect(Unit) {
        viewModel.initialize(HabitViewModelMode.ADD)
    }

    // Handle side effects like showing Toasts and navigating back
    LaunchedEffect(uiState.status) {
        when (uiState.status) {
            HabitOperationStatus.SUCCESS -> {
                Toast.makeText(context, "Habit Added!", Toast.LENGTH_SHORT).show()
                viewModel.consumeStatusEvent() // Signal that the event has been handled
            }
            HabitOperationStatus.ERROR -> {
                Toast.makeText(context, uiState.errorMessage ?: "Failed to save habit", Toast.LENGTH_SHORT).show()
                viewModel.consumeStatusEvent() // Signal that the event has been handled
            }
            else -> { }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Add new habit",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Habit Title Input
        OutlinedTextField(
            value = uiState.habitTitle,
            onValueChange = { viewModel.updateTitle(it) },
            label = {
                Text(
                    text = "Habit title",
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = uiState.status != HabitOperationStatus.SAVING
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Give your habit a name",
            fontSize = 15.sp,
            modifier = Modifier.align(Alignment.Start),
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Habit Description Input
        OutlinedTextField(
            value = uiState.habitDescription,
            onValueChange = { viewModel.updateDescription(it) },
            label = {
                Text(
                    text = "Habit description (optional)",
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            },
            modifier = Modifier.fillMaxWidth(),
            minLines = 4,
            maxLines = 5,
            enabled = uiState.status != HabitOperationStatus.SAVING
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Describe your habit",
            fontSize = 15.sp,
            modifier = Modifier.align(Alignment.Start),
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )

        Spacer(modifier = Modifier.height(30.dp))

        // Row of the Days of the Week Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
            for (i in days.indices) {
                DaySelector(
                    day = days[i],
                    isSelected = uiState.selectedDays[i],
                    enabled = uiState.status != HabitOperationStatus.SAVING,
                    onClick = { viewModel.toggleDay(i) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Select days when you want to keep up this habit",
            fontSize = 15.sp,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(bottom = 25.dp),
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )

        Spacer(modifier = Modifier.weight(1f))

        // ✅ Save Button
        Button(
            onClick = { viewModel.saveOrUpdateHabit() },
            enabled = uiState.isSaveEnabled && uiState.status != HabitOperationStatus.SAVING,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (uiState.status == HabitOperationStatus.SAVING) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "Add Habit",
                    color = Color.White
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}


@Composable
fun DaySelector(
    day: String,
    isSelected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
    val contentColor = MaterialTheme.colorScheme.onTertiary

    Box(
        modifier = Modifier
            .size(40.dp)
            .background(
                color = if (enabled) backgroundColor else backgroundColor.copy(alpha = 0.5f),
                shape = RoundedCornerShape(30),
            )
            .clickable(
                enabled = enabled,
                indication = null,
                interactionSource = interactionSource,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = day,
            color = if (enabled) contentColor else contentColor.copy(alpha = 0.5f),
            fontWeight = FontWeight.Normal
        )
    }
}