package dev.aidistillery.pocitaj

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.common.MlKitException
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.vision.digitalink.recognition.DigitalInkRecognition
import com.google.mlkit.vision.digitalink.recognition.DigitalInkRecognitionModel
import com.google.mlkit.vision.digitalink.recognition.DigitalInkRecognitionModelIdentifier
import com.google.mlkit.vision.digitalink.recognition.DigitalInkRecognizer
import com.google.mlkit.vision.digitalink.recognition.DigitalInkRecognizerOptions
import com.google.mlkit.vision.digitalink.recognition.Ink
import com.google.mlkit.vision.digitalink.recognition.RecognitionContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/** Class to manage model downloading, deletion, and selection.  */
class ModelManager : InkModelManager {
    private var model: DigitalInkRecognitionModel? = null
    private var recognizer: DigitalInkRecognizer? = null
    private val remoteModelManager = RemoteModelManager.getInstance()

    override suspend fun recognizeInk(
        ink: Ink,
        hint: String
    ): String =
        suspendCoroutine { continuation ->
            if (recognizer == null) {
                Log.e("InkRecognition", "Recognizer not set")
                continuation.resume("")
                return@suspendCoroutine
            }

            recognizer!!.recognize(
                ink,
                RecognitionContext.builder().setPreContext("1234").build()
            )
                .addOnSuccessListener { result ->
                    // on rare occasions, the recognized text includes a space on the start
                    val foundCandidate = result.candidates.firstOrNull { it.text.trim() == hint }
                        ?: result.candidates.firstOrNull()
                    val recognizedText = foundCandidate?.text?.trim() ?: ""
                    continuation.resume(recognizedText)
                    Log.i("ModelManager", "Recognized text: $recognizedText")
                }
                .addOnFailureListener { e: Exception ->
                    Log.e("InkRecognition", "Error during recognition", e)
                    continuation.resume("")
                }
        }

    override fun setModel(languageTag: String): String {
        // Clear the old model and recognizer.
        model = null
        recognizer?.close()
        recognizer = null

        // Try to parse the languageTag and get a model from it.
        val modelIdentifier = try {
            DigitalInkRecognitionModelIdentifier.fromLanguageTag(languageTag)
        } catch (_: MlKitException) {
            Log.e(
                TAG,
                "Failed to parse language '$languageTag'"
            )
            return ""
        } ?: return "No model for language: $languageTag"

        // Initialize the model and recognizer.
        model = DigitalInkRecognitionModel.builder(modelIdentifier).build()
        recognizer = DigitalInkRecognition.getClient(
            DigitalInkRecognizerOptions.builder(model!!).build()
        )
        Log.i(
            TAG, "Model set for language '$languageTag' ('$modelIdentifier.languageTag')."
        )
        return "Model set for language: $languageTag"
    }

    private fun checkIsModelDownloaded(): Task<Boolean?> {
        return remoteModelManager.isModelDownloaded(model!!)
    }

    override fun deleteActiveModel(): Task<String?> {
        if (model == null) {
            Log.i(TAG, "Model not set")
            return Tasks.forResult("Model not set")
        }
        return checkIsModelDownloaded()
            .onSuccessTask { result: Boolean? ->
                if (!result!!) {
                    return@onSuccessTask Tasks.forResult("Model not downloaded yet")
                }
                remoteModelManager
                    .deleteDownloadedModel(model!!)
                    .onSuccessTask { _: Void? ->
                        Log.i(
                            TAG,
                            "Model successfully deleted"
                        )
                        Tasks.forResult(
                            "Model successfully deleted"
                        )
                    }
            }
            .addOnFailureListener { e: Exception ->
                Log.e(
                    TAG,
                    "Error while model deletion: $e"
                )
            }
    }

    override fun download(): Task<String?> {
        return if (model == null) {
            Tasks.forResult("Model not selected.")
        } else remoteModelManager
            .download(model!!, DownloadConditions.Builder().build())
            .onSuccessTask { _: Void? ->
                Log.i(
                    TAG,
                    "Model download succeeded."
                )
                Tasks.forResult("Downloaded model successfully")
            }
            .addOnFailureListener { e: Exception ->
                Log.e(
                    TAG,
                    "Error while downloading the model: $e"
                )
            }
    }

    companion object {
        private const val TAG = "ModelManager"
    }
}