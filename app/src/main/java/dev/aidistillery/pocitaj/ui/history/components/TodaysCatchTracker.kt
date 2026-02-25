package dev.aidistillery.pocitaj.ui.history.components

import android.content.res.Configuration
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.aidistillery.pocitaj.R
import dev.aidistillery.pocitaj.ui.theme.AppTheme
import dev.aidistillery.pocitaj.ui.theme.motion
import java.time.LocalDate

private val SUSHI_ICONS = listOf(
    R.drawable.sushi_02_nigiri_amaebi,
    R.drawable.sushi_02_nigiri_tamago,
    R.drawable.sushi_02_nigiri_unagi,
    R.drawable.sushi_03_gunkanmaki_ikura,
    R.drawable.sushi_03_nigiri_ebi,
    R.drawable.sushi_03_nigiri_sake,
    R.drawable.sushi_03_nigiri_tai
)

@Composable
fun TodaysCatchTracker(
    todaysCount: Int,
    modifier: Modifier = Modifier
) {
    val thresholds = listOf(10, 30, 50)

    // Pick a random icon for each threshold, but keep them stable for the entire day
    val selectedIcons = remember {
        val seed = LocalDate.now().toEpochDay()
        getStableSushiIcons(seed, thresholds.size)
    }

    // Determine title text based on progress
    val titleText = when {
        todaysCount >= 50 -> stringResource(R.string.rewards_three_earned)
        todaysCount >= 30 -> stringResource(R.string.rewards_two_earned)
        todaysCount >= 10 -> stringResource(R.string.rewards_one_earned)
        todaysCount > 0 -> stringResource(R.string.rewards_lets_earn)
        else -> stringResource(R.string.rewards_zero_earned)
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 6.dp,
        tonalElevation = 4.dp,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = titleText,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                thresholds.forEachIndexed { index, threshold ->
                    val isAchieved = todaysCount >= threshold
                    MilestoneReward(
                        threshold = threshold,
                        isAchieved = isAchieved,
                        iconResId = selectedIcons[index]
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.exercises_completed_today, todaysCount),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun MilestoneReward(
    threshold: Int,
    isAchieved: Boolean,
    iconResId: Int,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        val isPreview = LocalInspectionMode.current
        val scale = if (isAchieved) {
            if (isPreview) {
                1.05f // Middle of 1.0f to 1.1f
            } else {
                val infiniteTransition = rememberInfiniteTransition(label = "starPulse")
                val scaleAnim by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(
                            durationMillis = MaterialTheme.motion.pulse,
                            easing = FastOutSlowInEasing
                        ),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "pulseScale"
                )
                scaleAnim
            }
        } else {
            1f
        }

        Box(
            modifier = Modifier
                .size(64.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .clip(CircleShape)
                .background(
                    if (isAchieved) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surfaceVariant
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = iconResId),
                contentDescription = stringResource(R.string.reward_content_description, threshold),
                modifier = Modifier.size(40.dp),
                tint = if (isAchieved) Color.Unspecified else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
            )

            if (isAchieved) {
                // Little checkmark badge
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(2.dp)
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = stringResource(R.string.achieved_content_description),
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "$threshold",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = if (isAchieved) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true, name = "Empty")
@Composable
fun TodaysCatchTrackerEmptyPreview() {
    AppTheme { Surface { TodaysCatchTracker(todaysCount = 0) } }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true, name = "1 Reward")
@Composable
fun TodaysCatchTrackerOnePreview() {
    AppTheme { Surface { TodaysCatchTracker(todaysCount = 15) } }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true, name = "All Rewards")
@Composable
fun TodaysCatchTrackerAllPreview() {
    AppTheme { Surface { TodaysCatchTracker(todaysCount = 55) } }
}

internal fun getStableSushiIcons(seed: Long, count: Int): List<Int> {
    val random = kotlin.random.Random(seed)
    return SUSHI_ICONS.shuffled(random).take(count)
}
