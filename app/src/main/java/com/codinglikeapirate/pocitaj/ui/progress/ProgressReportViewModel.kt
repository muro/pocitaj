package com.codinglikeapirate.pocitaj.ui.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.codinglikeapirate.pocitaj.App
import com.codinglikeapirate.pocitaj.data.FactMastery
import com.codinglikeapirate.pocitaj.data.FactMasteryDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProgressReportViewModel(
    private val factMasteryDao: FactMasteryDao
) : ViewModel() {

    private val _groupedFacts = MutableStateFlow<Map<String, List<FactMastery>>>(emptyMap())
    val groupedFacts: StateFlow<Map<String, List<FactMastery>>> = _groupedFacts.asStateFlow()

    init {
        loadFactMastery()
    }

    private fun loadFactMastery() {
        viewModelScope.launch {
            // Assuming userId is 1 for now, as there's only one user.
            val facts = factMasteryDao.getAllFactsForUser(1)
            _groupedFacts.value = facts.groupBy {
                // Extract operation from factId (e.g., "add-1-2" -> "add")
                it.factId.substringBefore('-')
            }
        }
    }
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
