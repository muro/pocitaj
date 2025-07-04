package com.codinglikeapirate.pocitaj

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController


class ExerciseBookActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModel: ExerciseBookViewModel by viewModels { ExerciseBookViewModelFactory }

        setContent {
            AppTheme {
                AppNavigation(viewModel)
            }
        }
    }
}

object Destinations {
    const val LOADING_ROUTE = "loading"
    const val HOME_ROUTE = "home"
    const val EXERCISE_ROUTE = "exercise/{type}"
    const val SUMMARY_ROUTE = "summary"
    fun exerciseDetailRoute(type: String) = "exercise/$type"
}

@Composable
fun AppNavigation(viewModel: ExerciseBookViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val navController = rememberNavController()

    LaunchedEffect(Unit) {
        viewModel.navigationEvents.collect { event ->
            when (event) {
                is NavigationEvent.NavigateToHome -> {
                    navController.navigate(Destinations.HOME_ROUTE) {
                        popUpTo(Destinations.LOADING_ROUTE) { inclusive = true }
                    }
                }

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
        startDestination = Destinations.LOADING_ROUTE
    ) {
        composable(route = Destinations.LOADING_ROUTE) {
            LoadingScreen(UiState.LoadingModel()) {
                viewModel.downloadModel("en-US")
            }
        }
        composable(route = Destinations.HOME_ROUTE) {
            ExerciseSetupScreen(viewModel) {
                viewModel.deleteActiveModel("en-US")
            }
        }
        composable(route = Destinations.EXERCISE_ROUTE) {
            val exerciseState = uiState as? UiState.ExerciseScreen
            if (exerciseState != null) {
                val exercise: Exercise = exerciseState.currentExercise
                ExerciseScreen(exercise, viewModel) { answer: String, elapsedMs: Int ->
                    viewModel.checkAnswer(answer, elapsedMs)
                }
            }
        }
        composable(route = Destinations.SUMMARY_ROUTE) {
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
fun LoadingScreen(state: UiState.LoadingModel, downloadModel: () -> Unit) {
    LaunchedEffect(Unit) {
        downloadModel()
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (state.errorMessage == null) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text("Downloading model...")
        } else {
            Text("Error downloading model:", color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(8.dp))
            Text(state.errorMessage)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Please close the app and restart while an internet connection is available.")
        }
    }
}

@Composable
fun ExerciseSetupScreen(
    exerciseBookViewModel: ExerciseBookViewModel,
    onModelDelete: () -> Unit
) {
    val showDebug by exerciseBookViewModel.showDebug.collectAsState()

    // UI for choosing exercise type and starting exercises
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Choose Exercise Type", modifier = Modifier.clickable {
            exerciseBookViewModel.onSecretAreaTapped()
        })
        Spacer(modifier = Modifier.height(16.dp))
        // Add UI elements (e.g., Radio buttons, dropdowns) for selecting exercise type and level
        Button(onClick = {
            val config =
                ExerciseConfig(ExerciseType.ADDITION.id, 10, 2) // Replace with actual selection
            exerciseBookViewModel.startExercises(config)
            Log.i("ExerciseBookActivity", "Starting Addition")
        }) {
            Text("Start Addition")
        }

        Button(onClick = {
            val config = ExerciseConfig(
                ExerciseType.MISSING_ADDEND.id,
                10,
                2
            ) // Replace with actual selection
            exerciseBookViewModel.startExercises(config)
            Log.i("ExerciseBookActivity", "Starting Addition with missing addend")
        }) {
            Text("Start with missing addend")
        }

        Button(onClick = {
            val config =
                ExerciseConfig(ExerciseType.SUBTRACTION.id, 10, 2) // Replace with actual selection
            exerciseBookViewModel.startExercises(config)
            Log.i("ExerciseBookActivity", "Starting Subtraction")
        }) {
            Text("Start Subtraction")
        }

        Button(onClick = {
            val config = ExerciseConfig(
                ExerciseType.MISSING_SUBTRAHEND.id,
                10,
                2
            ) // Replace with actual selection
            exerciseBookViewModel.startExercises(config)
            Log.i("ExerciseBookActivity", "Starting Subtraction with missing subtrahend")
        }) {
            Text("Start Subtraction with missing subtrahend")
        }

        Button(onClick = {
            val config = ExerciseConfig(
                ExerciseType.MULTIPLICATION.id,
                10,
                2
            ) // Replace with actual selection
            exerciseBookViewModel.startExercises(config)
            Log.i("ExerciseBookActivity", "Starting Multiplication")
        }) {
            Text("Start Multiplication")
        }

        Spacer(modifier = Modifier.height(64.dp))
        // Leave in for now
        if (showDebug) {
            Button(onClick = { onModelDelete() }) {
                Text("Delete model")
            }
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
    val viewModel: ExerciseBookViewModel = viewModel()
    AppTheme {
        ExerciseSetupScreen(viewModel) {}
    }
}

