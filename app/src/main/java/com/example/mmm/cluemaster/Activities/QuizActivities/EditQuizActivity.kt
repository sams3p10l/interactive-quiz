package com.example.mmm.cluemaster.Activities.QuizActivities

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.example.mmm.cluemaster.Activities.MainActivity
import com.example.mmm.cluemaster.Activities.NavigationActivities.MyQuizzesActivity
import com.example.mmm.cluemaster.Constants
import com.example.mmm.cluemaster.Models.Quiz
import com.example.mmm.cluemaster.R
import kotlinx.android.synthetic.main.activity_editquiz.*

class EditQuizActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editquiz)

        val intentToGetExtra = intent

        var name: String = intentToGetExtra.getStringExtra(Constants.INTENT_MSG_NAME)
        etName.setText(name)

        var desc: String = intentToGetExtra.getStringExtra(Constants.INTENT_MSG_DESC)
        etDesc.setText(desc)

        val id: String = intentToGetExtra.getStringExtra(Constants.INTENT_MSG_ID)

        btSave.setOnClickListener {
            name = etName.text.toString()
            desc = etDesc.text.toString()
            val quizzesRef = MainActivity.database.getReference(Constants.NODE_QUIZZESLIST).child(MainActivity.loggedUser.uid)
            val quiz = Quiz(id, name, desc)
            quizzesRef.child(id).setValue(quiz)

            val intent = Intent(this, MyQuizzesActivity::class.java)
            startActivity(intent)
            finish()
        }

        ivEditQuizExit.setOnClickListener {
            finish()
        }

    }
}