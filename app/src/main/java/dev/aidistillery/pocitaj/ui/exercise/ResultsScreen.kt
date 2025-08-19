package dev.aidistillery.pocitaj.ui.exercise

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.aidistillery.pocitaj.R
import dev.aidistillery.pocitaj.logic.SpeedBadge
import dev.aidistillery.pocitaj.ui.components.PocitajScreen
import dev.aidistillery.pocitaj.ui.theme.AppTheme
import dev.aidistillery.pocitaj.ui.theme.customColors
import kotlinx.coroutines.delay
import java.util.Locale

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 16.dp, horizontal = 16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = onDone, Modifier.testTag("Back")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        tint = MaterialTheme.colorScheme.onSurface,
                        contentDescription = stringResource(id = R.string.navigate_back)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.results_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            ResultsList(
                results,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
fun ResultsList(results: List<ResultDescription>, modifier: Modifier = Modifier) {
    val isPreview = LocalInspectionMode.current
    val visible = remember { mutableStateOf(isPreview) }
    val lazyListState = rememberLazyListState()

    val showTopFade by remember { derivedStateOf { lazyListState.canScrollBackward } }
    val showBottomFade by remember { derivedStateOf { lazyListState.canScrollForward } }

    if (!isPreview) {
        LaunchedEffect(Unit) {
            delay(100)
            visible.value = true
        }
    }

    Box(modifier = modifier) {
        LazyColumn(
            state = lazyListState,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(results) { index, result ->
                AnimatedVisibility(
                    visible = visible.value,
                    enter = fadeIn(
                        animationSpec = tween(
                            durationMillis = 500,
                            delayMillis = index * 100
                        )
                    ) +
                            slideInVertically(
                                initialOffsetY = { it / 2 },
                                animationSpec = tween(
                                    durationMillis = 500,
                                    delayMillis = index * 100
                                )
                            )
                ) {
                    ResultCard(result, modifier = Modifier.fillMaxWidth())
                }
            }
        }

        val fadeColor = MaterialTheme.customColors.backgroundGradientStart
        AnimatedVisibility(
            visible = showTopFade || isPreview,
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(fadeColor, Color.Transparent)
                        )
                    )
            )
        }

        AnimatedVisibility(
            visible = showBottomFade || isPreview,
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.Transparent, fadeColor)
                        )
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
    name = "Dark Mode Small Phone",
    device = "id:small_phone"
)
@Composable
fun PreviewResultsList() {
    val results = ArrayList<ResultDescription>()
    results.add(ResultDescription("2 + 2 = 4", ResultStatus.CORRECT, 1000, SpeedBadge.GOLD))
    results.add(ResultDescription("3 + 3 ≠ 5", ResultStatus.INCORRECT, 2100, SpeedBadge.SILVER))
    results.add(ResultDescription("123 + 456 = 579", ResultStatus.CORRECT, 123, SpeedBadge.BRONZE))
    results.add(ResultDescription("1 + 2 = 4", ResultStatus.INCORRECT, 123, SpeedBadge.NONE))
    results.add(ResultDescription("2 + 2 = 4", ResultStatus.CORRECT, 1000, SpeedBadge.GOLD))

    AppTheme {
        ResultsList(results)
    }
}

@Composable
fun ResultCard(result: ResultDescription, modifier: Modifier = Modifier) {
    val borderOffset = 80f
    val textColor = MaterialTheme.colorScheme.onSurfaceVariant
    val borderModifier = if (result.status == ResultStatus.INCORRECT) {
        Modifier.border(
            width = 2.dp,
            brush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFF8A2BE2),
                    Color(0xFFFFA500),
                    Color.Transparent
                ),
                start = androidx.compose.ui.geometry.Offset(0f, 0f),
                end = androidx.compose.ui.geometry.Offset(2 * borderOffset, borderOffset)
            ),
            shape = MaterialTheme.shapes.medium
        )
    } else {
        Modifier
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(borderModifier)
    ) {
        Box {
            Row(
                modifier = Modifier
                    .padding(8.dp)
                    .height(64.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    painter = painterResource(
                        when (result.status) {
                            ResultStatus.CORRECT -> R.drawable.excited_face
                            ResultStatus.INCORRECT -> R.drawable.sad_face
                            ResultStatus.NOT_RECOGNIZED -> R.drawable.confused_face
                        }
                    ),
                    contentDescription = "Result Status",
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .fillMaxHeight()
                        .aspectRatio(1f)
                        .width(16.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))

                BasicText(
                    text = result.equation,
                    style = MaterialTheme.typography.headlineMedium,
                    color = { textColor },
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    autoSize = TextAutoSize.StepBased(
                        minFontSize = 8.sp,
                        maxFontSize = 50.sp,
                        stepSize = 2.sp // The size difference between each step
                    )
                )
                Text(
                    text = String.format(Locale.US, "%.1fs", result.elapsedMs / 1000.0),
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
            SpeedBadgeIndicator(
                badge = if (result.status == ResultStatus.CORRECT) result.speedBadge else SpeedBadge.NONE,
                modifier = Modifier.align(Alignment.TopEnd)
            )
        }
    }
}

@Composable
fun SpeedBadgeIndicator(badge: SpeedBadge, modifier: Modifier = Modifier) {
    val badgeBrush = when (badge) {
        SpeedBadge.GOLD -> dev.aidistillery.pocitaj.ui.theme.speedBadgeGold
        SpeedBadge.SILVER -> dev.aidistillery.pocitaj.ui.theme.speedBadgeSilver
        SpeedBadge.BRONZE -> dev.aidistillery.pocitaj.ui.theme.speedBadgeBronze
        SpeedBadge.NONE -> null
    }

    if (badgeBrush != null) {
        Canvas(
            modifier = modifier
                .width(36.dp)
                .height(36.dp)
        ) {
            val path = Path().apply {
                moveTo(size.width, 0f)
                lineTo(size.width, size.height)
                lineTo(0f, 0f)
                close()
            }
            drawPath(path, brush = badgeBrush)
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
        ResultCard(
            ResultDescription(
                "123 + 456 = 579",
                ResultStatus.CORRECT,
                123,
                SpeedBadge.SILVER
            )
        )
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
        results.add(
            ResultDescription(
                "$i + ${i + 2} = ${2 * i + 2}",
                ResultStatus.CORRECT,
                1234,
                SpeedBadge.GOLD
            )
        )
        results.add(
            ResultDescription(
                "$i + ${i + 1} ≠ $i",
                ResultStatus.CORRECT,
                1,
                SpeedBadge.BRONZE
            )
        )
    }
    AppTheme {
        ResultsScreen(results) {}
    }
}
