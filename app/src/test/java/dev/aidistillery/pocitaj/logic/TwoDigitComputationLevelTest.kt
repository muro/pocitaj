package dev.aidistillery.pocitaj.logic

import dev.aidistillery.pocitaj.data.Operation
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.ints.shouldBeInRange
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.Test

class TwoDigitComputationLevelTest {

    @Test
    fun `level creates correct equation type`() {
        val levelAdd = TwoDigitComputationLevel("ADD_CARRY", Operation.ADDITION, true)
        val exerciseAdd = levelAdd.createExercise("19 + 19 = ?")
        exerciseAdd.equation.shouldBeInstanceOf<Addition>()
        levelAdd.recognizes(exerciseAdd.equation).shouldBeTrue()

        val levelSub = TwoDigitComputationLevel("SUB_BORROW", Operation.SUBTRACTION, true)
        val exerciseSub = levelSub.createExercise("24 - 19 = ?")
        exerciseSub.equation.shouldBeInstanceOf<Subtraction>()
        levelSub.recognizes(exerciseSub.equation).shouldBeTrue()
    }

    @Test
    fun `getAffectedFactIds handles multi-fact updates in TwoDigitComputationLevel`() {
        val level = TwoDigitComputationLevel("ADD_CARRY", Operation.ADDITION, true)
        val exercise = Exercise(Addition(19, 19))

        val affectedIds = level.getAffectedFactIds(exercise)

        // Assert main fact and component facts are identified
        affectedIds shouldContain "19 + 19 = ?"
        affectedIds shouldContain "9 + 9 = ?"
        affectedIds shouldContain "10 + 10 = ?"
        affectedIds.size shouldBe 3
    }

    @Test
    fun `getAllPossibleFactIds returns component IDs`() {
        val level = TwoDigitComputationLevel("ADD_CARRY", Operation.ADDITION, true)
        val ids = level.getAllPossibleFactIds()

        // Should contain component facts like "9 + 9 = ?" and "10 + 10 = ?"
        ids shouldContain "9 + 9 = ?"
        ids shouldContain "10 + 10 = ?"

        // Should NOT contain the composite 19+19 in its primary list
        // (getAllPossibleFactIds returns the underlying skills we track mastery for)
        ids shouldNotContain "19 + 19 = ?"

        // Size should be manageable (fundamental components only)
        // Ones: ~45 (sums >= 10). Tens: ~36 (sums <= 80). Total < 100.
        ids.size shouldBeInRange 50..100
    }
}
