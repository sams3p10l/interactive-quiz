package com.example.mmm.cluemaster.Adapters

import android.content.Context
import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.mmm.cluemaster.Activities.QuestionsActivities.QuestionsActivity
import com.example.mmm.cluemaster.Activities.QuizActivities.EditQuizActivity
import com.example.mmm.cluemaster.Constants
import com.example.mmm.cluemaster.Models.Quiz
import com.example.mmm.cluemaster.Models.localQuizDatabase
import com.example.mmm.cluemaster.R
import kotlinx.android.synthetic.main.list_quizzes.view.*


class QuizAdapter(val context: Context, private val quizzes: List<Quiz>) : RecyclerView.Adapter<QuizAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuizAdapter.MyViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.list_quizzes, parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return quizzes.size
    }

    override fun onBindViewHolder(p0: QuizAdapter.MyViewHolder, position: Int) {
        val quiz = quizzes[position]
        p0.loadData(quiz, position)
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var currentQuiz: Quiz? = null
        var currentPosition: Int = 0

        init {
            itemView.setOnClickListener {
                currentQuiz?.let {
                    val intentToOpenQuiz = Intent(context, QuestionsActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    intentToOpenQuiz.putExtra(Constants.INTENT_MSG_NAME, currentQuiz!!.title)
                    intentToOpenQuiz.putExtra(Constants.INTENT_MSG_DESC, currentQuiz!!.description)
                    intentToOpenQuiz.putExtra(Constants.INTENT_MSG_ID , currentQuiz!!.id)
                    context.startActivity(intentToOpenQuiz)
                }
            }

            itemView.edit_quiz.setOnClickListener {
                currentQuiz?.let {
                    val intentToEdit = Intent(context, EditQuizActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    intentToEdit.putExtra(Constants.INTENT_MSG_NAME, currentQuiz!!.title)
                    intentToEdit.putExtra(Constants.INTENT_MSG_DESC, currentQuiz!!.description)
                    intentToEdit.putExtra(Constants.INTENT_MSG_ID, currentQuiz!!.id)
                    context.startActivity(intentToEdit)
                }
            }

            itemView.delete_quiz.setOnClickListener {
                currentQuiz?.let {
                    localQuizDatabase.quizzes.remove(currentQuiz!!)
                    localQuizDatabase.removeQuiz(currentQuiz!!.id, this@QuizAdapter, adapterPosition)
                }
            }
        }

        fun loadData(quiz: Quiz?, pos: Int) {
            quiz?.let {
                itemView.quizTitle.text = quiz.title
            }
            this.currentQuiz = quiz
            this.currentPosition = pos
        }
    }

}