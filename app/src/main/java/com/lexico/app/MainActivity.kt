package com.lexico.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.lexico.app.ui.components.QuickAddBottomSheet
import com.lexico.app.ui.components.WordDetailBottomSheet
import com.lexico.app.ui.screens.HomeScreen
import com.lexico.app.ui.screens.SessionSummaryScreen
import com.lexico.app.ui.screens.SettingsScreen
import com.lexico.app.ui.screens.games.ActiveRecallScreen
import com.lexico.app.ui.screens.games.ContextClozeScreen
import com.lexico.app.ui.screens.games.LexicalSprintScreen
import com.lexico.app.ui.theme.LexicoTheme
import com.lexico.app.ui.viewmodel.LexiconViewModel
import com.lexico.app.ui.viewmodel.ScreenState

class MainActivity : ComponentActivity() {

    private val viewModel: LexiconViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val uiState by viewModel.uiState.collectAsState()

            LexicoTheme(themeMode = uiState.themeMode) {
                BackHandler(enabled = uiState.currentScreen !is ScreenState.Home) {
                    viewModel.returnToHome()
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when (val screen = uiState.currentScreen) {
                        is ScreenState.Home -> {
                            HomeScreen(
                                words = uiState.words,
                                streakData = uiState.streakData,
                                availableSources = uiState.availableSources,
                                onOpenQuickAdd = { viewModel.openQuickAdd() },
                                onStartPractice = { mode -> viewModel.startPractice(mode) },
                                onOpenSettings = { viewModel.openSettings() },
                                onWordSelected = { word -> viewModel.openWordDetail(word) }
                            )
                        }

                        is ScreenState.ActiveRecallGame -> {
                            ActiveRecallScreen(
                                sessionWords = screen.words,
                                onCompleteSession = { results -> viewModel.processSessionResults(results) },
                                onExit = { viewModel.returnToHome() }
                            )
                        }

                        is ScreenState.ContextClozeGame -> {
                            ContextClozeScreen(
                                sessionWords = screen.words,
                                onCompleteSession = { results -> viewModel.processSessionResults(results) },
                                onExit = { viewModel.returnToHome() }
                            )
                        }

                        is ScreenState.LexicalSprintGame -> {
                            LexicalSprintScreen(
                                sessionWords = screen.words,
                                onCompleteSprint = { _, results -> viewModel.processSessionResults(results) },
                                onExit = { viewModel.returnToHome() }
                            )
                        }

                        is ScreenState.SessionSummary -> {
                            SessionSummaryScreen(
                                items = screen.items,
                                accuracy = screen.accuracy,
                                currentStreak = screen.streak,
                                onReturnHome = { viewModel.returnToHome() }
                            )
                        }

                        is ScreenState.Settings -> {
                            SettingsScreen(
                                currentThemeMode = uiState.themeMode,
                                onSelectThemeMode = { mode -> viewModel.setThemeMode(mode) },
                                currentReminderHour = uiState.reminderHour,
                                currentReminderMinute = uiState.reminderMinute,
                                onSaveReminderTime = { h, m -> viewModel.saveReminderTime(h, m) },
                                onResetSeedData = { viewModel.resetToSeedData() },
                                onBack = { viewModel.returnToHome() }
                            )
                        }
                    }

                    if (uiState.isQuickAddOpen) {
                        QuickAddBottomSheet(
                            availableSources = uiState.availableSources,
                            onDismiss = { viewModel.closeQuickAdd() },
                            onSaveWord = { newWord -> viewModel.addWord(newWord) }
                        )
                    }

                    uiState.selectedWordForDetail?.let { word ->
                        WordDetailBottomSheet(
                            word = word,
                            onDismiss = { viewModel.closeWordDetail() },
                            onDeleteWord = { id -> viewModel.deleteWord(id) }
                        )
                    }
                }
            }
        }
    }
}
