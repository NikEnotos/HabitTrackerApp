package com.example.tracker.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tracker.model.HabitModel
import com.example.tracker.model.HabitUpdateResult
import com.example.tracker.utils.HabitCompletionUtils
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomePageUiState(
    val isLoading: Boolean = true,
    val habitsForToday: List<HabitModel> = emptyList(),
    val habitsForTodayDone: List<HabitModel> = emptyList(),
    val habitsNotForToday: List<HabitModel> = emptyList(),
    val error: String? = null
)

class HomeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(HomePageUiState())
    val uiState: StateFlow<HomePageUiState> = _uiState.asStateFlow()

    private val db = FirebaseFirestore.getInstance()
    val currentUser = Firebase.auth.currentUser

    init {
        if (currentUser != null) {
            loadHabits(currentUser.uid)
        } else {
            _uiState.value = HomePageUiState(isLoading = false, error = "User not logged in")
        }
    }

    fun loadHabits(userId: String) {
        viewModelScope.launch {
            db.collection("habits").document(userId).collection("userHabits")
                .addSnapshotListener { snapshot, e ->
                    if (e != null && Firebase.auth.currentUser?.uid != null) {
                        Log.e("Firebase", "Error fetching habits", e)
                        _uiState.value = HomePageUiState(
                            isLoading = false,
                            error = "Failed to load habits: ${e.message}"
                        )
                        return@addSnapshotListener
                    }

                    // Extract habits from snapshot
                    val allHabits = snapshot?.documents?.mapNotNull { document ->
                        val habitID = document.getString("habitID") ?: return@mapNotNull null
                        val habitName = document.getString("habitName") ?: return@mapNotNull null
                        val habitDescription =
                            document.getString("habitDescription") ?: return@mapNotNull null
                        val streak = document.getLong("streak")?.toInt() ?: 0
                        val lastTimeCompleted =
                            document.getTimestamp("lastTimeCompleted") ?: Timestamp.now()
                        val activeDays =
                            document.get("activeDays") as? List<Boolean> ?: List(7) { false }

                        var habit = HabitModel(
                            habitID = habitID,
                            habitName = habitName,
                            habitDescription = habitDescription,
                            streak = streak,
                            lastTimeCompleted = lastTimeCompleted,
                            activeDays = activeDays
                        )

                        // Check missed days for each habit
                        habit = HabitCompletionUtils.checkMissedDays(habit, userId) { _ -> }

                        habit
                    } ?: emptyList()

                    // Prioritize habits
                    categorizeHabits(allHabits)
                }
        }
    }

    private fun categorizeHabits(allHabits: List<HabitModel>) {
        val todayIndex = HabitCompletionUtils.getDayOfWeek(Timestamp.now())

        val forToday = allHabits.filter { it.activeDays[todayIndex] }
        val notForToday = allHabits.filter { !it.activeDays[todayIndex] }
        val forTodayCompleted =
            forToday.filter { HabitCompletionUtils.isCompletedToday(it.lastTimeCompleted) }
        val forTodayNotCompleted =
            forToday.filter { !HabitCompletionUtils.isCompletedToday(it.lastTimeCompleted) }

        _uiState.update { currentState ->
            currentState.copy(
                isLoading = false,
                habitsForToday = forTodayNotCompleted,
                habitsForTodayDone = forTodayCompleted,
                habitsNotForToday = notForToday
            )
        }
    }

    fun getSortedHabits(): List<HabitModel> {
        val state = _uiState.value
        return sortHabits(state.habitsForToday + state.habitsForTodayDone + state.habitsNotForToday)
    }

    fun hasNoHabits(): Boolean {
        val state = _uiState.value
        return !state.isLoading &&
                state.habitsForToday.isEmpty() &&
                state.habitsForTodayDone.isEmpty() &&
                state.habitsNotForToday.isEmpty()
    }

    fun markHabitAsDone(habitId: String, onResult: (HabitUpdateResult) -> Unit) {
        val userId = currentUser?.uid ?: return
        // This calls HabitCompletionUtils to handle the business logic
        HabitCompletionUtils.markHabitAsDone(habitId, userId, onResult)
    }

    private fun sortHabits(habits: List<HabitModel>): List<HabitModel> {
        val todayIndex = HabitCompletionUtils.getDayOfWeek(Timestamp.now())

        return habits.sortedWith(compareBy<HabitModel> {
            // First priority: For today, not completed
            if (it.activeDays[todayIndex] && !HabitCompletionUtils.isCompletedToday(it.lastTimeCompleted)) {
                0
            } else {
                1 // Otherwise, not the highest priority
            }
        }.thenBy {
            // Second priority: For today, completed
            if (it.activeDays[todayIndex] && HabitCompletionUtils.isCompletedToday(it.lastTimeCompleted)) {
                0
            } else {
                1 // Otherwise, not the second highest priority
            }
        }.thenBy {
            // Third priority: Not for today
            if (!it.activeDays[todayIndex]) {
                0
            } else {
                1
            }
        }.thenBy {
            // Fourth priority: Sort by habit name for consistent ordering
            it.habitName
        })
    }
}