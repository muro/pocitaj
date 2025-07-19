package com.codinglikeapirate.pocitaj.ui.theme

import androidx.compose.ui.graphics.Color

data class CustomColors(
    val additionGradientStart: Color,
    val additionGradientEnd: Color,
    val subtractionGradientStart: Color,
    val subtractionGradientEnd: Color,
    val multiplicationGradientStart: Color,
    val multiplicationGradientEnd: Color,
    val divisionGradientStart: Color,
    val divisionGradientEnd: Color,
    val factMastered: Color,
    val factLearning: Color,
    val factWeak: Color,
    val factNotAttempted: Color,
    val speedBadgeBronze: Color,
    val speedBadgeSilver: Color,
    val speedBadgeGold: Color
)

val lightCustomColors = CustomColors(
    additionGradientStart = additionGradientStartLight,
    additionGradientEnd = additionGradientEndLight,
    subtractionGradientStart = subtractionGradientStartLight,
    subtractionGradientEnd = subtractionGradientEndLight,
    multiplicationGradientStart = multiplicationGradientStartLight,
    multiplicationGradientEnd = multiplicationGradientEndLight,
    divisionGradientStart = divisionGradientStartLight,
    divisionGradientEnd = divisionGradientEndLight,
    factMastered = factMasteredLight,
    factLearning = factLearningLight,
    factWeak = factWeakLight,
    factNotAttempted = factNotAttemptedLight,
    speedBadgeBronze = speedBadgeBronzeLight,
    speedBadgeSilver = speedBadgeSilverLight,
    speedBadgeGold = speedBadgeGoldLight
)

val darkCustomColors = CustomColors(
    additionGradientStart = additionGradientStartDark,
    additionGradientEnd = additionGradientEndDark,
    subtractionGradientStart = subtractionGradientStartDark,
    subtractionGradientEnd = subtractionGradientEndDark,
    multiplicationGradientStart = multiplicationGradientStartDark,
    multiplicationGradientEnd = multiplicationGradientEndDark,
    divisionGradientStart = divisionGradientStartDark,
    divisionGradientEnd = divisionGradientEndDark,
    factMastered = factMasteredDark,
    factLearning = factLearningDark,
    factWeak = factWeakDark,
    factNotAttempted = factNotAttemptedDark,
    speedBadgeBronze = speedBadgeBronzeDark,
    speedBadgeSilver = speedBadgeSilverDark,
    speedBadgeGold = speedBadgeGoldDark
)
