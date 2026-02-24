package dev.aidistillery.pocitaj.logic

import androidx.annotation.StringRes
import dev.aidistillery.pocitaj.R
import dev.aidistillery.pocitaj.data.ExerciseAttempt
import java.time.LocalDate

sealed class SmartHighlight(
    @StringRes val titleResId: Int,
    @StringRes val messageResId: Int,
    val formatArgs: List<Any> = emptyList(),
    val icon: String
) {
    class SpeedyPaws(seconds: Int) : SmartHighlight(
        R.string.highlight_speedy_paws_title,
        R.string.highlight_speedy_paws_message,
        listOf(seconds),
        "⚡"
    )

    class LaserFocus(correctCount: Int) : SmartHighlight(
        R.string.highlight_laser_focus_title,
        R.string.highlight_laser_focus_message,
        listOf(correctCount),
        "🎯"
    )

    class Unstoppable(totalCount: Int) : SmartHighlight(
        R.string.highlight_unstoppable_title,
        R.string.highlight_unstoppable_message,
        listOf(totalCount),
        "🚀"
    )

    class PerfectPrecision : SmartHighlight(
        R.string.highlight_perfect_precision_title,
        R.string.highlight_perfect_precision_message,
        emptyList(),
        "✨"
    )
}

object ActivityAnalyzer {

    fun calculateStreak(
        dailyActivity: Map<LocalDate, Int>,
        today: LocalDate = LocalDate.now()
    ): Int {
        // Find the start date for counting (today, or yesterday if today is empty)
        var currentDate =
            if (dailyActivity.getOrDefault(today, 0) > 0) today else today.minusDays(1)

        // Count backward consecutively as long as there is activity
        return generateSequence(currentDate) { it.minusDays(1) }
            .takeWhile { dailyActivity.getOrDefault(it, 0) > 0 }
            .count()
    }

    fun generateHighlights(attempts: List<ExerciseAttempt>): List<SmartHighlight> {
        val highlights = mutableListOf<SmartHighlight>()
        if (attempts.isEmpty()) return highlights

        val total = attempts.size
        val correct = attempts.count { it.wasCorrect }
        val accuracy = if (total > 0) correct.toDouble() / total else 0.0
        val avgDurationMs = if (correct > 0) attempts.filter { it.wasCorrect }.map { it.durationMs }
            .average() else 0.0

        if (total >= 30) {
            highlights.add(SmartHighlight.Unstoppable(total))
        }

        if (total >= 10 && accuracy >= 0.95) {
            highlights.add(SmartHighlight.LaserFocus(correct))
        } else if (total in 5..9 && accuracy == 1.0) {
            highlights.add(SmartHighlight.PerfectPrecision())
        }

        if (total >= 10 && avgDurationMs in 1.0..3000.0) {
            val seconds = kotlin.math.round(avgDurationMs / 1000.0).toInt()
            highlights.add(SmartHighlight.SpeedyPaws(seconds))
        }

        // Return up to 2 highlights, prioritizing the most impressive ones (they are added top-down roughly by impressiveness)
        return highlights.take(2)
    }
}
