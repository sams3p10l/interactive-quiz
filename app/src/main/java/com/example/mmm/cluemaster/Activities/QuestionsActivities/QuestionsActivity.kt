package com.example.mmm.cluemaster.Activities.QuestionsActivities

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import com.example.mmm.cluemaster.Activities.MainActivity
import com.example.mmm.cluemaster.Activities.NavigationActivities.MyQuizzesActivity
import com.example.mmm.cluemaster.Adapters.QuestionAdapter
import com.example.mmm.cluemaster.Constants
import com.example.mmm.cluemaster.Models.Question
import com.example.mmm.cluemaster.Models.localDataQuestions
import com.example.mmm.cluemaster.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_questions.*

class QuestionsActivity : AppCompatActivity() {

    val layoutManager = LinearLayoutManager(this)
    val adapter = QuestionAdapter(this, localDataQuestions.questions)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_questions)

        val id: String = intent.getStringExtra(Constants.INTENT_MSG_ID)

        ivQuestionExit.setOnClickListener {
            finish()
            val intent = Intent(this, MyQuizzesActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
        }

        btAddQuestion.setOnClickListener {
            val intentToAddQuestion = Intent(this, AddQuestionActivity::class.java)
            intentToAddQuestion.putExtra(Constants.INTENT_MSG_ID, id)
            startActivity(intentToAddQuestion)
        }

        populateQuestionsList(id)
        setupRecyclerView()
    }

    private fun populateQuestionsList(id: String) {

        val questionsRef = MainActivity.database!!.getReference(Constants.NODE_QUESTIONLIST).child(id)
        questionsRef.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                localDataQuestions.questions.clear()

                for (questionSnapshot: DataSnapshot in dataSnapshot.children) {
                    val question: Question = questionSnapshot.getValue(Question::class.java)!!
                    localDataQuestions.questions.add(question)
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun setupRecyclerView() {
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        recyclerViewQuestions.layoutManager = layoutManager
        recyclerViewQuestions.adapter = adapter
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, MyQuizzesActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
        finish()
    }
}