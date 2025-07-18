package com.codinglikeapirate.pocitaj.ui.setup

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codinglikeapirate.pocitaj.data.Operation
import com.codinglikeapirate.pocitaj.ui.theme.AppTheme
import com.codinglikeapirate.pocitaj.ui.theme.customColors

@Composable
fun ExerciseSetupScreen(
    onStartClicked: (operation: Operation, count: Int, difficulty: Int) -> Unit,
    onProgressClicked: () -> Unit
) {
    // The Slider component works with a Float value, which we convert to an Int when needed.
    var questionCount by remember { mutableFloatStateOf(2f) }
    var difficulty by remember { mutableIntStateOf(10) }

    val operations = Operation.entries.toList()

    val gradients = listOf(
        Brush.linearGradient(
            listOf(
                MaterialTheme.customColors.additionGradientStart,
                MaterialTheme.customColors.additionGradientEnd
            )
        ),
        Brush.linearGradient(
            listOf(
                MaterialTheme.customColors.subtractionGradientStart,
                MaterialTheme.customColors.subtractionGradientEnd
            )
        ),
        Brush.linearGradient(
            listOf(
                MaterialTheme.customColors.multiplicationGradientStart,
                MaterialTheme.customColors.multiplicationGradientEnd
            )
        ),
        Brush.linearGradient(
            listOf(
                MaterialTheme.customColors.divisionGradientStart,
                MaterialTheme.customColors.divisionGradientEnd
            )
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        Row {
            // Title
            Text("Choose Your Challenge", style = MaterialTheme.typography.headlineMedium)
            // Progress Button
            Button(onClick = onProgressClicked) {
                Text("Progress")
            }
        }

        // Grid of Exercise Cards
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(operations.size) { index ->
                val operation = operations[index]
                ExerciseCard(
                    operation = operation,
                    gradient = gradients[index % gradients.size],
                    onClick = {
                        onStartClicked(operation, questionCount.toInt(), difficulty)
                    }
                )
            }
        }

        // Configuration Section
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Difficulty Selector
            Text("Difficulty (Numbers up to)", style = MaterialTheme.typography.titleMedium)
            Row {
                OutlinedButton(onClick = { difficulty = 10 }) {
                    Text("10")
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(onClick = { difficulty = 20 }) {
                    Text("20")
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(onClick = { difficulty = 100 }) {
                    Text("100")
                }
            }

            // Question Count Slider
            Text("Number of Questions: ${questionCount.toInt()}", style = MaterialTheme.typography.titleMedium)
            Slider(
                value = questionCount,
                onValueChange = { questionCount = it },
                valueRange = 2f..15f,
                steps = 12
            )
        }
    }
}

@Composable
fun ExerciseCard(
    operation: Operation,
    gradient: Brush,
    onClick: (Operation) -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .background(gradient)
            .clickable { onClick(operation) }
            .aspectRatio(1f),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = when (operation) {
                    Operation.ADDITION -> "+"
                    Operation.SUBTRACTION -> "-"
                    Operation.MULTIPLICATION -> "×"
                    Operation.DIVISION -> "÷"
                },
                fontSize = 48.sp,
                color = MaterialTheme.colorScheme.onPrimary
            )
            Text(
                operation.name.replace("_", " ").replaceFirstChar { it.uppercase() },
                color = MaterialTheme.colorScheme.onPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
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
fun PreviewExerciseSetupScreen() {
    AppTheme {
        ExerciseSetupScreen(onStartClicked = { _, _, _ -> }, onProgressClicked = {})
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
fun PreviewExerciseSetupScreenEmpty() {
    AppTheme {
        ExerciseSetupScreen(onStartClicked = { _, _, _ -> }, onProgressClicked = {})
    }
}