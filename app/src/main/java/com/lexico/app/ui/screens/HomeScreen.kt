package com.lexico.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lexico.app.data.model.MasteryStatus
import com.lexico.app.data.model.StreakData
import com.lexico.app.data.model.Word
import com.lexico.app.ui.components.EditorialBadge
import com.lexico.app.ui.components.EditorialSearchBar
import com.lexico.app.ui.components.HairlineDivider
import com.lexico.app.ui.components.StreakHeaderWidget
import com.lexico.app.ui.components.WordListItem
import com.lexico.app.ui.theme.TerracottaAccent

enum class PracticeGameMode {
    ACTIVE_RECALL,
    CONTEXT_CLOZE,
    LEXICAL_SPRINT
}

@Composable
fun HomeScreen(
    words: List<Word>,
    streakData: StreakData,
    availableSources: List<String>,
    onOpenQuickAdd: () -> Unit,
    onStartPractice: (PracticeGameMode) -> Unit,
    onOpenSettings: () -> Unit,
    onWordSelected: (Word) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedSourceFilter by remember { mutableStateOf<String?>(null) }
    var selectedStatusFilter by remember { mutableStateOf<MasteryStatus?>(null) }

    val dueWords = remember(words) { words.filter { it.isDueForReview } }

    val filteredWords = remember(words, searchQuery, selectedSourceFilter, selectedStatusFilter) {
        words.filter { word ->
            val matchesQuery = if (searchQuery.isBlank()) true else {
                word.word.contains(searchQuery, ignoreCase = true) ||
                        word.translation.contains(searchQuery, ignoreCase = true) ||
                        (word.sentence?.contains(searchQuery, ignoreCase = true) == true)
            }
            val matchesSource = if (selectedSourceFilter == null) true else {
                word.source.equals(selectedSourceFilter, ignoreCase = true)
            }
            val matchesStatus = if (selectedStatusFilter == null) true else {
                word.status == selectedStatusFilter
            }
            matchesQuery && matchesSource && matchesStatus
        }
    }

    val hasActiveFilters = selectedSourceFilter != null || selectedStatusFilter != null

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .statusBarsPadding(),
            contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 48.dp)
        ) {
            // Header with Integrated Clean "+ CAPTURAR" button (No overlapping FAB!)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Léxico",
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontFamily = FontFamily.Serif,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Text(
                            text = "RETENCIÓN REAL DE VOCABULARIO",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                letterSpacing = 1.2.sp
                            )
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(TerracottaAccent)
                                .clickable { onOpenQuickAdd() }
                                .padding(horizontal = 14.dp, vertical = 9.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.Add,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "CAPTURAR",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.8.sp
                                    )
                                )
                            }
                        }

                        IconButton(
                            onClick = onOpenSettings,
                            modifier = Modifier.size(42.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Settings,
                                contentDescription = "Ajustes",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HairlineDivider()
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Streak Widget
            item {
                StreakHeaderWidget(streakData = streakData)
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Daily Review Hub
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(6.dp))
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SESIÓN DIARIA DE REPASO",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                letterSpacing = 1.0.sp
                            )
                        )
                        Text(
                            text = "${dueWords.size} PENDIENTES",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (dueWords.isNotEmpty()) TerracottaAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (dueWords.isNotEmpty()) {
                            "Hay ${dueWords.size} palabras listas para consolidar hoy según el ritmo científico de repaso."
                        } else {
                            "Todas tus palabras están al día. Puedes explorar los 3 modos de práctica para acelerar la velocidad de reconocimiento."
                        },
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "MÉTODOS DE PRÁCTICA:",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 0.8.sp,
                            fontSize = 9.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    PracticeModeRow(
                        title = "1. Producción Activa (Typing)",
                        subtitle = "Escribe la palabra con soporte de pistas y letra inicial.",
                        onClick = { onStartPractice(PracticeGameMode.ACTIVE_RECALL) }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    PracticeModeRow(
                        title = "2. Contexto Cloze",
                        subtitle = "Deduce la palabra clave completando el hueco [ ... ] en la frase.",
                        onClick = { onStartPractice(PracticeGameMode.CONTEXT_CLOZE) }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    PracticeModeRow(
                        title = "3. Sprint Léxico (60s)",
                        subtitle = "Empareja términos y traducciones en un sprint dinámico de alta velocidad.",
                        onClick = { onStartPractice(PracticeGameMode.LEXICAL_SPRINT) }
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))
            }

            // Library Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Biblioteca",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontFamily = FontFamily.Serif,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Text(
                        text = "${filteredWords.size} de ${words.size} términos",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Search Bar
                EditorialSearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it }
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Unified and styled filter container
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "FILTRAR POR ORIGEN Y ESTADO",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 0.8.sp,
                            fontSize = 10.sp
                        )
                    )
                    if (hasActiveFilters) {
                        Text(
                            text = "LIMPIAR FILTROS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TerracottaAccent,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                letterSpacing = 0.5.sp
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(2.dp))
                                .clickable {
                                    selectedSourceFilter = null
                                    selectedStatusFilter = null
                                }
                                .padding(vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Source filter pills
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    EditorialBadge(
                        text = "TODOS LOS ORÍGENES",
                        isSelected = selectedSourceFilter == null,
                        onClick = { selectedSourceFilter = null }
                    )
                    availableSources.forEach { src ->
                        EditorialBadge(
                            text = src.uppercase(),
                            isSelected = selectedSourceFilter == src,
                            onClick = { selectedSourceFilter = src }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Mastery status filter pills
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    EditorialBadge(
                        text = "TODAS",
                        isSelected = selectedStatusFilter == null,
                        onClick = { selectedStatusFilter = null }
                    )
                    EditorialBadge(
                        text = "NUEVAS",
                        isSelected = selectedStatusFilter == MasteryStatus.NEW,
                        onClick = { selectedStatusFilter = MasteryStatus.NEW }
                    )
                    EditorialBadge(
                        text = "APRENDIENDO",
                        isSelected = selectedStatusFilter == MasteryStatus.LEARNING,
                        onClick = { selectedStatusFilter = MasteryStatus.LEARNING }
                    )
                    EditorialBadge(
                        text = "DOMINADAS",
                        isSelected = selectedStatusFilter == MasteryStatus.MASTERED,
                        onClick = { selectedStatusFilter = MasteryStatus.MASTERED }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                HairlineDivider()
            }

            // Word List Items
            if (filteredWords.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No se encontraron términos coincidentes.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            } else {
                itemsIndexed(filteredWords) { index, word ->
                    WordListItem(
                        word = word,
                        onClick = { onWordSelected(word) }
                    )
                    if (index < filteredWords.size - 1) {
                        HairlineDivider()
                    }
                }
            }
        }
    }
}

@Composable
private fun PracticeModeRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    letterSpacing = 0.5.sp
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            )
        }
    }
}
