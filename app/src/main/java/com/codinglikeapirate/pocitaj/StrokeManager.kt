package com.codinglikeapirate.pocitaj

import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.MotionEvent
import androidx.annotation.VisibleForTesting
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.digitalink.Ink
import com.google.mlkit.vision.digitalink.Ink.Point
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Manages the recognition logic and the content that has been added to the current page.
 */
class StrokeManager {

    @VisibleForTesting
    internal val modelManager = ModelManager()

    private val content = ArrayList<RecognitionTask.RecognizedInk>()
    private var recognitionTask: RecognitionTask? = null
    private var strokeBuilder = Ink.Stroke.builder()
    private var inkBuilder = Ink.builder()
    private var stateChangedSinceLastRequest = false
    private val contentChangedListeners: MutableList<ContentChangedListener> = CopyOnWriteArrayList()
    private var statusChangedListener: StatusChangedListener? = null
    private var downloadedModelsChangedListener: DownloadedModelsChangedListener? = null
    var isTriggerRecognitionAfterInput = true
    var isClearCurrentInkAfterRecognition = true
    private var status = ""
    var expectedResult = -1

    private val uiHandler = Handler(Looper.getMainLooper()) { msg: Message ->
        if (msg.what == TIMEOUT_TRIGGER) {
            Log.i(TAG, "Handling timeout trigger.")
            commitResult()
            return@Handler true
        }
        false
    }

    private fun commitResult() {
        if (recognitionTask?.done() == true && recognitionTask?.result() != null) {
            val result = recognitionTask!!.result()!!
            content.add(result)

            setStatus("Successful recognition: ${result.text}")
            if (isClearCurrentInkAfterRecognition) {
                resetCurrentInk()
            }

            val parsedResult = try {
                result.text?.toInt()
            } catch (ignored: NumberFormatException) {
                contentChangedListeners.forEach { it.onMisparsedRecognizedText(result.text) }
                return
            }

            val correct = parsedResult == expectedResult
            contentChangedListeners.forEach { it.onNewRecognizedText(result.text, correct) }
        }
    }

    fun reset() {
        Log.i(TAG, "reset")
        resetCurrentInk()
        content.clear()
        if (recognitionTask?.done() == false) {
            recognitionTask?.cancel()
        }
        setStatus("")
    }

    private fun resetCurrentInk() {
        inkBuilder = Ink.builder()
        strokeBuilder = Ink.Stroke.builder()
        stateChangedSinceLastRequest = false
    }

    fun getCurrentInk(): Ink = inkBuilder.build()

    fun addNewTouchEvent(event: MotionEvent): Boolean {
        val action = event.actionMasked
        val x = event.x
        val y = event.y
        val t = System.currentTimeMillis()

        uiHandler.removeMessages(TIMEOUT_TRIGGER)

        when (action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> strokeBuilder.addPoint(Point.create(x, y, t))
            MotionEvent.ACTION_UP -> {
                strokeBuilder.addPoint(Point.create(x, y, t))
                inkBuilder.addStroke(strokeBuilder.build())
                strokeBuilder = Ink.Stroke.builder()
                stateChangedSinceLastRequest = true
                if (isTriggerRecognitionAfterInput) {
                    recognize()
                }
            }
            else -> return false
        }
        return true
    }

    fun addContentChangedListener(contentChangedListener: ContentChangedListener) {
        contentChangedListeners.add(contentChangedListener)
    }

    fun removeContentChangedListener(contentChangedListener: ContentChangedListener) {
        contentChangedListeners.remove(contentChangedListener)
    }

    fun setStatusChangedListener(statusChangedListener: StatusChangedListener?) {
        this.statusChangedListener = statusChangedListener
    }

    fun setDownloadedModelsChangedListener(downloadedModelsChangedListener: DownloadedModelsChangedListener?) {
        this.downloadedModelsChangedListener = downloadedModelsChangedListener
    }

    fun getStatus(): String = status

    private fun setStatus(newStatus: String) {
        status = newStatus
        statusChangedListener?.onStatusChanged()
    }

    fun setActiveModel(languageTag: String) {
        setStatus(modelManager.setModel(languageTag))
    }

    fun deleteActiveModel(): Task<Void> {
        return modelManager.deleteActiveModel()
            .addOnSuccessListener { refreshDownloadedModelsStatus() }
            .onSuccessTask {
                setStatus(it!!)
                Tasks.forResult(null)
            }
    }

    fun download(): Task<Void> {
        setStatus("Download started.")
        return modelManager.download()
            .addOnSuccessListener { refreshDownloadedModelsStatus() }
            .onSuccessTask {
                setStatus(it!!)
                Tasks.forResult(null)
            }
    }

    fun recognize(): Task<String?> {
        if (!stateChangedSinceLastRequest || inkBuilder.isEmpty) {
            setStatus("No recognition, ink unchanged or empty")
            return Tasks.forResult(null)
        }
        if (modelManager.recognizer == null) {
            setStatus("Recognizer not set")
            return Tasks.forResult(null)
        }

        return modelManager.checkIsModelDownloaded()
            .onSuccessTask { result ->
                if (!result!!) {
                    setStatus("Model not downloaded yet")
                    return@onSuccessTask Tasks.forResult(null)
                }

                stateChangedSinceLastRequest = false
                recognitionTask = RecognitionTask(modelManager.recognizer!!, inkBuilder.build(), expectedResult)
                uiHandler.sendMessageDelayed(uiHandler.obtainMessage(TIMEOUT_TRIGGER), CONVERSION_TIMEOUT_MS)
                recognitionTask!!.run()
            }
    }

    fun refreshDownloadedModelsStatus() {
        modelManager.downloadedModelLanguages
            .addOnSuccessListener { downloadedLanguageTags ->
                downloadedModelsChangedListener?.onDownloadedModelsChanged(downloadedLanguageTags)
            }
    }

    interface ContentChangedListener {
        fun onNewRecognizedText(text: String?, correct: Boolean)
        fun onMisparsedRecognizedText(text: String?)
    }

    interface StatusChangedListener {
        fun onStatusChanged()
    }

    interface DownloadedModelsChangedListener {
        fun onDownloadedModelsChanged(downloadedLanguageTags: Set<String>)
    }

    companion object {
        @VisibleForTesting
        const val CONVERSION_TIMEOUT_MS: Long = 1000
        private const val TAG = "StrokeManager"
        private const val TIMEOUT_TRIGGER = 1
    }
}