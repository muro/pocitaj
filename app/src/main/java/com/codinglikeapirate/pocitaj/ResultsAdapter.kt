package com.codinglikeapirate.pocitaj

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class ResultsAdapter(private val results: ArrayList<ResultsActivity.ResultDescription>) :
    RecyclerView.Adapter<ResultsAdapter.ViewHolder>() {

    private var correctColor: Int = 0
    private var incorrectColor: Int = 0

    override fun getItemCount(): Int = results.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        correctColor = ContextCompat.getColor(parent.context, R.color.primary_text_color)
        incorrectColor = ContextCompat.getColor(parent.context, R.color.incorrect_text_color)

        val view = LayoutInflater.from(parent.context).inflate(R.layout.results_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val result = results[position]
        when (result.status) {
            ResultsActivity.ResultStatus.CORRECT -> {
                holder.textView.setTextColor(correctColor)
                holder.statusView.setImageResource(R.drawable.cat_heart)
            }
            ResultsActivity.ResultStatus.INCORRECT -> {
                holder.textView.setTextColor(incorrectColor)
                holder.statusView.setImageResource(R.drawable.cat_cry)
            }
            ResultsActivity.ResultStatus.NOT_RECOGNIZED -> {
                holder.textView.setTextColor(incorrectColor)
                holder.statusView.setImageResource(R.drawable.cat_big_eyes)
            }
        }
        holder.textView.text = result.equation
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.textView)
        val statusView: ImageView = itemView.findViewById(R.id.resultImageView)
    }
}