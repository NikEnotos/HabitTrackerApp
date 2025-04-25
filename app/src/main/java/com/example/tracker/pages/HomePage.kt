package com.example.tracker.pages


import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
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
import androidx.navigation.NavController
import com.example.tracker.R
import com.example.tracker.model.HabitModel
import com.example.tracker.model.HabitUpdateResult
import com.example.tracker.ui.theme.*
import com.example.tracker.utils.HabitCompletionUtils
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore


@Composable
fun HomePage(modifier: Modifier = Modifier, navController: NavController) {

    val context = LocalContext.current
    val db = Firebase.firestore
    val userId = Firebase.auth.currentUser?.uid
    val user = Firebase.auth.currentUser

    var habits by remember {
        mutableStateOf<List<HabitModel>>(emptyList())
    }
    var habitsNotForToday by remember {
        mutableStateOf<List<HabitModel>>(emptyList())
    }
    var isLoading by remember {
        mutableStateOf(true)
    }

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
                        val habitDescription =
                            document.getString("habitDescription") ?: return@mapNotNull null
                        val streak = document.getLong("streak")?.toInt() ?: 0
                        val lastTimeCompleted =
                            document.getTimestamp("lastTimeCompleted") ?: Timestamp.now()
                        val activeDays =
                            document.get("activeDays") as? List<Boolean> ?: List(7) { false }

                        val habit = HabitModel(
                            habitID = habitID,
                            habitName = habitName,
                            habitDescription = habitDescription,
                            streak = streak,
                            lastTimeCompleted = lastTimeCompleted,
                            activeDays = activeDays
                        )


                        val checkedHabit = HabitCompletionUtils.checkMissedDays(habit, userId) { isUpdated ->
                            if (habit.streak != 0)
                                if (isUpdated === HabitUpdateResult.SUCCESS) {
                                    Toast.makeText(
                                        context,
                                        "${habit.habitName} - streak is reset to 0",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else if (isUpdated === HabitUpdateResult.FAILURE) {
                                    Toast.makeText(
                                        context,
                                        "Failed to update streak in ${habit.habitName}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        }

                        checkedHabit
                    } ?: emptyList()

                    isLoading = false

                    // Prioritization
                    val todayIndex = (Timestamp.now().toDate().day + 6) % 7
                    for (habit in habits) {
                        val isForToday = habit.activeDays[todayIndex]
                        if (!isForToday) {
                            habitsNotForToday = habitsNotForToday.plus(habit)
                            habits = habits.minus(habit)
                        }
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
            AddNewHabitBanner(modifier)
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 30.dp, bottom = 80.dp)
            ) {
                items(habits) { habit ->
                    HabitItem(habit, userId, navController)
                    Spacer(modifier = Modifier.height(12.dp))
                }
                items(habitsNotForToday) { habit ->
                    HabitItem(habit, userId, navController)
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}


@Composable
fun AddNewHabitBanner(modifier: Modifier) {

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.weight(0.5f))

        Image(
            painter = painterResource(id = R.drawable.empty),
            contentDescription = "Login Banner",
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "It's a bit empty here...", fontSize = 20.sp)

        Spacer(modifier = Modifier.weight(0.5f))

        Text(text = "Let's add a new habit!", fontSize = 25.sp, fontWeight = FontWeight.Bold)

        Text(text = "↓", fontSize = 30.sp)
    }

}

@Composable
fun HabitItem(
    habit: HabitModel,
    userId: String?,
    navController: NavController
) {
    val context = LocalContext.current

    // Check if habit is completed today
    val todayIndex = (Timestamp.now().toDate().day + 6) % 7

    val today = Timestamp.now().toDate()
    val lastCompletedDate = habit.lastTimeCompleted.toDate()
    var isCompletedToday = lastCompletedDate.date == today.date
    val isForToday = habit.activeDays[todayIndex]


    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
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
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Icon(
                        painter = painterResource(
                            id = if (habit.streak > 0) {
                                if (!isCompletedToday && isForToday) R.drawable.flame_off else R.drawable.flame_on
                            } else R.drawable.lost_streak
                        ),
                        contentDescription = "Streak Flame",
                        modifier = Modifier.size(32.dp),
                        tint = if (habit.streak > 0) {
                            if (!isCompletedToday && isForToday) MaterialTheme.colorScheme.secondary else Color.Unspecified
                        } else Color.Unspecified// Keep original icon colors
                    )
                }

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Row() {
                        // 📰 Habit Title (Bold)
                        Text(
                            modifier = Modifier
                                .weight(1f)
                                .align(Alignment.CenterVertically),
                            text = habit.habitName,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                        // ✏️ Edit button
                        IconButton(
                            onClick = {
                                navController.navigate("editHabit/${habit.habitID}")
                            },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = MaterialTheme.colorScheme.secondary // Set the button's background color
                            ),
                            modifier = Modifier.padding(start = 10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = "Edit",
                                tint = MaterialTheme.colorScheme.onSecondary // Set the icon's color
                            )
                        }
                    }

                    // 📄 Habit Description (Small Text)
                    if (habit.habitDescription.isNotBlank()) {
                        Text(
                            modifier = Modifier.padding(top = 15.dp),
                            text = habit.habitDescription,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }


            }
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
            ) {
                // 🔻 Current day
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    for (i in 0..6) {
                        Box(
                            modifier = Modifier.size(width = 40.dp, height = 20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Current day pointer",
                                tint = if (i == todayIndex) CurrentDayPointerColor else NotCurrentDayPointerColor
                            )
                        }
                    }
                }
                // 📅 Days of the Week
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                    for (i in days.indices) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    if (habit.activeDays[i]) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                                    shape = RoundedCornerShape(30),
                                ),
                            contentAlignment = Alignment.Center
                        ) {
//                            if(i == getDayOfWeek(habit.lastTimeCompleted)) {
//                                OutlinedFilledText(
//                                    text = days[i],
//                                    outlineColor = StreakedDayOutline,
//                                    fillColor = WhitePrimaryText,
//                                    fontSize = 14.sp,
//                                )
//                            }else {
                            Text(
                                text = days[i],
                                fontWeight = FontWeight.Normal,
                                color = MaterialTheme.colorScheme.onTertiary,
                                fontSize = 14.sp,
                            )
//                            }
                        }
                    }
                }
                // 🔺 Current day
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    for (i in 0..6) {
                        Box(
                            modifier = Modifier.size(width = 40.dp, height = 20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowUp,
                                contentDescription = "Current day pointer",
                                tint = if (i == todayIndex) CurrentDayPointerColor else NotCurrentDayPointerColor
                            )
                        }
                    }
                }

                // ✅ Mark Habit as Completed Button
                if (isForToday) {
                    if (!isCompletedToday) {
                        Button(
                            onClick = {


                                if (userId != null) {
                                    HabitCompletionUtils.markHabitAsDone(context, habit.habitID, userId) { isUpdated ->
                                            if (isUpdated === HabitUpdateResult.SUCCESS) {
                                                Toast.makeText(
                                                    context,
                                                    "Marked as Completed!",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                isCompletedToday = true
                                            } else if (isUpdated === HabitUpdateResult.FAILURE) {
                                                Toast.makeText(
                                                    context,
                                                    "Failed to update habit!",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                    }
                                }
                                else{
                                    Toast.makeText(
                                        context,
                                        "Failed to update - userId is null",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }





//                                val updatedStreak = habit.streak + 1
//                                val updatedHabit = habit.copy(
//                                    streak = updatedStreak,
//                                    lastTimeCompleted = Timestamp.now()
//                                )
//
//                                userId?.let {
//                                    db.collection("habits")
//                                        .document(it)
//                                        .collection("userHabits")
//                                        .document(habit.habitID)
//                                        .set(updatedHabit)
//                                        .addOnSuccessListener {
//                                            Toast.makeText(
//                                                context,
//                                                "Marked as Completed!",
//                                                Toast.LENGTH_SHORT
//                                            ).show()
//                                            isCompletedToday = true
//                                        }
//                                        .addOnFailureListener {
//                                            Toast.makeText(
//                                                context,
//                                                "Failed to update habit!",
//                                                Toast.LENGTH_SHORT
//                                            ).show()
//                                        }
//                                }
                                //}
                            },
                            colors = ButtonDefaults.buttonColors(
                                //containerColor = if (isCompletedToday) CompleteButtonColor else IncompleteButtonColor,
                                containerColor = MaterialTheme.colorScheme.primary,
                            ),
                            modifier = Modifier.align(Alignment.End),
                        ) {
                            Text(
                                //text = if (!isForToday) "✘" else if (isCompletedToday) "✔" else "Mark Done?",
                                text = "Done",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(InvisibleBoxBackground)
                                .align(Alignment.End),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.checkmark),
                                contentDescription = "Check mark",
                                modifier = Modifier.fillMaxSize(),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
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
                    offset = Offset(2.0f, 2.0f),
                    blurRadius = 7f
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

