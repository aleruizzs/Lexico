package com.lexico.app.ui.screens.games

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lexico.app.data.model.Word
import com.lexico.app.ui.components.EditorialButton
import com.lexico.app.ui.components.HairlineDivider
import com.lexico.app.ui.theme.MutedRed
import com.lexico.app.ui.theme.TerracottaAccent
import kotlinx.coroutines.delay

@Composable
fun LexicalSprintScreen(
    sessionWords: List<Word>,
    onCompleteSprint: (matchedCount: Int, results: List<Pair<Word, Int>>) -> Unit,
    onExit: () -> Unit
) {
    if (sessionWords.size < 2) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Se requieren al menos 2 palabras para el Sprint.",
                style = MaterialTheme.typography.bodyLarge
            )
        }
        return
    }

    var secondsRemaining by remember { mutableIntStateOf(60) }
    var isGameOver by remember { mutableStateOf(false) }

    // All available words queue
    val allWords = remember { sessionWords.shuffled() }
    val remainingPool = remember { mutableListOf<Word>().apply { addAll(allWords) } }

    // Active displayed batch (up to 4 pairs at a time)
    val activeEnglishWords = remember { mutableListOf<Word>() }
    val activeSpanishItems = remember { mutableListOf<Word>() }

    // Matched and failed tracking
    val matchedWordIds = remember { mutableSetOf<Long>() }
    val failedWordIds = remember { mutableSetOf<Long>() }
    val results = remember { mutableListOf<Pair<Word, Int>>() }

    // Selections
    var selectedEnglishWord by remember { mutableStateOf<Word?>(null) }
    var selectedSpanishWord by remember { mutableStateOf<Word?>(null) }
    var matchErrorPair by remember { mutableStateOf<Pair<Long, Long>?>(null) }

    val haptic = LocalHapticFeedback.current

    // Initialize the active board with up to 4 items
    LaunchedEffect(Unit) {
        val initialBatchSize = minOf(4, remainingPool.size)
        val initialBatch = remainingPool.take(initialBatchSize)
        remainingPool.removeAll(initialBatch)

        activeEnglishWords.addAll(initialBatch)
        activeSpanishItems.addAll(initialBatch.shuffled())
    }

    // 60-second Timer
    LaunchedEffect(isGameOver) {
        if (!isGameOver) {
            while (secondsRemaining > 0) {
                delay(1000L)
                secondsRemaining--
            }
            isGameOver = true
        }
    }

    fun checkMatch(eng: Word, esp: Word) {
        if (eng.id == esp.id) {
            // Correct Match! Cognitive honesty: max quality 3 (passive recognition), quality 2 if failed before
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            matchedWordIds.add(eng.id)
            val quality = if (failedWordIds.contains(eng.id)) 2 else 3
            results.add(eng to quality)

            selectedEnglishWord = null
            selectedSpanishWord = null
            matchErrorPair = null

            // If there are words in remaining pool, replace the matched slot
            if (remainingPool.isNotEmpty()) {
                val next = remainingPool.removeAt(0)
                val engIdx = activeEnglishWords.indexOfFirst { it.id == eng.id }
                val espIdx = activeSpanishItems.indexOfFirst { it.id == esp.id }
                if (engIdx != -1) activeEnglishWords[engIdx] = next
                if (espIdx != -1) activeSpanishItems[espIdx] = next
            } else {
                activeEnglishWords.removeAll { it.id == eng.id }
                activeSpanishItems.removeAll { it.id == esp.id }

                if (activeEnglishWords.isEmpty()) {
                    isGameOver = true
                }
            }
        } else {
            // Mismatch: record failure quality 2 for cognitive honesty
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            failedWordIds.add(eng.id)
            results.add(eng to 2)

            matchErrorPair = eng.id to esp.id
            selectedEnglishWord = null
            selectedSpanishWord = null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        // Top Bar with Timer and Close
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(TerracottaAccent)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "SPRINT LÉXICO · 60S",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 1.2.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                val minutes = secondsRemaining / 60
                val seconds = secondsRemaining % 60
                val timeFormatted = String.format("%02d:%02d", minutes, seconds)

                Text(
                    text = timeFormatted,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        color = if (secondsRemaining <= 10) MutedRed else TerracottaAccent
                    )
                )
                Spacer(modifier = Modifier.width(16.dp))
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "Salir",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { onExit() }
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))
        HairlineDivider()
        Spacer(modifier = Modifier.height(18.dp))

        if (!isGameOver) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "TÉRMINOS EN INGLÉS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.0.sp
                    )
                )
                Text(
                    text = "TRADUCCIONES",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.0.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Two columns: Left (English), Right (Spanish) perfectly aligned row-by-row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // English column
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    activeEnglishWords.forEach { word ->
                        val isSelected = selectedEnglishWord?.id == word.id
                        val isError = matchErrorPair?.first == word.id

                        SprintItemCard(
                            text = word.word,
                            isSerif = true,
                            isSelected = isSelected,
                            isError = isError,
                            onClick = {
                                selectedEnglishWord = word
                                selectedSpanishWord?.let { checkMatch(word, it) }
                            }
                        )
                    }
                }

                // Spanish column
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    activeSpanishItems.forEach { word ->
                        val isSelected = selectedSpanishWord?.id == word.id
                        val isError = matchErrorPair?.second == word.id

                        SprintItemCard(
                            text = word.translation,
                            isSerif = false,
                            isSelected = isSelected,
                            isError = isError,
                            onClick = {
                                selectedSpanishWord = word
                                selectedEnglishWord?.let { checkMatch(it, word) }
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Score indicator
            Text(
                text = "EMPAREJADAS: ${matchedWordIds.size}",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.0.sp
                ),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(16.dp))
        } else {
            // Game Over / Summary View
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Sprint Finalizado",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontFamily = FontFamily.Serif,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${matchedWordIds.size}",
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontFamily = FontFamily.Serif,
                                color = TerracottaAccent
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "PALABRAS EMPAREJADAS CON ÉXITO",
                            style = MaterialTheme.typography.labelSmall.copy(
                                letterSpacing = 1.0.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                EditorialButton(
                    text = "Consolidar y Guardar",
                    onClick = {
                        onCompleteSprint(matchedWordIds.size, results)
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun SprintItemCard(
    text: String,
    isSerif: Boolean,
    isSelected: Boolean,
    isError: Boolean,
    onClick: () -> Unit
) {
    val borderColor = when {
        isError -> MutedRed
        isSelected -> TerracottaAccent
        else -> MaterialTheme.colorScheme.outline
    }
    val bgColor = when {
        isError -> MutedRed.copy(alpha = 0.08f)
        isSelected -> TerracottaAccent.copy(alpha = 0.08f)
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(76.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(bgColor)
            .border(if (isSelected || isError) 1.5.dp else 1.dp, borderColor, RoundedCornerShape(4.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            style = if (isSerif) {
                MaterialTheme.typography.titleMedium.copy(
                    fontFamily = FontFamily.Serif,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            } else {
                MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        )
    }
}
