package com.codinglikeapirate.pocitaj;

// Based on code from MlKit examples.

import android.util.Log;

import androidx.annotation.Nullable;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.mlkit.vision.digitalink.DigitalInkRecognizer;
import com.google.mlkit.vision.digitalink.Ink;
import com.google.mlkit.vision.digitalink.RecognitionCandidate;
import com.google.mlkit.vision.digitalink.RecognitionContext;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Task to run asynchronously to obtain recognition results.
 */
public class RecognitionTask {

  private static final String TAG = "RecognitionTask";
  private final DigitalInkRecognizer recognizer;
  private final Ink ink;
  private final AtomicBoolean cancelled;
  private final AtomicBoolean done;
  private final int expectedResult;
  @Nullable
  private RecognizedInk currentResult;

  public RecognitionTask(DigitalInkRecognizer recognizer, Ink ink, int expectedResult) {
    this.recognizer = recognizer;
    this.ink = ink;
    this.expectedResult = expectedResult;
    this.currentResult = null;
    cancelled = new AtomicBoolean(false);
    done = new AtomicBoolean(false);
  }

  public void cancel() {
    cancelled.set(true);
  }

  public boolean done() {
    return done.get();
  }

  @Nullable
  public RecognizedInk result() {
    return this.currentResult;
  }

  protected String findBestResult(List<RecognitionCandidate> candidates) {
    String first = "";
    String expected = String.format(Locale.ENGLISH, "%d", expectedResult);
    for (RecognitionCandidate rc : candidates) {
      String text = rc.getText();
      Log.i(TAG, "recognition candidate: " + text + " vs expected: " + expected);
      if (!text.matches("\\d+")) {
        continue;
      }
      if (first.isEmpty()) {
        first = text;
      }
      if (text.equals(expected)) {
        return expected;
      }
    }
    return first;
  }

  public Task<String> run() {
    Log.i(TAG, "RecognitionTask.run");
    return recognizer
        .recognize(this.ink, RecognitionContext.builder().setPreContext("1234").build())
        .onSuccessTask(
            result -> {
              if (cancelled.get() || result.getCandidates().isEmpty()) {
                return Tasks.forResult(null);
              }
              // return first result that's just numbers:
              String text = findBestResult(result.getCandidates());
              currentResult = new RecognizedInk(ink, text);
              Log.i(TAG, "result: " + currentResult.text);
              done.set(true);
              return Tasks.forResult(currentResult.text);
            });
  }

  /**
   * Helper class that stores an ink along with the corresponding recognized text.
   */
  public static class RecognizedInk {
    public final Ink ink;
    public final String text;

    RecognizedInk(Ink ink, String text) {
      this.ink = ink;
      this.text = text;
    }
  }
}
