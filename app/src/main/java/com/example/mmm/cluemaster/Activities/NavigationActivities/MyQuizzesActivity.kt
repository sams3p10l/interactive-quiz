package com.example.mmm.cluemaster.Activities.NavigationActivities

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import com.example.mmm.cluemaster.Activities.HomeActivity
import com.example.mmm.cluemaster.Utils.populateQuizList
import com.example.mmm.cluemaster.Activities.QuizActivities.CreateQuizActivity
import com.example.mmm.cluemaster.Adapters.QuizAdapter
import com.example.mmm.cluemaster.Models.localQuizDatabase.quizzes
import com.example.mmm.cluemaster.R
import kotlinx.android.synthetic.main.activity_myquizzes.*

class MyQuizzesActivity : AppCompatActivity() {

    val layoutManager = LinearLayoutManager(this)
    private val adapter = QuizAdapter(this, quizzes)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_myquizzes)

        btAddQuiz.setOnClickListener {
            val intent = Intent(this, CreateQuizActivity::class.java)
            startActivity(intent)
        }

        ivMyQuizzesExit.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }

        setupRecyclerView()
        populateQuizList(adapter)
    }

    private fun setupRecyclerView() {
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
    }
}
