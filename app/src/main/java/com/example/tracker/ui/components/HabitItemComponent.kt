package com.example.tracker.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.tracker.R
import com.example.tracker.model.HabitModel
import com.example.tracker.model.HabitUpdateResult
import com.example.tracker.ui.theme.*
import com.example.tracker.utils.HabitCompletionUtils
import com.google.firebase.Timestamp

@Composable
fun HabitItem(
    habit: HabitModel,
    onMarkAsDone: (String, (HabitUpdateResult) -> Unit) -> Unit,
    navController: NavController
) {
    val context = LocalContext.current

    // Check if habit is completed today
    val todayIndex = HabitCompletionUtils.getDayOfWeek(Timestamp.now())
    val isCompletedToday = HabitCompletionUtils.isCompletedToday(habit.lastTimeCompleted)
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
                        } else Color.Unspecified // Keeps original icon colors
                    )
                }

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Row() {
                        // 📰 Habit Title
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
                                containerColor = MaterialTheme.colorScheme.secondary
                            ),
                            modifier = Modifier.padding(start = 10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = "Edit",
                                tint = MaterialTheme.colorScheme.onSecondary
                            )
                        }
                    }

                    // 📄 Habit Description
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
                // 🔻 Current day indicators
                CurrentWeekdayIndicator(todayIndex)

                // 📅 Days of the Week List
                ActiveDaysIndicator(habit.activeDays)

                // 🔺 Current day indicators (bottom)
                CurrentWeekdayIndicator(todayIndex, isBottom = true)

                // ✅ Mark Habit as Completed Button
                if (isForToday) {
                    if (!isCompletedToday) {
                        Button(
                            onClick = {
                                onMarkAsDone(habit.habitID) { result ->
                                    when (result) {
                                        HabitUpdateResult.SUCCESS -> {
                                            Toast.makeText(
                                                context,
                                                "Marked as Completed!",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                        HabitUpdateResult.FAILURE -> {
                                            Toast.makeText(
                                                context,
                                                "Failed to update habit!",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                        else -> { /* No action needed */ }
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                            ),
                            modifier = Modifier.align(Alignment.End),
                        ) {
                            Text(
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
fun CurrentWeekdayIndicator(todayIndex: Int, isBottom: Boolean = false) {
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
                    imageVector = if (isBottom) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = "Current day pointer",
                    tint = if (i == todayIndex) CurrentDayPointerColor else NotCurrentDayPointerColor
                )
            }
        }
    }
}

@Composable
fun ActiveDaysIndicator(activeDays: List<Boolean>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        for (i in days.indices) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        if (activeDays[i]) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                        shape = RoundedCornerShape(30),
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = days[i],
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onTertiary,
                    fontSize = 14.sp,
                )
            }
        }
    }
}

//@Composable
//fun OutlinedFilledText(
//    text: String,
//    outlineColor: Color,
//    fillColor: Color,
//    fontSize: TextUnit,
//    modifier: Modifier = Modifier
//) {
//    Box(modifier = modifier, contentAlignment = Alignment.Center) {
//
//        Text(
//            text = text,
//            style = TextStyle(
//                fontSize = fontSize,
//                fontWeight = FontWeight.ExtraBold,
//                color = outlineColor,
//                shadow = Shadow(
//                    color = outlineColor,
//                    offset = Offset(2.0f, 2.0f),
//                    blurRadius = 7f
//                )
//            )
//        )
//        Text(
//            text = text,
//            style = TextStyle(
//                fontSize = fontSize,
//                fontWeight = FontWeight.Normal,
//                color = fillColor
//            )
//        )
//    }
//}