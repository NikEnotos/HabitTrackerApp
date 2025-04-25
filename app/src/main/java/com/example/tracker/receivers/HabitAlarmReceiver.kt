package com.example.tracker.receivers

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.tracker.model.HabitUpdateResult
import com.example.tracker.services.HabitReminderService
import com.example.tracker.utils.HabitCompletionUtils
import com.example.tracker.utils.NotificationUtils.getNotificationsTime
import com.google.firebase.auth.FirebaseAuth
import com.example.tracker.utils.NotificationUtils.scheduleHabitReminders

class HabitAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("HabitAlarmReceiver", "Received broadcast: ${intent.action}")

        // Start the habit reminder service
        val serviceIntent = Intent(context, HabitReminderService::class.java)
        context.startService(serviceIntent)

        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED -> {
                // TODO: Re-schedule alarms after device reboot
                Log.d("HabitAlarmReceiver", "Boot completed - reschedule alarms")
            }
            "com.example.tracker.DAILY_REMINDER" -> {
                Log.d("HabitAlarmReceiver", "Processing daily reminder")
                val time = getNotificationsTime(context)
                scheduleHabitReminders(context, time.first, time.second)
            }
            "com.example.tracker.MARK_HABIT_DONE" -> {
                val habitId = intent.getStringExtra("habitId")

                val notificationId = intent.getIntExtra("notificationId", -1)
                if (notificationId != -1) {
                    val notificationManager = ContextCompat.getSystemService(
                        context, NotificationManager::class.java
                    )
                    notificationManager?.cancel(notificationId)
                    Log.d("HabitAlarmReceiver", "Notification canceled: $notificationId")
                }

                val userId = FirebaseAuth.getInstance().currentUser?.uid
                if (habitId != null && userId != null) {
                    HabitCompletionUtils.markHabitAsDone(context, habitId, userId){ isUpdated ->
                        if (isUpdated === HabitUpdateResult.SUCCESS) {
                            Log.d("HabitAlarmReceiver", "Mark habit done: $habitId")
                        } else if (isUpdated === HabitUpdateResult.FAILURE) {
                            Log.w("HabitAlarmReceiver", "Habit was NOT marked as done: $habitId")
                        }
                    }

                }
            }
        }
    }
}