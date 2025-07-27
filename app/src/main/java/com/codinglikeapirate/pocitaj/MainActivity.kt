package com.codinglikeapirate.pocitaj

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.codinglikeapirate.pocitaj.data.ExerciseConfig
import com.codinglikeapirate.pocitaj.logic.Exercise
import com.codinglikeapirate.pocitaj.ui.exercise.ExerciseScreen
import com.codinglikeapirate.pocitaj.ui.exercise.ExerciseViewModel
import com.codinglikeapirate.pocitaj.ui.exercise.ExerciseViewModelFactory
import com.codinglikeapirate.pocitaj.ui.exercise.NavigationEvent
import com.codinglikeapirate.pocitaj.ui.exercise.ResultsScreen
import com.codinglikeapirate.pocitaj.ui.exercise.UiState
import com.codinglikeapirate.pocitaj.ui.history.HistoryScreen
import com.codinglikeapirate.pocitaj.ui.history.HistoryViewModel
import com.codinglikeapirate.pocitaj.ui.history.HistoryViewModelFactory
import com.codinglikeapirate.pocitaj.ui.progress.ProgressContainerScreen
import com.codinglikeapirate.pocitaj.ui.progress.ProgressReportViewModel
import com.codinglikeapirate.pocitaj.ui.progress.ProgressReportViewModelFactory
import com.codinglikeapirate.pocitaj.ui.setup.ExerciseSetupScreen
import com.codinglikeapirate.pocitaj.ui.setup.ExerciseSetupViewModel
import com.codinglikeapirate.pocitaj.ui.setup.ExerciseSetupViewModelFactory
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

                AnimatedContent(
                    targetState = isInitialized,
                    label = "app_navigation",
                    transitionSpec = {
                        fadeIn() togetherWith fadeOut()
                    }
                ) { targetState ->
                    if (targetState) {
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
}

object Destinations {
    const val HOME_ROUTE = "home"
    const val EXERCISE_ROUTE = "exercise/{type}"
    const val SUMMARY_ROUTE = "summary"
    const val PROGRESS_ROUTE = "progress"
    const val HISTORY_ROUTE = "history"
    fun exerciseDetailRoute(type: String) = "exercise/$type"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val exerciseViewModel: ExerciseViewModel = viewModel(factory = ExerciseViewModelFactory)
    val progressReportViewModel: ProgressReportViewModel = viewModel(factory = ProgressReportViewModelFactory)
    val historyViewModel: HistoryViewModel = viewModel(factory = HistoryViewModelFactory)
    val exerciseSetupViewModel: ExerciseSetupViewModel = viewModel(factory = ExerciseSetupViewModelFactory)

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
        startDestination = Destinations.HOME_ROUTE,
        enterTransition = { slideInHorizontally(initialOffsetX = { 1000 }) },
        exitTransition = { slideOutHorizontally(targetOffsetX = { -1000 }) },
        popEnterTransition = { slideInHorizontally(initialOffsetX = { -1000 }) },
        popExitTransition = { slideOutHorizontally(targetOffsetX = { 1000 }) }
    ) {
        composable(route = Destinations.HOME_ROUTE) {
            val operationLevels by exerciseSetupViewModel.operationLevels.collectAsState()
            ExerciseSetupScreen(
                operationLevels = operationLevels,
                onStartClicked = { operation, count, difficulty, levelId ->
                    val config = ExerciseConfig(operation, difficulty, count, levelId)
                    exerciseViewModel.startExercises(config)
                },
                onProgressClicked = {
                    navController.navigate(Destinations.PROGRESS_ROUTE)
                }
            )
        }
        composable(
            route = Destinations.EXERCISE_ROUTE,
        ) {
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
            val factProgressByOperation by progressReportViewModel.factProgressByOperation.collectAsState()
            val levelProgressByOperation by progressReportViewModel.levelProgressByOperation.collectAsState()
            val history by historyViewModel.historyByDate.collectAsState()
            ProgressContainerScreen(
                factProgressByOperation = factProgressByOperation,
                levelProgressByOperation = levelProgressByOperation,
                history = history
            )
        }
        composable(route = Destinations.HISTORY_ROUTE) {
            val history by historyViewModel.historyByDate.collectAsState()
            HistoryScreen(history = history)
        }
    }
}