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

    private fun updateWorkingSet() {
        val allFacts = level.getAllPossibleFactIds()
        val sortedFacts = allFacts.sortedBy { factId ->
            val (op1, op2) = parseFactId(factId) ?: return@sortedBy 0

            val prefix = if (level.operation == Operation.ADDITION) "ADD" else "SUB"
            // Decompose
            val o1 = op1 % 10
            val o2 = op2 % 10
            val t1 = op1 / 10
            val t2 = op2 / 10

            // For subtraction, logic in Level was:
            // "if Regrouping (Borrow): o1 < o2. Fact is HELD as 1{o1}, e.g. 14-7"
            // "Fact stores effective tens: SUB_TENS_{effT1}_{t2}"
            // This suggests I should match that logic if I want to use 'transient' mastery effectively.
            // But since it's transient, consistency is key.
            // Let's use a simpler consistent mapping for now:

            // TODO: These should also get closer to the new equation strings.
            // e.g.: "3 + 4" and "20 + 50" ?
            val onesFactId = "${prefix}_ONES_${o1}_${o2}"
            val tensFactId = "${prefix}_TENS_${t1}_${t2}"
            
            (getMastery(onesFactId).strength + getMastery(tensFactId).strength) / 2
        }
        workingSet.clear()
        workingSet.addAll(sortedFacts.take(workingSetSize))
    }

    private fun parseFactId(factId: String): Pair<Int, Int>? {
        // "14 + 23 = ?" or "34 - 12 = ?"
        // TODO: Simplify to a single regexp? Or is the operation check useful?
        return if (level.operation == Operation.ADDITION) {
            val match = Regex("""(\d+) \+ (\d+) = \?""").matchEntire(factId)
            match?.destructured?.let { (a, b) -> a.toInt() to b.toInt() }
        } else {
            val match = Regex("""(\d+) - (\d+) = \?""").matchEntire(factId)
            match?.destructured?.let { (a, b) -> a.toInt() to b.toInt() }
        }
    }

    override fun getNextExercise(): Exercise? {
        if (workingSet.isEmpty()) return null

        val factId = workingSet.removeAt(0)
        workingSet.add(factId) // Move to the end of the queue

        val (op1, op2) = parseFactId(factId) ?: return null

        val equation = TwoDigitEquation(op1, op2, level.operation, factId)
        return Exercise(equation)
    }

    override fun recordAttempt(exercise: Exercise, wasCorrect: Boolean): Pair<FactMastery?, String> {
        val factId = (exercise.equation as TwoDigitEquation).getFactId()
        val (op1, op2) = parseFactId(factId) ?: return null to level.id

        // TODO: Old style prefix - update
        val prefix = if (level.operation == Operation.ADDITION) "ADD" else "SUB"
        val o1 = op1 % 10
        val o2 = op2 % 10
        val t1 = op1 / 10
        val t2 = op2 / 10

        val onesFactId = "${prefix}_ONES_${o1}_${o2}"
        val tensFactId = "${prefix}_TENS_${t1}_${t2}"

        val onesMastery = recordInternal(onesFactId, exercise, wasCorrect)
        val tensMastery = recordInternal(tensFactId, exercise, wasCorrect)

        // Also record the semantic fact mastery
        // TODO: We need to calculate mastery of this level correctly.
        // Currently we store mastery for each individual fact,
        // ignoring that we treat tens and ones separately.
        val semanticMastery = recordInternal(factId, exercise, wasCorrect)

        userMastery[onesFactId] = onesMastery
        userMastery[tensFactId] = tensMastery
        userMastery[factId] = semanticMastery

        // If the combined mastery is high enough, replace it in the working set.
        if ((onesMastery.strength + tensMastery.strength) / 2 >= 4) {
            workingSet.remove(factId)
            updateWorkingSet() // Re-sort and fill
        }

        // Return the SEMANTIC mastery to be saved in DB
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
