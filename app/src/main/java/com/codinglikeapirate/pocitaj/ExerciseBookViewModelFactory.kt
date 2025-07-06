package com.codinglikeapirate.pocitaj

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras

object ExerciseBookViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        if (modelClass.isAssignableFrom(ExerciseBookViewModel::class.java)) {
            val application = checkNotNull(extras[APPLICATION_KEY]) as PocitajApplication
            val inkModelManager = application.inkModelManager
            val exerciseBook = application.exerciseBook
            return ExerciseBookViewModel(inkModelManager, exerciseBook) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
