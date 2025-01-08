package com.codinglikeapirate.pocitaj;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

public class ResultsAdapter extends RecyclerView.Adapter<ResultsAdapter.ViewHolder> {

  private final ExerciseBook exerciseBook;
  private int correctColor;
  private int incorrectColor;

  ResultsAdapter(ExerciseBook exerciseBook) {
    this.exerciseBook = exerciseBook;
  }

  @Override
  public int getItemCount() {
    return exerciseBook.getHistory().size();
  }

  @NonNull
  @Override
  public ResultsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    correctColor = ContextCompat.getColor(parent.getContext(), R.color.primary_text_color);
    incorrectColor = ContextCompat.getColor(parent.getContext(), R.color.incorrect_text_color);

    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.results_list_item, parent, false);
    return new ViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull ResultsAdapter.ViewHolder holder, int position) {
    ExerciseBook.Exercise exercise = exerciseBook.getHistory().get(position);
    holder.getTextView().setText(exercise.equation());
    if (exercise.correct()) {
      holder.getTextView().setTextColor(correctColor);
    } else {
      holder.getTextView().setTextColor(incorrectColor);
    }

    if (!exercise.solved()) {
      holder.getStatusView().setImageResource(R.drawable.cat_big_eyes);
    } else if (!exercise.correct()) {
      holder.getStatusView().setImageResource(R.drawable.cat_cry);
    } else {
      holder.getStatusView().setImageResource(R.drawable.cat_heart);
    }
  }

  public static class ViewHolder extends RecyclerView.ViewHolder {
    TextView textView;
    ImageView statusView;

    public ViewHolder(@NonNull View itemView) {
      super(itemView);
      textView = itemView.findViewById(R.id.textView);
      statusView = itemView.findViewById(R.id.resultImageView);
    }

    public TextView getTextView() {
      return textView;
    }

    public ImageView getStatusView() {
      return statusView;
    }
  }
}
