package com.example.tracker.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import com.example.tracker.receivers.HabitAlarmReceiver
import androidx.core.content.edit
import java.util.Calendar

object NotificationUtils {
    private const val PREFS_NAME = "habit_reminder_prefs"
    private const val NOTIFICATIONS_ENABLED_KEY = "notifications_enabled"
    private const val NOTIFICATION_HOUR_KEY = "notification_hour"
    private const val NOTIFICATION_MINUTE_KEY = "notification_minute"

    fun areNotificationsEnabled(context: Context): Boolean {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(NOTIFICATIONS_ENABLED_KEY, true) // Default to enabled
    }

    fun setNotificationsEnabled(context: Context, enabled: Boolean) {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit() {
            putBoolean(NOTIFICATIONS_ENABLED_KEY, enabled)
        }
    }

    fun cancelScheduledReminders(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, HabitAlarmReceiver::class.java).apply {
            action = "com.example.tracker.DAILY_REMINDER"
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        alarmManager.cancel(pendingIntent) // This will cancel the existing alarm
    }

    fun cancelAllNotifications(context: Context){
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        notificationManager.cancelAll()
    }

    fun setNotificationsTime(context: Context, hour: Int, minute: Int){
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit() {
            putInt(NOTIFICATION_HOUR_KEY, hour)
            putInt(NOTIFICATION_MINUTE_KEY, minute)
        }
    }

    fun getNotificationsTime(context: Context): Pair<Int, Int>{
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(NOTIFICATION_HOUR_KEY, 9) to prefs.getInt(NOTIFICATION_MINUTE_KEY, 0)
    }

    fun scheduleHabitReminders(context: Context, hour: Int, minute: Int) {
        if (!areNotificationsEnabled(context)) {
            Log.d("AuthViewModel", "Notifications are disabled. Not scheduling reminder.")
            cancelScheduledReminders(context)
            cancelAllNotifications(context)
            return
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, HabitAlarmReceiver::class.java).apply {
            action = "com.example.tracker.DAILY_REMINDER"
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Set daily reminder
        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)

            // If time has already passed today, schedule for tomorrow
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }

        Log.d("AuthViewModel", "Scheduled habit reminder for ${calendar.time}")
    }

}