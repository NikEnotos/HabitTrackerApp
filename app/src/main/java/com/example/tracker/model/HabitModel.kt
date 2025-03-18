package com.example.tracker.model
import com.google.firebase.Timestamp

data class HabitModel(
    val habitID : String = "",
    val habitName: String = "",
    val habitDescription: String = "",
    val streak: Int = 0,
    val lastTimeCompleted: Timestamp = Timestamp(seconds = 0, nanoseconds = 0), // To store the last activity timestamp
    val activeDays: List<Boolean> = List(7) { false }// To store daily habit tracking
    //val timeOfHabit: // To use for notifications
)

enum class HabitUpdateResult {
    SUCCESS, FAILURE, NO_UPDATE_NEEDED
}