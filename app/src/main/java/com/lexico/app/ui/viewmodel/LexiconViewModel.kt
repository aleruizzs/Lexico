package com.lexico.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lexico.app.data.db.LexiconDbHelper
import com.lexico.app.data.model.StreakData
import com.lexico.app.data.model.Word
import com.lexico.app.engine.SpacedRepetitionEngine
import com.lexico.app.notification.NotificationScheduler
import com.lexico.app.ui.screens.PracticeGameMode
import com.lexico.app.ui.theme.AppThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PracticedWordSummary(
    val word: Word,
    val lowestQuality: Int,
    val updatedWord: Word,
    val willReviewTomorrow: Boolean,
    val levelAdvanced: Boolean
)

sealed class ScreenState {
    object Home : ScreenState()
    data class ActiveRecallGame(val words: List<Word>) : ScreenState()
    data class ContextClozeGame(val words: List<Word>) : ScreenState()
    data class LexicalSprintGame(val words: List<Word>) : ScreenState()
    data class SessionSummary(
        val items: List<PracticedWordSummary>,
        val accuracy: Int,
        val streak: Int
    ) : ScreenState()
    object Settings : ScreenState()
}

data class LexiconUiState(
    val words: List<Word> = emptyList(),
    val streakData: StreakData = StreakData(),
    val availableSources: List<String> = emptyList(),
    val currentScreen: ScreenState = ScreenState.Home,
    val isQuickAddOpen: Boolean = false,
    val selectedWordForDetail: Word? = null,
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val reminderHour: Int = 21,
    val reminderMinute: Int = 30
)

class LexiconViewModel(application: Application) : AndroidViewModel(application) {

    private val dbHelper = LexiconDbHelper(application)

    private val _uiState = MutableStateFlow(LexiconUiState())
    val uiState: StateFlow<LexiconUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            val allWords = dbHelper.getAllWords()
            val streak = dbHelper.getStreakData()
            val sources = dbHelper.getUniqueSources()
            val remHour = dbHelper.getSetting("reminder_hour", "21").toIntOrNull() ?: 21
            val remMin = dbHelper.getSetting("reminder_minute", "30").toIntOrNull() ?: 30
            val themeStr = dbHelper.getSetting("theme_mode", AppThemeMode.SYSTEM.name)
            val savedTheme = try { AppThemeMode.valueOf(themeStr) } catch (_: Exception) { AppThemeMode.SYSTEM }

            _uiState.update {
                it.copy(
                    words = allWords,
                    streakData = streak,
                    availableSources = sources,
                    themeMode = savedTheme,
                    reminderHour = remHour,
                    reminderMinute = remMin
                )
            }
        }
    }

    fun openQuickAdd() {
        _uiState.update { it.copy(isQuickAddOpen = true) }
    }

    fun closeQuickAdd() {
        _uiState.update { it.copy(isQuickAddOpen = false) }
    }

    fun addWord(word: Word) {
        viewModelScope.launch {
            dbHelper.insertWord(word)
            loadData()
        }
    }

    fun startPractice(mode: PracticeGameMode) {
        val dueWords = _uiState.value.words.filter { it.isDueForReview }
        val targetWords = if (dueWords.isNotEmpty()) dueWords else _uiState.value.words

        when (mode) {
            PracticeGameMode.ACTIVE_RECALL -> {
                _uiState.update { it.copy(currentScreen = ScreenState.ActiveRecallGame(targetWords)) }
            }
            PracticeGameMode.CONTEXT_CLOZE -> {
                _uiState.update { it.copy(currentScreen = ScreenState.ContextClozeGame(targetWords)) }
            }
            PracticeGameMode.LEXICAL_SPRINT -> {
                _uiState.update { it.copy(currentScreen = ScreenState.LexicalSprintGame(targetWords)) }
            }
        }
    }

    fun processSessionResults(results: List<Pair<Word, Int>>) {
        viewModelScope.launch {
            if (results.isEmpty()) {
                returnToHome()
                return@launch
            }

            val now = System.currentTimeMillis()
            // Group session results by word.id and enforce lowest quality (minOf)
            val grouped = results.groupBy { it.first.id }
            val summaries = mutableListOf<PracticedWordSummary>()

            for ((_, wordPairs) in grouped) {
                val originalWord = wordPairs.first().first
                val minQuality = wordPairs.minOf { it.second }

                val updatedWord = SpacedRepetitionEngine.calculateNextReview(originalWord, minQuality, now)
                dbHelper.updateWord(updatedWord)

                val willReviewTomorrow = updatedWord.interval <= 1
                val levelAdvanced = updatedWord.repetitions > originalWord.repetitions

                summaries.add(
                    PracticedWordSummary(
                        word = originalWord,
                        lowestQuality = minQuality,
                        updatedWord = updatedWord,
                        willReviewTomorrow = willReviewTomorrow,
                        levelAdvanced = levelAdvanced
                    )
                )
            }

            dbHelper.recordPracticeActivity(summaries.size)

            val correctCount = summaries.count { it.lowestQuality >= 3 }
            val accuracy = if (summaries.isNotEmpty()) ((correctCount.toFloat() / summaries.size) * 100).toInt() else 100
            val updatedStreak = dbHelper.getStreakData()

            _uiState.update {
                it.copy(
                    currentScreen = ScreenState.SessionSummary(
                        items = summaries,
                        accuracy = accuracy,
                        streak = updatedStreak.currentStreak
                    )
                )
            }
            loadData()
        }
    }

    fun openSettings() {
        _uiState.update { it.copy(currentScreen = ScreenState.Settings) }
    }

    fun returnToHome() {
        _uiState.update { it.copy(currentScreen = ScreenState.Home) }
    }

    fun openWordDetail(word: Word) {
        _uiState.update { it.copy(selectedWordForDetail = word) }
    }

    fun closeWordDetail() {
        _uiState.update { it.copy(selectedWordForDetail = null) }
    }

    fun deleteWord(wordId: Long) {
        viewModelScope.launch {
            dbHelper.deleteWord(wordId)
            _uiState.update { it.copy(selectedWordForDetail = null) }
            loadData()
        }
    }

    fun setThemeMode(mode: AppThemeMode) {
        viewModelScope.launch {
            dbHelper.setSetting("theme_mode", mode.name)
            _uiState.update { it.copy(themeMode = mode) }
        }
    }

    fun saveReminderTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            dbHelper.setSetting("reminder_hour", hour.toString())
            dbHelper.setSetting("reminder_minute", minute.toString())
            NotificationScheduler.scheduleDailyReminder(getApplication(), hour, minute)
            _uiState.update { it.copy(reminderHour = hour, reminderMinute = minute) }
        }
    }

    fun resetToSeedData() {
        viewModelScope.launch {
            dbHelper.resetToSeedData()
            loadData()
        }
    }
}
