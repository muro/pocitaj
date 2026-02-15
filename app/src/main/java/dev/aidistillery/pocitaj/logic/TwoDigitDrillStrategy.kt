package dev.aidistillery.pocitaj.logic

import dev.aidistillery.pocitaj.data.FactMastery
import dev.aidistillery.pocitaj.data.Operation
import kotlin.time.Clock

class TwoDigitDrillStrategy(
    private val level: Level,
    private val userMastery: MutableMap<String, FactMastery>,
    private val workingSetSize: Int = 4,
    private val activeUserId: Long,
    private val clock: Clock = Clock.System
) : ExerciseProvider {

    internal val workingSet = mutableListOf<String>()

    init {
        updateWorkingSet()
    }

    private fun getMastery(factId: String): FactMastery {
        return userMastery[factId] ?: FactMastery(factId, activeUserId, "", 3, 0)
    }


    private fun getComponentFactIds(op1: Int, op2: Int): Pair<String, String> {
        val prefix = if (level.operation == Operation.ADDITION) "ADD" else "SUB"
        val o1 = op1 % 10
        val o2 = op2 % 10
        val t1 = op1 / 10
        val t2 = op2 / 10
        return "${prefix}_ONES_${o1}_${o2}" to "${prefix}_TENS_${t1}_${t2}"
    }

    private fun updateWorkingSet() {
        // Helper to find weakest facts
        fun getWeakest(facts: List<String>, count: Int): List<String> = facts
            .filter { getMastery(it).strength < 4 }
            .ifEmpty { facts }
            .sortedBy { getMastery(it).strength }
            .take(count)

        val allFacts = level.getAllPossibleFactIds()
        val onesFacts = allFacts.filter { it.contains("_ONES_") }
        val tensFacts = allFacts.filter { it.contains("_TENS_") }

        if (onesFacts.all { getMastery(it).strength >= 4 } &&
            tensFacts.all { getMastery(it).strength >= 4 }) {
            workingSet.clear()
            return
        }

        val pickCount = kotlin.math.sqrt(workingSetSize.toDouble()).toInt().coerceAtLeast(1)
        val weakOnes = getWeakest(onesFacts, pickCount)
        val weakTens = getWeakest(tensFacts, pickCount)

        workingSet.clear()
        // Cartesian product
        for (ones in weakOnes) {
            for (tens in weakTens) {
                // Parse "PREFIX_TYPE_A_B"
                val oParts = ones.split("_")
                val tParts = tens.split("_")

                val op1 = tParts[2].toInt() * 10 + oParts[2].toInt()
                val op2 = tParts[3].toInt() * 10 + oParts[3].toInt()

                val symbol = if (level.operation == Operation.ADDITION) "+" else "-"
                workingSet.add("$op1 $symbol $op2 = ?")
            }
        }
    }

    private fun parseFactId(factId: String): Pair<Int, Int>? {
        val parts = factId.split(" ")
        return if (parts.size >= 3) parts[0].toInt() to parts[2].toInt() else null
    }

    override fun getNextExercise(): Exercise? {
        if (workingSet.isEmpty()) return null
        val factId = workingSet.removeAt(0).also { workingSet.add(it) }
        val (op1, op2) = parseFactId(factId) ?: return null
        return Exercise(TwoDigitEquation(level.operation, op1, op2, factId))
    }

    override fun recordAttempt(exercise: Exercise, wasCorrect: Boolean): Pair<FactMastery?, String> {
        val factId = (exercise.equation as TwoDigitEquation).getFactId()
        val (op1, op2) = parseFactId(factId) ?: return null to level.id

        val (onesFactId, tensFactId) = getComponentFactIds(op1, op2)

        val onesMastery = recordInternal(onesFactId, exercise, wasCorrect)
        val tensMastery = recordInternal(tensFactId, exercise, wasCorrect)
        val semanticMastery = recordInternal(factId, exercise, wasCorrect)

        userMastery[onesFactId] = onesMastery
        userMastery[tensFactId] = tensMastery
        userMastery[factId] = semanticMastery

        if ((onesMastery.strength + tensMastery.strength) / 2 >= 4) {
            workingSet.remove(factId)
            updateWorkingSet()
        }

        return semanticMastery to level.id
    }

    private fun recordInternal(factId: String, exercise: Exercise, wasCorrect: Boolean): FactMastery {
        val mastery = getMastery(factId)
        val duration = exercise.timeTakenMillis?.toLong() ?: 0L

        return SpacedRepetitionSystem.updateMastery(
            currentMastery = mastery,
            wasCorrect = wasCorrect,
            durationMs = duration,
            speedBadge = exercise.speedBadge,
            clock = clock
        )
    }
}
