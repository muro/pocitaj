package com.codinglikeapirate.pocitaj.ui.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import com.codinglikeapirate.pocitaj.App
import com.codinglikeapirate.pocitaj.data.FactMasteryDao

class ProgressReportViewModel(
    private val factMasteryDao: FactMasteryDao
) : ViewModel() {
    // TODO: Implement logic to fetch and expose fact mastery data
}

object ProgressReportViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        val application = extras[APPLICATION_KEY] as App
        return ProgressReportViewModel(
            factMasteryDao = application.database.factMasteryDao()
        ) as T
    }
}
