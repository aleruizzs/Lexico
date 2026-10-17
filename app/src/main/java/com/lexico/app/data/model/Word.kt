package com.lexico.app.data.model

enum class MasteryStatus {
    NEW,
    LEARNING,
    MASTERED
}

data class Word(
    val id: Long = 0,
    val word: String,
    val translation: String,
    val sentence: String? = null,
    val source: String = "General",
    val easeFactor: Float = 2.5f,
    val interval: Int = 0, // Days
    val repetitions: Int = 0,
    val nextReviewDate: Long = System.currentTimeMillis(),
    val lastReviewedDate: Long? = null,
    val status: MasteryStatus = MasteryStatus.NEW,
    val createdAt: Long = System.currentTimeMillis()
) {
    val isDueForReview: Boolean
        get() = nextReviewDate <= System.currentTimeMillis()
}

data class DayActivity(
    val dayLabel: String, // "L", "M", "X", "J", "V", "S", "D"
    val dateString: String, // "YYYY-MM-DD"
    val isCompleted: Boolean,
    val isToday: Boolean
)

data class StreakData(
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val lastCompletedDate: String = "",
    val past7Days: List<DayActivity> = emptyList()
)
