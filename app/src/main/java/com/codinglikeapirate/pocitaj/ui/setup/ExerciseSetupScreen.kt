package com.codinglikeapirate.pocitaj.ui.setup

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import com.codinglikeapirate.pocitaj.logic.formatLevel
import com.codinglikeapirate.pocitaj.ui.components.AutoSizeText

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codinglikeapirate.pocitaj.R
import com.codinglikeapirate.pocitaj.data.Operation
import com.codinglikeapirate.pocitaj.logic.Curriculum
import com.codinglikeapirate.pocitaj.ui.theme.AppTheme
import com.codinglikeapirate.pocitaj.ui.theme.customColors

@Composable
fun ExerciseSetupScreen(
    operationLevels: List<OperationLevels>,
    onStartClicked: (operation: Operation, count: Int, difficulty: Int, levelId: String?) -> Unit,
    onProgressClicked: () -> Unit
) {
    var questionCount by remember { mutableFloatStateOf(10f) }
    var difficulty by remember { mutableIntStateOf(10) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .padding(top = 32.dp), // Add top padding to avoid camera cutout
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(stringResource(id = R.string.choose_your_challenge), style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface)

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(operationLevels) { operationState ->
                OperationCard(
                    operationLevels = operationState,
                    onStartClicked = { levelId ->
                        onStartClicked(operationState.operation, questionCount.toInt(), difficulty, levelId)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onProgressClicked,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(id = R.string.progress_button))
        }
    }
}

@Composable
fun OperationCard(
    operationLevels: OperationLevels,
    onStartClicked: (levelId: String?) -> Unit,
    initialExpanded: Boolean = false
) {
    var expanded by remember { mutableStateOf(initialExpanded) }
    val gradient = getGradientForOperation(operationLevels.operation)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(gradient)
                .clickable { expanded = !expanded }
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = when (operationLevels.operation) {
                        Operation.ADDITION -> "+"
                        Operation.SUBTRACTION -> "-"
                        Operation.MULTIPLICATION -> "×"
                        Operation.DIVISION -> "÷"
                    },
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    operationLevels.operation.name.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    // "Practice (Smart)" Button
                    Button(
                        onClick = { onStartClicked(null) },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(id = R.string.practice_smart))
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    // Grid of Level Tiles
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.height(200.dp) // Adjust height as needed
                    ) {
                        items(operationLevels.levels) { levelState ->
                            LevelTile(levelStatus = levelState, onClick = { onStartClicked(levelState.level.id) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LevelTile(levelStatus: LevelStatus, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable(enabled = levelStatus.isUnlocked, onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = if (levelStatus.isUnlocked) 0.8f else 0.3f),
            contentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = if (levelStatus.isUnlocked) 1f else 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AutoSizeText(
                text = formatLevel(levelStatus.level).shortLabel,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "🌟".repeat(levelStatus.starRating) + "☆".repeat(3 - levelStatus.starRating),
                fontSize = 20.sp
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
fun PreviewExpandedOperationCard() {
    val operationLevels = OperationLevels(
        operation = Operation.ADDITION,
        levels = listOf(
            LevelStatus(Curriculum.SumsUpTo5, isUnlocked = true, starRating = 3),
            LevelStatus(Curriculum.SumsUpTo10, isUnlocked = true, starRating = 1),
            LevelStatus(Curriculum.SumsUpTo20, isUnlocked = false, starRating = 0)
        )
    )
    AppTheme {
        OperationCard(
            operationLevels = operationLevels,
            onStartClicked = {},
            initialExpanded = true
        )
    }
}

@Composable
private fun getGradientForOperation(operation: Operation): Brush {
    return when (operation) {
        Operation.ADDITION -> Brush.linearGradient(
            listOf(
                MaterialTheme.customColors.additionGradientStart,
                MaterialTheme.customColors.additionGradientEnd
            )
        )
        Operation.SUBTRACTION -> Brush.linearGradient(
            listOf(
                MaterialTheme.customColors.subtractionGradientStart,
                MaterialTheme.customColors.subtractionGradientEnd
            )
        )
        Operation.MULTIPLICATION -> Brush.linearGradient(
            listOf(
                MaterialTheme.customColors.multiplicationGradientStart,
                MaterialTheme.customColors.multiplicationGradientEnd
            )
        )
        Operation.DIVISION -> Brush.linearGradient(
            listOf(
                MaterialTheme.customColors.divisionGradientStart,
                MaterialTheme.customColors.divisionGradientEnd
            )
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
fun PreviewExerciseSetupScreen() {
    val fakeOperationLevels = Operation.entries.map { op ->
        OperationLevels(
            operation = op,
            levels = listOf(
                LevelStatus(Curriculum.SumsUpTo5, isUnlocked = true, starRating = 3),
                LevelStatus(Curriculum.SumsUpTo10, isUnlocked = true, starRating = 1),
                LevelStatus(Curriculum.SumsUpTo20, isUnlocked = false, starRating = 0)
            )
        )
    }

    AppTheme {
        ExerciseSetupScreen(
            operationLevels = fakeOperationLevels,
            onStartClicked = { _, _, _, _ -> },
            onProgressClicked = {}
        )
    }
}
