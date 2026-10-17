package com.lexico.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lexico.app.ui.components.EditorialButton
import com.lexico.app.ui.components.HairlineDivider
import com.lexico.app.ui.theme.ForestGreen
import com.lexico.app.ui.theme.MutedRed
import com.lexico.app.ui.theme.TerracottaAccent
import com.lexico.app.ui.viewmodel.PracticedWordSummary

@Composable
fun SessionSummaryScreen(
    items: List<PracticedWordSummary>,
    accuracy: Int,
    currentStreak: Int,
    onReturnHome: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 20.dp)
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(TerracottaAccent)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "CONSOLIDACIÓN COMPLETADA",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 1.2.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Resumen de la Sesión",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontFamily = FontFamily.Serif,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Tus intervalos de memoria han sido reprogramados según el rigor cognitivo del algoritmo.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Three Metrics Row: Count, Accuracy %, Streak
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(6.dp))
                        .padding(vertical = 16.dp, horizontal = 12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SummaryMetricItem(
                        number = "${items.size}",
                        label = "TÉRMINOS",
                        valueColor = MaterialTheme.colorScheme.onSurface
                    )

                    MetricVerticalDivider()

                    SummaryMetricItem(
                        number = "$accuracy%",
                        label = "PRECISIÓN",
                        valueColor = if (accuracy >= 80) ForestGreen else TerracottaAccent
                    )

                    MetricVerticalDivider()

                    SummaryMetricItem(
                        number = "$currentStreak D",
                        label = "RACHA",
                        valueColor = TerracottaAccent
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                Text(
                    text = "DESGLOSE DE PALABRAS PRACTICADAS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 1.0.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))
                HairlineDivider()
            }

            items(items) { summary ->
                PracticedWordRow(summary = summary)
                HairlineDivider()
            }
        }

        // Pinned Bottom Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 24.dp, vertical = 14.dp)
        ) {
            EditorialButton(
                text = "Volver a la Biblioteca",
                onClick = onReturnHome,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun SummaryMetricItem(
    number: String,
    label: String,
    valueColor: androidx.compose.ui.graphics.Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = number,
            style = MaterialTheme.typography.headlineLarge.copy(
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                color = valueColor
            )
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 9.sp,
                letterSpacing = 0.8.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }
}

@Composable
private fun MetricVerticalDivider() {
    Box(
        modifier = Modifier
            .size(width = 1.dp, height = 36.dp)
            .background(MaterialTheme.colorScheme.outline)
    )
}

@Composable
private fun PracticedWordRow(summary: PracticedWordSummary) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = summary.word.word,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = summary.word.translation,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        if (summary.willReviewTomorrow) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(MutedRed.copy(alpha = 0.1f))
                    .border(1.dp, MutedRed.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "REPASO MAÑANA (1D)",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MutedRed,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(ForestGreen.copy(alpha = 0.1f))
                    .border(1.dp, ForestGreen.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "INTERVALO ${summary.updatedWord.interval} DÍAS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = ForestGreen,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}
