package com.example.mmm.cluemaster.Activities.NavigationActivities

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.widget.Toast
import com.example.mmm.cluemaster.Activities.MainActivity
import com.example.mmm.cluemaster.Adapters.MyScoresAdapter
import com.example.mmm.cluemaster.Constants
import com.example.mmm.cluemaster.Models.MyScoreModel
import com.example.mmm.cluemaster.Models.localScoreHistory
import com.example.mmm.cluemaster.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_myscore.*

class MyScoreActivity : AppCompatActivity() {

    private val mLayoutManager = LinearLayoutManager(this)
    var adapter: MyScoresAdapter = MyScoresAdapter(this, localScoreHistory.lshObject)
    val context = this

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_myscore)

        ivScoreBoardExit.setOnClickListener {
            finish()
        }
        setupRecyclerView()
    }

    override fun onStart() {
        super.onStart()

        populateScoreList(context)
    }

    private fun setupRecyclerView() {
        mLayoutManager.orientation = LinearLayoutManager.VERTICAL
        recyclerView_scoreboard.layoutManager = mLayoutManager
        recyclerView_scoreboard.adapter = adapter
    }

    private fun populateScoreList(ctx : Context)
    {
        val scoresRef = MainActivity.databaseRootRef.child(Constants.NODE_MYSCORES)
        var counter = 0
        localScoreHistory.lshObject.clear()

        scoresRef.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.hasChild(MainActivity.logUser.UUID!!))
                {
                    for (scoreObject in p0.child(MainActivity.logUser.UUID!!).children)
                    {
                        val scoreDate = scoreObject.child("date").value as String
                        val scoreQuizName = scoreObject.child("quizName").value as String
                        val scoreScore = scoreObject.child("score").value as Long

                        localScoreHistory.lshObject.add(counter++, MyScoreModel(scoreDate, scoreQuizName, scoreScore))
                    }
                    adapter.notifyDataSetChanged()
                }
                else {
                    Toast.makeText(ctx, getString(R.string.no_scores_toast), Toast.LENGTH_SHORT).show()
                }
            }
            override fun onCancelled(p0: DatabaseError) {}
        })
    }



}