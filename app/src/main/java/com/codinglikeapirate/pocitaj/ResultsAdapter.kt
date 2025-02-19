package com.codinglikeapirate.pocitaj;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ResultsAdapter extends RecyclerView.Adapter<ResultsAdapter.ViewHolder> {

  private final ArrayList<ResultsActivity.ResultDescription> results;
  private int correctColor;
  private int incorrectColor;

  public ResultsAdapter(ArrayList<ResultsActivity.ResultDescription> results) {
    this.results = results;
  }

  @Override
  public int getItemCount() {
    return results.size();
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
    ResultsActivity.ResultDescription result = results.get(position);
    switch (result.status()) {
      case CORRECT:
        holder.getTextView().setTextColor(correctColor);
        holder.getStatusView().setImageResource(R.drawable.cat_heart);
        break;
      case INCORRECT:
        holder.getTextView().setTextColor(incorrectColor);
        holder.getStatusView().setImageResource(R.drawable.cat_cry);
        break;
      case NOT_RECOGNIZED:
        holder.getTextView().setTextColor(incorrectColor);
        holder.getStatusView().setImageResource(R.drawable.cat_big_eyes);
        break;
    }
    holder.getTextView().setText(result.equation());
  }

  public static class ViewHolder extends RecyclerView.ViewHolder {
    final TextView textView;
    final ImageView statusView;

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
