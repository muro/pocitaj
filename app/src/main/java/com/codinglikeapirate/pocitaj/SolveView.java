package com.codinglikeapirate.pocitaj;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.codinglikeapirate.pocitaj.StrokeManager.ContentChangedListener;
import com.google.mlkit.vision.digitalink.Ink;

import java.util.List;

/**
 * Main view for rendering content.
 *
 * <p>The view accepts touch inputs, renders them on screen, and passes the content to the
 * StrokeManager. The view is also able to draw content from the StrokeManager.
 */
public class SolveView extends View implements ContentChangedListener {
  private static final String TAG = "SolveView";
  private static final int STROKE_WIDTH_DP = 6;

  private final Paint currentStrokePaint;
  private final Paint canvasPaint;

  private final TextPaint lastResultPaint;
  private final Path currentStroke;
  private String lastResult = "";
  private Canvas drawCanvas;
  private Bitmap canvasBitmap;
  private StrokeManager strokeManager;
  private ExerciseBook exerciseBook;
  private TextView questionView;
  private List<ImageView> progressIcons;

  public SolveView(Context context) {
    this(context, null);
  }

  public SolveView(Context context, AttributeSet attributeSet) {
    super(context, attributeSet);
    currentStrokePaint = new Paint();
    currentStrokePaint.setColor(0xFF0277BD); // dark blue.
    currentStrokePaint.setAntiAlias(true);

    // Set stroke width based on display density.
    currentStrokePaint.setStrokeWidth(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, STROKE_WIDTH_DP, getResources().getDisplayMetrics()));
    currentStrokePaint.setStyle(Paint.Style.STROKE);
    currentStrokePaint.setStrokeJoin(Paint.Join.ROUND);
    currentStrokePaint.setStrokeCap(Paint.Cap.ROUND);

    currentStroke = new Path();
    canvasPaint = new Paint(Paint.DITHER_FLAG);

    // TODO: remove last result from this view, move to a separate one.
    lastResultPaint = new TextPaint();
    // green
    lastResultPaint.setColor(0xFF116611);
    lastResultPaint.setTextSize(80);
  }

  void setStrokeManager(@NonNull StrokeManager strokeManager) {
    this.strokeManager = strokeManager;
    strokeManager.setExpectedResult(exerciseBook.getLast().getExpectedResult());
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
    updateProgressIcons();
  }

  public void redrawContent() {
    if (strokeManager == null) {
      return;
    }
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

  /** @noinspection EmptyMethod*/
  private void drawQuestion() {
  }

  public void clear() {
    currentStroke.reset();
    onSizeChanged(canvasBitmap.getWidth(), canvasBitmap.getHeight(), canvasBitmap.getWidth(), canvasBitmap.getHeight());
  }

  @Override
  protected void onDraw(Canvas canvas) {
    canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
    canvas.drawText(lastResult, 30, canvasBitmap.getHeight() - 100, lastResultPaint);
    updateProgressIcons();
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
        updateProgressIcons();
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
  public void onNewRecognizedText(String text, boolean correct) {
    if (exerciseBook.getLast().solved()) {
      Log.e(TAG, "last was solved");
    }
    int result = ExerciseBook.NOT_RECOGNIZED;
    try {
      result = Integer.parseInt(text);
    } catch (NumberFormatException ignored) {
    }

    boolean correctlySolved = exerciseBook.getLast().solve(result);
    if (correctlySolved != correct) {
      Log.e(TAG, "Passed-through solution didn't match expected result");
    }
    lastResult = exerciseBook.getLast().equation();
    lastResultPaint.setColor(correct ? 0xFF33AA33 : 0xFFFF8888);
    Log.i(TAG, "Stats: " + exerciseBook.getStats());
    updateProgressIcons();
    // do animation
    if (result != ExerciseBook.NOT_RECOGNIZED) {
      exerciseBook.generate();
    }
    strokeManager.setExpectedResult(exerciseBook.getLast().getExpectedResult());
    redrawContent();
  }

  @Override
  public void onMisparsedRecognizedText(String text) {
    redrawContent();
  }

  public void setQuestionView(TextView questionView) {
    this.questionView = questionView;
  }

  private void updateProgressIcons() {
    List<ExerciseBook.Exercise> history = exerciseBook.getHistoryList();
    for (int i = 0; i < progressIcons.size(); i++) {
      if (history.size() <= i) {
        progressIcons.get(i).setImageResource(R.drawable.cat_sleep);
        continue;
      }
      if (!history.get(i).solved()) {
        progressIcons.get(i).setImageResource(R.drawable.cat_big_eyes);
      } else if (!history.get(i).correct()) {
        progressIcons.get(i).setImageResource(R.drawable.cat_cry);
      } else {
        progressIcons.get(i).setImageResource(R.drawable.cat_heart);
      }
    }
  }

  public void setProgressIcons(List<ImageView> progressIcons) {
    this.progressIcons = progressIcons;
  }
}
