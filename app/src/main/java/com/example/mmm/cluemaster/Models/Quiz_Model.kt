package com.example.mmm.cluemaster.Models

import com.example.mmm.cluemaster.Activities.MainActivity
import com.example.mmm.cluemaster.Adapters.QuizAdapter
import com.example.mmm.cluemaster.Constants
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener


data class Quiz(var id: String = "", var title: String = "", var description: String = "")

object localQuizDatabase {

    var quizzes = mutableListOf<Quiz>()

    // Removes quiz from Firebase by given id
    fun removeQuiz(id: String, adapter: QuizAdapter, adapterPosition: Int) {
        val quizzesRef = MainActivity.database!!.getReference(Constants.NODE_QUIZZESLIST).child(MainActivity.loggedUser.uid)
        quizzesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (quizSnapshot: DataSnapshot in dataSnapshot.children) {
                    val quiz: Quiz = quizSnapshot.getValue(Quiz::class.java)!!
                    if (quiz.id == id) {
                        quizzesRef.child(quiz.id).removeValue()
                        adapter.notifyItemRemoved(adapterPosition)
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }
}

