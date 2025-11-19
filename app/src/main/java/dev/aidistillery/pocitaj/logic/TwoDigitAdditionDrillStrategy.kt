package dev.aidistillery.pocitaj.logic

import dev.aidistillery.pocitaj.data.FactMastery
import kotlin.time.Clock

class TwoDigitAdditionDrillStrategy(
    private val level: TwoDigitAdditionLevel,
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
        val sortedFacts = allFacts.sortedBy {
            val parts = it.split("_")
            val onesFactId = "ADD_ONES_${parts[2]}_${parts[3]}"
            val tensFactId = "ADD_TENS_${parts[6]}_${parts[7]}"
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
        val op1Ones = parts[2].toInt()
        val op2Ones = parts[3].toInt()
        val op1Tens = parts[6].toInt()
        val op2Tens = parts[7].toInt()

        val op1 = op1Tens * 10 + op1Ones
        val op2 = op2Tens * 10 + op2Ones

        return Exercise(TwoDigitAddition(op1, op2, factId))
    }

    override fun recordAttempt(exercise: Exercise, wasCorrect: Boolean): Pair<FactMastery?, String> {
        val factId = (exercise.equation as TwoDigitAddition).getFactId()
        val parts = factId.split("_")
        val onesFactId = "ADD_ONES_${parts[2]}_${parts[3]}"
        val tensFactId = "ADD_TENS_${parts[6]}_${parts[7]}"

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

        val newAvgDuration = if (mastery.avgDurationMs > 0) {
            (mastery.avgDurationMs * 0.8 + duration * 0.2).toLong()
        } else {
            duration
        }

        val newStrength = if (wasCorrect) {
            (mastery.strength + 1).coerceAtMost(5)
        } else {
            (mastery.strength - 1).coerceAtLeast(0)
        }

        return mastery.copy(
            strength = newStrength,
            lastTestedTimestamp = now,
            avgDurationMs = newAvgDuration
        )
    }
}
