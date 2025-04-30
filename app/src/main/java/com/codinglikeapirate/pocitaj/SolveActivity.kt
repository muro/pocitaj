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
    @JvmField
    @VisibleForTesting
    val strokeManager = StrokeManager()
    private val modelLanguageTag = "en-US"
    private var exerciseBook = ExerciseBook()
    private val progressIcons = ArrayList<ImageView>()
    private lateinit var questionView : TextView
    private lateinit var solveView : SolveView

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        solveView = findViewById(R.id.solve_view)
        solveView.setExerciseBook(exerciseBook)
        solveView.setStrokeManager(strokeManager)
        updateExerciseBook(null, false)

        questionView = findViewById(R.id.question_view)

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
        updateViews()

        strokeManager.addContentChangedListener(this)
        strokeManager.addContentChangedListener(solveView)
        strokeManager.setDownloadedModelsChangedListener(this)
        strokeManager.setClearCurrentInkAfterRecognition(true)
        strokeManager.setTriggerRecognitionAfterInput(false)
        strokeManager.setActiveModel(modelLanguageTag)

        // add continuation here:
        strokeManager.download()

        strokeManager.refreshDownloadedModelsStatus()
        strokeManager.reset()
        strokeManager.setTriggerRecognitionAfterInput(true)

        val inkActivityButton = findViewById<View>(R.id.compose_ink)
        inkActivityButton.setOnClickListener {
            val intent = Intent(this, ExerciseBookActivity::class.java)
            startActivity(intent)
        }
    }

    private fun updateViews() {
        val history = exerciseBook.historyList
        progressIcons.forEachIndexed { i, icon ->
            icon.setImageResource(
                when {
                    history.size <= i -> R.drawable.cat_sleep
                    !history[i].solved() -> R.drawable.cat_big_eyes
                    !history[i].correct() -> R.drawable.cat_cry
                    else -> R.drawable.cat_heart
                }
            )
        }
        questionView.text = exerciseBook.last.question()
        solveView.invalidate()
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

    private fun updateExerciseBook(text: String?, correct: Boolean) {
        if (exerciseBook.last.solved()) {
            Log.e(TAG, "last was solved")
        }
        val result = text?.toInt() ?: ExerciseBook.NOT_RECOGNIZED

        val correctlySolved = exerciseBook.last.solve(result)
        if (correctlySolved != correct) {
            Log.e(TAG, "Passed-through solution didn't match expected result")
        }
        Log.i(TAG, "Stats: ${exerciseBook.stats}")
        // do animation
        if (result != ExerciseBook.NOT_RECOGNIZED) {
            exerciseBook.generate()
        }
        strokeManager.expectedResult = exerciseBook.last.getExpectedResult()
    }

    override fun onNewRecognizedText(text: String?, correct: Boolean) {    val popupView = findViewById<ImageView>(R.id.popup_view)
        updateExerciseBook(text, correct)
        popupView.setImageResource(if (correct) R.drawable.heart_animation else R.drawable.teardrop)
        popupView.visibility = View.VISIBLE

        (popupView.drawable as? AnimatedVectorDrawable)?.start()

        Handler(Looper.getMainLooper()).postDelayed({
            popupView.visibility = View.GONE
            updateViews()

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