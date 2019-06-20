package com.example.mmm.cluemaster.Activities.QuizActivities

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.example.mmm.cluemaster.Activities.MainActivity
import com.example.mmm.cluemaster.Activities.NavigationActivities.MyQuizzesActivity
import com.example.mmm.cluemaster.Constants
import com.example.mmm.cluemaster.Models.Quiz
import com.example.mmm.cluemaster.R
import kotlinx.android.synthetic.main.activity_createquiz.*

class CreateQuizActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_createquiz)

        btSubbmitNewQuiz.setOnClickListener {
            val quizzesRef = MainActivity.database!!.getReference(Constants.NODE_QUIZZESLIST).child(MainActivity.loggedUser.uid)
            val quizzesRefID = quizzesRef.push().key
            val quiz = Quiz(quizzesRefID.toString(), etCreateQuizName.text.toString(), etCreateQuizDesc.text.toString())
            quizzesRef.child(quizzesRefID!!).setValue(quiz)

            val addQuizIntent = Intent(this, MyQuizzesActivity::class.java)
            addQuizIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(addQuizIntent)
        }

        ivCreateQuizExit.setOnClickListener {
            finish()
        }
    }
}