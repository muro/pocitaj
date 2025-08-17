package dev.aidistillery.pocitaj.ui.progress

import android.content.res.Configuration
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import dev.aidistillery.pocitaj.R
import dev.aidistillery.pocitaj.data.ExerciseAttempt
import dev.aidistillery.pocitaj.data.Operation
import dev.aidistillery.pocitaj.ui.history.HistoryScreen
import dev.aidistillery.pocitaj.ui.theme.AppTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ProgressContainerScreen(
    factProgressByOperation: Map<Operation, List<FactProgress>>,
    levelProgressByOperation: Map<Operation, Map<String, LevelProgress>>,
    history: Map<String, List<ExerciseAttempt>>,
    onBack: () -> Unit,
    initialPage: Int = 0
) {
    val pagerState = rememberPagerState(pageCount = { 2 }, initialPage = initialPage)
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.progress_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.back)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            TabRow(selectedTabIndex = pagerState.currentPage) {
                Tab(
                    text = { Text(stringResource(id = R.string.progress_tab)) },
                    selected = pagerState.currentPage == 0,
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(0)
                        }
                    }
                )
                Tab(
                    text = { Text(stringResource(id = R.string.history_tab)) },
                    selected = pagerState.currentPage == 1,
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(1)
                        }
                    }
                )
            }
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .testTag("swipeableTabsPager")
            ) { page ->
                when (page) {
                    0 -> ProgressReportScreen(
                        factProgressByOperation = factProgressByOperation,
                        levelProgressByOperation = levelProgressByOperation
                    )

                    1 -> HistoryScreen(history = history)
                }
            }
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
fun ProgressContainerScreenPreview() {
    AppTheme {
        ProgressContainerScreen(
            factProgressByOperation = emptyMap(),
            levelProgressByOperation = emptyMap(),
            history = emptyMap(),
            onBack = {}
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
fun ProgressContainerScreenHistoryPreview() {
    AppTheme {
        ProgressContainerScreen(
            factProgressByOperation = emptyMap(),
            levelProgressByOperation = emptyMap(),
            history = emptyMap(),
            onBack = {},
            initialPage = 1
        )
    }
}