package com.codinglikeapirate.pocitaj

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import com.codinglikeapirate.pocitaj.data.ExerciseConfig
import com.codinglikeapirate.pocitaj.logic.Exercise
import com.codinglikeapirate.pocitaj.ui.exercise.ExerciseBookViewModel
import com.codinglikeapirate.pocitaj.ui.exercise.ExerciseBookViewModelFactory
import com.codinglikeapirate.pocitaj.ui.exercise.ExerciseScreen
import com.codinglikeapirate.pocitaj.ui.exercise.NavigationEvent
import com.codinglikeapirate.pocitaj.ui.exercise.ResultsScreen
import com.codinglikeapirate.pocitaj.ui.exercise.UiState
import com.codinglikeapirate.pocitaj.ui.progress.ProgressReportScreen
import com.codinglikeapirate.pocitaj.ui.setup.ExerciseSetupScreen
import com.codinglikeapirate.pocitaj.ui.setup.StartupScreen
import com.codinglikeapirate.pocitaj.ui.setup.StartupViewModel
import com.codinglikeapirate.pocitaj.ui.theme.AppTheme


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
                    StartupScreen(
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
    const val PROGRESS_ROUTE = "progress"
    fun exerciseDetailRoute(type: String) = "exercise/$type"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val exerciseViewModel: ExerciseBookViewModel = viewModel(factory = ExerciseBookViewModelFactory)

    LaunchedEffect(Unit) {
        exerciseViewModel.navigationEvents.collect { event ->
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
                    exerciseViewModel.startExercises(config)
                },
                onProgressClicked = {
                    navController.navigate(Destinations.PROGRESS_ROUTE)
                }
            )
        }
        composable(route = Destinations.EXERCISE_ROUTE) {
            val uiState by exerciseViewModel.uiState.collectAsState()
            val exerciseState = uiState as? UiState.ExerciseScreen
            if (exerciseState != null) {
                val exercise: Exercise = exerciseState.currentExercise
                ExerciseScreen(exercise, exerciseViewModel) { answer: String, elapsedMs: Int ->
                    exerciseViewModel.checkAnswer(answer, elapsedMs)
                }
            }
        }
        composable(route = Destinations.SUMMARY_ROUTE) {
            val uiState by exerciseViewModel.uiState.collectAsState()
            val summaryState = uiState as? UiState.SummaryScreen
            if (summaryState != null) {
                ResultsScreen(summaryState.results) {
                    exerciseViewModel.onSummaryDone()
                }
            }
        }
        composable(route = Destinations.PROGRESS_ROUTE) {
            ProgressReportScreen()
        }
    }
}