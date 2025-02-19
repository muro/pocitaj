package com.codinglikeapirate.pocitaj

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ResultsActivity : AppCompatActivity() {

    companion object {
        const val EXERCISES_KEY = "exercises"
        const val RECOGNIZED_KEY = "recognized"
        const val CORRECTS_KEY = "corrects"
    }

    enum class ResultStatus {
        CORRECT, INCORRECT, NOT_RECOGNIZED;

        companion object {
            fun fromBooleanPair(recognized: Boolean, correct: Boolean): ResultStatus {
                return if (!recognized) {
                    NOT_RECOGNIZED
                } else {
                    if (correct) CORRECT else INCORRECT
                }
            }
        }
    }

    data class ResultDescription(val equation: String, val status: ResultStatus)

    private val results = ArrayList<ResultDescription>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // EdgeToEdge.enable(this)
        setContentView(R.layout.activity_results)

        val intent = intent
        val extras = intent.extras

        val exercises = extras?.getStringArray(EXERCISES_KEY) ?: emptyArray()
        val recognized = extras?.getBooleanArray(RECOGNIZED_KEY) ?: booleanArrayOf()
        val corrects = extras?.getBooleanArray(CORRECTS_KEY) ?: booleanArrayOf()

        results.clear()
        for (i in exercises.indices) {
            results.add(ResultDescription(exercises[i], ResultStatus.fromBooleanPair(recognized.getOrElse(i) { false }, corrects.getOrElse(i) { false })))
        }

        val adapter = ResultsAdapter(results)

        val listView = findViewById<RecyclerView>(R.id.recycler_view)
        listView.adapter = adapter
        listView.layoutManager = LinearLayoutManager(this)
    }
}