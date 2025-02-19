package com.codinglikeapirate.pocitaj

import android.content.Intent
import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AppCompatActivity
import com.codinglikeapirate.pocitaj.StrokeManager.ContentChangedListener

/** Main activity which creates a StrokeManager and connects it to the DrawingView. */
class SolveActivity : AppCompatActivity(), StrokeManager.DownloadedModelsChangedListener,
    ContentChangedListener {
    @JvmField @VisibleForTesting val strokeManager = StrokeManager()
    private val modelLanguageTag = "en-US"
    private var exerciseBook = ExerciseBook()

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val solveView = findViewById<SolveView>(R.id.solve_view)
        solveView.setExerciseBook(exerciseBook)
        solveView.setStrokeManager(strokeManager)

        val questionView = findViewById<TextView>(R.id.question_view)
        solveView.setQuestionView(questionView)

        val progressIcons = ArrayList<ImageView>()
        progressIcons.add(findViewById(R.id.progress_1))
        progressIcons.add(findViewById(R.id.progress_2))
        progressIcons.add(findViewById(R.id.progress_3))
        progressIcons.add(findViewById(R.id.progress_4))
        progressIcons.add(findViewById(R.id.progress_5))
        progressIcons.add(findViewById(R.id.progress_6))
        progressIcons.add(findViewById(R.id.progress_7))
        progressIcons.add(findViewById(R.id.progress_8))
        progressIcons.add(findViewById(R.id.progress_9))
        progressIcons.add(findViewById(R.id.progress_10))
        solveView.setProgressIcons(progressIcons)

        strokeManager.addContentChangedListener(solveView)
        strokeManager.addContentChangedListener(this)
        strokeManager.setDownloadedModelsChangedListener(this)
        strokeManager.setClearCurrentInkAfterRecognition(true)
        strokeManager.setTriggerRecognitionAfterInput(false)
        strokeManager.setActiveModel(modelLanguageTag)

        // add continuation here:
        strokeManager.download()

        strokeManager.refreshDownloadedModelsStatus()
        strokeManager.reset()
        strokeManager.setTriggerRecognitionAfterInput(true)
    }
//
//    fun recognizeClick(v: View?) {
//        strokeManager.recognize()
//    }
//
//    fun clearClick(v: View?) {
//        strokeManager.reset()
//        val drawingView = findViewById<SolveView>(R.id.solve_view)
//        drawingView.clear()
//    }
//
//    fun deleteClick(v: View?) {
//        strokeManager.deleteActiveModel()
//    }

    override fun onDownloadedModelsChanged(downloadedLanguageTags: Set<String>) {
        for (s in downloadedLanguageTags) {
            Log.i(TAG, "Downloaded models changed: $s")
        }
    }

    override fun onNewRecognizedText(text: String?, correct: Boolean) {    val popupView = findViewById<ImageView>(R.id.popup_view)
        popupView.setImageResource(if (correct) R.drawable.heart_animation else R.drawable.teardrop)
        popupView.visibility = View.VISIBLE

        (popupView.drawable as? AnimatedVectorDrawable)?.start()

        Handler(Looper.getMainLooper()).postDelayed({
            popupView.visibility = View.GONE

            if (exerciseBook.historyList.size < 5) {
                return@postDelayed
            }

            val intent = Intent(this, ResultsActivity::class.java)
            val bundle = Bundle()

            val exerciseCount = exerciseBook.historyList.size
            val exercises = Array(exerciseCount) { "" }
            val corrects = BooleanArray(exerciseCount)
            val recognized = BooleanArray(exerciseCount)

            for (i in 0 until exerciseCount) {
                val exercise = exerciseBook.historyList[i]
                exercises[i] = exercise.equation()
                corrects[i] = exercise.correct()
                recognized[i] = exercise.solved()
            }

            bundle.putStringArray(ResultsActivity.EXERCISES_KEY, exercises)
            bundle.putBooleanArray(ResultsActivity.CORRECTS_KEY, corrects)
            bundle.putBooleanArray(ResultsActivity.RECOGNIZED_KEY, recognized)

            intent.putExtras(bundle)
            startActivity(intent, bundle)
        }, 500)
    }

    override fun onMisparsedRecognizedText(text: String?) {
        val popupView = findViewById<ImageView>(R.id.popup_view)
        popupView.setImageResource(R.drawable.question)
        popupView.visibility = View.VISIBLE
        val d = popupView.getDrawable()
        if (d is AnimatedVectorDrawable) {
            d.start()
        }

        Handler(Looper.getMainLooper()).postDelayed({ popupView.visibility = View.GONE }, 500)
    }

    companion object {
        private const val TAG = "SolveActivity"
    }
}