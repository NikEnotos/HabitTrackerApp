package com.example.tracker.model
import com.google.firebase.Timestamp

data class HabitModel(
    val habitID : String = "",
    val habitName: String = "",
    val habitDescription: String = "",
    val streak: Int = 0,
    val lastTimeCompleted: Timestamp? = null, // To store the last activity timestamp
    val activeDays: List<Boolean> // To store daily habit tracking
    //val timeOfHabit: // To use for notifications
)