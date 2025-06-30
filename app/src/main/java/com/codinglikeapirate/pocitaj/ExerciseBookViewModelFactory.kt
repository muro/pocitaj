package com.codinglikeapirate.pocitaj

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ExerciseBookViewModelFactory(private val inkModelManager: InkModelManager) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExerciseBookViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ExerciseBookViewModel(inkModelManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
