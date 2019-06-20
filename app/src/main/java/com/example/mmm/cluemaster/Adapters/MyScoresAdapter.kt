package com.example.mmm.cluemaster.Adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.mmm.cluemaster.Models.MyScoreModel
import com.example.mmm.cluemaster.Models.Scoreboard
import com.example.mmm.cluemaster.R
import kotlinx.android.synthetic.main.score_item.view.*

class MyScoresAdapter(private val context: Context, private val scores: ArrayList<MyScoreModel>) : RecyclerView.Adapter<MyScoresAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(viewGroup: ViewGroup, p1: Int): MyScoresAdapter.MyViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.score_item, viewGroup, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return scores.size
    }

    override fun onBindViewHolder(p0: MyScoresAdapter.MyViewHolder, p1: Int) {
        val score = scores[p1]
        p0.loadData(score, p1)
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var currentScore: MyScoreModel? = null
        var currentPosition: Int = 0

        fun loadData(score: MyScoreModel?, pos: Int) {
            score?.let {
                itemView.tvScoreDate.text = it.date
                itemView.tvScoreQuizName.text = it.quizName
                itemView.tvScorePoints.text = it.score.toString()
            }

            this.currentScore = score
            this.currentPosition = pos
        }
    }
}