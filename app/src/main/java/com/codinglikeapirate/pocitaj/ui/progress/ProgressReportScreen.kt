package com.codinglikeapirate.pocitaj.ui.progress

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
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

    val op1Values = factsWithCoords.map { it.first }.distinct().sorted()
    val op2Values = factsWithCoords.map { it.second }.distinct().sorted()

    val factsMap = factsWithCoords.associateBy { Pair(it.first, it.second) }

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Header row for op2
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Top-left corner box for operation symbol
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = operation.toSymbol(), style = MaterialTheme.typography.bodySmall)
            }
            op2Values.forEach { op2 ->
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = op2.toString(), style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        op1Values.forEach { op1 ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Header cell for op1
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = op1.toString(), style = MaterialTheme.typography.bodySmall)
                }

                op2Values.forEach { op2 ->
                    val factProgress = factsMap[Pair(op1, op2)]?.third
                    val isPossible = factProgress != null

                    if (isPossible) {
                        val strength = factProgress?.mastery?.strength ?: 0
                        val color = when {
                            strength >= 5 -> Color(0xFF4CAF50) // Green
                            strength >= 3 -> Color(0xFFFFEB3B) // Yellow
                            strength > 0 -> Color(0xFFF44336)  // Red
                            else -> Color(0xFFE0E0E0)         // LightGray
                        }
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(color, shape = RoundedCornerShape(4.dp))
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (strength > 0) {
                                Text(
                                    text = strength.toString(),
                                    fontSize = 10.sp,
                                    color = Color.Black,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.size(28.dp))
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProgressReportScreenPreview() {
    AppTheme {
        val levels = Curriculum.getAllLevels()
        val progressByLevel = levels.associateWith { level ->
            level.getAllPossibleFactIds().map { factId ->
                FactProgress(factId, null)
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
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