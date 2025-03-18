package com.example.tracker.pages

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier


import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tracker.model.HabitModel
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun AddNewHabitPage(modifier: Modifier = Modifier) {

    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val userId = Firebase.auth.currentUser?.uid!!

    var habitTitle by remember {
        mutableStateOf(TextFieldValue(""))
    }
    var habitDescription by remember {
        mutableStateOf(TextFieldValue(""))
    }

    // State to track selected days (0 = Sunday, 6 = Saturday)
    // Track selected days as an array of booleans (default: all false)
    var selectedDays by remember {
        mutableStateOf(List(7) { true })
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(text = "Add new habit",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Habit Title Input
        OutlinedTextField(
            value = habitTitle,
            onValueChange = { habitTitle = it },
            label = {
                Text(
                    text ="Habit title",
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )},
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Give your habit a name",
            fontSize = 15.sp,
            modifier = Modifier.align(Alignment.Start),
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )

        Spacer(modifier = Modifier.height(16.dp))


        // Habit Description Input (Optional)
        OutlinedTextField(
            value = habitDescription,
            onValueChange = { habitDescription = it },
            label = {
                Text(
                    text = "Habit description (optional)",
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )},
            modifier = Modifier.fillMaxWidth(),
            minLines = 4,
            maxLines = 5
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(text = "Describe your habit",
            fontSize = 15.sp,
            modifier = Modifier.align(Alignment.Start),
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )

        Spacer(modifier = Modifier.height(30.dp))


        // Row of 7 Circle Radio Buttons for Days of the Week
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
            for (i in days.indices) {
                DaySelector(
                    day = days[i],
                    isSelected = selectedDays[i],
                    onClick = {
                        selectedDays = selectedDays.toMutableList().apply {
                            this[i] = !this[i] // Toggle selection
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(text = "Pick days when you should keep up this habit",
            fontSize = 15.sp,
            modifier = Modifier.align(Alignment.Start),
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Save Button
        Button(
            onClick = {
                if (habitTitle.text.isBlank()) {
                    Toast.makeText(context, "Habit Title is required!", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                if (selectedDays == List(7) { false }) {
                    Toast.makeText(context, "You have to pick at least one day", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                // Create HabitModel object
                val habit = HabitModel(
                    habitID = Timestamp.now().toDate().toString(),
                    habitName = habitTitle.text,
                    habitDescription = habitDescription.text,
                    lastTimeCompleted = Timestamp(seconds = 0, nanoseconds = 0),
                    streak = 0,
                    activeDays = selectedDays
                )

                // Save to Firebase
                db.collection("habits").document(userId)
                    .collection("userHabits")
                    .document(habit.habitID)
                    .set(habit)
                    .addOnSuccessListener {
                        Toast.makeText(context, "Habit Added!", Toast.LENGTH_SHORT).show()

                        // Return page to the default state
                        habitTitle = TextFieldValue("")
                        habitDescription = TextFieldValue("")
                        selectedDays = List(7) { true }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Save Habit",
                color = Color.White
                )
        }
    }
}

// Helper Composable for Circle Button Selector
@Composable
fun DaySelector(day: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .background(
                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                shape = RoundedCornerShape(30),
            )
            .clickable(
                indication = null, // Removes default ripple effect
                interactionSource = remember { MutableInteractionSource() }, // Prevents click animations
            ) { onClick() },
        contentAlignment = Alignment.Center,

    ) {
        Text(
            text = day,
            color = MaterialTheme.colorScheme.onTertiary,
            fontWeight = FontWeight.Normal
            )
    }
}

