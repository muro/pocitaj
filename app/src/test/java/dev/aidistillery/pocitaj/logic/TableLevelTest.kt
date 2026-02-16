package dev.aidistillery.pocitaj.logic

import dev.aidistillery.pocitaj.data.Operation
import io.kotest.assertions.withClue
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.ints.shouldBeInRange
import io.kotest.matchers.shouldBe
import org.junit.Test

class TableLevelTest {
    @Test
    fun `TableLevel multiplication generates correct exercise`() {
        val level = Curriculum.TableLevel(Operation.MULTIPLICATION, 3)
        repeat(50) {
            val exercise = level.generateExercise()
            val equation = exercise.equation as Multiplication
            val (op, op1, op2) = equation.getFact()
            level.id shouldBe "MUL_TABLE_3"
            op shouldBe Operation.MULTIPLICATION

            // Check if one operand is 3
            withClue("One operand must be 3 (op1=$op1, op2=$op2)") {
                (op1 == 3 || op2 == 3).shouldBeTrue()
            }
            // Check if other is in range 2..12
            val other = if (op1 == 3) op2 else op1
            withClue("The other operand must be between 2 and 12 (was $other)") {
                other shouldBeInRange 2..12
            }
        }
    }

    @Test
    fun `TableLevel division generates correct exercise`() {
        val level = Curriculum.TableLevel(Operation.DIVISION, 4)
        repeat(50) {
            val exercise = level.generateExercise()
            val equation = exercise.equation as Division
            val (op, op1, op2) = equation.getFact()
            level.id shouldBe "DIV_BY_4"
            op shouldBe Operation.DIVISION

            // Check divisor is 4
            withClue("Divisor should be 4") {
                op2 shouldBe 4
            }
            // Check dividend is multiple of 4
            withClue("Dividend $op1 should be a multiple of 4") {
                (op1 % 4 == 0).shouldBeTrue()
            }
            // Check result range 2..10
            val result = op1 / op2
            withClue("Result should be between 2 and 10 (was $result)") {
                result shouldBeInRange 2..10
            }
        }
    }

    @Test
    fun `TableLevel facts match expected set for multiplication`() {
        val level = Curriculum.TableLevel(Operation.MULTIPLICATION, 5)
        val facts = level.getAllPossibleFactIds().toSet()

        // 2..12 -> 11 pairs. Each pair generates 2 IDs (AxB and BxA)
        // However, for op2 = table (5), 5x5 is generated twice but stored once in the Set.
        // So 10 * 2 + 1 = 21 facts expected.
        withClue("Should have 21 unique facts for multiplication table 5") {
            facts.size shouldBe 21
        }
        facts shouldContain "5 * 2 = ?"
        facts shouldContain "2 * 5 = ?"
    }

    @Test
    fun `TableLevel facts match expected set for division`() {
        val level = Curriculum.TableLevel(Operation.DIVISION, 6)
        val facts = level.getAllPossibleFactIds().toSet()

        // 2..10 -> 9 items. Each item generates 1 fact (Dividend / Divisor)
        // Total 9 facts expected.
        withClue("Should have 9 unique facts for division table 6") {
            facts.size shouldBe 9
        }
        facts shouldContain "12 / 6 = ?"
        facts shouldContain "60 / 6 = ?"
    }
}
