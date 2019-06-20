package com.example.mmm.cluemaster.Activities.NavigationActivities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.widget.Toast
import com.example.mmm.cluemaster.Adapters.HostQuizAdapter
import com.example.mmm.cluemaster.Models.localQuizDatabase.quizzes
import com.example.mmm.cluemaster.Utils.populateQuizList
import com.example.mmm.cluemaster.R
import kotlinx.android.synthetic.main.activity_hostquiz.*


class HostQuizActivity : AppCompatActivity() {

    val layoutManager = LinearLayoutManager(this)
    private val adapter = HostQuizAdapter(this, quizzes)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hostquiz)

        setupRecyclerView()
        populateQuizList(adapter)
    }

    override fun onStart() {
        super.onStart()

        Toast.makeText(this, "Long click a quiz to get more info about it", Toast.LENGTH_SHORT).show()
    }

    private fun setupRecyclerView() {
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        recyclerView_host.layoutManager = layoutManager
        recyclerView_host.adapter = adapter
    }
}

