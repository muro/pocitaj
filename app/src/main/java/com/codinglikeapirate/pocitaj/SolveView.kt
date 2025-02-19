package com.codinglikeapirate.pocitaj

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.text.TextPaint
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.codinglikeapirate.pocitaj.StrokeManager.ContentChangedListener
import com.google.mlkit.vision.digitalink.Ink

/**
 * Main view for rendering content.
 *
 * The view accepts touch inputs, renders them on screen, and passes the content to the
 * StrokeManager. The view is also able to draw content from the StrokeManager.
 */
class SolveView @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null
) :
    View(context, attrs), ContentChangedListener {

    private val currentStrokePaint = Paint().apply {
        color = ContextCompat.getColor(context!!, R.color.current_stroke_color)
        isAntiAlias = true
        strokeWidth = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            STROKE_WIDTH_DP,
            resources.displayMetrics
        )
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
    }

    private val canvasPaint = Paint(Paint.DITHER_FLAG)
    private val lastResultPaint = TextPaint().apply {
        color = ContextCompat.getColor(context!!, R.color.correct_text_color)
        textSize = LAST_RESULT_TEXT_SIZE
    }

    private val currentStroke = Path()
    private var lastResult = ""
    private lateinit var drawCanvas: Canvas
    private lateinit var canvasBitmap: Bitmap
    private var strokeManager: StrokeManager? = null
    private lateinit var exerciseBook: ExerciseBook
    private var questionView: TextView? = null
    private var progressIcons: List<ImageView>? = null

    fun setStrokeManager(strokeManager: StrokeManager) {
        this.strokeManager = strokeManager
        strokeManager.expectedResult = exerciseBook.last.getExpectedResult()
    }

    fun setExerciseBook(exerciseBook: ExerciseBook) {
        this.exerciseBook = exerciseBook
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        Log.i(TAG, "onSizeChanged")
        canvasBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        drawCanvas = Canvas(canvasBitmap)
        if (width != oldWidth || height != oldHeight) {
            redrawContent()
        }
        invalidate()
        updateProgressIcons()
    }

    fun redrawContent() {
        strokeManager ?: return
        clear()
        val currentInk = strokeManager!!.currentInk
        drawInk(currentInk, currentStrokePaint)

        drawQuestion()
        questionView?.text = exerciseBook.last.question()
        invalidate()
    }

    private fun drawInk(ink: Ink, paint: Paint) {
        ink.strokes.forEach { drawStroke(it, paint) }
    }

    private fun drawStroke(s: Ink.Stroke, paint: Paint) {
        val path = Path()
        s.points.forEach { p ->
            if (path.isEmpty) {
                path.moveTo(p.x, p.y)
            } else {
                path.lineTo(p.x, p.y)
            }
        }
        drawCanvas.drawPath(path, paint)
    }

    /** @noinspection EmptyMethod */
    private fun drawQuestion() {
        // Method is empty, no need to do anything here.
    }

    fun clear() {
        currentStroke.reset()
        onSizeChanged(
            canvasBitmap.width,
            canvasBitmap.height,
            canvasBitmap.width,
            canvasBitmap.height
        )
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawBitmap(canvasBitmap, 0f, 0f, canvasPaint)
        canvas.drawText(lastResult, 30f, canvasBitmap.height - 100f, lastResultPaint)
        updateProgressIcons()
        canvas.drawPath(currentStroke, currentStrokePaint)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                currentStroke.moveTo(x, y)
                updateProgressIcons()
            }

            MotionEvent.ACTION_MOVE -> currentStroke.lineTo(x, y)
            MotionEvent.ACTION_UP -> {
                currentStroke.lineTo(x, y)
                drawCanvas.drawPath(currentStroke, currentStrokePaint)
                currentStroke.reset()
            }
        }
        if (strokeManager?.addNewTouchEvent(event) == false) {
            Log.w(TAG, "onTouchEvent: stroke manager didn't process event")
        }
        invalidate()
        return true
    }

    override fun onNewRecognizedText(text: String?, correct: Boolean) {
        if (exerciseBook.last.solved()) {
            Log.e(TAG, "last was solved")
        }
        val result = text?.toInt() ?: ExerciseBook.NOT_RECOGNIZED

        val correctlySolved = exerciseBook.last.solve(result)
        if (correctlySolved != correct) {
            Log.e(TAG, "Passed-through solution didn't match expected result")
        }
        lastResult = exerciseBook.last.equation()
        lastResultPaint.color = ContextCompat.getColor(
            context,
            if (correct) R.color.correct_text_color else R.color.incorrect_text_color
        )
        Log.i(TAG, "Stats: ${exerciseBook.stats}")
        updateProgressIcons()
        // do animation
        if (result != ExerciseBook.NOT_RECOGNIZED) {
            exerciseBook.generate()
        }
        strokeManager?.expectedResult = exerciseBook.last.getExpectedResult()
        redrawContent()
    }

    override fun onMisparsedRecognizedText(text: String?) {
        redrawContent()
    }

    fun setQuestionView(questionView: TextView) {
        this.questionView = questionView
    }

    private fun updateProgressIcons() {
        val history = exerciseBook.historyList
        progressIcons?.forEachIndexed { i, icon ->
            icon.setImageResource(
                when {
                    history.size <= i -> R.drawable.cat_sleep
                    !history[i].solved() -> R.drawable.cat_big_eyes
                    !history[i].correct() -> R.drawable.cat_cry
                    else -> R.drawable.cat_heart
                }
            )
        }
    }

    fun setProgressIcons(progressIcons: List<ImageView>) {
        this.progressIcons = progressIcons
    }

    companion object {
        private const val TAG = "SolveView"
        private const val STROKE_WIDTH_DP = 6f
        private const val LAST_RESULT_TEXT_SIZE = 80f
    }
}