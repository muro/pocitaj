package com.codinglikeapirate.pocitaj.ui.progress

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.codinglikeapirate.pocitaj.R
import com.codinglikeapirate.pocitaj.data.FactMastery
import com.codinglikeapirate.pocitaj.data.Operation
import com.codinglikeapirate.pocitaj.data.toSymbol
import com.codinglikeapirate.pocitaj.logic.Curriculum
import com.codinglikeapirate.pocitaj.ui.theme.AppTheme

@Composable
fun ProgressReportScreen(
    factProgressByOperation: Map<Operation, List<FactProgress>>,
    levelProgressByOperation: Map<Operation, Map<String, LevelProgress>>
) {
    if (factProgressByOperation.isEmpty() && levelProgressByOperation.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                stringResource(id = R.string.no_progress_yet),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.height(32.dp))
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .testTag("progress_report_list"),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(Operation.entries.toList()) { operation ->
                    Card(modifier = Modifier.testTag("operation_card_${operation.name}")) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = getOperationTitle(operation),
                                style = MaterialTheme.typography.headlineMedium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            OperationProgress(
                                operation = operation,
                                factProgress = factProgressByOperation[operation] ?: emptyList(),
                                levelProgress = levelProgressByOperation[operation] ?: emptyMap()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun getOperationTitle(operation: Operation): String {
    return when (operation) {
        Operation.ADDITION -> stringResource(R.string.operation_addition)
        Operation.SUBTRACTION -> stringResource(R.string.operation_subtraction)
        Operation.MULTIPLICATION -> stringResource(R.string.operation_multiplication)
        Operation.DIVISION -> stringResource(R.string.operation_division)
    }
}

@Composable
fun OperationProgress(
    operation: Operation,
    factProgress: List<FactProgress>,
    levelProgress: Map<String, LevelProgress>
) {
    when (operation) {
        Operation.ADDITION, Operation.SUBTRACTION -> {
            LevelProgressList(levelProgress = levelProgress)
        }
        Operation.MULTIPLICATION -> {
            val factsWithCoords = mapFactsToCoords(factProgress)
                .filter { (op1, op2, _) -> op1 <= 12 && op2 <= 12 }
            if (factsWithCoords.isNotEmpty()) {
                StandardGrid(factsWithCoords, operation)
            } else {
                Text("No facts to display for this level.")
            }
        }
        Operation.DIVISION -> {
            val factsWithCoords = mapFactsToCoords(factProgress)
                .filter { (_, op2, _) -> op2 != 0 && op2 <= 12 }
            if (factsWithCoords.isNotEmpty()) {
                DivisionGrid(factsWithCoords)
            } else {
                Text("No facts to display for this level.")
            }
        }
    }
}

@Composable
fun LevelProgressList(levelProgress: Map<String, LevelProgress>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        levelProgress.forEach { (levelId, progress) ->
            Column {
                Text(
                    text = levelId, // You might want to map this to a more user-friendly name
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { progress.progress },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}


private fun mapFactsToCoords(facts: List<FactProgress>): List<Triple<Int, Int, FactProgress>> {
    return facts.mapNotNull { factProgress ->
        val parts = factProgress.factId.split('_')
        if (parts.size == 3) {
            val op1 = parts[1].toIntOrNull()
            val op2 = parts[2].toIntOrNull()
            if (op1 != null && op2 != null) {
                Triple(op1, op2, factProgress)
            } else {
                null
            }
        } else {
            null
        }
    }
}

@Composable
fun StandardGrid(factsWithCoords: List<Triple<Int, Int, FactProgress>>, operation: Operation) {
    val maxOp1 = factsWithCoords.maxOfOrNull { it.first } ?: 0
    val maxOp2 = factsWithCoords.maxOfOrNull { it.second } ?: 0
    val maxOperand = maxOf(maxOp1, maxOp2, 10) // Ensure grid is at least 10x10

    val opValues = (0..maxOperand).toList()
    val factsMap = factsWithCoords.associateBy { Pair(it.first, it.second) }

    @Suppress("UnusedBoxWithConstraintsScope")
    BoxWithConstraints {
        val spacing = 4.dp
        val totalSpacing = spacing * opValues.size
        val cellSize = (maxWidth - totalSpacing) / (opValues.size + 1)

        Column(verticalArrangement = Arrangement.spacedBy(spacing)) {
            // Header
            Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
                GridCell(text = operation.toSymbol(), size = cellSize)
                opValues.forEach { op2 ->
                    GridCell(text = op2.toString(), size = cellSize)
                }
            }

            // Grid Body
            opValues.forEach { op1 ->
                Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
                    GridCell(text = op1.toString(), size = cellSize)
                    opValues.forEach { op2 ->
                        val factProgress = factsMap[Pair(op1, op2)]?.third
                        val result = when (operation) {
                            Operation.ADDITION -> op1 + op2
                            Operation.SUBTRACTION -> op1 - op2
                            Operation.MULTIPLICATION -> op1 * op2
                            Operation.DIVISION -> if (op2 != 0) op1 / op2 else 0
                        }
                        FactCell(factProgress = factProgress, result = result, size = cellSize)
                    }
                }
            }
        }
    }
}

@Composable
fun DivisionGrid(factsWithCoords: List<Triple<Int, Int, FactProgress>>) {
    val divisors = factsWithCoords.map { it.second }.distinct().sorted()
    val multipliers = (0..10).toList()
    val factsMap = factsWithCoords.associateBy { Pair(it.first, it.second) }

    @Suppress("UnusedBoxWithConstraintsScope")
    BoxWithConstraints {
        val spacing = 4.dp
        val totalSpacing = spacing * divisors.size
        val cellSize = (maxWidth - totalSpacing) / (divisors.size + 1)

        Column(verticalArrangement = Arrangement.spacedBy(spacing)) {
            // Header
            Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
                GridCell(text = "×", size = cellSize) // Using multiplication symbol for clarity
                divisors.forEach { divisor ->
                    GridCell(text = divisor.toString(), size = cellSize)
                }
            }

            // Grid Body
            multipliers.forEach { multiplier ->
                Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
                    GridCell(text = multiplier.toString(), size = cellSize)
                    divisors.forEach { divisor ->
                        val dividend = multiplier * divisor
                        val factProgress = factsMap[Pair(dividend, divisor)]?.third
                        FactCell(factProgress = factProgress, result = dividend, size = cellSize)
                    }
                }
            }
        }
    }
}

@Composable
fun GridCell(text: String, size: androidx.compose.ui.unit.Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun FactCell(factProgress: FactProgress?, result: Int, size: androidx.compose.ui.unit.Dp) {
    val isPossible = factProgress != null
    val strength = factProgress?.mastery?.strength ?: 0
    val color = if (isPossible) {
        when {
            strength >= 5 -> Color(0xFF4CAF50) // Green
            strength >= 3 -> Color(0xFFFFEB3B) // Yellow
            strength > 0 -> Color(0xFFF44336)  // Red
            else -> Color(0xFFE0E0E0)         // LightGray for possible but not attempted
        }
    } else {
        Color.Transparent // Not in the level
    }

    Box(
        modifier = Modifier
            .size(size)
            .background(color, shape = RoundedCornerShape(4.dp))
            .border(
                1.dp,
                if (isPossible) Color.Transparent else Color.LightGray,
                shape = RoundedCornerShape(4.dp)
            )
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isPossible) {
            Text(
                text = result.toString(),
                fontSize = with(LocalDensity.current) { (size / 3).toSp() },
                color = Color.Black,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    showBackground = true,
    name = "Light Mode"
)
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
    name = "Dark Mode"
)
@Composable
fun ProgressReportScreenPreview() {
    AppTheme {
        val allLevels = Curriculum.getAllLevels()

        // Create some fake mastery data for a more realistic preview
        val fakeMasteredFacts = mapOf(
            // Addition
            "ADDITION_1_1" to FactMastery("ADDITION_1_1", 1, 5, 0), // Mastered
            "ADDITION_2_3" to FactMastery("ADDITION_2_3", 1, 3, 0), // Learning
            "ADDITION_3_4" to FactMastery("ADDITION_3_4", 1, 1, 0), // Struggling
            "ADDITION_8_8" to FactMastery("ADDITION_8_8", 1, 6, 0), // Mastered
            "ADDITION_9_1" to FactMastery("ADDITION_9_1", 1, 2, 0), // Struggling

            // Subtraction
            "SUBTRACTION_5_2" to FactMastery("SUBTRACTION_5_2", 1, 5, 0), // Mastered
            "SUBTRACTION_4_1" to FactMastery("SUBTRACTION_4_1", 1, 4, 0), // Learning
            "SUBTRACTION_3_3" to FactMastery("SUBTRACTION_3_3", 1, 1, 0), // Struggling

            // Multiplication
            "MULTIPLICATION_2_5" to FactMastery("MULTIPLICATION_2_5", 1, 5, 0), // Mastered
            "MULTIPLICATION_5_2" to FactMastery("MULTIPLICATION_5_2", 1, 3, 0), // Learning
            "MULTIPLICATION_10_1" to FactMastery("MULTIPLICATION_10_1", 1, 7, 0), // Mastered
            "MULTIPLICATION_3_7" to FactMastery("MULTIPLICATION_3_7", 1, 1, 0), // Struggling

            // Division
            "DIVISION_10_2" to FactMastery("DIVISION_10_2", 1, 5, 0), // Mastered
            "DIVISION_25_5" to FactMastery("DIVISION_25_5", 1, 4, 0)  // Learning
        )

        val factProgressByOperation = allLevels
            .groupBy { it.operation }
            .mapValues { (_, levels) ->
                levels.flatMap { it.getAllPossibleFactIds() }.distinct().map { factId ->
                    FactProgress(factId, fakeMasteredFacts[factId])
                }
            }

        val levelProgressByOperation = allLevels
            .groupBy { it.operation }
            .mapValues { (_, levels) ->
                levels.associate { level ->
                    val levelFacts = level.getAllPossibleFactIds()
                    val masteredCount = levelFacts.count { factId ->
                        (fakeMasteredFacts[factId]?.strength ?: 0) >= 5
                    }
                    val progress = if (levelFacts.isNotEmpty()) {
                        masteredCount.toFloat() / levelFacts.size
                    } else {
                        0f
                    }
                    level.id to LevelProgress(progress, progress >= 1.0f)
                }
            }

        ProgressReportScreen(
            factProgressByOperation = factProgressByOperation,
            levelProgressByOperation = levelProgressByOperation
        )
    }
}

@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    showBackground = true,
    name = "Light Mode"
)
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
    name = "Dark Mode"
)
@Composable
fun EmptyProgressReportScreenPreview() {
    AppTheme {
        ProgressReportScreen(
            factProgressByOperation = emptyMap(),
            levelProgressByOperation = emptyMap()
        )
    }
}