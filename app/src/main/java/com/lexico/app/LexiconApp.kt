package com.lexico.app

import android.app.Application
import com.lexico.app.data.db.LexiconDbHelper
import com.lexico.app.notification.NotificationScheduler

class LexiconApp : Application() {
    lateinit var dbHelper: LexiconDbHelper
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        dbHelper = LexiconDbHelper(this)
        NotificationScheduler.createNotificationChannel(this)
        NotificationScheduler.scheduleDailyReminder(this)
    }

    companion object {
        lateinit var instance: LexiconApp
            private set
    }
}
