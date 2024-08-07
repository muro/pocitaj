package com.codinglikeapirate.pocitaj;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.VisibleForTesting;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.codinglikeapirate.pocitaj.StrokeManager.DownloadedModelsChangedListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SolveActivity extends AppCompatActivity implements DownloadedModelsChangedListener {

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

    strokeManager.setContentChangedListener(solveView);
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
}