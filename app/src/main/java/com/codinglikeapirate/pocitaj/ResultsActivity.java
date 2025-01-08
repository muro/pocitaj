package com.codinglikeapirate.pocitaj;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ResultsActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    EdgeToEdge.enable(this);
    setContentView(R.layout.activity_results);

    // fetch the exercise book and populate the list out of it
    ExerciseBook exerciseBook = new ExerciseBook();
    exerciseBook.generate();
    exerciseBook.getLast().solve(exerciseBook.getLast().getExpectedResult());
    exerciseBook.generate();
    exerciseBook.getLast().solve(exerciseBook.getLast().getExpectedResult()+1);
    exerciseBook.generate();
    exerciseBook.getLast().solve(exerciseBook.getLast().getExpectedResult());

    ResultsAdapter adapter = new ResultsAdapter(exerciseBook);

    RecyclerView listView = findViewById(R.id.recycler_view);
    listView.setAdapter(adapter);
    listView.setLayoutManager(new LinearLayoutManager(this));
  }
}