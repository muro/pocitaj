package com.codinglikeapirate.pocitaj;

// Based on code from MlKit examples.

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.mlkit.vision.digitalink.Ink;
import com.google.mlkit.vision.digitalink.Ink.Point;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Manages the recognition logic and the content that has been added to the current page.
 */
public class StrokeManager {

  @VisibleForTesting
  static final long CONVERSION_TIMEOUT_MS = 1000;
  private static final String TAG = "StrokeManager";
  // This is a constant that is used as a message identifier to trigger the timeout.
  private static final int TIMEOUT_TRIGGER = 1;
  @VisibleForTesting
  final ModelManager modelManager = new ModelManager();
  /** @noinspection MismatchedQueryAndUpdateOfCollection*/ // Managing the recognition queue.
  private final List<RecognitionTask.RecognizedInk> content = new ArrayList<>();
  // For handling recognition and model downloading.
  private RecognitionTask recognitionTask = null;
  // Managing ink currently drawn.
  private Ink.Stroke.Builder strokeBuilder = Ink.Stroke.builder();
  private Ink.Builder inkBuilder = Ink.builder();
  private boolean stateChangedSinceLastRequest = false;
  @NonNull
  private final List<ContentChangedListener> contentChangedListeners = new ArrayList<>();
  @Nullable
  private StatusChangedListener statusChangedListener = null;
  @Nullable
  private DownloadedModelsChangedListener downloadedModelsChangedListener = null;
  private boolean triggerRecognitionAfterInput = true;
  private boolean clearCurrentInkAfterRecognition = true;
  private String status = "";
  private int expectedResult = -1;

  // Handler to handle the UI Timeout.
  // This handler is only used to trigger the UI timeout. Each time a UI interaction happens,
  // the timer is reset by clearing the queue on this handler and sending a new delayed message (in
  // addNewTouchEvent).
  private final Handler uiHandler =
      new Handler(
          Looper.getMainLooper(),
          msg -> {
            if (msg.what == TIMEOUT_TRIGGER) {
              Log.i(TAG, "Handling timeout trigger.");
              commitResult();
              return true;
            }
            // In the current use this statement is never reached because we only ever send
            // TIMEOUT_TRIGGER messages to this handler.
            // This line is necessary because otherwise Java's static analysis doesn't allow for
            // compiling. Returning false indicates that a message wasn't handled.
            return false;
          });

  public void setTriggerRecognitionAfterInput(boolean shouldTrigger) {
    triggerRecognitionAfterInput = shouldTrigger;
  }

  public void setClearCurrentInkAfterRecognition(boolean shouldClear) {
    clearCurrentInkAfterRecognition = shouldClear;
  }

  private void commitResult() {
    if (recognitionTask.done() && recognitionTask.result() != null) {
      RecognitionTask.RecognizedInk result = recognitionTask.result();
      content.add(result);

      //noinspection DataFlowIssue
      setStatus("Successful recognition: " + result.text);
      if (clearCurrentInkAfterRecognition) {
        resetCurrentInk();
      }

      int parsedResult;
      try {
        parsedResult = Integer.parseInt(result.text);
      } catch (NumberFormatException ignored) {
        for (ContentChangedListener contentChangedListener : contentChangedListeners) {
          contentChangedListener.onMisparsedRecognizedText(result.text);
        }
        return;
      }

      // must be stored, as onNewRecognizedText could modify this.expectedResult
      boolean correct = parsedResult == expectedResult;
      for (ContentChangedListener contentChangedListener : contentChangedListeners) {
        contentChangedListener.onNewRecognizedText(result.text, correct);
      }
    }
  }

  public void reset() {
    Log.i(TAG, "reset");
    resetCurrentInk();
    content.clear();
    if (recognitionTask != null && !recognitionTask.done()) {
      recognitionTask.cancel();
    }
    setStatus("");
  }

  private void resetCurrentInk() {
    inkBuilder = Ink.builder();
    strokeBuilder = Ink.Stroke.builder();
    stateChangedSinceLastRequest = false;
  }

  public Ink getCurrentInk() {
    return inkBuilder.build();
  }

  /**
   * This method is called when a new touch event happens on the drawing client and notifies the
   * StrokeManager of new content being added.
   *
   * <p>This method takes care of triggering the UI timeout and scheduling recognitions on the
   * background thread.
   *
   * @return whether the touch event was handled.
   */
  public boolean addNewTouchEvent(MotionEvent event) {
    int action = event.getActionMasked();
    float x = event.getX();
    float y = event.getY();
    long t = System.currentTimeMillis();

    // A new event happened -> clear all pending timeout messages.
    uiHandler.removeMessages(TIMEOUT_TRIGGER);

    switch (action) {
      case MotionEvent.ACTION_DOWN:
      case MotionEvent.ACTION_MOVE:
        strokeBuilder.addPoint(Point.create(x, y, t));
        break;
      case MotionEvent.ACTION_UP:
        strokeBuilder.addPoint(Point.create(x, y, t));
        inkBuilder.addStroke(strokeBuilder.build());
        strokeBuilder = Ink.Stroke.builder();
        stateChangedSinceLastRequest = true;
        if (triggerRecognitionAfterInput) {
          recognize();
        }
        break;
      default:
        // Indicate touch event wasn't handled.
        return false;
    }

    return true;
  }

  // Listeners to update the drawing and status.
  public void addContentChangedListener(@NonNull ContentChangedListener contentChangedListener) {
    contentChangedListeners.add(contentChangedListener);
  }

  /** @noinspection unused*/
  public void removeContentChangedListener(@NonNull ContentChangedListener contentChangedListener) {
    contentChangedListeners.remove(contentChangedListener);
  }

  public void setStatusChangedListener(@Nullable StatusChangedListener statusChangedListener) {
    this.statusChangedListener = statusChangedListener;
  }

  public void setDownloadedModelsChangedListener(
      @Nullable DownloadedModelsChangedListener downloadedModelsChangedListener) {
    this.downloadedModelsChangedListener = downloadedModelsChangedListener;
  }

  public String getStatus() {
    return status;
  }

  private void setStatus(String newStatus) {
    status = newStatus;
    if (statusChangedListener != null) {
      statusChangedListener.onStatusChanged();
    }
  }

  public void setActiveModel(String languageTag) {
    setStatus(modelManager.setModel(languageTag));
  }

  public Task<Void> deleteActiveModel() {
    return modelManager
        .deleteActiveModel()
        .addOnSuccessListener(unused -> refreshDownloadedModelsStatus())
        .onSuccessTask(
            status -> {
              setStatus(status);
              return Tasks.forResult(null);
            });
  }

  /** @noinspection UnusedReturnValue*/
  public Task<Void> download() {
    setStatus("Download started.");
    return modelManager
        .download()
        .addOnSuccessListener(unused -> refreshDownloadedModelsStatus())
        .onSuccessTask(
            status -> {
              setStatus(status);
              return Tasks.forResult(null);
            });
  }

  public void setExpectedResult(int expectedResult) {
    Log.i(TAG, "StrokeManager.setExpectedResult: " + expectedResult);
    this.expectedResult = expectedResult;
  }

  // Model downloading / deleting / setting.

  /** @noinspection UnusedReturnValue*/
  public Task<String> recognize() {

    if (!stateChangedSinceLastRequest || inkBuilder.isEmpty()) {
      setStatus("No recognition, ink unchanged or empty");
      return Tasks.forResult(null);
    }
    if (modelManager.getRecognizer() == null) {
      setStatus("Recognizer not set");
      return Tasks.forResult(null);
    }

    return modelManager
        .checkIsModelDownloaded()
        .onSuccessTask(
            result -> {
              if (!result) {
                setStatus("Model not downloaded yet");
                return Tasks.forResult(null);
              }

              stateChangedSinceLastRequest = false;
              recognitionTask =
                  new RecognitionTask(modelManager.getRecognizer(), inkBuilder.build(),
                      expectedResult);
              uiHandler.sendMessageDelayed(
                  uiHandler.obtainMessage(TIMEOUT_TRIGGER), CONVERSION_TIMEOUT_MS);
              return recognitionTask.run();
            });
  }

  public void refreshDownloadedModelsStatus() {
    modelManager
        .getDownloadedModelLanguages()
        .addOnSuccessListener(
            downloadedLanguageTags -> {
              if (downloadedModelsChangedListener != null) {
                downloadedModelsChangedListener.onDownloadedModelsChanged(downloadedLanguageTags);
              }
            });
  }

  /**
   * Interface to register to be notified of changes in the recognized content.
   */
  public interface ContentChangedListener {

    /**
     * This method is called when the strokes are recognized, with the new content as parameter.
     */
    void onNewRecognizedText(String text, boolean correct);

    /** @noinspection unused*/ // Called when the text can't be parsed as a number
    void onMisparsedRecognizedText(String text);
  }

  // Recognition-related.

  /**
   * Interface to register to be notified of changes in the status - a description string.
   */
  public interface StatusChangedListener {

    /**
     * This method is called when the status (= description string) changes.
     */
    void onStatusChanged();
  }

  /**
   * Interface to register to be notified of changes in the downloaded model state.
   */
  public interface DownloadedModelsChangedListener {

    /**
     * This method is called when the downloaded models changes.
     */
    void onDownloadedModelsChanged(Set<String> downloadedLanguageTags);
  }
}
