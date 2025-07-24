package com.codinglikeapirate.pocitaj.logic

data class LevelRepresentation(
    val shortLabel: String,
    val icon: Int? = null // Using Int for drawable resource ID
)

fun formatLevel(level: Level): LevelRepresentation {
    return when (level.id) {
        // Addition
        "ADD_SUM_5" -> LevelRepresentation("2+3")
        "ADD_SUM_10" -> LevelRepresentation("7+2")
        "ADD_SUM_20" -> LevelRepresentation("8+9")
        "ADD_DOUBLES" -> LevelRepresentation("🐾+🐾")
        "ADD_NEAR_DOUBLES" -> LevelRepresentation("🐾🐾.")
        "ADD_MAKING_10S" -> LevelRepresentation("7❤️3")
        "ADD_TENS" -> LevelRepresentation("20+30")
        "ADD_TWO_DIGIT_NO_CARRY" -> LevelRepresentation("23+45")
        "ADD_TWO_DIGIT_CARRY" -> LevelRepresentation("28+45")

        // Subtraction
        "SUB_FROM_5" -> LevelRepresentation("5-2")
        "SUB_FROM_10" -> LevelRepresentation("9-4")
        "SUB_FROM_20" -> LevelRepresentation("17-8")
        "SUB_TENS" -> LevelRepresentation("70-20")
        "SUB_TWO_DIGIT_NO_BORROW" -> LevelRepresentation("75-23")
        "SUB_TWO_DIGIT_BORROW" -> LevelRepresentation("72-28")

        // Dynamic and Review Levels
        else -> when {
            level.id.contains("REVIEW") -> when {
                level.id.endsWith("_HARD") -> LevelRepresentation("🧶🧶🧶")
                level.id.endsWith("_MEDIUM") -> LevelRepresentation("🧶🧶")
                else -> LevelRepresentation("🧶")
            }
            level.id.startsWith("MUL_TABLE_") -> {
                val table = level.id.removePrefix("MUL_TABLE_")
                LevelRepresentation("x$table")
            }
            level.id.startsWith("DIV_BY_") -> {
                val divisor = level.id.removePrefix("DIV_BY_")
                LevelRepresentation("÷$divisor")
            }
            else -> LevelRepresentation(level.id) // Fallback
        }
    }
}
