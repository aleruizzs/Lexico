package com.lexico.app.ui.screens.games

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lexico.app.data.model.Word
import com.lexico.app.ui.components.EditorialBadge
import com.lexico.app.ui.components.EditorialButton
import com.lexico.app.ui.components.HairlineDivider
import com.lexico.app.ui.theme.ForestGreen
import com.lexico.app.ui.theme.MutedRed
import com.lexico.app.ui.theme.TerracottaAccent

@Composable
fun ActiveRecallScreen(
    sessionWords: List<Word>,
    onCompleteSession: (List<Pair<Word, Int>>) -> Unit,
    onExit: () -> Unit
) {
    if (sessionWords.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No hay palabras disponibles para esta sesión.",
                style = MaterialTheme.typography.bodyLarge
            )
        }
        return
    }

    val workingQueue = remember { mutableListOf<Word>().apply { addAll(sessionWords) } }
    var currentIndex by remember { mutableIntStateOf(0) }
    val results = remember { mutableListOf<Pair<Word, Int>>() }

    var currentWord by remember { mutableStateOf(workingQueue.first()) }
    var userInput by remember { mutableStateOf("") }
    var isHintRevealed by remember { mutableStateOf(false) }
    var feedbackState by remember { mutableStateOf<FeedbackType?>(null) }

    val focusRequester = remember { FocusRequester() }
    val haptic = LocalHapticFeedback.current
    val scrollState = rememberScrollState()

    fun handleCheck() {
        if (userInput.isBlank()) return

        val isCorrect = userInput.trim().equals(currentWord.word.trim(), ignoreCase = true)
        if (isCorrect) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            val quality = if (isHintRevealed) 3 else 5
            results.add(currentWord to quality)
            feedbackState = FeedbackType.CORRECT
        } else {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            results.add(currentWord to 1)
            workingQueue.add(currentWord)
            feedbackState = FeedbackType.INCORRECT
        }
    }

    fun advanceToNext() {
        feedbackState = null
        userInput = ""
        isHintRevealed = false

        if (currentIndex + 1 < workingQueue.size) {
            currentIndex++
            currentWord = workingQueue[currentIndex]
        } else {
            onCompleteSession(results)
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
                            text = "PRODUCCIÓN ACTIVA",
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
                if (feedbackState == null) {
                    EditorialButton(
                        text = "Comprobar respuesta",
                        enabled = userInput.isNotBlank(),
                        onClick = { handleCheck() },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    EditorialButton(
                        text = if (currentIndex + 1 < workingQueue.size) "Siguiente término" else "Finalizar sesión",
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

            // Source Pill
            EditorialBadge(text = currentWord.source)

            Spacer(modifier = Modifier.height(20.dp))

            // Translation prompt in Spanish
            Text(
                text = "TRADUCE AL INGLÉS",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.0.sp
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = currentWord.translation,
                style = MaterialTheme.typography.displayMedium.copy(
                    fontFamily = FontFamily.Serif,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Input Row with Humanized Hint Button DIRECTLY ABOVE Input (100% visible with keyboard)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ESCRIBE EL TÉRMINO",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.0.sp
                    )
                )

                if (feedbackState == null) {
                    if (!isHintRevealed) {
                        Text(
                            text = "Pedir pista · Letra inicial",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TerracottaAccent,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.5.sp
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable {
                                    isHintRevealed = true
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                                .padding(vertical = 4.dp, horizontal = 4.dp)
                        )
                    } else {
                        Text(
                            text = "Pista: ${buildScaffold(currentWord.word)}",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontFamily = FontFamily.Serif,
                                color = TerracottaAccent,
                                letterSpacing = 1.2.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(
                        width = 1.dp,
                        color = when (feedbackState) {
                            FeedbackType.CORRECT -> ForestGreen
                            FeedbackType.INCORRECT -> MutedRed
                            null -> MaterialTheme.colorScheme.outline
                        },
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                if (userInput.isEmpty() && feedbackState == null) {
                    Text(
                        text = if (isHintRevealed) buildScaffold(currentWord.word) else "Escribe aquí…",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontFamily = FontFamily.Serif,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    )
                }
                BasicTextField(
                    value = userInput,
                    onValueChange = { if (feedbackState == null) userInput = it },
                    textStyle = MaterialTheme.typography.headlineLarge.copy(
                        fontFamily = FontFamily.Serif,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    cursorBrush = SolidColor(TerracottaAccent),
                    singleLine = true,
                    enabled = feedbackState == null,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { handleCheck() }),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                )
            }

            // Feedback / Result Area
            AnimatedVisibility(visible = feedbackState != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 18.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            if (feedbackState == FeedbackType.CORRECT) ForestGreen.copy(alpha = 0.08f)
                            else MutedRed.copy(alpha = 0.08f)
                        )
                        .padding(16.dp)
                ) {
                    Text(
                        text = if (feedbackState == FeedbackType.CORRECT) "RESPUESTA CORRECTA" else "RESPUESTA INCORRECTA",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = if (feedbackState == FeedbackType.CORRECT) ForestGreen else MutedRed,
                            letterSpacing = 1.0.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = currentWord.word,
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontFamily = FontFamily.Serif,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    if (!currentWord.sentence.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "“${currentWord.sentence}”",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = FontFamily.Serif,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            )
                        )
                    }
                    if (feedbackState == FeedbackType.INCORRECT) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Esta palabra volverá a aparecer al final de la sesión para consolidarla.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    LaunchedEffect(currentWord) {
        focusRequester.requestFocus()
    }
}

private enum class FeedbackType {
    CORRECT,
    INCORRECT
}

private fun buildScaffold(word: String): String {
    if (word.isEmpty()) return ""
    val firstChar = word.first()
    val remainder = word.drop(1).map { if (it.isWhitespace()) " " else "_" }.joinToString(" ")
    return "$firstChar $remainder"
}
