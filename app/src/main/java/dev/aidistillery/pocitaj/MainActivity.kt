package dev.aidistillery.pocitaj

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
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
import dev.aidistillery.pocitaj.data.ExerciseConfig
import dev.aidistillery.pocitaj.logic.Exercise
import dev.aidistillery.pocitaj.ui.credits.CreditsScreen
import dev.aidistillery.pocitaj.ui.exercise.ExerciseScreen
import dev.aidistillery.pocitaj.ui.exercise.ExerciseViewModel
import dev.aidistillery.pocitaj.ui.exercise.ExerciseViewModelFactory
import dev.aidistillery.pocitaj.ui.exercise.NavigationEvent
import dev.aidistillery.pocitaj.ui.exercise.ResultsScreen
import dev.aidistillery.pocitaj.ui.exercise.UiState
import dev.aidistillery.pocitaj.ui.history.HistoryScreen
import dev.aidistillery.pocitaj.ui.history.HistoryViewModel
import dev.aidistillery.pocitaj.ui.history.HistoryViewModelFactory
import dev.aidistillery.pocitaj.ui.profile.UserProfileScreen
import dev.aidistillery.pocitaj.ui.profile.UserProfileViewModel
import dev.aidistillery.pocitaj.ui.profile.UserProfileViewModelFactory
import dev.aidistillery.pocitaj.ui.progress.ProgressContainerScreen
import dev.aidistillery.pocitaj.ui.progress.ProgressReportViewModel
import dev.aidistillery.pocitaj.ui.progress.ProgressReportViewModelFactory
import dev.aidistillery.pocitaj.ui.setup.ExerciseSetupScreen
import dev.aidistillery.pocitaj.ui.setup.ExerciseSetupViewModel
import dev.aidistillery.pocitaj.ui.setup.ExerciseSetupViewModelFactory
import dev.aidistillery.pocitaj.ui.setup.StartupScreen
import dev.aidistillery.pocitaj.ui.setup.StartupViewModel
import dev.aidistillery.pocitaj.ui.setup.StartupViewModelFactory
import dev.aidistillery.pocitaj.ui.theme.AppTheme


class MainActivity : ComponentActivity() {

    private val startupViewModel: StartupViewModel by viewModels { StartupViewModelFactory }

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
                        AppNavigation(::restartApp)
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

    private fun restartApp() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }
}

object Destinations {
    const val HOME_ROUTE = "home"
    const val EXERCISE_ROUTE = "exercise/{type}"
    const val SUMMARY_ROUTE = "summary"
    const val PROGRESS_ROUTE = "progress"
    const val HISTORY_ROUTE = "history"
    const val CREDITS_ROUTE = "credits"
    const val PROFILE_ROUTE = "profile"
    fun exerciseDetailRoute(type: String) = "exercise/$type"
}

@Composable
fun AppNavigation(restartApp: () -> Unit) {
    val navController = rememberNavController()
    val exerciseViewModel: ExerciseViewModel = viewModel(factory = ExerciseViewModelFactory)
    val progressReportViewModel: ProgressReportViewModel =
        viewModel(factory = ProgressReportViewModelFactory)
    val historyViewModel: HistoryViewModel = viewModel(factory = HistoryViewModelFactory)
    val exerciseSetupViewModel: ExerciseSetupViewModel =
        viewModel(factory = ExerciseSetupViewModelFactory)
    val userProfileViewModel: UserProfileViewModel =
        viewModel(factory = UserProfileViewModelFactory)

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
                activeUser = exerciseSetupViewModel.activeUser,
                onStartClicked = { operation, count, difficulty, levelId ->
                    val config = ExerciseConfig(operation, difficulty, count, levelId)
                    exerciseViewModel.startExercises(config)
                },
                onProgressClicked = {
                    navController.navigate(Destinations.PROGRESS_ROUTE)
                },
                onCreditsClicked = {
                    navController.navigate(Destinations.CREDITS_ROUTE)
                },
                onProfileClicked = {
                    navController.navigate(Destinations.PROFILE_ROUTE)
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
        composable(route = Destinations.CREDITS_ROUTE) {
            CreditsScreen {
                navController.navigateUp()
            }
        }
        composable(route = Destinations.PROFILE_ROUTE) {
            val users by userProfileViewModel.users.collectAsState()
            val activeUser = exerciseSetupViewModel.activeUser
            UserProfileScreen(
                users = users,
                onUserSelected = { userId ->
                    userProfileViewModel.setActiveUser(userId)
                    restartApp()
                },
                onAddUserClicked = {
                    // TODO: Navigate to an "Add User" screen
                }
            )
        }
    }
}
