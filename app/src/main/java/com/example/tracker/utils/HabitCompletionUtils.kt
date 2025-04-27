package com.example.tracker.utils

import android.util.Log
import com.example.tracker.model.HabitModel
import com.example.tracker.model.HabitUpdateResult
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import java.util.Calendar
import java.util.concurrent.TimeUnit

object HabitCompletionUtils {
    fun markHabitAsDone(habitId: String, userId: String, isUpdated: (HabitUpdateResult) -> Unit) {
        val db = FirebaseFirestore.getInstance()

        db.collection("habits").document(userId).collection("userHabits")
            .document(habitId)
            .get()
            .addOnSuccessListener { document ->
                // Extract manually since we're in a static context
                val habitID = document.getString("habitID") ?: return@addOnSuccessListener
                val habitName = document.getString("habitName") ?: return@addOnSuccessListener
                val habitDescription = document.getString("habitDescription") ?: return@addOnSuccessListener
                val streak = document.getLong("streak")?.toInt() ?: 0
                val lastTimeCompleted = document.getTimestamp("lastTimeCompleted") ?: Timestamp.now()
                val activeDays = document.get("activeDays") as? List<Boolean> ?: List(7) { false }

                // Check if the habit is already completed today
                val isCompletedToday = isCompletedToday(lastTimeCompleted)

                if (!isCompletedToday) {
                    val habit = HabitModel(
                        habitID = habitID,
                        habitName = habitName,
                        habitDescription = habitDescription,
                        streak = streak,
                        lastTimeCompleted = lastTimeCompleted,
                        activeDays = activeDays
                    )


                    // Update the streak and timestamp
                    val updatedStreak = habit.streak + 1
                    val updatedHabit = habit.copy(
                        streak = updatedStreak,
                        lastTimeCompleted = Timestamp.now()
                    )

                    db.collection("habits")
                        .document(userId)
                        .collection("userHabits")
                        .document(habitId)
                        .set(updatedHabit)
                        .addOnSuccessListener {
                            Log.d("HabitCompletionUtils", "Habit marked as done")
                            isUpdated(HabitUpdateResult.SUCCESS)
                        }
                        .addOnFailureListener { e ->
                            Log.w("HabitCompletionUtils", "Error updating habit", e)
                            isUpdated(HabitUpdateResult.FAILURE)
                        }
                }
                else{
                    isUpdated(HabitUpdateResult.NO_UPDATE_NEEDED)
                }
            }
            .addOnFailureListener { e ->
                Log.w("HabitCompletionUtils", "Error getting habit", e)
                isUpdated(HabitUpdateResult.FAILURE)
            }
    }

    fun checkMissedDays(
        habit: HabitModel,
        userId: String,
        isUpdated: (HabitUpdateResult) -> Unit
    ): HabitModel {

        val lastActivityTimestamp = habit.lastTimeCompleted
        val currentTimestamp = Timestamp.now()

        val diffInMilliSec = currentTimestamp.toDate().time - lastActivityTimestamp.toDate().time
        val diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMilliSec).toInt()

        if (diffInDays > 7) {
            return resetStreak(habit, userId) { updateResult -> isUpdated(updateResult) }
        }

        // Calculate the difference in days between lastActivity and current
        val lastActivityDaysSinceEpoch =
            TimeUnit.MILLISECONDS.toDays(lastActivityTimestamp.toDate().time)
        val currentDaysSinceEpoch = TimeUnit.MILLISECONDS.toDays(currentTimestamp.toDate().time)

        // Iterate through each day between lastActivity and current
        for (daysSinceEpoch in (lastActivityDaysSinceEpoch + 1) until currentDaysSinceEpoch) {
            val currentIterationTimestamp =
                Timestamp(java.util.Date(TimeUnit.DAYS.toMillis(daysSinceEpoch)))
            val dayOfWeek = getDayOfWeek(currentIterationTimestamp)

            //if (habit.activeDays[dayOfWeek] && !isSameDay(currentIterationTimestamp, lastActivityTimestamp) && !isSameDay(currentIterationTimestamp, currentTimestamp)) {
            if (habit.activeDays[dayOfWeek] && !isSameDay(
                    currentIterationTimestamp,
                    currentTimestamp
                )
            ) {
                return resetStreak(habit, userId) { updateResult -> isUpdated(updateResult) }
            }
        }

        isUpdated(HabitUpdateResult.NO_UPDATE_NEEDED)
        return habit
    }

    fun getDayOfWeek(timestamp: Timestamp): Int {
        val calendar = Calendar.getInstance().apply { time = timestamp.toDate() }
        val dayIndex = (calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7
        return dayIndex
    }

    fun isSameDay(timestamp1: Timestamp, timestamp2: Timestamp): Boolean {
        val days1 = TimeUnit.MILLISECONDS.toDays(timestamp1.toDate().time)
        val days2 = TimeUnit.MILLISECONDS.toDays(timestamp2.toDate().time)
        return days1 == days2
    }

    fun resetStreak(
        habit: HabitModel,
        userId: String,
        isUpdated: (HabitUpdateResult) -> Unit
    ): HabitModel {

        val updatedHabit = habit.copy(streak = 0)

        Log.w(
            "Reset STREAK",
            "Updating: ${habit.habitName} with ID '${habit.habitID}' to ${updatedHabit}"
        )

        Firebase.firestore.collection("habits")
            .document(userId)
            .collection("userHabits")
            .document(habit.habitID)
            .set(updatedHabit)
            .addOnSuccessListener {
                isUpdated(HabitUpdateResult.SUCCESS)
            }
            .addOnFailureListener {
                isUpdated(HabitUpdateResult.FAILURE)
            }

        return updatedHabit
    }

    fun isCompletedToday(lastCompletedDate: Timestamp): Boolean {

        val lastCompletedCalendar = Calendar.getInstance().apply { time = lastCompletedDate.toDate() }
        val today = Calendar.getInstance()

        val isCompletedToday = lastCompletedCalendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) &&
                lastCompletedCalendar.get(Calendar.YEAR) == today.get(Calendar.YEAR)

        return isCompletedToday
    }
}