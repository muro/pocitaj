package dev.aidistillery.pocitaj.ui.setup

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dev.aidistillery.pocitaj.App
import dev.aidistillery.pocitaj.Globals
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class StartupViewModel(
    private val globals: Globals
) : ViewModel() {

    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun initializeApp() {
        viewModelScope.launch {
            val modelDownload = async {
                downloadModel()
            }
            val sourceCreation = async {
                createExerciseSource()
            }
            val userManagerInitialization = async {
                globals.activeUserManager.init()
            }

            val modelDownloaded = modelDownload.await()
            sourceCreation.await()
            userManagerInitialization.await()

            if (modelDownloaded) {
                _isInitialized.value = true
            }
        }
    }

    private suspend fun downloadModel(): Boolean {
        Log.i("StartupViewModel", "Downloading model...")
        return suspendCoroutine { continuation ->
            val inkModelManager = globals.inkModelManager
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
        // No-op. The exercise source is now initialized in the Globals container.
    }
}

object StartupViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StartupViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StartupViewModel(App.app.globals) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}