package com.lexico.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = TerracottaAccent,
    onPrimary = PaperBackground,
    primaryContainer = TerracottaSubtle,
    onPrimaryContainer = TerracottaAccent,
    secondary = CharcoalInk,
    onSecondary = PaperBackground,
    secondaryContainer = StoneSurfaceElevated,
    onSecondaryContainer = CharcoalInk,
    tertiary = ForestGreen,
    onTertiary = PaperBackground,
    background = PaperBackground,
    onBackground = CharcoalInk,
    surface = PaperBackground,
    onSurface = CharcoalInk,
    surfaceVariant = StoneSurface,
    onSurfaceVariant = WarmGray,
    outline = HairlineDivider,
    outlineVariant = HairlineSubtle
)

private val DarkColorScheme = darkColorScheme(
    primary = TerracottaAccent,
    onPrimary = BoneText,
    primaryContainer = TerracottaSubtle,
    onPrimaryContainer = BoneText,
    secondary = BoneText,
    onSecondary = DarkBackground,
    secondaryContainer = DarkSurfaceElevated,
    onSecondaryContainer = BoneText,
    tertiary = ForestGreen,
    onTertiary = BoneText,
    background = DarkBackground,
    onBackground = BoneText,
    surface = DarkBackground,
    onSurface = BoneText,
    surfaceVariant = DarkSurface,
    onSurfaceVariant = MutedGray,
    outline = DarkHairlineDivider,
    outlineVariant = DarkHairlineSubtle
)

enum class AppThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

@Composable
fun LexicoTheme(
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()
    val darkTheme = when (themeMode) {
        AppThemeMode.SYSTEM -> isSystemDark
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
    }

    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = EditorialTypography,
        content = content
    )
}
