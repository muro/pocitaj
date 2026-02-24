package dev.aidistillery.pocitaj.ui.history.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.aidistillery.pocitaj.R
import dev.aidistillery.pocitaj.ui.theme.AppTheme

@Composable
fun HabitBuilderHeader(
    currentStreak: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp, horizontal = 24.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val streakText = if (currentStreak > 0) {
            stringResource(R.string.habit_streak_active, currentStreak)
        } else {
            stringResource(R.string.habit_streak_empty)
        }

        Text(
            text = streakText,
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    showBackground = true,
    name = "Light Mode - Active Streak"
)
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
    name = "Dark Mode - Active Streak"
)
@Composable
fun HabitBuilderHeaderActivePreview() {
    AppTheme {
        Surface {
            HabitBuilderHeader(currentStreak = 5)
        }
    }
}

@Preview(
    showBackground = true,
    name = "No Streak"
)
@Composable
fun HabitBuilderHeaderEmptyPreview() {
    AppTheme {
        Surface {
            HabitBuilderHeader(currentStreak = 0)
        }
    }
}
