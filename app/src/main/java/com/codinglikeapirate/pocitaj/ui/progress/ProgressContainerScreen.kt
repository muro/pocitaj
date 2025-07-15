package com.codinglikeapirate.pocitaj.ui.progress

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.codinglikeapirate.pocitaj.R
import com.codinglikeapirate.pocitaj.data.ExerciseAttempt
import com.codinglikeapirate.pocitaj.logic.Level
import com.codinglikeapirate.pocitaj.ui.history.HistoryScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProgressContainerScreen(
    progressByLevel: Map<Level, List<FactProgress>>,
    history: Map<String, List<ExerciseAttempt>>
) {
    val pagerState = rememberPagerState(pageCount = { 2 })
    val coroutineScope = rememberCoroutineScope()

    Column {
        Spacer(modifier = Modifier.height(32.dp))
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
            modifier = Modifier.weight(1f).testTag("swipeableTabsPager")
        ) { page ->
            when (page) {
                0 -> ProgressReportScreen(progressByLevel = progressByLevel, onHistoryClicked = {})
                1 -> HistoryScreen(history = history)
            }
        }
    }
}
