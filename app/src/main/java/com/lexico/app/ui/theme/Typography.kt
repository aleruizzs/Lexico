package com.lexico.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.lexico.app.R

// Real packaged fonts: Newsreader (Editorial Serif) and Inter (Clean Sans-Serif)
val EditorialSerif = FontFamily(
    Font(resId = R.font.newsreader_regular, weight = FontWeight.Normal, style = FontStyle.Normal),
    Font(resId = R.font.newsreader_regular, weight = FontWeight.Medium, style = FontStyle.Normal),
    Font(resId = R.font.newsreader_regular, weight = FontWeight.SemiBold, style = FontStyle.Normal),
    Font(resId = R.font.newsreader_regular, weight = FontWeight.Bold, style = FontStyle.Normal),
    Font(resId = R.font.newsreader_italic, weight = FontWeight.Normal, style = FontStyle.Italic),
    Font(resId = R.font.newsreader_italic, weight = FontWeight.Medium, style = FontStyle.Italic)
)

val CleanSans = FontFamily(
    Font(resId = R.font.inter_regular, weight = FontWeight.Normal, style = FontStyle.Normal),
    Font(resId = R.font.inter_regular, weight = FontWeight.Medium, style = FontStyle.Normal),
    Font(resId = R.font.inter_regular, weight = FontWeight.SemiBold, style = FontStyle.Normal),
    Font(resId = R.font.inter_regular, weight = FontWeight.Bold, style = FontStyle.Normal),
    Font(resId = R.font.inter_italic, weight = FontWeight.Normal, style = FontStyle.Italic)
)

val EditorialTypography = Typography(
    // Editorial Serif for headers and terms
    displayLarge = TextStyle(
        fontFamily = EditorialSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 36.sp,
        lineHeight = 42.sp,
        letterSpacing = (-0.5).sp
    ),
    displayMedium = TextStyle(
        fontFamily = EditorialSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        letterSpacing = (-0.25).sp
    ),
    headlineLarge = TextStyle(
        fontFamily = EditorialSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp,
        lineHeight = 30.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = EditorialSerif,
        fontStyle = FontStyle.Italic,
        fontWeight = FontWeight.Normal,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontFamily = EditorialSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    ),

    // Clean Sans-Serif for translations, labels, metadata, and controls
    titleMedium = TextStyle(
        fontFamily = CleanSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    titleSmall = TextStyle(
        fontFamily = CleanSans,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = CleanSans,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.2.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = CleanSans,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.2.sp
    ),
    bodySmall = TextStyle(
        fontFamily = CleanSans,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.3.sp
    ),
    labelLarge = TextStyle(
        fontFamily = CleanSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelMedium = TextStyle(
        fontFamily = CleanSans,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.8.sp
    ),
    labelSmall = TextStyle(
        fontFamily = CleanSans,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        letterSpacing = 1.0.sp
    )
)
