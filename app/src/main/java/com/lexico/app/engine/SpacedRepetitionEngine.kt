package com.lexico.app.engine

import com.lexico.app.data.model.MasteryStatus
import com.lexico.app.data.model.Word
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Scientific SuperMemo SM-2 Spaced Repetition Engine.
 * Calculates next review interval, ease factor, and repetition counter
 * based on cognitive recall quality.
 */
object SpacedRepetitionEngine {

    const val DEFAULT_EASE_FACTOR = 2.5f
    const val MINIMUM_EASE_FACTOR = 1.3f

    /**
     * @param word Target word being practiced
     * @param quality Quality grade from 0 to 5:
     *   5: Perfect recall, immediate response without assistance
     *   4: Correct recall with slight hesitation
     *   3: Correct response after viewing hint (scaffold)
     *   2: Incorrect response, but correct answer felt familiar
     *   1: Incorrect response, correct answer was remembered once shown
     *   0: Total blackout, complete failure to recognize
     * @param currentTime Timestamp ms
     */
    fun calculateNextReview(word: Word, quality: Int, currentTime: Long = System.currentTimeMillis()): Word {
        val clampedQuality = quality.coerceIn(0, 5)

        val newRepetitions: Int
        val newInterval: Int

        if (clampedQuality >= 3) {
            newRepetitions = word.repetitions + 1
            newInterval = when (word.repetitions) {
                0 -> 1 // 1 day
                1 -> 6 // 6 days
                else -> {
                    val computed = (word.interval * word.easeFactor).roundToInt()
                    max(1, computed)
                }
            }
        } else {
            // Failed recall resets consecutive repetitions
            newRepetitions = 0
            newInterval = 1 // Review again tomorrow
        }

        // SM-2 Ease Factor calculation
        val qDiff = (5 - clampedQuality).toFloat()
        val delta = 0.1f - qDiff * (0.08f + qDiff * 0.02f)
        val newEaseFactor = max(MINIMUM_EASE_FACTOR, word.easeFactor + delta)

        val nextReviewTimestamp = currentTime + (newInterval.toLong() * 24L * 60L * 60L * 1000L)

        val newStatus = when {
            newRepetitions >= 3 && newInterval >= 14 -> MasteryStatus.MASTERED
            newRepetitions > 0 -> MasteryStatus.LEARNING
            else -> MasteryStatus.NEW
        }

        return word.copy(
            easeFactor = newEaseFactor,
            interval = newInterval,
            repetitions = newRepetitions,
            nextReviewDate = nextReviewTimestamp,
            lastReviewedDate = currentTime,
            status = newStatus
        )
    }
}
