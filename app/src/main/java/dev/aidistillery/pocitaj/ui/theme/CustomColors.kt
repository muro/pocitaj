package dev.aidistillery.pocitaj.ui.theme

import androidx.compose.ui.graphics.Color

data class CustomColors(
    val backgroundGradientStart: Color,
    val backgroundGradientEnd: Color,
    val paperGradientStart: Color,
    val paperGradientEnd: Color,
    val additionGradientStart: Color = Color(0xFFF9A825),
    val additionGradientEnd: Color = Color(0xFFFDD835),
    val subtractionGradientStart: Color = Color(0xFF29B6F6),
    val subtractionGradientEnd: Color = Color(0xFF26A69A),
    val multiplicationGradientStart: Color = Color(0xFFAB47BC),
    val multiplicationGradientEnd: Color = Color(0xFFEC407A),
    val divisionGradientStart: Color = Color(0xFF66BB6A),
    val divisionGradientEnd: Color = Color(0xFF9CCC65),
    val factMastered: Color = Color(0xFF4CAF50),
    val factLearning: Color = Color(0xFFFFEB3B),
    val factWeak: Color = Color(0xFFF44336),
    val factNotAttempted: Color = Color(0xFFE0E0E0),
    val speedBadgeBronze: Color = Color(0xFFCD7F32),
    val speedBadgeSilver: Color = Color(0xFFC0C0C0),
    val speedBadgeGold: Color = Color(0xFFFFD700)
)

val lightCustomColors = CustomColors(
    backgroundGradientStart = Color(0xFFFFF9EE),
    backgroundGradientEnd = Color(0xFFFDFCF5),
    paperGradientStart = Color(0xFFFAF8F2),
    paperGradientEnd = Color(0xFFF0EDE4)
)

val darkCustomColors = CustomColors(
    backgroundGradientStart = Color(0xFF1E1B13),
    backgroundGradientEnd = Color(0xFF222017),
    paperGradientStart = Color(0xFF3A3A3A),
    paperGradientEnd = Color(0xFF303030)
)
