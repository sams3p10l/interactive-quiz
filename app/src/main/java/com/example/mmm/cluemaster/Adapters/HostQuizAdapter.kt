package com.example.mmm.cluemaster.Adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.mmm.cluemaster.Models.Quiz
import com.example.mmm.cluemaster.R
import com.example.mmm.cluemaster.Utils
import kotlinx.android.synthetic.main.list_quizzes_host.view.*

class HostQuizAdapter(val context: Context, private val quizzes: List<Quiz>) : RecyclerView.Adapter<HostQuizAdapter.MyViewHolder>() {
    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): MyViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.list_quizzes_host, p0, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return quizzes.size
    }

    override fun onBindViewHolder(p0: MyViewHolder, p1: Int) {
        val quiz = quizzes[p1]
        p0.loadData(quiz, p1)
    }

    inner class MyViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        lateinit var currentQuiz: Quiz
        var position: Int? = null

        init {
            view.setOnClickListener {
                currentQuiz?.let {
                    Utils.checkIfHasQuestions(currentQuiz.id, context)
                }
            }

            view.setOnLongClickListener {
                toggleView(view.tv_description)
                return@setOnLongClickListener true
            }

        }

        fun loadData(quiz: Quiz?, pos: Int) {
            quiz?.let {
                view.quizTitle_host.text = quiz.title
                view.tv_description.text = quiz.description
            }

            this.currentQuiz = quiz!!
            this.position = pos
        }

        private fun toggleView(view: View)
        {
            if (view.visibility == View.GONE)
                view.visibility = View.VISIBLE
            else if (view.visibility == View.VISIBLE)
                view.visibility = View.GONE
        }

    }

}
