package com.codinglikeapirate.pocitaj.ui.history

import android.icu.text.SimpleDateFormat
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.codinglikeapirate.pocitaj.R
import com.codinglikeapirate.pocitaj.data.ExerciseAttempt
import com.codinglikeapirate.pocitaj.data.Operation
import com.codinglikeapirate.pocitaj.ui.theme.AppTheme
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    history: Map<String, List<ExerciseAttempt>>
) {
    if (history.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(stringResource(id = R.string.no_history_yet))
        }
    } else {
        HistoryList(history = history)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HistoryList(history: Map<String, List<ExerciseAttempt>>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        history.forEach { (date, attempts) ->
            stickyHeader {
                Text(
                    text = date,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(vertical = 8.dp)
                )
            }
            items(attempts) { attempt ->
                HistoryItem(attempt = attempt)
                Spacer(modifier = Modifier.height(8.dp))
            }
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
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "${attempt.problemText} = ${attempt.submittedAnswer}")
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(text = "%.1fs".format(durationInSeconds), style = MaterialTheme.typography.bodySmall)
            Text(text = time, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HistoryScreenPreview() {
    AppTheme {
        val history = mapOf(
            "Jul 13, 2025" to listOf(
                ExerciseAttempt(problemText = "2 + 2", submittedAnswer = 4, wasCorrect = true, correctAnswer = 4, durationMs = 1000, timestamp = System.currentTimeMillis(), userId = 1, logicalOperation = Operation.ADDITION),
                ExerciseAttempt(problemText = "3 + 3", submittedAnswer = 5, wasCorrect = false, correctAnswer = 6, durationMs = 2100, timestamp = System.currentTimeMillis() - 1000 * 60 * 5, userId = 1, logicalOperation = Operation.ADDITION),
            ),
            "Jul 12, 2025" to listOf(
                ExerciseAttempt(problemText = "4 + 4", submittedAnswer = 8, wasCorrect = true, correctAnswer = 8, durationMs = 1500, timestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 24, userId = 1, logicalOperation = Operation.ADDITION)
            )
        )
        HistoryScreen(history = history)
    }
}

@Preview(showBackground = true)
@Composable
fun HistoryScreenEmptyPreview() {
    AppTheme {
        HistoryScreen(history = emptyMap())
    }
}
