package com.codinglikeapirate.pocitaj;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ResultsActivity extends AppCompatActivity {

  public final static String EXERCISES_KEY = "exercises";
  public final static String RECOGNIZED_KEY = "recognized";
  public final static String CORRECTS_KEY = "corrects";

  public enum ResultStatus {
    CORRECT, INCORRECT, NOT_RECOGNIZED;

    static public ResultStatus fromBooleanPair(boolean recognized, boolean correct) {
      if (!recognized) {
        return NOT_RECOGNIZED;
      }
      return correct ? ResultStatus.CORRECT : ResultStatus.INCORRECT;
    }
  }

  public class ResultDescription {
    public final String equation;
    public final ResultStatus status;

    public ResultDescription(String equation, ResultStatus status) {
      this.equation = equation;
      this.status = status;
    }
  }

  private final ArrayList<ResultDescription> results = new ArrayList<>();


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // EdgeToEdge.enable(this);
    setContentView(R.layout.activity_results);

    Intent intent = getIntent();
    Bundle extras = intent.getExtras();

    String[] exercises = extras.getStringArray(EXERCISES_KEY);
    boolean[] recognized = extras.getBooleanArray(RECOGNIZED_KEY);
    boolean[] corrects = extras.getBooleanArray(CORRECTS_KEY);
    results.clear();
    for (int i = 0; i < exercises.length; i++) {
      results.add(new ResultDescription(exercises[i], ResultStatus.fromBooleanPair(recognized[i], corrects[i])));
    }

    ResultsAdapter adapter = new ResultsAdapter(results);

    RecyclerView listView = findViewById(R.id.recycler_view);
    listView.setAdapter(adapter);
    listView.setLayoutManager(new LinearLayoutManager(this));
  }
}