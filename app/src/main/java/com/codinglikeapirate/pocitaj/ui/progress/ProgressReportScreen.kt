package com.codinglikeapirate.pocitaj.ui.progress

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.codinglikeapirate.pocitaj.data.FactMastery
import java.util.Locale

@Composable
fun ProgressReportScreen(
    viewModel: ProgressReportViewModel = viewModel(factory = ProgressReportViewModelFactory)
) {
    val groupedFacts by viewModel.groupedFacts.collectAsState()

    if (groupedFacts.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No progress data yet.")
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            items(groupedFacts.entries.toList()) { (operation, facts) ->
                Text(
                    text = operation.replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase(
                            Locale.getDefault()
                        ) else it.toString()
                    },
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Heatmap(facts)
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun Heatmap(facts: List<FactMastery>) {
    val factsByFirstNum = facts.groupBy { it.factId.split("-")[1].toInt() }
    val maxSecondNum = facts.maxOfOrNull { it.factId.split("-")[2].toInt() } ?: 0

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        for (i in 0..factsByFirstNum.keys.maxOrNull()!!) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                for (j in 0..maxSecondNum) {
                    val fact = factsByFirstNum[i]?.find { it.factId.split("-")[2].toInt() == j }
                    val strength = fact?.strength ?: 0
                    val color = when {
                        strength > 4 -> Color.Green
                        strength > 2 -> Color.Yellow
                        strength > 0 -> Color.Red
                        else -> Color.LightGray
                    }
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(color),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = strength.toString(),
                            fontSize = 10.sp,
                            color = Color.Black,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
