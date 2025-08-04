package dev.aidistillery.pocitaj.logic

import dev.aidistillery.pocitaj.data.Operation

enum class SpeedBadge {
    NONE,
    BRONZE,
    SILVER,
    GOLD
}

fun getSpeedBadge(operation: Operation, op1: Int, op2: Int, avgDurationMs: Long): SpeedBadge {
    if (avgDurationMs == 0L) return SpeedBadge.NONE

    val threshold = getSpeedThreshold(operation, op1, op2)
    val ratio = avgDurationMs.toFloat() / threshold.toFloat()

    return when {
        ratio <= 0.5f -> SpeedBadge.GOLD
        ratio <= 0.7f -> SpeedBadge.SILVER
        ratio <= 1.0f -> SpeedBadge.BRONZE
        else -> SpeedBadge.NONE
    }
}

private fun getSpeedThreshold(operation: Operation, op1: Int, op2: Int): Long {
    return when (operation) {
        Operation.ADDITION -> when {
            op1 < 10 && op2 < 10 && op1 + op2 < 10 -> 2500L // Single digit, no carry
            op1 < 10 && op2 < 10 -> 4000L // Single digit, with carry
            op1 < 100 && op2 < 100 && (op1 % 10) + (op2 % 10) < 10 -> 7000L // Double digit, no carry
            else -> 10000L // Double digit, with carry
        }

        Operation.SUBTRACTION -> when {
            op1 < 10 && op2 < 10 -> 3000L // Single digit
            op1 < 100 && op2 < 100 && (op1 % 10) >= (op2 % 10) -> 7500L // Double digit, no borrow
            else -> 10500L // Double digit, with borrow
        }

        Operation.MULTIPLICATION -> when {
            op1 <= 10 && op2 <= 10 -> 3000L // Standard times table
            op1 <= 12 || op2 <= 12 -> 5000L // Extended times table
            else -> 12000L // Larger numbers
        }

        Operation.DIVISION -> 4000L // Division is generally a bit slower
    }
}
