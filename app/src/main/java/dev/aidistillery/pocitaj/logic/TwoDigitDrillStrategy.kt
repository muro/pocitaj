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
            val parts = factId.split("_")
            // Format: PREFIX_ONES_o1_o2_PREFIX_TENS_t1_t2
            // e.g. ADD_ONES_3_4_ADD_TENS_1_2
            // parts[0] is ADD/SUB. parts[1] is ONES.
            val prefix = parts[0] // ADD or SUB
            val onesFactId = "${prefix}_ONES_${parts[2]}_${parts[3]}"
            val tensFactId = "${prefix}_TENS_${parts[6]}_${parts[7]}"
            (getMastery(onesFactId).strength + getMastery(tensFactId).strength) / 2
        }
        workingSet.clear()
        workingSet.addAll(sortedFacts.take(workingSetSize))
    }

    override fun getNextExercise(): Exercise? {
        if (workingSet.isEmpty()) return null

        val factId = workingSet.removeAt(0)
        workingSet.add(factId) // Move to the end of the queue

        val parts = factId.split("_")
        val isAddition = parts[0] == "ADD"

        val op1OnesOrTeens = parts[2].toInt()
        val op2Ones = parts[3].toInt()
        val op1TensEffective = parts[6].toInt()
        val op2Tens = parts[7].toInt()

        val op2 = op2Tens * 10 + op2Ones

        val op1: Int
        if (op1OnesOrTeens >= 10 && !isAddition) { // Subtraction Borrow Case
            // 14 means ones digit was 4, borrowed from tens
            val onesDigit = op1OnesOrTeens - 10
            val originalTens = op1TensEffective + 1
            op1 = originalTens * 10 + onesDigit
        } else {
            // Normal case (Addition or Subtraction No Borrow)
            val onesDigit = op1OnesOrTeens
            val originalTens = op1TensEffective
            op1 = originalTens * 10 + onesDigit
        }

        val operation = if (isAddition) Operation.ADDITION else Operation.SUBTRACTION
        val equation = TwoDigitEquation(op1, op2, operation, factId)

        return Exercise(equation)
    }

    override fun recordAttempt(exercise: Exercise, wasCorrect: Boolean): Pair<FactMastery?, String> {
        val factId = (exercise.equation as TwoDigitEquation).getFactId()
        
        val parts = factId.split("_")
        val prefix = parts[0] // ADD or SUB
        val onesFactId = "${prefix}_ONES_${parts[2]}_${parts[3]}"
        val tensFactId = "${prefix}_TENS_${parts[6]}_${parts[7]}"

        val onesMastery = recordInternal(onesFactId, exercise, wasCorrect)
        val tensMastery = recordInternal(tensFactId, exercise, wasCorrect)

        userMastery[onesFactId] = onesMastery
        userMastery[tensFactId] = tensMastery

        // If the combined mastery is high enough, replace it in the working set.
        if ((onesMastery.strength + tensMastery.strength) / 2 >= 4) {
            workingSet.remove(factId)
            updateWorkingSet() // Re-sort and fill
        }
        return tensMastery to level.id
    }

    private fun recordInternal(factId: String, exercise: Exercise, wasCorrect: Boolean): FactMastery {
        val mastery = getMastery(factId)
        val now = clock.now().toEpochMilliseconds()
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
