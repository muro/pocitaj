package com.codinglikeapirate.pocitaj.ui.progress

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.codinglikeapirate.pocitaj.data.FactMastery
import com.codinglikeapirate.pocitaj.data.Operation
import com.codinglikeapirate.pocitaj.data.toSymbol
import com.codinglikeapirate.pocitaj.logic.Curriculum
import com.codinglikeapirate.pocitaj.ui.theme.AppTheme

@Composable
fun ProgressReportScreen(
    viewModel: ProgressReportViewModel = viewModel(factory = ProgressReportViewModelFactory)
) {
    val progressByLevel by viewModel.progressByLevel.collectAsState()

    if (progressByLevel.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No progress data yet.")
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .testTag("progress_report_list")
        ) {
            items(progressByLevel.entries.toList()) { (level, facts) ->
                Text(
                    text = level.id,
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                FactGrid(facts, level.operation)
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun FactGrid(facts: List<FactProgress>, operation: Operation) {
    val factsWithCoords = facts.mapNotNull { factProgress ->
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

    if (factsWithCoords.isEmpty()) {
        Text("No facts to display for this level.")
        return
    }

    if (operation == Operation.DIVISION) {
        DivisionGrid(factsWithCoords)
    } else {
        StandardGrid(factsWithCoords, operation)
    }
}

@Composable
fun StandardGrid(factsWithCoords: List<Triple<Int, Int, FactProgress>>, operation: Operation) {
    val maxOp1 = factsWithCoords.maxOfOrNull { it.first } ?: 0
    val maxOp2 = factsWithCoords.maxOfOrNull { it.second } ?: 0
    val maxOperand = maxOf(maxOp1, maxOp2)

    val opValues = (0..maxOperand).toList()
    val factsMap = factsWithCoords.associateBy { Pair(it.first, it.second) }

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

@Preview(showBackground = true)
@Composable
fun ProgressReportScreenPreview() {
    AppTheme {
        val allLevels = Curriculum.getAllLevels()

        // Replicate the filtering logic from the ViewModel for an accurate preview
        val levelsToDisplay = allLevels.filter { level ->
            allLevels.none { otherLevel ->
                level != otherLevel &&
                        level.operation == otherLevel.operation &&
                        otherLevel.getAllPossibleFactIds().size > level.getAllPossibleFactIds().size &&
                        otherLevel.getAllPossibleFactIds().toSet().containsAll(level.getAllPossibleFactIds())
            }
        }

        // Create some fake mastery data for a more realistic preview
        val fakeMasteredFacts = mapOf(
            "ADDITION_1_1" to FactMastery("ADDITION_1_1", 1, 5, 0),
            "ADDITION_2_3" to FactMastery("ADDITION_2_3", 1, 3, 0),
            "ADDITION_8_8" to FactMastery("ADDITION_8_8", 1, 6, 0),
            "SUBTRACTION_5_2" to FactMastery("SUBTRACTION_5_2", 1, 1, 0),
            "SUBTRACTION_3_3" to FactMastery("SUBTRACTION_3_3", 1, 4, 0),
            "MULTIPLICATION_2_5" to FactMastery("MULTIPLICATION_2_5", 1, 5, 0),
            "MULTIPLICATION_5_2" to FactMastery("MULTIPLICATION_5_2", 1, 2, 0),
            "MULTIPLICATION_10_1" to FactMastery("MULTIPLICATION_10_1", 1, 7, 0)
        )

        val progressByLevel = levelsToDisplay.associateWith { level ->
            level.getAllPossibleFactIds().map { factId ->
                FactProgress(factId, fakeMasteredFacts[factId])
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .testTag("progress_report_list")
        ) {
            items(progressByLevel.entries.toList()) { (level, facts) ->
                Text(
                    text = level.id,
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                FactGrid(facts, level.operation)
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
