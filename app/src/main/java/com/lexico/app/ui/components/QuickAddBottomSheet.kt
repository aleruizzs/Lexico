package com.lexico.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lexico.app.data.model.Word
import com.lexico.app.data.service.DictionaryLookupService
import com.lexico.app.ui.theme.ForestGreen
import com.lexico.app.ui.theme.TerracottaAccent
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickAddBottomSheet(
    availableSources: List<String>,
    onDismiss: () -> Unit,
    onSaveWord: (Word) -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    var englishWord by remember { mutableStateOf("") }
    var spanishTranslation by remember { mutableStateOf("") }
    var contextSentence by remember { mutableStateOf("") }
    var selectedSource by remember { mutableStateOf(availableSources.firstOrNull() ?: "General") }
    var customSource by remember { mutableStateOf("") }
    var isEnteringCustomSource by remember { mutableStateOf(false) }

    var isLookingUp by remember { mutableStateOf(false) }
    var lookupStatusMessage by remember { mutableStateOf<String?>(null) }

    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val haptic = LocalHapticFeedback.current
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    val clipboardManager = LocalClipboardManager.current
    val clipboardText = remember {
        val clip = clipboardManager.getText()?.text?.trim()?.replace("\n", " ")
        if (!clip.isNullOrBlank() && clip.length in 2..60) clip else null
    }

    fun triggerAutocomplete(term: String) {
        if (term.isBlank()) return
        scope.launch {
            isLookingUp = true
            lookupStatusMessage = null
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)

            val result = DictionaryLookupService.lookupWord(term)
            result.onSuccess { lookup ->
                spanishTranslation = lookup.translation
                if (!lookup.exampleSentence.isNullOrBlank()) {
                    contextSentence = lookup.exampleSentence
                }
                lookupStatusMessage = "Autocompletado con éxito"
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            }.onFailure {
                lookupStatusMessage = "No se encontró significado online"
            }
            isLookingUp = false
        }
    }

    val canSave = englishWord.isNotBlank() && spanishTranslation.isNotBlank()

    fun performSave() {
        if (canSave) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            val finalSource = if (isEnteringCustomSource && customSource.isNotBlank()) {
                customSource.trim()
            } else {
                selectedSource.trim()
            }
            onSaveWord(
                Word(
                    word = englishWord.trim(),
                    translation = spanishTranslation.trim(),
                    sentence = contextSentence.trim().ifEmpty { null },
                    source = finalSource.ifEmpty { "General" }
                )
            )
            onDismiss()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 10.dp, bottom = 6.dp)
                    .width(36.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.outline)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
        ) {
            // Pinned Top Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Añadir a Léxico",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontFamily = FontFamily.Serif,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                Text(
                    text = "CANCELAR",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.0.sp
                    ),
                    modifier = Modifier.clickable { onDismiss() }
                )
            }

            // Clipboard Fast Capture chip
            if (clipboardText != null && englishWord.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .background(TerracottaAccent.copy(alpha = 0.08f))
                            .border(1.dp, TerracottaAccent.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                            .clickable {
                                englishWord = clipboardText
                                triggerAutocomplete(clipboardText)
                            }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ContentPaste,
                            contentDescription = null,
                            tint = TerracottaAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Pegar portapapeles: \"$clipboardText\"",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TerracottaAccent,
                                fontWeight = FontWeight.SemiBold
                            ),
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Scrollable fields area (automatically compact)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 24.dp)
            ) {
                // 1. English Word field (with autofocus and autocomplete trigger)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TÉRMINO EN INGLÉS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 0.8.sp
                        )
                    )

                    if (englishWord.isNotBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable(enabled = !isLookingUp) {
                                    triggerAutocomplete(englishWord)
                                }
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            if (isLookingUp) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(12.dp),
                                    strokeWidth = 1.5.dp,
                                    color = TerracottaAccent
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Buscando…",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = TerracottaAccent,
                                        fontSize = 10.sp
                                    )
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Outlined.AutoAwesome,
                                    contentDescription = null,
                                    tint = TerracottaAccent,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Autocompletar",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = TerracottaAccent,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    if (englishWord.isEmpty()) {
                        Text(
                            text = "ej. Ineffable, serendipity…",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                fontSize = 16.sp
                            )
                        )
                    }
                    BasicTextField(
                        value = englishWord,
                        onValueChange = { englishWord = it },
                        textStyle = MaterialTheme.typography.headlineMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 16.sp
                        ),
                        cursorBrush = SolidColor(TerracottaAccent),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                    )
                }

                AnimatedVisibility(visible = lookupStatusMessage != null) {
                    Text(
                        text = lookupStatusMessage.orEmpty(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (lookupStatusMessage?.startsWith("Auto") == true) ForestGreen else TerracottaAccent,
                            fontSize = 10.sp
                        ),
                        modifier = Modifier.padding(top = 4.dp, start = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 2. Spanish translation field
                Text(
                    text = "TRADUCCIÓN EN ESPAÑOL",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 0.8.sp
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    if (spanishTranslation.isEmpty()) {
                        Text(
                            text = "ej. Que no se puede explicar con palabras…",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        )
                    }
                    BasicTextField(
                        value = spanishTranslation,
                        onValueChange = { spanishTranslation = it },
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        cursorBrush = SolidColor(TerracottaAccent),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 3. Context sentence (optional)
                Text(
                    text = "FRASE DE CONTEXTO (OPCIONAL)",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 0.8.sp
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    if (contextSentence.isEmpty()) {
                        Text(
                            text = "Frase del libro o artículo donde leíste el término…",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        )
                    }
                    BasicTextField(
                        value = contextSentence,
                        onValueChange = { contextSentence = it },
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        cursorBrush = SolidColor(TerracottaAccent),
                        minLines = 2,
                        maxLines = 3,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { performSave() }),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 4. Source / Tag selection
                Text(
                    text = "ORIGEN / ETIQUETA",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 0.8.sp
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))

                val sources = (listOf("General") + availableSources).distinct()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    sources.forEach { src ->
                        EditorialBadge(
                            text = src,
                            isSelected = (!isEnteringCustomSource && selectedSource == src),
                            onClick = {
                                isEnteringCustomSource = false
                                selectedSource = src
                            }
                        )
                    }
                    EditorialBadge(
                        text = "+ NUEVO",
                        isSelected = isEnteringCustomSource,
                        onClick = {
                            isEnteringCustomSource = true
                        }
                    )
                }

                if (isEnteringCustomSource) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        if (customSource.isEmpty()) {
                            Text(
                                text = "Título del libro o revista…",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                            )
                        }
                        BasicTextField(
                            value = customSource,
                            onValueChange = { customSource = it },
                            textStyle = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            cursorBrush = SolidColor(TerracottaAccent),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { performSave() }),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            // Pinned Bottom Action Button (ALWAYS ABOVE THE KEYBOARD)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 24.dp, vertical = 10.dp)
            ) {
                EditorialButton(
                    text = "Guardar en Léxico",
                    enabled = canSave,
                    onClick = { performSave() },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}
