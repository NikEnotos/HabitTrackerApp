package com.example.tracker.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.tracker.model.HabitModel
import com.example.tracker.ui.theme.DangerousButton
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun EditHabitScreen(modifier: Modifier = Modifier, navController: NavController, habitId: String?) {

    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val userId = Firebase.auth.currentUser?.uid

    var habit by remember {
        mutableStateOf(HabitModel())
    }
    var isLoading by remember {
        mutableStateOf(true)
    }

    var habitTitleFieldValue by remember {
        mutableStateOf(TextFieldValue(""))
    }
    var habitDescriptionFieldValue by remember {
        mutableStateOf(TextFieldValue(""))
    }
    // Track selected days as an array of booleans (default: all false)
    var selectedDays by remember {
        mutableStateOf(List(7) { true })
    }

    // Fetch habits from Firebase
    LaunchedEffect(habitId) {
        if (userId != null && habitId != null) {

            db.collection("habits")
                .document(userId)
                .collection("userHabits")
                .document(habitId)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        isLoading = false
                        val document = task.result
                        if (document != null && document.exists()) {
                            // ✅ Extract data from a single document
                            val habitID =
                                document.getString("habitID") ?: return@addOnCompleteListener
                            val habitName =
                                document.getString("habitName") ?: return@addOnCompleteListener
                            val habitDescription = document.getString("habitDescription")
                                ?: return@addOnCompleteListener
                            val streak = document.getLong("streak")?.toInt() ?: 0
                            val lastTimeCompleted =
                                document.getTimestamp("lastTimeCompleted") ?: Timestamp.now()
                            val activeDays =
                                document.get("activeDays") as? List<Boolean> ?: List(7) { false }

                            habit = HabitModel(
                                habitID = habitID,
                                habitName = habitName,
                                habitDescription = habitDescription,
                                streak = streak,
                                lastTimeCompleted = lastTimeCompleted,
                                activeDays = activeDays
                            )

                            habitTitleFieldValue = TextFieldValue(habit.habitName)
                            habitDescriptionFieldValue = TextFieldValue(habit.habitDescription)
                            selectedDays = habit.activeDays

                        } else {
                            Log.d("Firebase", "No such document")
                            Toast.makeText(context, "Habit not found", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        isLoading = false
                        Log.d("Firebase", "get failed with ", task.exception)
                        Toast.makeText(context, "Failed to retrieve habit", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
        }

    }


    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {


        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                        //.background(PrimaryBackground, shape = RoundedCornerShape(12.dp)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                        )
                        {
                            Text(
                                text = "Edit habit",
                                fontSize = 30.sp,
                                fontWeight = FontWeight.Bold,
                                //modifier = Modifier.align(Alignment.Start)
                                modifier = Modifier.weight(1f)
                            )
                            // 🗑 Delete button
                            IconButton(
                                onClick = {
                                    db.collection("habits")
                                        .document(userId!!)
                                        .collection("userHabits")
                                        .document(habitId!!)
                                        .delete()
                                        .addOnSuccessListener {
                                            Toast.makeText(
                                                context,
                                                "Habit deleted successfully",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            navController.popBackStack()
                                        }
                                        .addOnFailureListener { e ->
                                            Log.w("Firebase", "Error deleting document", e)
                                            Toast.makeText(
                                                context,
                                                "Failed to delete habit",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                },
                                colors = IconButtonDefaults.iconButtonColors(
                                    containerColor = DangerousButton // Set the button's background color
                                ),
                                modifier = Modifier.padding(start = 10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Delete,
                                    contentDescription = "Delete habit",
                                    tint = Color.White // Set the icon's color
                                )
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
                            value = habitTitleFieldValue,
                            onValueChange = { habitTitleFieldValue = it },
                            modifier = Modifier
                                .fillMaxWidth(),
                            //.padding(horizontal = 24.dp),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(16.dp))


                        // Habit Description Input (Optional)
                        OutlinedTextField(
                            label = {
                                Text(
                                    text = "Habit description (optional)",
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            },
                            value = habitDescriptionFieldValue,
                            onValueChange = { habitDescriptionFieldValue = it },
                            modifier = Modifier
                                .fillMaxWidth(),
                            //.padding(horizontal = 24.dp),
                            minLines = 4,
                            maxLines = 4
                        )

                        Spacer(modifier = Modifier.height(30.dp))


                        // Row of 7 Circle Radio Buttons for Days of the Week
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            //.padding(start = 24.dp, end = 24.dp, bottom = 24.dp),
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
                    }
                }
            }
            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Spacer(modifier = Modifier.weight(0.3f))

                    // Cancel button
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        onClick = {
                            navController.popBackStack()
                        },

                        ) {
                        Text(
                            text = "Cancel",
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }

                    Spacer(modifier = Modifier.weight(0.3f))

                    // Update button
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        onClick = {
                            if (habitTitleFieldValue.text.isBlank()) {
                                Toast.makeText(
                                    context,
                                    "Habit Title is required!",
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                                return@Button
                            }

                            if (userId == null) {
                                Toast.makeText(context, "User not found", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            // Create HabitModel object
                            val updatedHabit = HabitModel(
                                habitID = habit.habitID,
                                habitName = habitTitleFieldValue.text,
                                habitDescription = habitDescriptionFieldValue.text,
                                lastTimeCompleted = habit.lastTimeCompleted,
                                streak = habit.streak,
                                activeDays = selectedDays
                            )

                            // Save to Firebase
                            db.collection("habits").document(userId)
                                .collection("userHabits")
                                .document(habit.habitID)
                                .set(updatedHabit)
                                .addOnSuccessListener {
                                    Toast.makeText(
                                        context,
                                        "Habit edited successfully!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    navController.popBackStack()
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(
                                        context,
                                        "Failed: ${e.message}",
                                        Toast.LENGTH_SHORT
                                    )
                                        .show()
                                }
                        },
                    ) {
                        Text(
                            text = "Update",
                            fontSize = 20.sp,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.weight(0.3f))
                }
            }
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
                interactionSource = remember { MutableInteractionSource() } // Prevents click animations
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text = day, color = MaterialTheme.colorScheme.onTertiary)
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

//fun DeleteHabit(
//    db: FirebaseFirestore,
//    userId: String,
//    habitId: String,
//    onDeletionComplete: (Boolean) -> Unit // Callback to indicate success or failure
//) {
//    val context = LocalContext.current
//
//    db.collection("habits")
//        .document(userId)
//        .collection("userHabits")
//        .document(habitId)
//        .delete()
//        .addOnSuccessListener {
//            onDeletionComplete(true) // Indicate success
//            Toast.makeText(context, "Habit deleted successfully", Toast.LENGTH_SHORT).show()
//        }
//        .addOnFailureListener { e ->
//            onDeletionComplete(false) // Indicate failure
//            Log.w("Firebase", "Error deleting document", e)
//            Toast.makeText(context, "Failed to delete habit", Toast.LENGTH_SHORT).show()
//        }
//}