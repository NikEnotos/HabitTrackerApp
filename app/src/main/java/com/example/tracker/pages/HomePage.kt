package com.example.tracker.pages


import android.R.attr.text
import android.util.Log
import android.util.Range
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tracker.R
import com.example.tracker.model.HabitModel
import com.example.tracker.ui.theme.*
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore


@Composable
fun HomePage(modifier: Modifier = Modifier) {

    val context = LocalContext.current
    val db = Firebase.firestore
    val userId = Firebase.auth.currentUser?.uid
    val user = Firebase.auth.currentUser

    var habits by remember { mutableStateOf<List<HabitModel>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    if (user == null) {
        Toast.makeText(context, "User not logged in!", Toast.LENGTH_SHORT).show()
        return
    }

    // Fetch habits from Firebase
    LaunchedEffect(userId) {
        if (userId != null) {

            db.collection("habits").document(userId).collection("userHabits")
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Log.e("Firebase", "Error fetching habits", e)
                        Toast.makeText(context, "Failed to load habits", Toast.LENGTH_SHORT).show()
                        isLoading = false
                        return@addSnapshotListener
                    }

                    // ✅ Manually extracting data instead of using `.toObject()`
                    habits = snapshot?.documents?.mapNotNull { document ->
                        val habitID = document.getString("habitID") ?: return@mapNotNull null
                        val habitName = document.getString("habitName") ?: return@mapNotNull null
                        val habitDescription = document.getString("habitDescription") ?: return@mapNotNull null
                        val streak = document.getLong("streak")?.toInt() ?: 0
                        val lastTimeCompleted = document.getTimestamp("lastTimeCompleted") ?: Timestamp.now()
                        val activeDays = document.get("activeDays") as? List<Boolean> ?: List(7) { false }

                        HabitModel(
                            habitID = habitID,
                            habitName = habitName,
                            habitDescription = habitDescription,
                            streak = streak,
                            lastTimeCompleted = lastTimeCompleted,
                            activeDays = activeDays
                        )
                    } ?: emptyList()

                    isLoading = false

                    // ✅ Check if the habits list is empty
                    if (habits.isEmpty()) {
                        Toast.makeText(context, "No habits found.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "User has ${habits.size} habits.", Toast.LENGTH_SHORT).show()
                    }
                }
        }

    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        if (habits.isEmpty()) {
            AddNewHabitButton({

            })
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                items(habits) { habit ->
                    HabitItem(habit, db, userId)
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun AddNewHabitButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,

    ) {
        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(containerColor = ActiveButtonColor),
            contentPadding = PaddingValues(16.dp) // Add padding inside the button
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "Add",
                tint = WhitePrimaryText,
                modifier = Modifier.size(28.dp) // Adjust icon size
            )
            Spacer(modifier = Modifier.width(8.dp)) // Add space between icon and text
            Text(
                text = "Add new habit",
                color = WhitePrimaryText,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        }
    }
}

@Composable
fun HabitItem(habit: HabitModel, db: FirebaseFirestore, userId: String?) {
    val context = LocalContext.current

    // Check if habit is completed today
    val todayIndex = (Timestamp.now().toDate().day + 6) % 7

    val today = Timestamp.now().toDate()
    val lastCompletedDate = habit.lastTimeCompleted?.toDate()
    var isCompletedToday = lastCompletedDate!!.date == today.date
    val isForToday = habit.activeDays[todayIndex]


    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(PrimaryBackground, shape = RoundedCornerShape(12.dp)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp),
                verticalAlignment = Alignment.Top
            ) {
                // 🔥 Streak Counter with Flame Icon
                Column(
                    modifier = Modifier.padding(end = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = habit.streak.toString(),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = BlackPrimaryText
                    )

                    Icon(
                        painter = painterResource(id = if (isCompletedToday) R.drawable.flame_on else R.drawable.flame_off),
                        contentDescription = "Streak Flame",
                        modifier = Modifier.size(32.dp),
                        tint = Color.Unspecified // Keep original icon colors
                    )
                }

                Column(modifier = Modifier
                    //.padding(end = 10.dp)
                    .weight(1f)) {
                    Row() {
                        // 🏷 Habit Title (Bold)
                        Text(
                            modifier = Modifier.weight(1f),
                            text = habit.habitName,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = BlackPrimaryText,
                        )

                        IconButton(
                            onClick = {},
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = SecondaryButton // Set the button's background color
                            ),
                            modifier = Modifier.padding(start = 10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = "Edit",
                                tint = Color.Black // Set the icon's color
                            )
                        }
                    }

                    // 📄 Habit Description (Small Text)
                    if (habit.habitDescription.isNotBlank()) {
                        Text(
                            modifier = Modifier.padding(top = 15.dp),
                            text = habit.habitDescription,
                            fontSize = 14.sp,
                            color = BlackSecondaryText
                        )
                    }
                }


            }
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
            ) {
                // 📅 Days of the Week
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                    for (i in days.indices) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    if (habit.activeDays[i]) ActiveDayColor else InactiveDayColor,
                                    shape = RoundedCornerShape(30),
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if(i == todayIndex) {
                                OutlinedFilledText(
                                    text = days[i],
                                    outlineColor = CurrentDayOutlineColor,
                                    fillColor = WhitePrimaryText,
                                    fontSize = 14.sp,
                                )
                            }
                            else{
                                Text(
                                    text = days[i],
                                    fontWeight = FontWeight.Normal,
                                    color = WhitePrimaryText,
                                    fontSize = 14.sp,
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                }

                // ✅ Mark Habit as Completed Button
                if(isForToday) {
                    Button(
                        onClick = {
                            if (!isCompletedToday && isForToday) {
                                val updatedStreak =
                                    if (habit.activeDays[todayIndex]) habit.streak + 1 else 0
                                val updatedHabit = habit.copy(
                                    streak = updatedStreak,
                                    lastTimeCompleted = Timestamp.now()
                                )

                                userId?.let {
                                    db.collection("habits")
                                        .document(it)
                                        .collection("userHabits")
                                        .document(habit.habitID)
                                        .set(updatedHabit)
                                        .addOnSuccessListener {
                                            Toast.makeText(
                                                context,
                                                "Marked as Completed!",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            isCompletedToday = !isCompletedToday
                                        }
                                        .addOnFailureListener {
                                            Toast.makeText(
                                                context,
                                                "Failed to update habit!",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isCompletedToday) CompleteButtonColor else IncompleteButtonColor,
                        ),
                        modifier = Modifier.align(Alignment.End),
                        //enabled = isForToday && !isCompletedToday,

                    ) {
                        Text(
                            //text = if (!isForToday) "✘" else if (isCompletedToday) "✔" else "Mark Done?",
                            text = if (isCompletedToday) "✔" else "✘",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

            }
        }
    }
}



@Composable
fun OutlinedFilledText(
    text: String,
    outlineColor: Color,
    fillColor: Color,
    fontSize: TextUnit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        // Outline text (using shadow)
        Text(
            text = text,
            style = TextStyle(
                fontSize = fontSize,
                fontWeight = FontWeight.ExtraBold,
                color = outlineColor,
                shadow = Shadow(
                    color = outlineColor,
                    offset = androidx.compose.ui.geometry.Offset(2.0f, 2.0f),
                    blurRadius = 5f
                )
            )
        )
        Text(
            text = text,
            style = TextStyle(
                fontSize = fontSize,
                fontWeight = FontWeight.Normal,
                color = fillColor
            )
        )

    }
}
