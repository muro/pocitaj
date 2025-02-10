package com.codinglikeapirate.pocitaj

import android.util.Log
import com.google.android.gms.tasks.SuccessContinuation
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.digitalink.DigitalInkRecognizer
import com.google.mlkit.vision.digitalink.Ink
import com.google.mlkit.vision.digitalink.RecognitionCandidate
import com.google.mlkit.vision.digitalink.RecognitionContext
import com.google.mlkit.vision.digitalink.RecognitionResult
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.text.format

/** Task to run asynchronously to obtain recognition results.  */
class RecognitionTask(private val recognizer: DigitalInkRecognizer?, private val ink: Ink, private val expectedResult: Int) {
    private var currentResult: RecognizedInk? = null
    private val cancelled: AtomicBoolean
    private val done: AtomicBoolean
    fun cancel() {
        cancelled.set(true)
    }

    fun done(): Boolean {
        return done.get()
    }

    fun result(): RecognizedInk? {
        return currentResult
    }

    /** Helper class that stores an ink along with the corresponding recognized text.  */
    class RecognizedInk internal constructor(val ink: Ink, val text: String?)

    fun findBestResult(candidates: List<RecognitionCandidate>): String {
        var first = ""
        val expected = "$expectedResult"
        for (rc in candidates) {
            val text = rc.text
            Log.i(TAG, "recognition candidate: $text vs expected: $expected")
            if (!text.matches("\\d+".toRegex())) {
                continue
            }
            if (first.isEmpty()) {
                first = text
            }
            if (text == expected) {
                return expected
            }
        }
        return first
    }

    fun run(): Task<String?> {
        Log.i(TAG, "RecoTask.run")
        return recognizer!!
            .recognize(ink, RecognitionContext.builder().setPreContext("1234").build())
            .onSuccessTask(
                SuccessContinuation { result: RecognitionResult? ->
                    if (cancelled.get() || result == null || result.candidates.isEmpty()
                    ) {
                        return@SuccessContinuation Tasks.forResult<String?>(null)
                    }
                    currentResult =
                        RecognizedInk(
                            ink,
                            findBestResult(result.candidates.toList())
                        )
                    Log.i(
                        TAG,
                        "result: " + currentResult!!.text
                    )
                    done.set(
                        true
                    )
                    return@SuccessContinuation Tasks.forResult<String?>(currentResult!!.text)
                }
            )
    }

    companion object {
        private const val TAG = "RecognitionTask"
    }

    init {
        cancelled = AtomicBoolean(false)
        done = AtomicBoolean(false)
    }
}