package dev.aidistillery.pocitaj.logic

import dev.aidistillery.pocitaj.data.ExerciseAttempt
import dev.aidistillery.pocitaj.data.Operation
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.Test
import java.time.LocalDate

class ActivityAnalyzerTest {

    @Test
    fun `calculateStreak - no activity returns 0`() {
        val streak = ActivityAnalyzer.calculateStreak(emptyMap(), LocalDate.of(2024, 1, 5))
        streak shouldBe 0
    }

    @Test
    fun `calculateStreak - played today only returns 1`() {
        val today = LocalDate.of(2024, 1, 5)
        val activity = mapOf(today to 5)
        val streak = ActivityAnalyzer.calculateStreak(activity, today)
        streak shouldBe 1
    }

    @Test
    fun `calculateStreak - played yesterday but not today returns 1 (Grace period)`() {
        val today = LocalDate.of(2024, 1, 5)
        val activity = mapOf(today.minusDays(1) to 5)
        val streak = ActivityAnalyzer.calculateStreak(activity, today)
        streak shouldBe 1
    }

    @Test
    fun `calculateStreak - played 3 days including today returns 3`() {
        val today = LocalDate.of(2024, 1, 5)
        val activity = mapOf(
            today to 2,
            today.minusDays(1) to 5,
            today.minusDays(2) to 1
        )
        val streak = ActivityAnalyzer.calculateStreak(activity, today)
        streak shouldBe 3
    }

    @Test
    fun `calculateStreak - played 3 days including yesterday returns 3`() {
        val today = LocalDate.of(2024, 1, 5)
        val activity = mapOf(
            today.minusDays(1) to 5,
            today.minusDays(2) to 1,
            today.minusDays(3) to 4
        )
        val streak = ActivityAnalyzer.calculateStreak(activity, today)
        streak shouldBe 3
    }

    @Test
    fun `calculateStreak - broken streak returns only recent`() {
        val today = LocalDate.of(2024, 1, 10)
        val activity = mapOf(
            today to 2,
            today.minusDays(1) to 5,
            // Gap on day 2
            today.minusDays(3) to 1,
            today.minusDays(4) to 4
        )
        val streak = ActivityAnalyzer.calculateStreak(activity, today)
        streak shouldBe 2 // Only today and yesterday
    }

    @Test
    fun `calculateStreak - long gap yesterday means 0`() {
        val today = LocalDate.of(2024, 1, 10)
        val activity = mapOf(
            today.minusDays(2) to 5,
            today.minusDays(3) to 1
        )
        val streak = ActivityAnalyzer.calculateStreak(activity, today)
        streak shouldBe 0 // Broken because yesterday and today are empty
    }

    @Test
    fun `generateHighlights - returns empty for no attempts`() {
        val highlights = ActivityAnalyzer.generateHighlights(emptyList())
        highlights.size shouldBe 0
    }

    @Test
    fun `generateHighlights - Unstoppable for 30+ exercises`() {
        val attempts = List(35) { createAttempt(correct = true, duration = 5000) }
        val highlights = ActivityAnalyzer.generateHighlights(attempts)

        highlights.size shouldBe 2 // Unstoppable + Laser Focus
        highlights[0].shouldBeInstanceOf<SmartHighlight.Unstoppable>()
    }

    @Test
    fun `generateHighlights - LaserFocus for high accuracy`() {
        val attempts = List(15) { createAttempt(correct = true, duration = 5000) }
        val highlights = ActivityAnalyzer.generateHighlights(attempts)

        highlights.size shouldBe 1
        highlights[0].shouldBeInstanceOf<SmartHighlight.LaserFocus>()
    }

    @Test
    fun `generateHighlights - PerfectPrecision for 100pc small batch`() {
        val attempts = List(7) { createAttempt(correct = true, duration = 5000) }
        val highlights = ActivityAnalyzer.generateHighlights(attempts)

        highlights.size shouldBe 1
        highlights[0].shouldBeInstanceOf<SmartHighlight.PerfectPrecision>()
    }

    @Test
    fun `generateHighlights - SpeedyPaws for fast average`() {
        val attempts = List(15) { createAttempt(correct = true, duration = 2000) }
        val highlights = ActivityAnalyzer.generateHighlights(attempts)

        // Should get Laser Focus and Speedy Paws
        highlights.size shouldBe 2
        highlights[0].shouldBeInstanceOf<SmartHighlight.LaserFocus>()
        highlights[1].shouldBeInstanceOf<SmartHighlight.SpeedyPaws>()
    }

    @Test
    fun `generateHighlights - mixed accuracy omits precision but can still award SpeedyPaws`() {
        // 15 total attempts: 10 correct, 5 incorrect. Accuracy = 66%
        val correctAttempts = List(10) { createAttempt(correct = true, duration = 2000) }
        val incorrectAttempts = List(5) { createAttempt(correct = false, duration = 2000) }
        val highlights = ActivityAnalyzer.generateHighlights(correctAttempts + incorrectAttempts)

        // Should NOT get Laser Focus or Perfect Precision due to low accuracy.
        // Should STILL get Speedy Paws because avg duration of *correct* answers is 2 seconds.
        highlights.size shouldBe 1
        highlights[0].shouldBeInstanceOf<SmartHighlight.SpeedyPaws>()
    }

    private fun createAttempt(correct: Boolean, duration: Long): ExerciseAttempt {
        return ExerciseAttempt(
            userId = 1,
            timestamp = System.currentTimeMillis(),
            problemText = "1+1",
            logicalOperation = Operation.ADDITION,
            correctAnswer = 2,
            submittedAnswer = if (correct) 2 else 3,
            wasCorrect = correct,
            durationMs = duration
        )
    }
}
