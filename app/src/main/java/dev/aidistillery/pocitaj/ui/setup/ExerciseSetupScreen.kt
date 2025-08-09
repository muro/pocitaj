package dev.aidistillery.pocitaj.ui.setup

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.aidistillery.pocitaj.R
import dev.aidistillery.pocitaj.data.Operation
import dev.aidistillery.pocitaj.data.User
import dev.aidistillery.pocitaj.data.UserAppearance
import dev.aidistillery.pocitaj.data.toSymbol
import dev.aidistillery.pocitaj.logic.Curriculum
import dev.aidistillery.pocitaj.logic.formatLevel
import dev.aidistillery.pocitaj.ui.components.AutoSizeText
import dev.aidistillery.pocitaj.ui.components.PocitajScreen
import dev.aidistillery.pocitaj.ui.theme.AppTheme
import dev.aidistillery.pocitaj.ui.theme.getGradientForOperation

@Composable
fun ExerciseSetupScreen(
    operationLevels: List<OperationLevels>,
    activeUser: User,
    onStartClicked: (operation: Operation, count: Int, difficulty: Int, levelId: String?) -> Unit,
    onProgressClicked: () -> Unit,
    onCreditsClicked: () -> Unit,
    onProfileClicked: () -> Unit,
) {
    PocitajScreen {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(top = 32.dp), // Add top padding to avoid camera cutout
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onProfileClicked,
                    modifier = Modifier.testTag("user_profile_${activeUser.name}")
                        .padding(end = 8.dp)
                ) {
                    val iconRes = UserAppearance.icons[activeUser.iconId]
                    if (iconRes != null) {
                        Icon(
                            painter = painterResource(id = iconRes),
                            contentDescription = stringResource(id = R.string.user_profile),
                            tint = Color(activeUser.color)
                        )
                    }
                }
                Text(
                    text = activeUser.name,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = onProgressClicked) {
                    Icon(
                        imageVector = Icons.Default.BarChart,
                        contentDescription = stringResource(id = R.string.progress_button),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Text(
                stringResource(id = R.string.choose_your_challenge),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .testTag("setup_lazy_column"),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(operationLevels) { operationState ->
                    OperationCard(
                        operationLevels = operationState,
                        modifier = Modifier.testTag("operation_card_${operationState.operation.toSymbol()}"),
                        initialExpanded = false
                    )
                    { levelId -> onStartClicked(operationState.operation, 10, 10, levelId) }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onCreditsClicked) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Credits",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OperationCard(
    operationLevels: OperationLevels,
    modifier: Modifier = Modifier,
    initialExpanded: Boolean = false,
    onStartClicked: (levelId: String?) -> Unit,
) {
    var expanded by remember { mutableStateOf(initialExpanded) }
    val gradient = getGradientForOperation(operationLevels.operation)
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale = if (isPressed) 0.98f else 1f


    Card(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { expanded = !expanded },
        shape = RoundedCornerShape(24.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(gradient)
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = when (operationLevels.operation) {
                        Operation.ADDITION -> "+"
                        Operation.SUBTRACTION -> "-"
                        Operation.MULTIPLICATION -> "×"
                        Operation.DIVISION -> "÷"
                    },
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    operationLevels.operation.name.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    Button(
                        onClick = { onStartClicked("Smart Practice") },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(id = R.string.practice_smart))
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        for (row in operationLevels.levels.chunked(2)) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                for (level in row) {
                                    LevelTile(
                                        levelStatus = level,
                                        onClick = { onStartClicked(level.level.id) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                if (row.size == 1) {
                                    Spacer(Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LevelTile(levelStatus: LevelStatus, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .aspectRatio(1f)
            .testTag("${levelStatus.level.id}-${levelStatus.starRating}_stars")
            .clickable(enabled = levelStatus.isUnlocked, onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = if (levelStatus.isUnlocked) 0.8f else 0.3f),
            contentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = if (levelStatus.isUnlocked) 1f else 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AutoSizeText(
                text = formatLevel(levelStatus.level).shortLabel,
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "🌟".repeat(levelStatus.starRating) + "☆".repeat(3 - levelStatus.starRating),
                fontSize = 20.sp
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
fun PreviewExpandedOperationCard() {
    val operationLevels = OperationLevels(
        operation = Operation.ADDITION,
        levels = listOf(
            LevelStatus(Curriculum.SumsUpTo5, isUnlocked = true, starRating = 3),
            LevelStatus(Curriculum.SumsUpTo10, isUnlocked = true, starRating = 1),
            LevelStatus(Curriculum.SumsUpTo20, isUnlocked = false, starRating = 0)
        )
    )
    AppTheme {
        OperationCard(
            operationLevels = operationLevels,
            initialExpanded = true
        ) {}
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
fun PreviewExerciseSetupScreen() {
    val fakeOperationLevels = Operation.entries.map { op ->
        OperationLevels(
            operation = op,
            levels = listOf(
                LevelStatus(Curriculum.SumsUpTo5, isUnlocked = true, starRating = 3),
                LevelStatus(Curriculum.SumsUpTo10, isUnlocked = true, starRating = 1),
                LevelStatus(Curriculum.SumsUpTo20, isUnlocked = false, starRating = 0)
            )
        )
    }

    AppTheme {
        ExerciseSetupScreen(
            operationLevels = fakeOperationLevels,
            activeUser = User(7, "John Doe"),
            onStartClicked = { _, _, _, _ -> },
            onProgressClicked = { },
            onCreditsClicked = { },
            onProfileClicked = { }
        )
    }
}