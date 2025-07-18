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
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.codinglikeapirate.pocitaj.data.FactMasteryDao
import com.codinglikeapirate.pocitaj.data.Operation
import com.codinglikeapirate.pocitaj.logic.Curriculum
import com.codinglikeapirate.pocitaj.ui.theme.AppTheme
import com.codinglikeapirate.pocitaj.ui.theme.customColors
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun ExerciseSetupScreen(
    onStartClicked: (operation: Operation, count: Int, difficulty: Int, levelId: String?) -> Unit,
    onProgressClicked: () -> Unit,
    viewModel: ExerciseSetupViewModel = viewModel(factory = ExerciseSetupViewModelFactory)
) {
    val uiState by viewModel.uiState.collectAsState()
    var questionCount by remember { mutableFloatStateOf(10f) }
    var difficulty by remember { mutableIntStateOf(10) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Choose Your Challenge", style = MaterialTheme.typography.headlineMedium)
            Button(onClick = onProgressClicked) {
                Text("Progress")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(uiState.operations) { operationState ->
                OperationCard(
                    operationUiState = operationState,
                    onStartClicked = { levelId ->
                        onStartClicked(operationState.operation, questionCount.toInt(), difficulty, levelId)
                    }
                )
            }
        }
    }
}

@Composable
fun OperationCard(
    operationUiState: OperationUiState,
    onStartClicked: (levelId: String?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val gradient = getGradientForOperation(operationUiState.operation)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
    ) {
        Column(
            modifier = Modifier
                .background(gradient)
                .clickable { expanded = !expanded }
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = when (operationUiState.operation) {
                        Operation.ADDITION -> "+"
                        Operation.SUBTRACTION -> "-"
                        Operation.MULTIPLICATION -> "×"
                        Operation.DIVISION -> "÷"
                    },
                    fontSize = 48.sp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    operationUiState.operation.name.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    Button(
                        onClick = { onStartClicked(null) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Practice (Smart)")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    operationUiState.levels.forEach { levelState ->
                        LevelButton(levelState = levelState, onClick = { onStartClicked(levelState.level.id) })
                    }
                }
            }
        }
    }
}

@Composable
fun LevelButton(levelState: LevelUiState, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = levelState.isUnlocked,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = "${levelState.level.id} ${if (levelState.isMastered) "🌟" else ""}"
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
    val fakeViewModel = object : ExerciseSetupViewModel(FakeFactMasteryDao()) {
        override val uiState = MutableStateFlow(
            ExerciseSetupUiState(
                operations = Operation.entries.map { op ->
                    OperationUiState(
                        operation = op,
                        levels = listOf(
                            LevelUiState(Curriculum.SumsUpTo5, isUnlocked = true, isMastered = true),
                            LevelUiState(Curriculum.SumsUpTo10, isUnlocked = true, isMastered = false),
                            LevelUiState(Curriculum.SumsUpTo20, isUnlocked = false, isMastered = false)
                        )
                    )
                }
            )
        )
    }

    AppTheme {
        ExerciseSetupScreen(
            onStartClicked = { _, _, _, _ -> },
            onProgressClicked = {},
            viewModel = fakeViewModel
        )
    }
}

/**
 * A fake [FactMasteryDao] for use in Jetpack Compose previews.
 */
class FakeFactMasteryDao : FactMasteryDao {
    override fun getAllFactsForUser(userId: Long) = MutableStateFlow<List<com.codinglikeapirate.pocitaj.data.FactMastery>>(emptyList())
    override suspend fun getFactMastery(userId: Long, factId: String): com.codinglikeapirate.pocitaj.data.FactMastery? = null
    override suspend fun upsert(factMastery: com.codinglikeapirate.pocitaj.data.FactMastery) {}
}
