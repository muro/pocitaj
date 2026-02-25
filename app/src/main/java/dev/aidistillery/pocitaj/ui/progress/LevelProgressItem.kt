package dev.aidistillery.pocitaj.ui.progress

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.aidistillery.pocitaj.data.FactMastery
import dev.aidistillery.pocitaj.logic.Curriculum
import dev.aidistillery.pocitaj.logic.SpeedBadge
import dev.aidistillery.pocitaj.logic.getLevelDisplayName
import dev.aidistillery.pocitaj.ui.theme.AppTheme

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LevelProgressItem(
    levelId: String,
    progress: LevelProgress,
    factProgress: List<FactProgress>,
    initiallyExpanded: Boolean = false
) {
    var expanded by remember { mutableStateOf(initiallyExpanded) }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        label = "level_scale"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { expanded = !expanded }
            .testTag("level_row_$levelId")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            CircularProgressIndicator(
                progress = { progress.progress },
                modifier = Modifier
                    .size(32.dp)
                    .testTag("progress_${levelId}_${"%.1f".format(progress.progress)}")
            )
            Text(
                text = stringResource(id = getLevelDisplayName(levelId)),
                style = MaterialTheme.typography.titleMedium
            )
        }

        AnimatedVisibility(visible = expanded) {
            if (progress.progress > 0f && progress.progress < 1f) {
                val level = Curriculum.getAllLevels().find { it.id == levelId }
                if (level != null) {
                    val levelFacts = level.getAllPossibleFactIds()

                    val allWeakFacts = factProgress
                        .filter {
                            it.factId in levelFacts &&
                                    it.mastery != null &&
                                    it.mastery.strength < 5
                        }
                        .sortedWith(
                            compareBy<FactProgress> { it.mastery?.strength ?: 0 }
                                .thenBy { it.factId }
                        )

                    val showOverflow = allWeakFacts.size > 6
                    val renderFacts =
                        if (showOverflow) allWeakFacts.take(5) else allWeakFacts.take(6)

                    if (renderFacts.isNotEmpty()) {
                        FlowRow(
                            modifier = Modifier.padding(top = 8.dp, start = 48.dp, bottom = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            renderFacts.forEach { weakFact ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.errorContainer,
                                    modifier = Modifier.testTag("weak_fact_${weakFact.factId}")
                                ) {
                                    Text(
                                        text = formatFactIdForDisplay(weakFact.factId),
                                        modifier = Modifier.padding(
                                            horizontal = 8.dp,
                                            vertical = 4.dp
                                        ),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                            if (showOverflow) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.testTag("weak_fact_overflow")
                                ) {
                                    Text(
                                        text = "+${allWeakFacts.size - 5}",
                                        modifier = Modifier.padding(
                                            horizontal = 8.dp,
                                            vertical = 4.dp
                                        ),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun formatFactIdForDisplay(factId: String): String {
    return factId.replace(" = ?", "")
}

@Preview(showBackground = true, name = "Light Mode - Collapsed")
@Preview(
    showBackground = true,
    name = "Dark Mode - Collapsed",
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun LevelProgressItemCollapsedPreview() {
    PreviewContainer {
        LevelProgressItem(
            levelId = Curriculum.SumsUpTo10.id,
            progress = LevelProgress(0.3f, false),
            factProgress = emptyList()
        )
    }
}

@Preview(showBackground = true, name = "Light Mode - Expanded")
@Preview(
    showBackground = true,
    name = "Dark Mode - Expanded",
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun LevelProgressItemExpandedPreview() {
    PreviewContainer {
        LevelProgressItem(
            levelId = Curriculum.SumsUpTo10.id,
            progress = LevelProgress(0.7f, false),
            factProgress = listOf(
                FactProgress(
                    "3 + 7 = ?",
                    FactMastery("3 + 7 = ?", 1, "ADD_SUM_10", 1, 0),
                    SpeedBadge.NONE
                ),
                FactProgress(
                    "4 + 6 = ?",
                    FactMastery("4 + 6 = ?", 1, "ADD_SUM_10", 2, 0),
                    SpeedBadge.NONE
                ),
                FactProgress(
                    "2 + 8 = ?",
                    FactMastery("2 + 8 = ?", 1, "ADD_SUM_10", 3, 0),
                    SpeedBadge.NONE
                ),
                FactProgress(
                    "7 + 3 = ?",
                    FactMastery("3 + 7 = ?", 1, "ADD_SUM_10", 1, 0),
                    SpeedBadge.NONE
                ),
                FactProgress(
                    "6 + 4 = ?",
                    FactMastery("4 + 6 = ?", 1, "ADD_SUM_10", 2, 0),
                    SpeedBadge.NONE
                ),
                FactProgress(
                    "8 + 2 = ?",
                    FactMastery("2 + 8 = ?", 1, "ADD_SUM_10", 3, 0),
                    SpeedBadge.NONE
                ),
                FactProgress(
                    "9 + 1 = ?",
                    FactMastery("1 + 9 = ?", 1, "ADD_SUM_10", 1, 0),
                    SpeedBadge.NONE
                )
            ),
            initiallyExpanded = true
        )
    }
}

@Composable
private fun PreviewContainer(content: @Composable () -> Unit) {
    AppTheme {
        Surface(
            modifier = Modifier.padding(16.dp),
            color = MaterialTheme.colorScheme.background
        ) {
            content()
        }
    }
}
