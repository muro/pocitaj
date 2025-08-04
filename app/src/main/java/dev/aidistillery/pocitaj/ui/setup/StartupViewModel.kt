package dev.aidistillery.pocitaj.ui.setup

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.aidistillery.pocitaj.App
import dev.aidistillery.pocitaj.ModelManager
import dev.aidistillery.pocitaj.data.AdaptiveExerciseSource
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class StartupViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun initializeApp() {
        viewModelScope.launch {
            val app = getApplication<App>()

            val modelDownload = async {
                if (!app.isInkModelManagerInitialized) {
                    app.inkModelManager = ModelManager()
                }
                downloadModel()
            }
            val sourceCreation = async {
                if (!app.isExerciseSourceInitialized) {
                    createExerciseSource()
                }
            }

            val modelDownloaded = modelDownload.await()
            sourceCreation.await()

            if (modelDownloaded) {
                _isInitialized.value = true
            }
        }
    }

    private suspend fun downloadModel(): Boolean {
        Log.i("StartupViewModel", "Downloading model...")
        return suspendCoroutine { continuation ->
            val inkModelManager = getApplication<App>().inkModelManager
            inkModelManager.setModel("en-US")
            inkModelManager.download()
                .addOnSuccessListener {
                    continuation.resume(true)
                    Log.i("StartupViewModel", "Success Downloading model...")
                }
                .addOnFailureListener { e ->
                    Log.e("StartupViewModel", "Failed to download model", e)
                    _error.value =
                        "Failed to download necessary files. Please check your internet connection."
                    continuation.resume(false)
                }
        }
    }

    private suspend fun createExerciseSource() {
        val app = getApplication<App>()
        app.exerciseSource = AdaptiveExerciseSource(
            app.database.factMasteryDao(),
            app.database.exerciseAttemptDao(),
            app.database.userDao(),
            1L
        )
    }
}