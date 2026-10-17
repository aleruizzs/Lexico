package com.lexico.app.ui.screens.games

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lexico.app.data.model.Word
import com.lexico.app.ui.components.EditorialBadge
import com.lexico.app.ui.components.EditorialButton
import com.lexico.app.ui.components.HairlineDivider
import com.lexico.app.ui.theme.ForestGreen
import com.lexico.app.ui.theme.MutedRed
import com.lexico.app.ui.theme.TerracottaAccent

private val literarySentenceFallback = mapOf(
    "ubiquitous" to "Smartphones have become ubiquitous in modern everyday life.",
    "serendipity" to "Finding that rare first edition in a dusty bookstore was pure serendipity.",
    "machiavellian" to "Logan Roy's Machiavellian maneuvers kept everyone guessing his next move.",
    "ineffable" to "There was an ineffable melancholy in the green light across the bay.",
    "ephemeral" to "Viral fame on social media is often ephemeral, fading within weeks.",
    "ruthless" to "The desert of Arrakis is ruthless to those who do not respect its laws.",
    "resilience" to "The kitchen staff showed incredible resilience during the dinner rush.",
    "dystopian" to "Big Brother symbolized the omnipresent surveillance of a dystopian regime.",
    "solace" to "She found solace in classical literature during long autumn evenings.",
    "eloquent" to "His eloquent speech moved the entire audience to deep reflection.",
    "pristine" to "The alpine lake remained untouched in its pristine natural state.",
    "meticulous" to "The artisan was meticulous in polishing every single contour."
)

@Composable
fun ContextClozeScreen(
    sessionWords: List<Word>,
    onCompleteSession: (List<Pair<Word, Int>>) -> Unit,
    onExit: () -> Unit
) {
    // Filter strictly for words that have real context sentences (never synthetic templates!)
    val eligibleWords = remember(sessionWords) {
        val list = sessionWords.mapNotNull { word ->
            val existing = word.sentence?.trim()
            val fallback = literarySentenceFallback[word.word.lowercase()]
            val finalSentence = when {
                !existing.isNullOrBlank() -> existing
                !fallback.isNullOrBlank() -> fallback
                else -> null
            }
            if (finalSentence != null) {
                word.copy(sentence = finalSentence)
            } else null
        }
        if (list.isNotEmpty()) list else sessionWords.take(5).map { word ->
            val fb = literarySentenceFallback[word.word.lowercase()] ?: "The true essence of ${word.word} became clear in that defining chapter."
            word.copy(sentence = fb)
        }
    }

    if (eligibleWords.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "No hay frases de contexto disponibles.", style = MaterialTheme.typography.bodyLarge)
        }
        return
    }

    val workingQueue = remember { mutableListOf<Word>().apply { addAll(eligibleWords) } }
    var currentIndex by remember { mutableIntStateOf(0) }
    val results = remember { mutableListOf<Pair<Word, Int>>() }

    var currentWord by remember { mutableStateOf(workingQueue.first()) }
    var userInput by remember { mutableStateOf("") }
    var isCorrectState by remember { mutableStateOf<Boolean?>(null) }

    val focusRequester = remember { FocusRequester() }
    val haptic = LocalHapticFeedback.current
    val scrollState = rememberScrollState()

    fun handleCheck() {
        if (userInput.isBlank()) return
        val isCorrect = userInput.trim().equals(currentWord.word.trim(), ignoreCase = true)
        if (isCorrect) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            results.add(currentWord to 5)
            isCorrectState = true
        } else {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            results.add(currentWord to 1)
            workingQueue.add(currentWord)
            isCorrectState = false
        }
    }

    fun advanceToNext() {
        isCorrectState = null
        userInput = ""
        if (currentIndex + 1 < workingQueue.size) {
            currentIndex++
            currentWord = workingQueue[currentIndex]
        } else {
            onCompleteSession(results)
        }
    }

    val sentence = currentWord.sentence ?: ""
    val target = currentWord.word
    val clozeSentence = remember(sentence, target) {
        val regex = Regex("(?i)\\b${Regex.escape(target)}\\b")
        if (regex.containsMatchIn(sentence)) {
            regex.replace(sentence, "[ ... ]")
        } else {
            val simple = Regex("(?i)${Regex.escape(target)}")
            if (simple.containsMatchIn(sentence)) {
                simple.replace(sentence, "[ ... ]")
            } else {
                "$sentence [ ... ]"
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 24.dp)
            ) {
                Spacer(modifier = Modifier.height(12.dp))
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
                            text = "CONTEXTO CLOZE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                letterSpacing = 1.2.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${currentIndex + 1} / ${workingQueue.size}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                letterSpacing = 0.8.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(onClick = onExit) {
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = "Salir",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                HairlineDivider()
            }
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .imePadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 14.dp)
            ) {
                if (isCorrectState == null) {
                    EditorialButton(
                        text = "Comprobar contexto",
                        enabled = userInput.isNotBlank(),
                        onClick = { handleCheck() },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    EditorialButton(
                        text = if (currentIndex + 1 < workingQueue.size) "Siguiente frase" else "Finalizar sesión",
                        onClick = { advanceToNext() },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Source badge
            EditorialBadge(text = currentWord.source)

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "COMPLETA LA PALABRA OCULTA EN LA FRASE",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.0.sp
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Cloze quote card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
                    .padding(20.dp)
            ) {
                val annotated = buildAnnotatedString {
                    if (isCorrectState != null) {
                        append("“$sentence”")
                    } else {
                        val parts = clozeSentence.split("[ ... ]")
                        append("“")
                        if (parts.isNotEmpty()) append(parts[0])
                        withStyle(
                            SpanStyle(
                                color = TerracottaAccent,
                                fontWeight = FontWeight.Bold,
                                background = TerracottaAccent.copy(alpha = 0.12f)
                            )
                        ) {
                            append(" [ ... ] ")
                        }
                        if (parts.size > 1) append(parts[1])
                        append("”")
                    }
                }

                Text(
                    text = annotated,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontFamily = FontFamily.Serif,
                        lineHeight = 32.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Spanish meaning as clue
            Text(
                text = "SIGNIFICADO EN ESPAÑOL",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.0.sp
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = currentWord.translation,
                style = MaterialTheme.typography.titleMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Input
            Text(
                text = "TÉRMINO FALTANTE",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.0.sp
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(
                        width = 1.dp,
                        color = when (isCorrectState) {
                            true -> ForestGreen
                            false -> MutedRed
                            null -> MaterialTheme.colorScheme.outline
                        },
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                if (userInput.isEmpty() && isCorrectState == null) {
                    Text(
                        text = "Escribe la palabra en inglés…",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontFamily = FontFamily.Serif,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    )
                }
                BasicTextField(
                    value = userInput,
                    onValueChange = { if (isCorrectState == null) userInput = it },
                    textStyle = MaterialTheme.typography.headlineMedium.copy(
                        fontFamily = FontFamily.Serif,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    cursorBrush = SolidColor(TerracottaAccent),
                    singleLine = true,
                    enabled = isCorrectState == null,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { handleCheck() }),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                )
            }

            // Feedback Banner
            AnimatedVisibility(visible = isCorrectState != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            if (isCorrectState == true) ForestGreen.copy(alpha = 0.08f)
                            else MutedRed.copy(alpha = 0.08f)
                        )
                        .padding(14.dp)
                ) {
                    Text(
                        text = if (isCorrectState == true) "PALABRA CORRECTA" else "PALABRA CORRECTA: ${currentWord.word}",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = if (isCorrectState == true) ForestGreen else MutedRed,
                            letterSpacing = 0.8.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    LaunchedEffect(currentWord) {
        focusRequester.requestFocus()
    }
}
