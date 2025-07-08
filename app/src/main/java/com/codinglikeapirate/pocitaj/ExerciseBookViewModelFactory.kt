package com.codinglikeapirate.pocitaj

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.codinglikeapirate.pocitaj.data.AdaptiveExerciseSource
import com.codinglikeapirate.pocitaj.data.ExerciseSource
import kotlinx.coroutines.launch
import javax.inject.Provider

object ExerciseBookViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        val application = extras[APPLICATION_KEY] as PocitajApplication
        return ExerciseBookViewModel(
            inkModelManager = application.inkModelManager,
            exerciseSource = application.exerciseSource
        ) as T
    }
}