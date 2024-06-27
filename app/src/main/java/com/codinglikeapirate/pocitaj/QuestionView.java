package com.codinglikeapirate.pocitaj;

import android.content.Context;
import android.util.AttributeSet;

import com.google.android.material.textview.MaterialTextView;

public class QuestionView extends MaterialTextView {
  private ExerciseBook exerciseBook;

  public QuestionView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public void setExerciseBook(ExerciseBook exerciseBook) {
    this.exerciseBook = exerciseBook;
  }
}
