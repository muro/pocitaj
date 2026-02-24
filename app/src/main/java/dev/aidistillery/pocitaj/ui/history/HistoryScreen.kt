package dev.aidistillery.pocitaj.ui.history

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.aidistillery.pocitaj.data.ExerciseAttempt
import dev.aidistillery.pocitaj.ui.theme.AppTheme

@Composable
fun HistoryScreen(uiState: HistoryUiState = HistoryUiState()) {
    // Temporary Placeholder UI for Phase 1 (Data Layer verification)
    // Full UI will be implemented in Phase 2
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Phase 1: Activity Center Data Layer", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(32.dp))
        Text("Current Streak: ${uiState.currentStreak}")
        Text("Today's Count: ${uiState.todaysCount}")
        Spacer(modifier = Modifier.height(16.dp))
        Text("Highlights:")
        uiState.todaysHighlights.forEach { highlight ->
            Text("${highlight.icon} ${dev.aidistillery.pocitaj.App.app.getString(highlight.titleResId)}")
        }
    }
}

/**
 * Formats an ExerciseAttempt into a display-ready string for the history screen.
 * It correctly handles different equation types, including those with missing operands.
 */
fun ExerciseAttempt.toHistoryString(): String {
    return problemText.replace("?", submittedAnswer.toString())
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
fun HistoryScreenPreview() {
    AppTheme {
        Surface {
            HistoryScreen(
                uiState = HistoryUiState(
                    currentStreak = 5,
                    todaysCount = 15
                )
            )
        }
    }
}
