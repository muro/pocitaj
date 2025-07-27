package com.codinglikeapirate.pocitaj.ui.exercise

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.codinglikeapirate.pocitaj.R
import com.codinglikeapirate.pocitaj.ui.components.PocitajScreen
import com.codinglikeapirate.pocitaj.ui.theme.AppTheme
import java.util.Locale
import com.codinglikeapirate.pocitaj.logic.SpeedBadge
import com.codinglikeapirate.pocitaj.ui.theme.customColors

enum class ResultStatus {
    CORRECT, INCORRECT, NOT_RECOGNIZED;

    companion object {
        fun fromBooleanPair(recognized: Boolean, correct: Boolean): ResultStatus {
            return if (!recognized) {
                NOT_RECOGNIZED
            } else {
                if (correct) CORRECT else INCORRECT
            }
        }
    }
}

data class ResultDescription(
    val equation: String,
    val status: ResultStatus,
    val elapsedMs: Int,
    val speedBadge: SpeedBadge
)

@Composable
fun ResultsScreen(results: List<ResultDescription>, onDone: () -> Unit) {
    PocitajScreen {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(vertical = 16.dp)
        ) {
            ResultsList(
                results, modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        bottom = 72.dp, // Add padding at the bottom to make space for the button
                        start = 16.dp, end = 16.dp, top = 16.dp
                    )
            )
            Button(onClick = onDone, modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 32.dp)) {
                Text(stringResource(id = R.string.done))
            }
        }
    }
}

@Composable
fun ResultsList(results: List<ResultDescription>, modifier: Modifier = Modifier) {
    LazyColumn(modifier) {
        items(results) { result ->
            ResultCard(result, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewResultsList() {
    val results = ArrayList<ResultDescription>()
    results.add(ResultDescription("2 + 2 = 4", ResultStatus.CORRECT, 1000, SpeedBadge.GOLD))
    results.add(ResultDescription("3 + 3 ≠ 5", ResultStatus.INCORRECT, 2100, SpeedBadge.SILVER))
    results.add(ResultDescription("3 + 3 = ?", ResultStatus.NOT_RECOGNIZED, 3511, SpeedBadge.BRONZE))

    AppTheme {
        ResultsList(results)
    }
}

@Composable
fun ResultCard(result: ResultDescription, modifier: Modifier = Modifier) {
    val backgroundColor = when (result.speedBadge) {
        SpeedBadge.GOLD -> MaterialTheme.customColors.speedBadgeGold
        SpeedBadge.SILVER -> MaterialTheme.customColors.speedBadgeSilver
        SpeedBadge.BRONZE -> MaterialTheme.customColors.speedBadgeBronze
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val textColor = MaterialTheme.colorScheme.onSurface

    Surface(
        color = backgroundColor,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(
                    when (result.status) {
                        ResultStatus.CORRECT -> R.drawable.cat_heart
                        ResultStatus.INCORRECT -> R.drawable.cat_cry
                        ResultStatus.NOT_RECOGNIZED -> R.drawable.cat_big_eyes
                    }
                ),
                contentDescription = "Result Status",
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .fillMaxHeight()
                    .aspectRatio(1f)
            )
            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = result.equation,
                style = MaterialTheme.typography.headlineMedium,
                color = textColor,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = String.format(Locale.US, "%.1fs", result.elapsedMs / 1000.0),
                style = MaterialTheme.typography.bodyMedium,
                color = textColor,
                modifier = Modifier.padding(horizontal = 8.dp)
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
fun PreviewResultCard() {
    AppTheme {
        ResultCard(ResultDescription("2 + 2 = 4", ResultStatus.CORRECT, 123, SpeedBadge.SILVER))
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
fun PreviewResultCardSlow() {
    AppTheme {
        ResultCard(ResultDescription("2 + 2 = 4", ResultStatus.CORRECT, 5123, SpeedBadge.NONE))
    }
}

@Preview(
    device = "id:pixel_4",
    showSystemUi = true,
    name = "Pixel 4"
)
@Preview(
    device = "id:pixel_9_pro_xl",
    showSystemUi = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Pixel 9 Pro XL"
)
@Composable
fun PreviewResultsScreen() {
    val results = ArrayList<ResultDescription>()
    for (i in 1..5) {
        results.add(ResultDescription("$i + ${i + 2} = ${2 * i + 2}", ResultStatus.CORRECT, 1234, SpeedBadge.GOLD))
        results.add(ResultDescription("$i + ${i + 1} ≠ $i", ResultStatus.CORRECT, 1, SpeedBadge.BRONZE))
    }
    AppTheme {
        ResultsScreen(results) {}
    }
}
