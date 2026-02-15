package dev.aidistillery.pocitaj.ui.progress

import dev.aidistillery.pocitaj.data.FactMastery
import dev.aidistillery.pocitaj.data.FactMasteryDao
import dev.aidistillery.pocitaj.data.Operation
import dev.aidistillery.pocitaj.logic.Curriculum
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test

class ProgressIntegrationTest {

    @Test
    fun `two digit level progress is based on components not combinations`() = runBlocking {
        // ARRANGE
        val mockDao = mockk<FactMasteryDao>()
        val activeUserId = 1L

        // Simulate mastery of 10 component facts (e.g. 5 ones + 5 tens)
        val masteredFacts = (0..19).map { i ->
            FactMastery("ADD_ONES_0_$i", activeUserId, "", 5, 0)
        }

        every { mockDao.getAllFactsForUser(activeUserId) } returns flowOf(masteredFacts)

        val viewModel = ProgressReportViewModel(mockDao, activeUserId)

        // ACT
        // We need to give the StateFlow a moment to calculate, as it uses start=SharingStarted.WhileSubscribed(5000)
        // and does calculation in map block.

        // Let's collect items until we get a non-empty one or timeout
        val progressMap: Map<Operation, Map<String, LevelProgress>> =
            viewModel.levelProgressByOperation
                .filter { it.isNotEmpty() }
                .first()

        val additionProgress = progressMap[Operation.ADDITION] ?: emptyMap()

        // We look at specific level: TwoDigitAdditionNoCarry
        val levelId = Curriculum.TwoDigitAdditionNoCarry.id
        val levelProgress = additionProgress[levelId]

        // ASSERT
        val progressValue = levelProgress?.progress ?: 0f

        // We expect at least 1% progress for 20 mastered items if total is ~200.
        // In the "Red" state (combinatorial), this should fail because 20 / 2500 < 0.01
        assertTrue(
            "Progress should be significant (> 1%), but was $progressValue",
            progressValue > 0.01f
        )
    }
}
