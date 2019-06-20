package com.example.mmm.cluemaster.Adapters

import android.content.Context
import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.mmm.cluemaster.Activities.QuestionsActivities.EditQuestionActivity
import com.example.mmm.cluemaster.Constants
import com.example.mmm.cluemaster.Models.Question
import com.example.mmm.cluemaster.Models.localDataQuestions
import com.example.mmm.cluemaster.R
import kotlinx.android.synthetic.main.list_questions.view.*

class QuestionAdapter(val context: Context, private val questions: List<Question>) : RecyclerView.Adapter<QuestionAdapter.MyViewHolder>() {

    private val TAG: String = "QuestionAdapter"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestionAdapter.MyViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.list_questions, parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return questions.size
    }

    override fun onBindViewHolder(p0: QuestionAdapter.MyViewHolder, position: Int) {
        val question = questions[position]
        p0.loadData(question, position)
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private var currentQuestion: Question? = null
        private var currentPosition: Int = 0

        init {
            itemView.edit_question.setOnClickListener {
                currentQuestion?.let {
                    val intentToEdit = Intent(context, EditQuestionActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    intentToEdit.putExtra(Constants.INTENT_MSG_QUESTION_TITLE, currentQuestion!!.q_text)
                    intentToEdit.putExtra(Constants.INTENT_MSG_QUESTION_ANS1, currentQuestion!!.answers[0])
                    intentToEdit.putExtra(Constants.INTENT_MSG_QUESTION_ANS2, currentQuestion!!.answers[1])
                    intentToEdit.putExtra(Constants.INTENT_MSG_QUESTION_ANS3, currentQuestion!!.answers[2])
                    intentToEdit.putExtra(Constants.INTENT_MSG_QUESTION_ANS4, currentQuestion!!.answers[3])
                    intentToEdit.putExtra(Constants.INTENT_MSG_QUESTION_CORRECT_POS, currentQuestion!!.correctPos)
                    intentToEdit.putExtra(Constants.INTENT_MSG_ID, currentQuestion!!.id)
                    intentToEdit.putExtra(Constants.INTENT_MSG_QUESTION_FILE_PATH, currentQuestion!!.media)
                    context.startActivity(intentToEdit)
                }
            }

            itemView.delete_question.setOnClickListener {
                currentQuestion?.let {
                    val currentAdapterPosition = adapterPosition
                    Log.d(TAG, "onRemove: Position value -> $currentPosition")
                    localDataQuestions.questions.remove(currentQuestion!!)
                    localDataQuestions.removeQuestion(currentQuestion!!.id, this@QuestionAdapter, currentAdapterPosition)
                    Log.d(TAG, "${questions.size}")
                }
            }
        }

        fun loadData(question: Question?, pos: Int) {
            question?.let {
                itemView.questionTitle.text = question.q_text
            }

            Log.d(TAG, "loadData: Position value -> $pos")
            this.currentQuestion = question
            this.currentPosition = pos
        }
    }
}

