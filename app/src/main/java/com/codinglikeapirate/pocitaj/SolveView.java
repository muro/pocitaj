package com.codinglikeapirate.pocitaj;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;

import com.codinglikeapirate.pocitaj.StrokeManager.ContentChangedListener;
import com.google.mlkit.vision.digitalink.Ink;

/**
 * Main view for rendering content.
 *
 * <p>The view accepts touch inputs, renders them on screen, and passes the content to the
 * StrokeManager. The view is also able to draw content from the StrokeManager.
 */
public class SolveView extends View implements ContentChangedListener {
  private static final String TAG = "SolveView";
  private static final int STROKE_WIDTH_DP = 3;

  private final Paint currentStrokePaint;
  private final Paint canvasPaint;

  private final TextPaint lastResultPaint;
  private final Path currentStroke;
  private final TextPaint largeTextPaint;
  private String lastResult = "";
  private String stats = "";
  private Canvas drawCanvas;
  private Bitmap canvasBitmap;
  private StrokeManager strokeManager;
  private ExerciseBook exerciseBook;
  private QuestionView questionView;

  public SolveView(Context context) {
    this(context, null);
  }

  public SolveView(Context context, AttributeSet attributeSet) {
    super(context, attributeSet);
    currentStrokePaint = new Paint();
    currentStrokePaint.setColor(0xFFFF00FF); // pink.
    currentStrokePaint.setAntiAlias(true);
    // Set stroke width based on display density.
    currentStrokePaint.setStrokeWidth(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, STROKE_WIDTH_DP, getResources().getDisplayMetrics()));
    currentStrokePaint.setStyle(Paint.Style.STROKE);
    currentStrokePaint.setStrokeJoin(Paint.Join.ROUND);
    currentStrokePaint.setStrokeCap(Paint.Cap.ROUND);

    currentStroke = new Path();
    canvasPaint = new Paint(Paint.DITHER_FLAG);

    lastResultPaint = new TextPaint();
    // green
    lastResultPaint.setColor(0xFF116611);
    lastResultPaint.setTextSize(80);

    largeTextPaint = new TextPaint();
    largeTextPaint.setColor(0xAAAAAAFF);
    largeTextPaint.setAntiAlias(true);
    largeTextPaint.setTypeface(Typeface.SERIF);
    largeTextPaint.setTextSize(250);
  }

  void setStrokeManager(@NonNull StrokeManager strokeManager) {
    this.strokeManager = strokeManager;
  }

  public void setExerciseBook(@NonNull ExerciseBook exerciseBook) {
    this.exerciseBook = exerciseBook;
  }

  @Override
  protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
    Log.i(TAG, "onSizeChanged");
    canvasBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
    drawCanvas = new Canvas(canvasBitmap);
    if (width != oldWidth || height != oldHeight) {
      redrawContent();
    }
    invalidate();
  }

  public void redrawContent() {
    clear();
    Ink currentInk = strokeManager.getCurrentInk();
    drawInk(currentInk, currentStrokePaint);

    drawQuestion();
    questionView.setText(exerciseBook.getLast().question());
    invalidate();
  }

  private void drawInk(@NonNull Ink ink, Paint paint) {
    for (Ink.Stroke s : ink.getStrokes()) {
      drawStroke(s, paint);
    }
  }

  private void drawStroke(@NonNull Ink.Stroke s, Paint paint) {
    Path path = new Path();
    for (Ink.Point p : s.getPoints()) {
      if (path.isEmpty()) {
        path.moveTo(p.getX(), p.getY());
      } else {
        path.lineTo(p.getX(), p.getY());
      }
    }
    drawCanvas.drawPath(path, paint);
  }

  private void drawQuestion() {
    drawCanvas.drawText(exerciseBook.getLast().question(), 100, 400, largeTextPaint);
  }

  public void clear() {
    currentStroke.reset();
    onSizeChanged(canvasBitmap.getWidth(), canvasBitmap.getHeight(), canvasBitmap.getWidth(), canvasBitmap.getHeight());
  }

  @Override
  protected void onDraw(Canvas canvas) {
    canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
    canvas.drawText(lastResult, 30, canvasBitmap.getHeight() - 100, lastResultPaint);
    canvas.drawText(stats, canvasBitmap.getWidth() - 450, canvasBitmap.getHeight() - 100, lastResultPaint);
    canvas.drawPath(currentStroke, currentStrokePaint);
  }

  @SuppressLint("ClickableViewAccessibility")
  @Override
  public boolean onTouchEvent(MotionEvent event) {
    int action = event.getActionMasked();
    float x = event.getX();
    float y = event.getY();

    switch (action) {
      case MotionEvent.ACTION_DOWN:
        currentStroke.moveTo(x, y);
        break;
      case MotionEvent.ACTION_MOVE:
        currentStroke.lineTo(x, y);
        break;
      case MotionEvent.ACTION_UP:
        currentStroke.lineTo(x, y);
        drawCanvas.drawPath(currentStroke, currentStrokePaint);
        currentStroke.reset();
        break;
      default:
        break;
    }
    if (!strokeManager.addNewTouchEvent(event)) {
      Log.w(TAG, "onTouchEvent: stroke manager didn't process event");
    }
    invalidate();
    return true;
  }

  @Override
  public void onContentChanged() {
    if (exerciseBook.getLast().solved()) {
      Log.e(TAG, "last was solved");
    }
    String text = strokeManager.getContent().get(strokeManager.getContent().size() - 1).text;
    int result = ExerciseBook.NOT_RECOGNIZED;
    try {
      result = Integer.parseInt(text);
    } catch (NumberFormatException ignored) {
    }

    boolean correct = exerciseBook.getLast().solve(result);
    lastResult = exerciseBook.getLast().equation();
    lastResultPaint.setColor(correct ? 0xFF33AA33 : 0xFFFF8888);
    stats = exerciseBook.getStats();

    // do animation
    if (result != ExerciseBook.NOT_RECOGNIZED) {
      exerciseBook.generate();
    }
    redrawContent();
  }

  public void setQuestionView(QuestionView questionView) {
    this.questionView = questionView;
  }
}
