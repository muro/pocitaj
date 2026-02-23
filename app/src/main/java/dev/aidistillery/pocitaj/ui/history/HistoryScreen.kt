package dev.aidistillery.pocitaj.ui.history

import android.content.res.Configuration
import android.icu.text.SimpleDateFormat
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.aidistillery.pocitaj.R
import dev.aidistillery.pocitaj.data.ExerciseAttempt
import dev.aidistillery.pocitaj.data.Operation
import dev.aidistillery.pocitaj.ui.theme.AppTheme
import java.time.LocalDate
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    uiState: HistoryUiState = HistoryUiState(),
    onDateSelected: (LocalDate) -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxSize()) {
        ActivityHeatmap(
            dailyActivity = uiState.dailyActivity,
            selectedDate = uiState.selectedDate,
            onDateSelected = onDateSelected
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (uiState.filteredHistory.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(id = R.drawable.sleepy),
                        contentDescription = null,
                        modifier = Modifier.size(128.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        stringResource(id = R.string.no_history_for_date),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            HistoryList(uiState = uiState, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun ActivityHeatmap(
    dailyActivity: Map<LocalDate, Int>,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val today = LocalDate.now()
    // Start from the Monday 4 weeks before the current week
    val firstDayOfWeek = today.with(java.time.DayOfWeek.MONDAY).minusWeeks(4)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .testTag("activity_heatmap")
    ) {
        Text(
            text = stringResource(id = R.string.activity_center),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Column {
            for (week in 0..4) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    for (day in 0..6) {
                        val date = firstDayOfWeek.plusDays((week * 7 + day).toLong())
                        val count = dailyActivity[date] ?: 0
                        val tier = when {
                            count >= 50 -> 3
                            count >= 30 -> 2
                            count >= 10 -> 1
                            else -> 0
                        }
                        val isFuture = date.isAfter(today)

                        if (!isFuture) {
                            HeatmapDay(
                                date = date,
                                tier = tier,
                                isSelected = date == selectedDate,
                                onClick = { onDateSelected(date) }
                            )
                        } else {
                            // Placeholder for future days
                            Box(modifier = Modifier.size(32.dp))
                        }
                    }
                }
                if (week < 4) Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

@Composable
fun HeatmapDay(
    date: LocalDate,
    tier: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = when (tier) {
        3 -> Color(0xFFFFD700) // Gold
        2 -> Color(0xFFC0C0C0) // Silver
        1 -> Color(0xFFCD7F32) // Bronze
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        Color.Transparent
    }

    Surface(
        onClick = onClick,
        modifier = Modifier
            .size(32.dp)
            .testTag("heatmap_day_$date"),
        shape = MaterialTheme.shapes.extraSmall,
        color = backgroundColor,
        border = androidx.compose.foundation.BorderStroke(2.dp, borderColor)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = date.dayOfMonth.toString(),
                style = MaterialTheme.typography.labelSmall,
                color = if (tier > 0) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun HistoryList(
    uiState: HistoryUiState,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        items(uiState.filteredHistory) { attempt ->
            HistoryItem(attempt = attempt)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun HistoryItem(attempt: ExerciseAttempt) {
    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    val time = timeFormat.format(Date(attempt.timestamp))
    val durationInSeconds = attempt.durationMs / 1000.0

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (attempt.wasCorrect) Icons.Default.Check else Icons.Default.Close,
                contentDescription = if (attempt.wasCorrect) "Correct" else "Incorrect",
                tint = if (attempt.wasCorrect) Color.Green else Color.Red
            )
            Spacer(
                modifier = Modifier
                    .width(8.dp)
                    .testTag("history_exercise_text")
            )
            Text(
                text = attempt.toHistoryString(),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "%.1fs".format(durationInSeconds),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = time,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
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
fun HistoryScreenPreview() {
    AppTheme {
        val attempts = listOf(
            ExerciseAttempt(
                id = 1,
                userId = 1,
                timestamp = System.currentTimeMillis(),
                problemText = "2 + 3 = ?",
                logicalOperation = Operation.ADDITION,
                correctAnswer = 5,
                submittedAnswer = 5,
                wasCorrect = true,
                durationMs = 2000
            ),
            ExerciseAttempt(
                id = 2,
                userId = 1,
                timestamp = System.currentTimeMillis() - 10000,
                problemText = "10 * 2 = ?",
                logicalOperation = Operation.MULTIPLICATION,
                correctAnswer = 20,
                submittedAnswer = 20,
                wasCorrect = true,
                durationMs = 1500
            )
        )
        Surface {
            HistoryScreen(
                uiState = HistoryUiState(
                    filteredHistory = attempts
                )
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
fun HistoryScreenEmptyPreview() {
    AppTheme {
        HistoryScreen()
    }
}
