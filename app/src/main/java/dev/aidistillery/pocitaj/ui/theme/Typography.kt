package dev.aidistillery.pocitaj.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp

/**
 * Custom typography for the Pocitaj app.
 * These styles are mapped to the MaterialTheme typography scale in Theme.kt.
 */
object PocitajTypography {
    val exerciseLabelStyle = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 64.sp,
        lineHeight = 72.sp,
        textAlign = TextAlign.Center
    )
    val screenTitle = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    )

    val operationSymbol = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 48.sp,
        lineHeight = 56.sp,
        letterSpacing = 0.sp
    )

    val operationTitle = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    )

    val levelButtonLabel = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 48.sp,
        lineHeight = 56.sp,
        letterSpacing = 0.5.sp
    )
}

// Default Material 3 typography, customized with our styles
val AppTypography = Typography(
    displayLarge = PocitajTypography.exerciseLabelStyle,
    displayMedium = PocitajTypography.operationSymbol,
    headlineLarge = PocitajTypography.screenTitle,
    headlineMedium = PocitajTypography.levelButtonLabel,
    headlineSmall = PocitajTypography.operationTitle,

    // Default text style if none is specified
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
)
