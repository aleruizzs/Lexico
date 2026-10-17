package com.lexico.app.data.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.lexico.app.data.model.DayActivity
import com.lexico.app.data.model.MasteryStatus
import com.lexico.app.data.model.StreakData
import com.lexico.app.data.model.Word
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class LexiconDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "lexico_editorial.db"
        const val DATABASE_VERSION = 1

        const val TABLE_WORDS = "words"
        const val COLUMN_ID = "id"
        const val COLUMN_WORD = "word"
        const val COLUMN_TRANSLATION = "translation"
        const val COLUMN_SENTENCE = "sentence"
        const val COLUMN_SOURCE = "source"
        const val COLUMN_EASE_FACTOR = "ease_factor"
        const val COLUMN_INTERVAL = "review_interval"
        const val COLUMN_REPETITIONS = "repetitions"
        const val COLUMN_NEXT_REVIEW = "next_review_date"
        const val COLUMN_LAST_REVIEWED = "last_reviewed_date"
        const val COLUMN_STATUS = "status"
        const val COLUMN_CREATED_AT = "created_at"

        const val TABLE_DAILY_LOGS = "daily_logs"
        const val COLUMN_LOG_DATE = "log_date" // YYYY-MM-DD
        const val COLUMN_WORDS_REVIEWED = "words_reviewed"
        const val COLUMN_TIMESTAMP = "timestamp"

        const val TABLE_SETTINGS = "settings"
        const val COLUMN_KEY = "setting_key"
        const val COLUMN_VALUE = "setting_value"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createWordsTable = """
            CREATE TABLE $TABLE_WORDS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_WORD TEXT NOT NULL,
                $COLUMN_TRANSLATION TEXT NOT NULL,
                $COLUMN_SENTENCE TEXT,
                $COLUMN_SOURCE TEXT NOT NULL,
                $COLUMN_EASE_FACTOR REAL NOT NULL,
                $COLUMN_INTERVAL INTEGER NOT NULL,
                $COLUMN_REPETITIONS INTEGER NOT NULL,
                $COLUMN_NEXT_REVIEW INTEGER NOT NULL,
                $COLUMN_LAST_REVIEWED INTEGER,
                $COLUMN_STATUS TEXT NOT NULL,
                $COLUMN_CREATED_AT INTEGER NOT NULL
            )
        """.trimIndent()

        val createLogsTable = """
            CREATE TABLE $TABLE_DAILY_LOGS (
                $COLUMN_LOG_DATE TEXT PRIMARY KEY,
                $COLUMN_WORDS_REVIEWED INTEGER NOT NULL,
                $COLUMN_TIMESTAMP INTEGER NOT NULL
            )
        """.trimIndent()

        val createSettingsTable = """
            CREATE TABLE $TABLE_SETTINGS (
                $COLUMN_KEY TEXT PRIMARY KEY,
                $COLUMN_VALUE TEXT NOT NULL
            )
        """.trimIndent()

        db.execSQL(createWordsTable)
        db.execSQL(createLogsTable)
        db.execSQL(createSettingsTable)

        seedInitialWords(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_WORDS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_DAILY_LOGS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_SETTINGS")
        onCreate(db)
    }

    private fun seedInitialWords(db: SQLiteDatabase) {
        val now = System.currentTimeMillis()
        val seeds = listOf(
            Word(
                word = "Ubiquitous",
                translation = "Presente en todas partes a la vez",
                sentence = "Smartphones have become ubiquitous in modern everyday life.",
                source = "The Atlantic",
                nextReviewDate = now - 1000L
            ),
            Word(
                word = "Serendipity",
                translation = "Hallazgo afortunado e inesperado",
                sentence = "Finding that rare first edition in a dusty bookstore was pure serendipity.",
                source = "The Shadow of the Wind",
                nextReviewDate = now - 2000L
            ),
            Word(
                word = "Machiavellian",
                translation = "Maquiavélico, calculador y sin escrúpulos",
                sentence = "Logan Roy's Machiavellian maneuvers kept everyone guessing his next move.",
                source = "Succession",
                nextReviewDate = now - 3000L
            ),
            Word(
                word = "Ineffable",
                translation = "Inefable, que no puede expresarse con palabras",
                sentence = "There was an ineffable melancholy in the green light across the bay.",
                source = "The Great Gatsby",
                nextReviewDate = now - 4000L
            ),
            Word(
                word = "Ephemeral",
                translation = "Efímero, fugaz, de corta duración",
                sentence = "Viral fame on social media is often ephemeral, fading within weeks.",
                source = "New Yorker",
                nextReviewDate = now - 5000L
            ),
            Word(
                word = "Ruthless",
                translation = "Despiadado, implacable",
                sentence = "The desert of Arrakis is ruthless to those who do not respect its laws.",
                source = "Dune",
                nextReviewDate = now - 6000L
            ),
            Word(
                word = "Resilience",
                translation = "Capacidad de sobreponerse y resistir",
                sentence = "The kitchen staff showed incredible resilience during the dinner rush.",
                source = "The Bear",
                nextReviewDate = now - 7000L
            ),
            Word(
                word = "Dystopian",
                translation = "Distópico, sociedad opresiva y deshumanizada",
                sentence = "Big Brother symbolized the omnipresent surveillance of a dystopian regime.",
                source = "1984",
                nextReviewDate = now - 8000L
            )
        )

        for (word in seeds) {
            val values = ContentValues().apply {
                put(COLUMN_WORD, word.word)
                put(COLUMN_TRANSLATION, word.translation)
                put(COLUMN_SENTENCE, word.sentence)
                put(COLUMN_SOURCE, word.source)
                put(COLUMN_EASE_FACTOR, word.easeFactor)
                put(COLUMN_INTERVAL, word.interval)
                put(COLUMN_REPETITIONS, word.repetitions)
                put(COLUMN_NEXT_REVIEW, word.nextReviewDate)
                put(COLUMN_LAST_REVIEWED, word.lastReviewedDate)
                put(COLUMN_STATUS, word.status.name)
                put(COLUMN_CREATED_AT, word.createdAt)
            }
            db.insert(TABLE_WORDS, null, values)
        }
    }

    fun insertWord(word: Word): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_WORD, word.word.trim())
            put(COLUMN_TRANSLATION, word.translation.trim())
            put(COLUMN_SENTENCE, word.sentence?.trim())
            put(COLUMN_SOURCE, word.source.trim().ifEmpty { "General" })
            put(COLUMN_EASE_FACTOR, word.easeFactor)
            put(COLUMN_INTERVAL, word.interval)
            put(COLUMN_REPETITIONS, word.repetitions)
            put(COLUMN_NEXT_REVIEW, word.nextReviewDate)
            put(COLUMN_LAST_REVIEWED, word.lastReviewedDate)
            put(COLUMN_STATUS, word.status.name)
            put(COLUMN_CREATED_AT, word.createdAt)
        }
        return db.insert(TABLE_WORDS, null, values)
    }

    fun updateWord(word: Word): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_WORD, word.word.trim())
            put(COLUMN_TRANSLATION, word.translation.trim())
            put(COLUMN_SENTENCE, word.sentence?.trim())
            put(COLUMN_SOURCE, word.source.trim().ifEmpty { "General" })
            put(COLUMN_EASE_FACTOR, word.easeFactor)
            put(COLUMN_INTERVAL, word.interval)
            put(COLUMN_REPETITIONS, word.repetitions)
            put(COLUMN_NEXT_REVIEW, word.nextReviewDate)
            put(COLUMN_LAST_REVIEWED, word.lastReviewedDate)
            put(COLUMN_STATUS, word.status.name)
        }
        return db.update(TABLE_WORDS, values, "$COLUMN_ID = ?", arrayOf(word.id.toString()))
    }

    fun deleteWord(id: Long): Int {
        val db = writableDatabase
        return db.delete(TABLE_WORDS, "$COLUMN_ID = ?", arrayOf(id.toString()))
    }

    fun getAllWords(): List<Word> {
        val db = readableDatabase
        val cursor = db.query(TABLE_WORDS, null, null, null, null, null, "$COLUMN_CREATED_AT DESC")
        val words = mutableListOf<Word>()
        cursor.use {
            while (it.moveToNext()) {
                words.add(cursorToWord(it))
            }
        }
        return words
    }

    fun getDueWords(): List<Word> {
        val now = System.currentTimeMillis()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_WORDS,
            null,
            "$COLUMN_NEXT_REVIEW <= ?",
            arrayOf(now.toString()),
            null,
            null,
            "$COLUMN_NEXT_REVIEW ASC"
        )
        val words = mutableListOf<Word>()
        cursor.use {
            while (it.moveToNext()) {
                words.add(cursorToWord(it))
            }
        }
        return words
    }

    fun getUniqueSources(): List<String> {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT DISTINCT $COLUMN_SOURCE FROM $TABLE_WORDS ORDER BY $COLUMN_SOURCE ASC", null)
        val sources = mutableListOf<String>()
        cursor.use {
            while (it.moveToNext()) {
                val src = it.getString(0)
                if (!src.isNullOrBlank()) {
                    sources.add(src)
                }
            }
        }
        return sources
    }

    fun recordPracticeActivity(wordsCount: Int = 1) {
        val db = writableDatabase
        val todayStr = getTodayDateString()
        val now = System.currentTimeMillis()

        // Check if today already logged
        val cursor = db.query(
            TABLE_DAILY_LOGS,
            arrayOf(COLUMN_WORDS_REVIEWED),
            "$COLUMN_LOG_DATE = ?",
            arrayOf(todayStr),
            null,
            null,
            null
        )

        var existingCount = 0
        val exists = cursor.use {
            if (it.moveToFirst()) {
                existingCount = it.getInt(0)
                true
            } else false
        }

        val values = ContentValues().apply {
            put(COLUMN_LOG_DATE, todayStr)
            put(COLUMN_WORDS_REVIEWED, existingCount + wordsCount)
            put(COLUMN_TIMESTAMP, now)
        }

        if (exists) {
            db.update(TABLE_DAILY_LOGS, values, "$COLUMN_LOG_DATE = ?", arrayOf(todayStr))
        } else {
            db.insert(TABLE_DAILY_LOGS, null, values)
        }

        // Persist best_streak if current streak exceeds historical record
        val currentStreakData = getStreakData()
        val storedBest = getStoredIntSetting("best_streak", 0)
        if (currentStreakData.currentStreak > storedBest) {
            setSetting("best_streak", currentStreakData.currentStreak.toString())
        }
    }

    fun hasPracticedToday(): Boolean {
        val db = readableDatabase
        val todayStr = getTodayDateString()
        val cursor = db.query(
            TABLE_DAILY_LOGS,
            arrayOf(COLUMN_WORDS_REVIEWED),
            "$COLUMN_LOG_DATE = ?",
            arrayOf(todayStr),
            null,
            null,
            null
        )
        return cursor.use { it.moveToFirst() && it.getInt(0) > 0 }
    }

    fun getStreakData(): StreakData {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_DAILY_LOGS,
            arrayOf(COLUMN_LOG_DATE, COLUMN_WORDS_REVIEWED),
            null,
            null,
            null,
            null,
            "$COLUMN_LOG_DATE DESC"
        )

        val completedDates = mutableSetOf<String>()
        var lastCompleted = ""
        cursor.use {
            while (it.moveToNext()) {
                val date = it.getString(0)
                val count = it.getInt(1)
                if (count >= 1) { // Any practice counts toward streak
                    completedDates.add(date)
                    if (lastCompleted.isEmpty()) {
                        lastCompleted = date
                    }
                }
            }
        }

        val today = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayStr = sdf.format(today.time)

        // Calculate consecutive streak
        var streak = 0
        val checkCal = Calendar.getInstance()
        // If today is completed, streak starts today, otherwise check yesterday
        if (completedDates.contains(todayStr)) {
            streak++
            checkCal.add(Calendar.DAY_OF_YEAR, -1)
        } else {
            // Check yesterday
            val yesterdayCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
            val yestStr = sdf.format(yesterdayCal.time)
            if (completedDates.contains(yestStr)) {
                checkCal.add(Calendar.DAY_OF_YEAR, -1)
            } else {
                // Streak is 0
                checkCal.timeInMillis = 0
            }
        }

        if (checkCal.timeInMillis > 0) {
            while (true) {
                val dStr = sdf.format(checkCal.time)
                if (completedDates.contains(dStr)) {
                    if (dStr != todayStr) streak++
                    checkCal.add(Calendar.DAY_OF_YEAR, -1)
                } else {
                    break
                }
            }
        }

        // Generate past 7 days activity (from 6 days ago up to today)
        val past7 = mutableListOf<DayActivity>()
        val iterCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -6) }

        for (i in 0..6) {
            val dStr = sdf.format(iterCal.time)
            val dayOfWeek = iterCal.get(Calendar.DAY_OF_WEEK) // 1=Sun, 2=Mon...
            val label = when (dayOfWeek) {
                Calendar.MONDAY -> "L"
                Calendar.TUESDAY -> "M"
                Calendar.WEDNESDAY -> "X"
                Calendar.THURSDAY -> "J"
                Calendar.FRIDAY -> "V"
                Calendar.SATURDAY -> "S"
                Calendar.SUNDAY -> "D"
                else -> "·"
            }
            past7.add(
                DayActivity(
                    dayLabel = label,
                    dateString = dStr,
                    isCompleted = completedDates.contains(dStr),
                    isToday = (dStr == todayStr)
                )
            )
            iterCal.add(Calendar.DAY_OF_YEAR, 1)
        }

        return StreakData(
            currentStreak = streak,
            bestStreak = maxOf(streak, getStoredIntSetting("best_streak", streak)),
            lastCompletedDate = lastCompleted,
            past7Days = past7
        )
    }

    fun getSetting(key: String, defaultVal: String): String {
        val db = readableDatabase
        val cursor = db.query(TABLE_SETTINGS, arrayOf(COLUMN_VALUE), "$COLUMN_KEY = ?", arrayOf(key), null, null, null)
        return cursor.use {
            if (it.moveToFirst()) it.getString(0) else defaultVal
        }
    }

    fun setSetting(key: String, value: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_KEY, key)
            put(COLUMN_VALUE, value)
        }
        db.insertWithOnConflict(TABLE_SETTINGS, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    private fun getStoredIntSetting(key: String, defaultVal: Int): Int {
        return getSetting(key, defaultVal.toString()).toIntOrNull() ?: defaultVal
    }

    fun resetToSeedData() {
        val db = writableDatabase
        db.execSQL("DELETE FROM $TABLE_WORDS")
        seedInitialWords(db)
    }

    private fun getTodayDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun cursorToWord(cursor: Cursor): Word {
        return Word(
            id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)),
            word = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_WORD)),
            translation = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TRANSLATION)),
            sentence = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SENTENCE)),
            source = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SOURCE)),
            easeFactor = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_EASE_FACTOR)),
            interval = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_INTERVAL)),
            repetitions = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_REPETITIONS)),
            nextReviewDate = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_NEXT_REVIEW)),
            lastReviewedDate = if (cursor.isNull(cursor.getColumnIndexOrThrow(COLUMN_LAST_REVIEWED))) null else cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_LAST_REVIEWED)),
            status = try {
                MasteryStatus.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STATUS)))
            } catch (e: Exception) {
                MasteryStatus.NEW
            },
            createdAt = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_CREATED_AT))
        )
    }
}
