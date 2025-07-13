package com.codinglikeapirate.pocitaj.data

/**
 * Represents the logical arithmetic operation being performed.
 */
enum class Operation {
    ADDITION,
    SUBTRACTION,
    MULTIPLICATION,
    DIVISION;

    companion object {
        fun fromString(type: String): Operation? {
            return entries.find { it.name.equals(type, ignoreCase = true) }
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
