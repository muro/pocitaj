package dev.aidistillery.pocitaj.data

import dev.aidistillery.pocitaj.ui.exercise.ResultDescription

data class SessionResult(
    val results: List<ResultDescription>,
    val starProgress: StarProgress
)

data class StarProgress(
    val initialStars: Int,
    val finalStars: Int
)
