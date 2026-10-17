package com.lexico.app.ui.screens

import android.Manifest
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.lexico.app.notification.NotificationScheduler
import com.lexico.app.ui.components.EditorialBadge
import com.lexico.app.ui.components.EditorialButton
import com.lexico.app.ui.components.HairlineDivider
import com.lexico.app.ui.theme.AppThemeMode
import com.lexico.app.ui.theme.TerracottaAccent

@Composable
fun SettingsScreen(
    currentThemeMode: AppThemeMode,
    onSelectThemeMode: (AppThemeMode) -> Unit,
    currentReminderHour: Int,
    currentReminderMinute: Int,
    onSaveReminderTime: (hour: Int, minute: Int) -> Unit,
    onResetSeedData: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    var selectedHour by remember(currentReminderHour) { mutableIntStateOf(currentReminderHour) }
    var selectedMinute by remember(currentReminderMinute) { mutableIntStateOf(currentReminderMinute) }
    var testNotificationSent by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            NotificationScheduler.showDailyReminderNotification(context)
            testNotificationSent = true
        }
    }

    fun openNativeTimePicker() {
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                selectedHour = hourOfDay
                selectedMinute = minute
                onSaveReminderTime(hourOfDay, minute)
                NotificationScheduler.scheduleDailyReminder(context, hourOfDay, minute)
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            },
            selectedHour,
            selectedMinute,
            true
        ).show()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Top bar with clean, unorphaned title
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.size(42.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Volver",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Ajustes",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontFamily = FontFamily.Serif,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        HairlineDivider()
        Spacer(modifier = Modifier.height(24.dp))

        // Section 0: Theme Mode (No raw hex codes!)
        Text(
            text = "ESTÉTICA Y TEMA VISUAL",
            style = MaterialTheme.typography.labelSmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.0.sp
            )
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Elige entre la paleta de tinta suiza sobre papel cálido o el modo oscuro editorial en carbón.",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            EditorialBadge(
                text = "SISTEMA",
                isSelected = currentThemeMode == AppThemeMode.SYSTEM,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onSelectThemeMode(AppThemeMode.SYSTEM)
                }
            )
            EditorialBadge(
                text = "PAPEL CÁLIDO",
                isSelected = currentThemeMode == AppThemeMode.LIGHT,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onSelectThemeMode(AppThemeMode.LIGHT)
                }
            )
            EditorialBadge(
                text = "TINTA CARBÓN",
                isSelected = currentThemeMode == AppThemeMode.DARK,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onSelectThemeMode(AppThemeMode.DARK)
                }
            )
        }

        Spacer(modifier = Modifier.height(28.dp))
        HairlineDivider()
        Spacer(modifier = Modifier.height(28.dp))

        // Section 1: Daily Reminder with Clean Native Time Picker
        Text(
            text = "RECORDATORIO DIARIO INTELIGENTE",
            style = MaterialTheme.typography.labelSmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.0.sp
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Léxico programa una alarma en tu dispositivo para avisarte si tienes términos pendientes de repasar y mantener tu racha viva.",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Time Picker Card (Clickable to open native TimePickerDialog)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
                .clickable { openNativeTimePicker() }
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "HORA PROGRAMADA",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 0.8.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = String.format("%02d:%02d", selectedHour, selectedMinute),
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontFamily = FontFamily.Serif,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .border(1.dp, TerracottaAccent, RoundedCornerShape(4.dp))
                        .background(TerracottaAccent.copy(alpha = 0.08f))
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.AccessTime,
                            contentDescription = null,
                            tint = TerracottaAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "CAMBIAR HORA",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TerracottaAccent,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Test Notification Button
        EditorialButton(
            text = if (testNotificationSent) "Notificación enviada al dispositivo" else "Probar notificación ahora",
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val hasPermission = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
                    if (!hasPermission) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        NotificationScheduler.showDailyReminderNotification(context)
                        testNotificationSent = true
                    }
                } else {
                    NotificationScheduler.showDailyReminderNotification(context)
                    testNotificationSent = true
                }
            },
            isPrimary = false,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))
        HairlineDivider()
        Spacer(modifier = Modifier.height(28.dp))

        // Section 2: Data Management
        Text(
            text = "DATOS Y BIBLIOTECA",
            style = MaterialTheme.typography.labelSmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.0.sp
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Puedes recargar los 8 términos literarios y cinematográficos de muestra (Dune, Succession, 1984, etc.) en cualquier momento.",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        EditorialButton(
            text = "Recargar datos de muestra",
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onResetSeedData()
            },
            isPrimary = false,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))
        HairlineDivider()
        Spacer(modifier = Modifier.height(28.dp))

        // Section 3: Architecture & Manifesto
        Text(
            text = "DIRECCIÓN DE DISEÑO Y CIENCIA",
            style = MaterialTheme.typography.labelSmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.0.sp
            )
        )
        Spacer(modifier = Modifier.height(12.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
                .padding(16.dp)
        ) {
            Text(
                text = "Manifiesto Anti-Vibecoding",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontFamily = FontFamily.Serif,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Inspirado en el diseño tipográfico suizo y herramientas editoriales de autor (Things 3, Readwise Reader, iA Writer). Sin degradados de neón, sin emojis saturados y con el algoritmo SM-2 de repetición espaciada en la base de datos local SQLite.",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
