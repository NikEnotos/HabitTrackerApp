package com.example.tracker.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.tracker.MainActivity
import com.example.tracker.R
import com.example.tracker.model.HabitModel
import com.example.tracker.receivers.HabitAlarmReceiver
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

class HabitReminderService : Service() {
    private val TAG = "HabitReminderService"
    private val CHANNEL_ID = "HabitReminderChannel"
    private val NOTIFICATION_ID = 100

    override fun onBind(intent: Intent?): IBinder? {
        return null // Not a bound service
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service started")

        // Check if user is logged in
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            checkHabitsForToday(userId)
        } else {
            Log.d(TAG, "No user logged in")
            stopSelf()
        }

        return START_NOT_STICKY
    }

    private fun checkHabitsForToday(userId: String) {
        val db = FirebaseFirestore.getInstance()
        val today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        // Convert from Calendar day (1-7, Sunday = 1) to our app's format (0-6, Monday = 0)
        val todayIndex = (today + 5) % 7

        db.collection("habits").document(userId).collection("userHabits")
            .get()
            .addOnSuccessListener { documents ->
                val habitsForToday = mutableListOf<HabitModel>()

                for (document in documents) {
                    val habitID = document.getString("habitID") ?: continue
                    val habitName = document.getString("habitName") ?: continue
                    val habitDescription = document.getString("habitDescription") ?: continue
                    val streak = document.getLong("streak")?.toInt() ?: 0
                    val lastTimeCompleted = document.getTimestamp("lastTimeCompleted") ?: Timestamp.now()
                    val activeDays = document.get("activeDays") as? List<Boolean> ?: List(7) { false }

                    val habit = HabitModel(
                        habitID = habitID,
                        habitName = habitName,
                        habitDescription = habitDescription,
                        streak = streak,
                        lastTimeCompleted = lastTimeCompleted,
                        activeDays = activeDays
                    )

                    // Check if today is an active day for this habit
                    if (habit.activeDays[todayIndex]) {
                        // Check if the habit was completed today
                        val lastCompletedDate = habit.lastTimeCompleted.toDate()
                        val lastCompletedCalendar = Calendar.getInstance().apply { time = lastCompletedDate }
                        val today = Calendar.getInstance()

                        val sameDay = lastCompletedCalendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) &&
                                lastCompletedCalendar.get(Calendar.YEAR) == today.get(Calendar.YEAR)

                        if (!sameDay) {
                            habitsForToday.add(habit)
                        }
                    }
                }

                if (habitsForToday.isNotEmpty()) {
                    sendReminderNotification(habitsForToday)
                }

                // Service work complete
                stopSelf()
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error getting habits", e)
                stopSelf()
            }
    }

    private fun sendReminderNotification(habitsForToday: List<HabitModel>) {
        val habitCount = habitsForToday.size
        val contentTitle = "Habits for today"
        val contentText = when {
            habitCount == 1 -> "Don't forget to complete your habit: ${habitsForToday[0].habitName}"
            habitCount > 1 -> "You have $habitCount habits to complete today"
            else -> "No habits to complete today"
        }

        // Create intent to open the app when notification is clicked
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.flame_on)
            .setContentTitle(contentTitle)
            .setContentText(contentText)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        if (habitCount == 1) {
            val habit = habitsForToday[0]

            // Create intent for marking the habit as done
            val markDoneIntent = Intent(this, HabitAlarmReceiver::class.java).apply {
                action = "com.example.tracker.MARK_HABIT_DONE"
                putExtra("notificationId", NOTIFICATION_ID)
                putExtra("habitId", habit.habitID)
            }
            val markDonePendingIntent = PendingIntent.getBroadcast(
                this,
                0,
                markDoneIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Add "Mark Done" action to notification
            notification.addAction(
                R.drawable.checkmark,
                "Mark as Done",
                markDonePendingIntent
            )
        }



        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.notify(NOTIFICATION_ID, notification.build())
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Habit Reminders"
            val descriptionText = "Notifications for habit tracking reminders"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}