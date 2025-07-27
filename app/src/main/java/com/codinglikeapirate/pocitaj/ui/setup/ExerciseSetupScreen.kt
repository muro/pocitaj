package com.codinglikeapirate.pocitaj.ui.setup

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codinglikeapirate.pocitaj.R
import com.codinglikeapirate.pocitaj.data.Operation
import com.codinglikeapirate.pocitaj.logic.Curriculum
import com.codinglikeapirate.pocitaj.logic.formatLevel
import com.codinglikeapirate.pocitaj.ui.components.AutoSizeText
import com.codinglikeapirate.pocitaj.ui.components.PocitajScreen
import com.codinglikeapirate.pocitaj.ui.theme.AppTheme
import com.codinglikeapirate.pocitaj.ui.theme.getGradientForOperation

@Composable
fun ExerciseSetupScreen(
    operationLevels: List<OperationLevels>,
    onStartClicked: (operation: Operation, count: Int, difficulty: Int, levelId: String?) -> Unit,
    onProgressClicked: () -> Unit
) {
    PocitajScreen {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(top = 32.dp), // Add top padding to avoid camera cutout
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(id = R.string.choose_your_challenge),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = onProgressClicked) {
                    Icon(
                        imageVector = Icons.Default.BarChart,
                        contentDescription = stringResource(id = R.string.progress_button),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(operationLevels) { operationState ->
                    OperationCard(
                        operationLevels = operationState,
                        onStartClicked = { levelId ->
                            onStartClicked(operationState.operation, 10, 10, levelId)
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OperationCard(
    operationLevels: OperationLevels,
    onStartClicked: (levelId: String?) -> Unit,
    initialExpanded: Boolean = false
) {
    var expanded by remember { mutableStateOf(initialExpanded) }
    val gradient = getGradientForOperation(operationLevels.operation)
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale = if (isPressed) 0.98f else 1f


    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { expanded = !expanded },
        shape = RoundedCornerShape(24.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(gradient)
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
                    style = MaterialTheme.typography.displayMedium,
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
                    androidx.compose.material3.Button(
                        onClick = { onStartClicked("Smart Practice") },
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

                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        for (row in operationLevels.levels.chunked(2)) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                for (level in row) {
                                    LevelTile(
                                        levelStatus = level,
                                        onClick = { onStartClicked(level.level.id) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                if (row.size == 1) {
                                    Spacer(Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LevelTile(levelStatus: LevelStatus, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .aspectRatio(1f)
            .clickable(enabled = levelStatus.isUnlocked, onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = if (levelStatus.isUnlocked) 0.8f else 0.3f),
            contentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = if (levelStatus.isUnlocked) 1f else 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AutoSizeText(
                text = formatLevel(levelStatus.level).shortLabel,
                style = MaterialTheme.typography.headlineMedium
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