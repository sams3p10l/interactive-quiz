package com.example.mmm.cluemaster.Models

import com.example.mmm.cluemaster.Activities.MainActivity
import com.example.mmm.cluemaster.Adapters.QuestionAdapter
import com.example.mmm.cluemaster.Constants
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlin.collections.ArrayList

data class Question(var id: String = "", var q_text: String = "", var answers: ArrayList<String> = arrayListOf(), var media: String = "", var correctPos: Int = -1)

object localDataQuestions {
    var questions = mutableListOf<Question>()
    var gameQuestions = arrayListOf<Question>()

    // Removes question from Firebase by given id
    fun removeQuestion(id: String, adapter: QuestionAdapter, adapterPosition: Int) {

        val questionsRef = MainActivity.database!!.getReference(Constants.NODE_QUESTIONLIST)
        questionsRef.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {

                firstLoop@ for (quizSnapshot: DataSnapshot in dataSnapshot.children) {
                    for (questionSnapshot: DataSnapshot in quizSnapshot.children) {
                        val question: Question = questionSnapshot.getValue(Question::class.java)!!
                        if (question.id == id) {
                            questionSnapshot.ref.removeValue()
                            adapter.notifyItemRemoved(adapterPosition)
                            break@firstLoop
                        }
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }
}