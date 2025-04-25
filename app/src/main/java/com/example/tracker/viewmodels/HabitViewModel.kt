package com.example.tracker.viewmodels

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tracker.model.HabitModel
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


enum class HabitOperationStatus {
    IDLE,           // Initial state
    LOADING,        // Loading habit data
    LOADED,         // Habit data loaded
    SAVING,         // Save/update operation in progress
    SUCCESS,        // Save/update operation completed
    DELETING,       // Delete operation in progress
    DELETED,        // Delete operation completed
    ERROR           // Any operation failed
}


enum class HabitViewModelMode {
    ADD,    // Adding a new habit
    EDIT    // Editing an existing habit
}

data class HabitUiState(
    val mode: HabitViewModelMode = HabitViewModelMode.ADD,    // Default to ADD mode
    val habitId: String? = null,                              // Null for new habits
    val originalHabit: HabitModel? = null,                    // Original habit for reset
    val habitTitle: TextFieldValue = TextFieldValue(""),
    val habitDescription: TextFieldValue = TextFieldValue(""),
    val selectedDays: List<Boolean> = List(7) { true },
    val status: HabitOperationStatus = HabitOperationStatus.IDLE, // Current operation status
    val errorMessage: String? = null,                         // Error message if any
    val isSaveEnabled: Boolean = false,                       // Whether save/update is allowed
    val navigateBackEvent: Boolean = false                    // Navigation event flag
)

class HabitViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val userId = Firebase.auth.currentUser?.uid

    private val _uiState = MutableStateFlow(HabitUiState())
    val uiState: StateFlow<HabitUiState> = _uiState.asStateFlow()


    fun initialize(mode: HabitViewModelMode, habitId: String? = null) {
        _uiState.update {
            it.copy(
                mode = mode,
                habitId = habitId
            )
        }

        // If in EDIT mode and habitId provided, load the habit
        if (mode == HabitViewModelMode.EDIT && habitId != null) {
            loadHabit(habitId)
        }
    }


    private fun loadHabit(habitId: String?) {
        if (habitId == null || userId == null) {
            _uiState.update { it.copy(
                status = HabitOperationStatus.ERROR,
                errorMessage = "Invalid habit or user ID."
            )}
            return
        }

        // Prevent reloading if already loaded or loading
        if (_uiState.value.status == HabitOperationStatus.LOADING ||
            (_uiState.value.status == HabitOperationStatus.LOADED && _uiState.value.habitId == habitId)) {
            return
        }

        _uiState.update { it.copy(
            status = HabitOperationStatus.LOADING,
            habitId = habitId,
            errorMessage = null
        )}

        viewModelScope.launch {
            try {
                val documentSnapshot = db.collection("habits")
                    .document(userId)
                    .collection("userHabits")
                    .document(habitId)
                    .get()
                    .await()

                if (documentSnapshot.exists()) {
                    val habit = documentSnapshot.toObject(HabitModel::class.java)
                    if (habit != null) {

                        _uiState.update { currentState ->
                            currentState.copy(
                                originalHabit = habit,
                                habitTitle = TextFieldValue(habit.habitName),
                                habitDescription = TextFieldValue(habit.habitDescription),
                                selectedDays = habit.activeDays,
                                status = HabitOperationStatus.LOADED,
                                isSaveEnabled = isSavePossible(habit.habitName, habit.activeDays)
                            )
                        }

                    } else {
                        _uiState.update { it.copy(
                            status = HabitOperationStatus.ERROR,
                            errorMessage = "Failed to parse habit data."
                        )}
                    }
                } else {
                    _uiState.update { it.copy(
                        status = HabitOperationStatus.ERROR,
                        errorMessage = "Habit not found."
                    )}
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    status = HabitOperationStatus.ERROR,
                    errorMessage = e.message ?: "Failed to load habit."
                )}
            }
        }
    }

    fun updateTitle(newTitle: TextFieldValue) {
        _uiState.update { currentState ->
            currentState.copy(
                habitTitle = newTitle,
                isSaveEnabled = isSavePossible(newTitle.text, currentState.selectedDays)
            )
        }
    }

    fun updateDescription(newDescription: TextFieldValue) {
        _uiState.update { it.copy(habitDescription = newDescription) }
    }

    fun toggleDay(dayIndex: Int) {
        if (dayIndex in 0..6) {
            _uiState.update { currentState ->
                val updatedDays = currentState.selectedDays.toMutableList().apply {
                    this[dayIndex] = !this[dayIndex]
                }
                currentState.copy(
                    selectedDays = updatedDays,
                    isSaveEnabled = isSavePossible(currentState.habitTitle.text, updatedDays)
                )
            }
        }
    }

    fun saveOrUpdateHabit() {
        val currentState = _uiState.value

        // Check if user is logged in and save/update requirements are met
        if (userId == null || !currentState.isSaveEnabled) {
            _uiState.update { it.copy(
                status = HabitOperationStatus.ERROR,
                errorMessage = if(userId == null) "User not logged in" else "Cannot save habit"
            )}
            return
        }

        _uiState.update { it.copy(status = HabitOperationStatus.SAVING, errorMessage = null) }

        when (currentState.mode) {
            HabitViewModelMode.ADD -> saveNewHabit()
            HabitViewModelMode.EDIT -> updateExistingHabit()
        }
    }

    private fun saveNewHabit() {
        val currentState = _uiState.value

        // Generate a unique ID for the new habit
        //val habitId = db.collection("habits").document(userId!!)
        //    .collection("userHabits").document().id

        val habit = HabitModel(
            habitID = Timestamp.now().toDate().toString(),
            habitName = currentState.habitTitle.text,
            habitDescription = currentState.habitDescription.text,
            lastTimeCompleted = Timestamp(seconds = 0, nanoseconds = 0),
            streak = 0,
            activeDays = currentState.selectedDays
        )

        viewModelScope.launch {
            try {
                db.collection("habits").document(userId!!)
                    .collection("userHabits")
                    .document(habit.habitID)
                    .set(habit)
                    .await()

                _uiState.update {
                    HabitUiState(
                        mode = HabitViewModelMode.ADD,
                        status = HabitOperationStatus.SUCCESS
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        status = HabitOperationStatus.ERROR,
                        errorMessage = e.message ?: "Failed to save habit"
                    )
                }
            }
        }
    }

    private fun updateExistingHabit() {
        val currentState = _uiState.value
        if (currentState.habitId == null || currentState.originalHabit == null) {
            _uiState.update { it.copy(
                status = HabitOperationStatus.ERROR,
                errorMessage = "Cannot update habit."
            )}
            return
        }


        val updatedHabit = HabitModel(
            habitID = currentState.habitId,
            habitName = currentState.habitTitle.text,
            habitDescription = currentState.habitDescription.text,
            lastTimeCompleted = currentState.originalHabit.lastTimeCompleted, // Keep original
            streak = currentState.originalHabit.streak, // Keep original
            activeDays = currentState.selectedDays
        )

        viewModelScope.launch {
            try {
                db.collection("habits").document(userId!!)
                    .collection("userHabits")
                    .document(currentState.habitId)
                    .set(updatedHabit)
                    .await()

                // Update successful
                _uiState.update { it.copy(
                    status = HabitOperationStatus.SUCCESS,
                    navigateBackEvent = true
                )}
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    status = HabitOperationStatus.ERROR,
                    errorMessage = e.message ?: "Failed to update habit."
                )}
            }
        }
    }

    fun deleteHabit() {
        val currentState = _uiState.value
        if (userId == null || currentState.habitId == null) {
            _uiState.update { it.copy(
                status = HabitOperationStatus.ERROR,
                errorMessage = "Cannot delete habit."
            )}
            return
        }

        _uiState.update { it.copy(status = HabitOperationStatus.DELETING, errorMessage = null) }

        viewModelScope.launch {
            try {
                db.collection("habits").document(userId)
                    .collection("userHabits")
                    .document(currentState.habitId)
                    .delete()
                    .await()

                _uiState.update { it.copy(
                    status = HabitOperationStatus.DELETED,
                    navigateBackEvent = true
                )}
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    status = HabitOperationStatus.ERROR,
                    errorMessage = e.message ?: "Failed to delete habit."
                )}
            }
        }
    }

    // Reset status after UI handled the event
    fun consumeStatusEvent() {
        val currentStatus = _uiState.value.status

        when (currentStatus) {
            HabitOperationStatus.ERROR, HabitOperationStatus.SUCCESS, HabitOperationStatus.DELETED -> {
                // For ADD mode, reset to initial state after SUCCESS
                if (_uiState.value.mode == HabitViewModelMode.ADD && currentStatus == HabitOperationStatus.SUCCESS) {
                    _uiState.update { HabitUiState(mode = HabitViewModelMode.ADD) } // reset to initial state
                } else {
                    // For EDIT mode or ERROR in any mode, just reset status
                    _uiState.update {
                        it.copy(
                            status = if (it.originalHabit != null) HabitOperationStatus.LOADED else HabitOperationStatus.IDLE,
                            errorMessage = null
                        )
                    }
                }
            }
            else -> { /* No changes for other statuses */ }
        }
    }

    // Reset the navigation event flag after navigation has occurred
    fun consumeNavigationEvent() {
        _uiState.update { it.copy(navigateBackEvent = false) }
    }

    // Check if habit title is not blank and at least one day selected
    private fun isSavePossible(title: String, days: List<Boolean>): Boolean {
        return title.isNotBlank() && days.contains(true)
    }
}