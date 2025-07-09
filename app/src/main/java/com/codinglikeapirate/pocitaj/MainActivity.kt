package com.codinglikeapirate.pocitaj

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.codinglikeapirate.pocitaj.data.ExerciseConfig
import com.codinglikeapirate.pocitaj.data.ExerciseType
import com.codinglikeapirate.pocitaj.logic.Exercise
import com.codinglikeapirate.pocitaj.ui.theme.AppTheme
import com.codinglikeapirate.pocitaj.ui.theme.customColors


class MainActivity : ComponentActivity() {

    private val startupViewModel: StartupViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppTheme {
                val isInitialized by startupViewModel.isInitialized.collectAsState()
                val error by startupViewModel.error.collectAsState()

                if (isInitialized) {
                    AppNavigation()
                } else {
                    LoadingScreen(
                        error = error,
                        onRetry = { startupViewModel.initializeApp() }
                    )
                }
            }
        }
    }
}

object Destinations {
    const val HOME_ROUTE = "home"
    const val EXERCISE_ROUTE = "exercise/{type}"
    const val SUMMARY_ROUTE = "summary"
    fun exerciseDetailRoute(type: String) = "exercise/$type"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val viewModel: ExerciseBookViewModel = viewModel(factory = ExerciseBookViewModelFactory)

    LaunchedEffect(Unit) {
        viewModel.navigationEvents.collect { event ->
            when (event) {
                is NavigationEvent.NavigateToExercise -> {
                    navController.navigate(Destinations.exerciseDetailRoute(event.type))
                }
                is NavigationEvent.NavigateToSummary -> {
                    navController.navigate(Destinations.SUMMARY_ROUTE) {
                        popUpTo(Destinations.HOME_ROUTE) { inclusive = false }
                    }
                }
                is NavigationEvent.NavigateBackToHome -> {
                    navController.navigate(Destinations.HOME_ROUTE) {
                        popUpTo(Destinations.HOME_ROUTE) { inclusive = true }
                    }
                }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Destinations.HOME_ROUTE
    ) {
        composable(route = Destinations.HOME_ROUTE) {
            ExerciseSetupScreen(
                onStartClicked = { exerciseType, count, difficulty ->
                    val config = ExerciseConfig(exerciseType.id, difficulty, count)
                    viewModel.startExercises(config)
                }
            )
        }
        composable(route = Destinations.EXERCISE_ROUTE) {
            val uiState by viewModel.uiState.collectAsState()
            val exerciseState = uiState as? UiState.ExerciseScreen
            if (exerciseState != null) {
                val exercise: Exercise = exerciseState.currentExercise
                ExerciseScreen(exercise, viewModel) { answer: String, elapsedMs: Int ->
                    viewModel.checkAnswer(answer, elapsedMs)
                }
            }
        }
        composable(route = Destinations.SUMMARY_ROUTE) {
            val uiState by viewModel.uiState.collectAsState()
            val summaryState = uiState as? UiState.SummaryScreen
            if (summaryState != null) {
                ResultsScreen(summaryState.results) {
                    viewModel.onSummaryDone()
                }
            }
        }
    }
}

@Composable
fun LoadingScreen(error: String?, onRetry: () -> Unit) {
    LaunchedEffect(Unit) {
        onRetry()
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (error == null) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text("Loading...")
        } else {
            Text("Error:", color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(8.dp))
            Text(error)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}

@Composable
fun ExerciseSetupScreen(
    onStartClicked: (exerciseType: ExerciseType, count: Int, difficulty: Int) -> Unit
) {
    // The Slider component works with a Float value, which we convert to an Int when needed.
    var questionCount by remember { mutableFloatStateOf(2f) }
    var difficulty by remember { mutableIntStateOf(10) }

    val exerciseTypes = listOf(
        ExerciseType.ADDITION,
        ExerciseType.SUBTRACTION,
        ExerciseType.MULTIPLICATION,
        ExerciseType.DIVISION
    )

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
        // Title
        Text("Choose Your Challenge", style = MaterialTheme.typography.headlineMedium)

        // Grid of Exercise Cards
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(exerciseTypes.size) { index ->
                ExerciseCard(
                    exerciseType = exerciseTypes[index],
                    gradient = gradients[index % gradients.size],
                    onClick = {
                        onStartClicked(exerciseTypes[index], questionCount.toInt(), difficulty)
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
fun ExerciseCard(exerciseType: ExerciseType, gradient: Brush, onClick: (ExerciseType) -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .background(gradient)
            .clickable { onClick(exerciseType) }
            .aspectRatio(1f),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = when (exerciseType) {
                    ExerciseType.ADDITION -> "+"
                    ExerciseType.SUBTRACTION -> "-"
                    ExerciseType.MULTIPLICATION -> "×"
                    ExerciseType.DIVISION -> "÷"
                },
                fontSize = 48.sp,
                color = MaterialTheme.colorScheme.onPrimary
            )
            Text(
                exerciseType.id.replace("_", " ").replaceFirstChar { it.uppercase() },
                color = MaterialTheme.colorScheme.onPrimary
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
fun PreviewExerciseSetupScreen() {
    AppTheme {
        ExerciseSetupScreen(onStartClicked = { _, _, _ -> })
    }
}

