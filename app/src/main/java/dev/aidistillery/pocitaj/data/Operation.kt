package dev.aidistillery.pocitaj.data

/**
 * Represents the logical arithmetic operation being performed.
 */
enum class Operation {
    ADDITION,
    SUBTRACTION,
    MULTIPLICATION,
    DIVISION;

    // Keep for symmetry
    @Suppress("unused")
    companion object {
        fun fromString(type: String): Operation? {
            return entries.find { it.name.equals(type, ignoreCase = true) }
        }

        fun fromSymbol(symbol: String): Operation? {
            return Operation.entries.find { it.toSymbol() == symbol }
        }
    }
}

fun Operation.toSymbol(): String {
    return when (this) {
        Operation.ADDITION -> "+"
        Operation.SUBTRACTION -> "-"
        Operation.MULTIPLICATION -> "×"
        Operation.DIVISION -> "÷"
    }
}

