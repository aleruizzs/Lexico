package com.lexico.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lexico.app.data.model.MasteryStatus
import com.lexico.app.data.model.Word
import com.lexico.app.ui.theme.TerracottaAccent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordDetailBottomSheet(
    word: Word,
    onDismiss: () -> Unit,
    onDeleteWord: (Long) -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    var isConfirmingDelete by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    val scrollState = rememberScrollState()

    val isDue = word.isDueForReview

    // Humanized cognitive stability metrics
    val stabilityPct = ((word.easeFactor - 1.3f) / (2.5f - 1.3f) * 100).toInt().coerceIn(35, 98)
    val stabilityLabel = when {
        word.easeFactor >= 2.4f -> "Alta ($stabilityPct%)"
        word.easeFactor >= 1.8f -> "Media ($stabilityPct%)"
        else -> "Refuerzo ($stabilityPct%)"
    }

    val nextReviewText = if (isDue) {
        "¡Pendiente hoy!"
    } else {
        val days = maxOf(1, ((word.nextReviewDate - System.currentTimeMillis()) / (24L * 60 * 60 * 1000L)).toInt())
        "En $days ${if (days == 1) "día" else "días"}"
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
        tonalElevation = 0.dp,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .width(36.dp)
                    .height(4.dp)
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
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            // Badges row: Source and Mastery Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                EditorialBadge(
                    text = word.source.uppercase(),
                    isSelected = false
                )

                val statusLabel = when (word.status) {
                    MasteryStatus.NEW -> "• NUEVA"
                    MasteryStatus.LEARNING -> "• APRENDIENDO"
                    MasteryStatus.MASTERED -> "• DOMINADA"
                }
                Text(
                    text = statusLabel,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        letterSpacing = 0.8.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main Word
            Text(
                text = word.word,
                style = MaterialTheme.typography.displayMedium.copy(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Translation
            Text(
                text = word.translation,
                style = MaterialTheme.typography.titleMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 22.sp
                )
            )

            Spacer(modifier = Modifier.height(20.dp))
            HairlineDivider()
            Spacer(modifier = Modifier.height(20.dp))

            // Context Sentence if available
            if (!word.sentence.isNullOrBlank()) {
                Text(
                    text = "CONTEXTO EDITORIAL",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 1.0.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            text = "“${word.sentence}”",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontStyle = FontStyle.Italic,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 22.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "— Fuente: ${word.source}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Humanized Retention & Memory Status (No robotic code jargon)
            Text(
                text = "ESTADO DE RETENCIÓN EN MEMORIA",
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 1.0.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MetricColumn(
                            title = "ESTABILIDAD EN MEMORIA",
                            value = stabilityLabel
                        )
                        MetricColumn(
                            title = "PRÓXIMO REPASO",
                            value = nextReviewText
                        )
                    }

                    HairlineDivider()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MetricColumn(
                            title = "FRECUENCIA DE REPASO",
                            value = "Cada ${word.interval} ${if (word.interval == 1) "día" else "días"}"
                        )
                        MetricColumn(
                            title = "SESIONES SUPERADAS",
                            value = "${word.repetitions} ${if (word.repetitions == 1) "repaso" else "repasos"}"
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Actions
            if (!isConfirmingDelete) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    EditorialButton(
                        text = "ELIMINAR TÉRMINO",
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            isConfirmingDelete = true
                        },
                        isPrimary = false,
                        modifier = Modifier.weight(1f)
                    )

                    EditorialButton(
                        text = "CERRAR",
                        onClick = onDismiss,
                        isPrimary = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.dp, TerracottaAccent, RoundedCornerShape(4.dp))
                        .padding(16.dp)
                ) {
                    Text(
                        text = "¿Deseas eliminar este término de tu biblioteca?",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Se perderá todo el historial de retención y métricas de repetición espaciada.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        EditorialButton(
                            text = "CANCELAR",
                            onClick = { isConfirmingDelete = false },
                            isPrimary = false,
                            modifier = Modifier.weight(1f)
                        )

                        EditorialButton(
                            text = "SÍ, ELIMINAR",
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onDeleteWord(word.id)
                                onDismiss()
                            },
                            isPrimary = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricColumn(
    title: String,
    value: String
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                letterSpacing = 0.8.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        )
    }
}
