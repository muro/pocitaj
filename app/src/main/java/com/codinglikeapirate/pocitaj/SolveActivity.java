package com.codinglikeapirate.pocitaj;

import android.content.Intent;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.VisibleForTesting;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.codinglikeapirate.pocitaj.StrokeManager.DownloadedModelsChangedListener;
import com.codinglikeapirate.pocitaj.StrokeManager.ContentChangedListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SolveActivity extends AppCompatActivity implements DownloadedModelsChangedListener, ContentChangedListener {

  private static final String TAG = "SolveActivity";

  @VisibleForTesting
  final StrokeManager strokeManager = new StrokeManager();

  private static final String modelLanguageTag = "en-US";
  private final ExerciseBook exerciseBook = new ExerciseBook();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    EdgeToEdge.enable(this);
    setContentView(R.layout.activity_main);
    ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
      Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
      v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
      return insets;
    });

    SolveView solveView = findViewById(R.id.solve_view);
    solveView.setExerciseBook(exerciseBook);
    solveView.setStrokeManager(strokeManager);

    TextView questionView = findViewById(R.id.question_view);
    solveView.setQuestionView(questionView);

    List<ImageView> progressIcons = new ArrayList<>();
    progressIcons.add(findViewById(R.id.progress_1));
    progressIcons.add(findViewById(R.id.progress_2));
    progressIcons.add(findViewById(R.id.progress_3));
    progressIcons.add(findViewById(R.id.progress_4));
    progressIcons.add(findViewById(R.id.progress_5));
    progressIcons.add(findViewById(R.id.progress_6));
    progressIcons.add(findViewById(R.id.progress_7));
    progressIcons.add(findViewById(R.id.progress_8));
    progressIcons.add(findViewById(R.id.progress_9));
    progressIcons.add(findViewById(R.id.progress_10));
    solveView.setProgressIcons(progressIcons);

    // strokeManager.deleteActiveModel();

    strokeManager.addContentChangedListener(solveView);
    strokeManager.addContentChangedListener(this);
    strokeManager.setDownloadedModelsChangedListener(this);
    strokeManager.setClearCurrentInkAfterRecognition(true);
    strokeManager.setTriggerRecognitionAfterInput(false);
    strokeManager.reset();
    strokeManager.setActiveModel(modelLanguageTag);
    strokeManager.download();
    strokeManager.refreshDownloadedModelsStatus();
    strokeManager.setTriggerRecognitionAfterInput(true);
  }

  public void onDownloadedModelsChanged(Set<String> downloadedLanguageTags) {
    for (String s : downloadedLanguageTags) {
      Log.i(TAG, "Downloaded models changed: " + s);
    }
  }

  @Override
  public void onNewRecognizedText(String text, boolean correct) {
    ImageView popupView = findViewById(R.id.popup_view);
    if (correct) {
      popupView.setImageResource(R.drawable.heart_animation);
    } else {
      popupView.setImageResource(R.drawable.teardrop);
    }
    popupView.setVisibility(View.VISIBLE);
    Drawable d = popupView.getDrawable();
    AnimatedVectorDrawable animation;
    if (d instanceof AnimatedVectorDrawable) {
      animation = (AnimatedVectorDrawable) d;
      animation.start();
    }

    new Handler(Looper.getMainLooper()).postDelayed(() -> {
      popupView.setVisibility(View.GONE);

      if (exerciseBook.getHistory().size() < 5)
        return;

      Intent intent = new Intent(this, ResultsActivity.class);
      Bundle bundle = new Bundle();

      int exerciseCount = exerciseBook.getHistory().size();
      String[] exercises = new String[exerciseCount];
      boolean[] corrects = new boolean[exerciseCount];
      boolean[] recognized = new boolean[exerciseCount];
      for (int i = 0; i < exerciseCount; i++) {
        exercises[i] = exerciseBook.getHistory().get(i).equation();
        corrects[i] = exerciseBook.getHistory().get(i).correct();
        recognized[i] = exerciseBook.getHistory().get(i).solved();
      }
      bundle.putStringArray("exercises", exercises);
      bundle.putBooleanArray("corrects", corrects);
      bundle.putBooleanArray("recognized", recognized);
      intent.putExtras(bundle);

      startActivity(intent, bundle);
    }, 500);
  }

  @Override
  public void onMisparsedRecognizedText(String text) {
    ImageView popupView = findViewById(R.id.popup_view);
    popupView.setImageResource(R.drawable.question);
    popupView.setVisibility(View.VISIBLE);
    Drawable d = popupView.getDrawable();
    AnimatedVectorDrawable animation;
    if (d instanceof AnimatedVectorDrawable) {
      animation = (AnimatedVectorDrawable) d;
      animation.start();
    }

    new Handler(Looper.getMainLooper()).postDelayed(() -> popupView.setVisibility(View.GONE), 500);
  }
}