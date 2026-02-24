package dev.aidistillery.pocitaj.ui.history

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.aidistillery.pocitaj.logic.SmartHighlight
import dev.aidistillery.pocitaj.ui.history.components.HabitBuilderHeader
import dev.aidistillery.pocitaj.ui.history.components.SmartHighlightCard
import dev.aidistillery.pocitaj.ui.history.components.TodaysCatchTracker
import dev.aidistillery.pocitaj.ui.theme.AppTheme

@Composable
fun HistoryScreen(uiState: HistoryUiState = HistoryUiState()) {
    // State logic to trigger entry animations on first composition
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(500, delayMillis = 100)) + slideInVertically(
                    animationSpec = tween(500, delayMillis = 100),
                    initialOffsetY = { -it / 2 }
                )
            ) {
                HabitBuilderHeader(currentStreak = uiState.currentStreak)
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(500, delayMillis = 250)) + slideInVertically(
                    animationSpec = tween(500, delayMillis = 250),
                    initialOffsetY = { it / 2 }
                )
            ) {
                TodaysCatchTracker(todaysCount = uiState.todaysCount)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        items(uiState.todaysHighlights) { highlight ->
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(500, delayMillis = 400)) + slideInVertically(
                    animationSpec = tween(500, delayMillis = 400),
                    initialOffsetY = { it / 2 }
                )
            ) {
                SmartHighlightCard(highlight = highlight)
            }
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
fun HistoryScreenPreview() {
    AppTheme {
        Surface {
            HistoryScreen(
                uiState = HistoryUiState(
                    currentStreak = 5,
                    todaysCount = 15,
                    todaysHighlights = listOf(
                        SmartHighlight.SpeedyPaws(3),
                        SmartHighlight.PerfectPrecision()
                    )
                )
            )
        }
    }
}

