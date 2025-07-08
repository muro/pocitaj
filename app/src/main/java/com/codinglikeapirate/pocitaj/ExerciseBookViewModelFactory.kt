package com.codinglikeapirate.pocitaj

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras

object ExerciseBookViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        val application = extras[APPLICATION_KEY] as PocitajApplication
        return ExerciseBookViewModel(
            inkModelManager = application.inkModelManager,
            exerciseSource = application.exerciseSource
        ) as T
    }
}