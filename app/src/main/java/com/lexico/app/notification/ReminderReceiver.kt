package com.lexico.app.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.lexico.app.data.db.LexiconDbHelper

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action

        // If system finished booting, re-establish the AlarmManager schedule
        if (action == Intent.ACTION_BOOT_COMPLETED) {
            NotificationScheduler.scheduleDailyReminder(context)
            return
        }

        val db = LexiconDbHelper(context)
        val hasPracticed = db.hasPracticedToday()
        val dueWords = db.getDueWords()

        // Only fire if the user hasn't completed practice today and has pending words
        if (!hasPracticed && dueWords.isNotEmpty()) {
            NotificationScheduler.showDailyReminderNotification(context)
        }

        // Automatically reschedule for the next day
        NotificationScheduler.scheduleDailyReminder(context)
    }
}
