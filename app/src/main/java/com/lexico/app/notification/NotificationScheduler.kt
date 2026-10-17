package com.lexico.app.notification

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.lexico.app.MainActivity
import com.lexico.app.R
import com.lexico.app.data.db.LexiconDbHelper
import java.util.Calendar

object NotificationScheduler {

    const val CHANNEL_ID = "lexico_daily_reminder"
    const val NOTIFICATION_ID = 1001
    const val REMINDER_REQUEST_CODE = 9001
    const val ACTION_DAILY_REMINDER = "com.lexico.app.ACTION_DAILY_REMINDER"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Recordatorio Diario Léxico"
            val descriptionText = "Avisos discretos de repaso de vocabulario"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableVibration(true)
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Schedules a real system alarm with AlarmManager pointing to the configured hour:minute.
     * Uses setExactAndAllowWhileIdle with safe fallback to setAndAllowWhileIdle.
     */
    fun scheduleDailyReminder(context: Context, hour: Int? = null, minute: Int? = null) {
        createNotificationChannel(context)

        val db = LexiconDbHelper(context)
        val targetHour = hour ?: (db.getSetting("reminder_hour", "21").toIntOrNull() ?: 21)
        val targetMinute = minute ?: (db.getSetting("reminder_minute", "30").toIntOrNull() ?: 30)

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, targetHour)
            set(Calendar.MINUTE, targetMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            // If time already passed today, target tomorrow
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ACTION_DAILY_REMINDER
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REMINDER_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTime = calendar.timeInMillis
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                } else {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            }
        } catch (_: SecurityException) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        }
    }

    fun showDailyReminderNotification(context: Context) {
        createNotificationChannel(context)

        val db = LexiconDbHelper(context)
        val dueWords = db.getDueWords()
        val streak = db.getStreakData()

        val title = "Léxico · Repaso pendiente"
        val message = if (dueWords.isNotEmpty()) {
            if (streak.currentStreak > 0) {
                "Tienes ${dueWords.size} palabras listas para repasar hoy. Mantén tu racha de ${streak.currentStreak} días."
            } else {
                "Tienes ${dueWords.size} palabras listas para repasar. Comienza hoy tu racha de lectura."
            }
        } else {
            "Tu biblioteca está al día. Puedes capturar nuevas palabras o hacer un sprint de 60 segundos."
        }

        showInstantNotification(context, title, message)
    }

    fun showInstantNotification(context: Context, title: String, message: String) {
        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setColor(0xFFC85A32.toInt()) // Terracotta editorial accent

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(NOTIFICATION_ID, builder.build())
        } catch (_: SecurityException) {
            // Android 13+ permission not yet granted
        }
    }
}
