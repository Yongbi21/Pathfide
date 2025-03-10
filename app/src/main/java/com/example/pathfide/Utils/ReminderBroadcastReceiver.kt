package com.example.pathfide.Utils

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.pathfide.R
import java.text.SimpleDateFormat
import java.util.*

class ReminderBroadcastReceiver : BroadcastReceiver() {
    companion object {
        private const val CHANNEL_ID = "session_reminders"
        private const val CHANNEL_NAME = "Session Reminders"
        private const val CHANNEL_DESCRIPTION = "Notifications for upcoming therapy sessions"



        // Helper method to schedule a reminder
        fun scheduleReminder(
            context: Context,
            sessionId: String,
            sessionTime: String,
            therapistName: String,
            activityClass: Class<*>
        ) {
            val intent = Intent(context, ReminderBroadcastReceiver::class.java).apply {
                putExtra("sessionId", sessionId)
                putExtra("sessionTime", sessionTime)
                putExtra("therapistName", therapistName)
            }

            // Create unique request code based on session ID
            val requestCode = sessionId.hashCode()

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Parse session time and set alarm for 1 hour before
            try {
                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                val sessionDateTime = sdf.parse(sessionTime)
                sessionDateTime?.let {
                    val reminderTime = it.time - (60 * 60 * 1000) // 1 hour before
                    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        alarmManager.setExactAndAllowWhileIdle(
                            android.app.AlarmManager.RTC_WAKEUP,
                            reminderTime,
                            pendingIntent
                        )
                    } else {
                        alarmManager.setExact(
                            android.app.AlarmManager.RTC_WAKEUP,
                            reminderTime,
                            pendingIntent
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Helper method to cancel a reminder
        fun cancelReminder(context: Context, sessionId: String) {
            val intent = Intent(context, ReminderBroadcastReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                sessionId.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
            alarmManager.cancel(pendingIntent)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        val sessionId = intent.getStringExtra("sessionId") ?: return
        val sessionTime = intent.getStringExtra("sessionTime") ?: return
        val therapistName = intent.getStringExtra("therapistName") ?: return

        val notificationManager = context.getSystemService(NotificationManager::class.java)

        // Create a PendingIntent to open the app when notification is clicked
        val openAppIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("sessionId", sessionId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            sessionId.hashCode(),
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build and show the notification
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Upcoming Therapy Session")
            .setContentText("Your session with $therapistName starts in 1 hour at $sessionTime")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .build()

        notificationManager?.notify(sessionId.hashCode(), notification)
    }
}